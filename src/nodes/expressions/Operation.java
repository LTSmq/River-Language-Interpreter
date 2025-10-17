package nodes.expressions;

import data.OperationDirection;
import data.OperationType;

public abstract class Operation extends Expression {
    public Expression leftOperand;
    public Expression rightOperand;
    public OperationDirection direction;

    public static class Comparator extends Operation {
        public boolean equalInclusive;
    }

    public static class Arithmetic extends Operation {
        public OperationType type;
    }
}
