package data.metatokens;

import java.util.List;
import java.util.ArrayList;


public class LiteralMetaToken extends OperandMetaToken {
    public LiteralMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch (group) {
            case "token" -> type = Type.TOKEN;
            case "rule" -> type = Type.RULE;
            default -> {}
        }
        name = lexeme;
    }

    public enum Type {
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