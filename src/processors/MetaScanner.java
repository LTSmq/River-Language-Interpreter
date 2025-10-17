package processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;


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
        // Find all operand tokens
        LinkedList<LinkedList<MetaToken>> operandQueue = new LinkedList<>();
        LinkedList<OperatorMetaToken> operatorQueue = new LinkedList<>();
        for (int i = 0; i < tokenSequence.size(); i++) {
            MetaToken metaToken = tokenSequence.get(i);
            switch (metaToken) {
                case OperandMetaToken operand -> {
                    LinkedList<MetaToken> operandList = new LinkedList<>();
                    operandQueue.add(operandList);
                    operandList.add(operand);
                    
                    if (!(operand instanceof BracketMetaToken bracket)) {
                        continue;
                    }
                    System.out.println(bracket + " at position " + i);
                    int end = bracket.findTerminus(tokenSequence, i);
                    while (i <= end) {
                        operandList.add(tokenSequence.get(i));
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

            operandTokens = parenthesize(operandTokens.subList(1, operandTokens.size() - 1));
            operandQueue.set(i, operandTokens);
        }

        // Build new sequence
        LinkedList<MetaToken> result = new LinkedList<>();
        while (!operandQueue.isEmpty() || !operatorQueue.isEmpty()) {
            OperatorMetaToken operator = (operatorQueue.isEmpty()) ? null : operatorQueue.pop();
            LinkedList<MetaToken> operandTokens = operandQueue.pop();
            result.addAll(operandTokens);
            result.addLast(BracketMetaToken.CLOSE);
            if (operator != null) result.addLast(operator);
            result.addFirst(BracketMetaToken.OPEN);
        }

        return result;
    }

    public static LinkedHashMap<String, List<MetaToken>> scan(String configSource) {
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
                                String tag = grammarExtractor.group(tagName);
                                operandMetaToken.tag = tag;
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
            result.put(rule, parenthesize(result.get(rule)));
        }

        for (String rule : result.keySet()) {
            System.out.println(rule + ":");
            for (MetaToken token : result.get(rule)) {
                System.out.print(token.lexeme + " ");
            }
            System.out.println();
        }
        return result;
    }
}

abstract class MetaToken {
    public final String lexeme;
    public MetaToken(String lexeme, String group) { this.lexeme = lexeme; }
    public List<String> groups() { return null; }

    @Override 
    public String toString() {
        return this.getClass().getName().replace("processors.", "") + "( " + lexeme + " )";
    }
}

abstract class OperandMetaToken extends MetaToken {
    public OperandMetaToken(String lexeme, String group) { super(lexeme, group); }
    public String tag = "";

    public String tagName(String _givenGroup) {
        return null;
    }

    @Override
    public String toString() {
        if (tag == null || tag.equals("")) {
            return super.toString();
        }
        else {
            return super.toString() + "::" + tag;
        }
    }
}

class LiteralMetaToken extends OperandMetaToken {
    public LiteralMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch (group) {
            case "token" -> type = Type.TOKEN;
            case "rule" -> type = Type.RULE;
            default -> {}
        }
        name = lexeme;
    }

    enum Type {
        TOKEN,
        RULE,
    }

    public Type type;
    public String name;

    @Override
    public List<String> groups() {
        return new ArrayList<>(){{
            add("token");
            add("rule");
        }};
    }

    @Override 
    public String tagName(String givenGroup) {
        return givenGroup + "Tag";
    }
}


class BracketMetaToken extends OperandMetaToken {
    public BracketMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch (group) {
            case "openGroup" -> type = Type.OPEN;
            case "closeGroup" -> type = Type.CLOSE;
            default -> {}
        }
    }

    public static final BracketMetaToken OPEN = new BracketMetaToken(" (", "openGroup");
    public static final BracketMetaToken CLOSE = new BracketMetaToken(") ", "closeGroup");

    enum Type {
        OPEN,
        CLOSE,
    }

    public Type type;

    @Override
    public List<String> groups() {
        return new ArrayList<>(){{
            add("openGroup");
            add("closeGroup");
        }};
    }

    @Override 
    public String tagName(String givenGroup) {
        return "groupTag";
    }

    public int findTerminus(List<MetaToken> metaTokens, int head) {
        // In: A sequence of tokens and an integer `head` stating where in the sequence this is
        // Out: The index of the matching closing token or -1 if it cannot be resolved
        int stack = 0;
        while (head < metaTokens.size()) {
            MetaToken cursor = metaTokens.get(head);

            switch (cursor) {
                case BracketMetaToken bracket -> {
                    switch (bracket.type) {
                        case BracketMetaToken.Type.OPEN -> {
                            stack++;
                        }
                        case BracketMetaToken.Type.CLOSE -> {
                            stack --;
                            if (stack <= 0) return head;
                        }
                    }
                }
                default -> { }
            }
            head++;
        }

        return -1;
    }
    
}


abstract class OperatorMetaToken extends MetaToken {
    public OperatorMetaToken(String lexeme, String group) { super(lexeme, group); }
}


class InfixMetaToken extends OperatorMetaToken {
    public InfixMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch (group) {
            case "concat" -> type = Type.CONCAT;
            case "or" -> type = Type.OR;
            default -> {}
        }
    }
    enum Type {
        OR,
        CONCAT,
    }
    public Type type;

    @Override
    public List<String> groups() {
        return new ArrayList<>(){{
            add("or");
            add("concat");
        }};
    }
}


class PostfixMetaToken extends OperatorMetaToken {
    public PostfixMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch(group) {
            case "zeroPlus" -> type = Type.ZERO_PLUS;
            case "onePlus" -> type = Type.ONE_PLUS;
            case "zeroOne" -> type = Type.ZERO_ONE;
        }
    }
    enum Type {
        ZERO_PLUS,
        ONE_PLUS,
        ZERO_ONE,
    }

    public Type type;

    @Override
    public List<String> groups() {
        return new ArrayList<>(){{
            add("zeroPlus");
            add("onePlus");
            add("zeroOne");
        }};
    }
}
