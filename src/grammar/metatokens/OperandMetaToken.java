package grammar.metatokens;

import java.util.LinkedHashSet;

public abstract class OperandMetaToken extends MetaToken{
    public OperandMetaToken(String lexeme, String group) { super(lexeme, group); }
    public LinkedHashSet<String> hints = new LinkedHashSet<>();

    public String tagName(String _givenGroup) {
        return null;
    }

    @Override
    public String toString() {
        if (hints.isEmpty()) return super.toString();
        String hintString = String.join(":", hints);
        return super.toString() + "::" + hintString;
    }


}
