import java.util.List;

public class Executor implements Statement.Visitor<Boolean>, Expression.Visitor<Measure>{
    public void execute(List<Statement> program) {
        for (Statement statement : program) {
            statement.accept(this);
        }
    }

    @Override
    public Measure xpVisitLiteral(Expression.LiteralExpression e) {

        return e.value;
    }

    @Override
    public Measure xpVisitVariable(Expression.VariableExpression e) {
        
        return Variable.get(e.variableName);
    }

    @Override
    public Measure xpVisitOperation(Expression.OperationExpression e) {
        Measure left_operand = e.left_operand.accept(this);
        Measure right_operand = e.right_operand.accept(this);
        Measure result = null;

        switch (e.operation) {
            case ADD:           result = left_operand.add(right_operand);
            break;
            case SUBTRACT:      result = left_operand.subtract(right_operand);
            break;
            case MULTIPLY:      result = left_operand.multiply(right_operand);
            break;
            case DIVIDE:        result = left_operand.divide(right_operand);
            break;
            case EQUAL:         result = Measure.booleanOf(left_operand.equals(right_operand));
            break;
            case UNEQUAL:       result = Measure.booleanOf(!left_operand.equals(right_operand));
            break;
            case GREATER:       result = Measure.booleanOf(left_operand.compared_to(right_operand) > 0);
            break;
            case LESSER:        result = Measure.booleanOf(left_operand.compared_to(right_operand) < 0);
            break;
            case GREATER_EQUAL: result = Measure.booleanOf(left_operand.compared_to(right_operand) >= 0);
            break;
            case LESSER_EQUAL:  result = Measure.booleanOf(left_operand.compared_to(right_operand) <= 0);
            break;
            case UNDEFINED:;
        }

        return result;
    }

    @Override
    public Measure xpVisitCall(Expression.CallExpression e) {
        
        return null;
    }


    @Override
    public Boolean stmtVisitAssignment(AssignmentStatement s) {
        Variable.set(
            s.subject.variableName,
            s.target.accept(this)
        );

        return true;
    }

    @Override 
    public Boolean stmtVisitIf(IfStatement s) {
        while (s.condition.accept(this).equals(Measure.trueValue)) {
            execute(s.statements);

            if (!s.whileLoop) break;
        }
        return true;
    }

    @Override
    public Boolean stmtVisitCall(CallStatement s) {
        
        return false;
    }

}
