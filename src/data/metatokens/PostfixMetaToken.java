package data.metatokens;

import java.util.List;
import java.util.ArrayList;


public class PostfixMetaToken extends OperatorMetaToken {
    public PostfixMetaToken(String lexeme, String group) {
        super(lexeme, group);
        switch(group) {
            case "zeroPlus" -> type = Type.ZERO_PLUS;
            case "onePlus" -> type = Type.ONE_PLUS;
            case "zeroOne" -> type = Type.ZERO_ONE;
        }
    }
    
    public enum Type {
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
