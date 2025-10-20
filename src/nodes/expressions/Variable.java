package nodes.expressions;

import nodes.tokens.Word;

public class Variable extends Operand {
    public Word name() {
        return (Word) children.get("name");
    }

    public Expression index() {
        return (Expression) children.get("index");
    }
}
