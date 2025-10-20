package nodes.expressions;

import data.Measure;
import nodes.tokens.TMeasure;

public class Literal extends Operand {

    public TMeasure value() {
        return child("value", TMeasure.class);
    }

    public Measure measure() {
        TMeasure token = value();
        return (token == null) ? null : token.measure;
    }
}
