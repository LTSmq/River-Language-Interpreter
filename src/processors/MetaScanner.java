package processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.Line;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;

import data.metatokens.*;


public class MetaScanner {
    static final Pattern ASSIGNMENT_EXTRACTION_PATTERN = Pattern.compile("(?<rule>[A-Z][A-Z_]+)\\s*\\-\\>\\s*(?<value>[^;]+)");
    static final Pattern META_TOKEN_EXTRACTION_PATTERN = Pattern.compile("(?<zeroPlus>\\*)|(?<onePlus>\\+)|(?<zeroOne>\\?)|(?<or>\\|)|(?:(?:(?<groupTag>[a-z][a-zA-Z]+):)?(?<openGroup>\\())|(?<closeGroup>\\))|(?:(?:(?<tokenTag>[a-z][a-zA-Z]+)\\s*:\\s*)?(?<token>\\b[A-Z](?=[A-Za-z]*[a-z])[A-Za-z]+\\b))|(?:(?:(?<ruleTag>[a-z][a-zA-Z]+)\\s*:\\s*)?(?<rule>[A-Z][A-Z_]+))|(?<concat>\\.)");
    static final HashMap<String, Class<? extends MetaToken>> groupClasses = new HashMap<>() {{
        put("token", LiteralMetaToken.class);
        put("rule", LiteralMetaToken.class);
        put("openGroup", BracketMetaToken.class);
        put("closeGroup", BracketMetaToken.class);
        put("or", InfixMetaToken.class);
        put("concat", InfixMetaToken.class);
        put("zeroPlus", PostfixMetaToken.class);
        put("onePlus", PostfixMetaToken.class);
        put("zeroOne", PostfixMetaToken.class);
    }};

    static LinkedList<MetaToken> parenthesize(List<MetaToken> tokenSequence) {
        return parenthesize(tokenSequence, (String) null);
    }

    static LinkedList<MetaToken> parenthesize(List<MetaToken> tokenSequence, String tag) {
        // Initialize storage
        LinkedList<LinkedList<MetaToken>> operandQueue = new LinkedList<>();
        LinkedList<OperatorMetaToken> operatorQueue = new LinkedList<>();

        // File tokens into queues, including subsequences as operands when bracketed
        for (int i = 0; i < tokenSequence.size(); i++) {
            MetaToken metaToken = tokenSequence.get(i);
            switch (metaToken) {
                case OperandMetaToken operand -> {
                    // Create new token list for operand
                    LinkedList<MetaToken> operandList = new LinkedList<>();
                    operandQueue.add(operandList);

                    // Add current token to list
                    operandList.add(operand);
                    
                    // Continue iteration if current token is not a bracket
                    if (!(operand instanceof BracketMetaToken bracket)) continue;

                    // Find matching end bracket position
                    int end = bracket.findTerminus(tokenSequence, i);

                    // Dump everything between the current token and end bracket to list
                    while (i < end) {
                        MetaToken groupToken = tokenSequence.get(i+1);
                        if (groupToken instanceof OperandMetaToken groupOperand) groupOperand.hints.addAll(bracket.hints);
                        operandList.add(groupToken);
                        i++;
                    }
                }
                
                case OperatorMetaToken operator -> operatorQueue.add(operator);
                default -> {}
            }
        }
        
        // Recurse all grouped operands
        for (int i = 0; i < operandQueue.size(); i++) {
            LinkedList<MetaToken> operandTokens = operandQueue.get(i);
            if (operandTokens.size() <= 1) continue;

            List<MetaToken> stripped = operandTokens.subList(1, operandTokens.size() - 1);
            operandTokens = parenthesize(stripped);
            operandQueue.set(i, operandTokens);
        }

        // Build new sequence
        LinkedList<MetaToken> result = new LinkedList<>();
        while (!operandQueue.isEmpty() || !operatorQueue.isEmpty()) {
            
            LinkedList<MetaToken> operandTokens = operandQueue.pop();
            result.addAll(operandTokens);

            OperatorMetaToken operator = (operatorQueue.isEmpty()) ? null : operatorQueue.pop();
            while (operator instanceof PostfixMetaToken) {
                result.addLast(operator);
                operator = (operatorQueue.isEmpty()) ? null : operatorQueue.pop();
            }

            result.addLast(BracketMetaToken.CLOSE);
            if (operator != null) result.addLast(operator);
            result.addFirst(BracketMetaToken.OPEN);
            
        }

        return result;
    }

    static List<MetaToken> shunt(List<MetaToken> parentesizedTokenSequence) {
        return shunt(new LinkedList<>(parentesizedTokenSequence));
    }

