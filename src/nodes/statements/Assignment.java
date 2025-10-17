package nodes.statements;

import nodes.expressions.Expression;
import nodes.expressions.Variable;

public class Assignment extends Statement {
    public Expression source;
    public Variable destination;

    public Assignment(Expression source, Variable destination) {
        this.source = source;
        this.destination = destination;
    }

}
