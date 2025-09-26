import java.util.List;
import java.util.ArrayList;


public interface Expression extends SyntaxNode {
    static interface Visitor<Type> {
        Type xpVisitLiteral(LiteralExpression e);
        Type xpVisitVariable(VariableExpression e);
        Type xpVisitOperation(OperationExpression e);
        Type xpVisitCall(CallExpression e);
        Type xpVisitArray(ArrayExpression e);
    }

    public <Type> Type accept(Visitor<Type> v);



    abstract class AtomicExpression implements Expression {
        public List<SyntaxNode> children() { return null; };
    }


    class LiteralExpression extends AtomicExpression {
        Measure value;
        public <Type> Type accept(Visitor<Type> v) { return v.xpVisitLiteral(this); }
        LiteralExpression(Measure v) {
            value = v;
        }
    }


    class VariableExpression extends AtomicExpression {
        String variableName;
        public <Type> Type accept(Visitor<Type> v) { return v.xpVisitVariable(this); }
        VariableExpression(String name) {
            variableName = name;
        }
    }


    class ArrayExpression implements Expression {
        List<Expression> contents;
        public List<SyntaxNode> children() { return new ArrayList<>(contents); }
        
        public <Type> Type accept(Visitor<Type> v) {
            return v.xpVisitArray(this);
        }
    }


    class OperationExpression implements Expression {
        enum Operation {
            UNDEFINED,
            ADD, SUBTRACT, MULTIPLY, DIVIDE,
            EQUAL, UNEQUAL, 
            GREATER, LESSER, 
            GREATER_EQUAL, LESSER_EQUAL,
        }

        AtomicExpression left_operand;
        Expression right_operand;
        Operation operation;

        OperationExpression(AtomicExpression lo, Operation o, Expression ro) {
            left_operand = lo;
            operation = o;
            right_operand = ro;
        }
        
        public static Operation parseOperation(String operationString) {
            Operation operation = Operation.UNDEFINED;
            switch (operationString){
                case "+":   operation = Operation.ADD;
                break;
                case "-":   operation = Operation.SUBTRACT;
                break;
                case "/":   operation = Operation.DIVIDE;
                break;
                case "*":   operation = Operation.MULTIPLY;
                break;
                case "==":   operation = Operation.EQUAL;
                break;
                case "!=":   operation = Operation.UNEQUAL;
                break;
                case ">":   operation = Operation.GREATER;
                break;
                case "<":   operation = Operation.LESSER;
                break;
                case ">=":   operation = Operation.GREATER_EQUAL;
                break;
                case "<=":   operation = Operation.LESSER_EQUAL;
            }
            return operation;
        }

        public <Type> Type accept(Visitor<Type> v) { return v.xpVisitOperation(this); }

        public List<SyntaxNode> children() { 
            return new ArrayList<SyntaxNode>() {{
                add(left_operand); 
                add(right_operand);
            }};
        }
    }

    class CallExpression implements Expression {
        List<Expression> arguments;

        public <Type> Type accept(Visitor<Type> v) { return v.xpVisitCall(this); }
        public List<SyntaxNode> children() { return new ArrayList<SyntaxNode>(arguments); }
    }
}