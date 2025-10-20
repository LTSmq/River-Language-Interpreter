package data.metatokens;

import java.util.List;
import java.util.ArrayList;


public class InfixMetaToken extends OperatorMetaToken {
    public InfixMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch (group) {
            case "concat" -> type = Type.CONCAT;
            case "or" -> type = Type.OR;
            default -> {}
        }
    }

    public enum Type {
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

    @Override
    public int ary() { return 2; }
}
