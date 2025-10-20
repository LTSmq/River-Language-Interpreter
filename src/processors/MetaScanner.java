package processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;
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

    public static LinkedHashMap<String, List<MetaToken>> getRules(String configSource) {
        LinkedHashMap<String, String> ruleGrammars = new LinkedHashMap<>();
        
        Matcher assignmentExtractor = ASSIGNMENT_EXTRACTION_PATTERN.matcher(configSource);
        while (assignmentExtractor.find()) {
            ruleGrammars.put(
                assignmentExtractor.group("rule"),
                assignmentExtractor.group("value")
            );
        }

        LinkedHashMap<String, List<MetaToken>> result = new LinkedHashMap<>();

        for (String rule : ruleGrammars.keySet()) {

            String grammarString = ruleGrammars.get(rule);
            Matcher grammarExtractor = META_TOKEN_EXTRACTION_PATTERN.matcher(grammarString);
            while (grammarExtractor.find()) {
                for (String group : groupClasses.keySet()) {
                    String matchString = grammarExtractor.group(group);
                    if (matchString != null) {
                        Class<? extends MetaToken> metaTokenClass = groupClasses.get(group);
                        try{
                            MetaToken metaToken = metaTokenClass.getDeclaredConstructor(String.class, String.class).newInstance(matchString, group);
                            if (metaToken instanceof OperandMetaToken operandMetaToken) {
                                String tagName = operandMetaToken.tagName(group);
                                String sourceHint = grammarExtractor.group(tagName);
                                if (sourceHint != null) operandMetaToken.hints.add(sourceHint);
                            }
                            List<MetaToken> tokenList = result.getOrDefault(rule, null);
                            if (tokenList == null) {
                                tokenList = new ArrayList<>();
                                result.put(rule, tokenList);
                            }
                            tokenList.add(metaToken);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
        }

        for (String rule : result.keySet()) {
            List<MetaToken> tokenSequence = result.get(rule);

            tokenSequence = parenthesize(tokenSequence);
            tokenSequence = shunt(tokenSequence);

            result.put(rule, tokenSequence);
        }

        for (String rule : result.keySet()) {
            System.out.println(rule + ":");
            System.out.print("\t");
            for (MetaToken token : result.get(rule)) {
                System.out.print(token.lexeme + " ");
            }
            System.out.println("\n");
        }
        return result;
    }
}
