package grammar.metatokens;

import java.util.ArrayList;
import java.util.List;

public class BracketMetaToken extends OperandMetaToken {
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

    public enum Type {
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