    static LinkedList<MetaToken> shunt(LinkedList<MetaToken> tokenSequence) {
        // Initialize storage
        LinkedList<MetaToken> outputQueue = new LinkedList<>();
        LinkedList<MetaToken> operatorStack = new LinkedList<>();
        
        // Begin iteration through sequence
        for (MetaToken token : tokenSequence) {
            switch (token) {
                case BracketMetaToken bracket -> {
                    // Push opening bracket to operator stack for later resolution
                    if (bracket.type == BracketMetaToken.Type.OPEN) operatorStack.push(bracket);
                    
                    // Dump everything in operator stack to queue until matching opening bracket is found
                    else if (bracket.type == BracketMetaToken.Type.CLOSE) {
                        while (!operatorStack.isEmpty() && !(
                                operatorStack.peek() instanceof BracketMetaToken open 
                            &&  open.type == BracketMetaToken.Type.OPEN
                        ))  {
                            outputQueue.add(operatorStack.pop());
                        }
                        
                        // Consume opening bracket
                        assert !operatorStack.isEmpty();
                        operatorStack.pop();
                    }
                }
    
                case OperatorMetaToken operator -> operatorStack.push(operator);
                case OperandMetaToken operand -> outputQueue.add(operand);  // Won't be a bracket as its already been checked
    
                default -> throw new RuntimeException("Unknown meta token type: " + token);
            }
        }
    
        // Drain remaining operators to output
        while (!operatorStack.isEmpty()) {
            MetaToken token = operatorStack.pop();
            assert !(token instanceof BracketMetaToken);
            outputQueue.add(token);
        }
    
        return outputQueue;
    }

    // Return rules with metatoken sequences as they occur in the source
    public static LinkedHashSet<Rule> getRulesPlain(String configSource) {
        // Initialise storage
        LinkedHashSet<Rule> result = new LinkedHashSet<>();
        LinkedHashMap<String, String> ruleGrammars = new LinkedHashMap<>();
        
        // Extract rule/expression pairs from source using extraction pattern
        Matcher assignmentExtractor = ASSIGNMENT_EXTRACTION_PATTERN.matcher(configSource);
        while (assignmentExtractor.find()) {
            ruleGrammars.put(
                assignmentExtractor.group("rule"),
                assignmentExtractor.group("value")
            );
        }
        
        // Iterate across rule names
        for (String ruleString : ruleGrammars.keySet()) {
            // Extract token matches from grammar string
            String grammarString = ruleGrammars.get(ruleString);
            Matcher grammarExtractor = META_TOKEN_EXTRACTION_PATTERN.matcher(grammarString);

            // Iterate across matches
            while (grammarExtractor.find()) {
                // Iterate across known group name to metatoken class map 
                for (String group : groupClasses.keySet()) {
                    // Check for grouping match
                    String matchString = grammarExtractor.group(group);
                    if (matchString == null) continue;

                    // Fetch class from map
                    Class<? extends MetaToken> metaTokenClass = groupClasses.get(group);

                    // Enter try block to catch invalid instantiations
                    try{
                        // Create metatoken from (String lexeme, String group) constructor
                        MetaToken metaToken = metaTokenClass.getDeclaredConstructor(String.class, String.class).newInstance(matchString, group);

                        // Attach hint to operand
                        if (metaToken instanceof OperandMetaToken operandMetaToken) {
                            String tagName = operandMetaToken.tagName(group);
                            String sourceHint = grammarExtractor.group(tagName);
                            if (sourceHint != null) operandMetaToken.hints.add(sourceHint);
                        }

                        Rule rule = null;
                        for (Rule existingRule : result) if (existingRule.name.equals(ruleString)) { rule = existingRule; break; }
                        if (rule == null) {
                            rule = new Rule(ruleString);
                            result.add(rule);
                        }

                        rule.add(metaToken);
                    } 
                    
                    // Print instantiation error without interruption
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
        }

        return result; 
    }

    public static Rule toPostFix(Rule infixRule) {
        Rule rule = infixRule;

        rule = new Rule(rule.name, parenthesize(rule));
        rule = new Rule(rule.name, shunt(rule));
        
        return rule;
    }

    // Return grammar syntax rules formatted as metatokens in a postfix sequence
    public static LinkedHashSet<Rule> getRulesPostfix(String configSource) {
        LinkedHashSet<Rule> result = getRulesPlain(configSource);   

        result = result.stream().map(MetaScanner::toPostFix).collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        return result;
    }
}
