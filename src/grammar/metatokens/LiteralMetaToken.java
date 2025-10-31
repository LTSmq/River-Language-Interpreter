package grammar.metatokens;

import java.util.List;

import grammar.Rule;

import java.util.ArrayList;


public class LiteralMetaToken extends OperandMetaToken {
    public LiteralMetaToken(String lexeme, String group) {
        super(lexeme, group);
    }

    public Rule rule;
    public String name;

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