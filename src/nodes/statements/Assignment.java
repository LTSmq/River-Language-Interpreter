package nodes.statements;

import nodes.expressions.Expression;
import nodes.expressions.Variable;

public class Assignment extends Statement {
    public Expression source() {
        return child("source", Expression.class);
    }

    public Variable destination() {
        return child("destination", Variable.class);
    }
}
