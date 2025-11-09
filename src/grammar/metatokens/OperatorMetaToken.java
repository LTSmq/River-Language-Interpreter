package grammar.metatokens;


public abstract class OperatorMetaToken extends MetaToken {
    public OperatorMetaToken(String lexeme, String group) { super(lexeme, group); }

    public int ary() { return 0; }
}
