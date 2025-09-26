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
    public Measure xpVisitArray(Expression.ArrayExpression e) {
        if (e.contents == null) return null;

        Measure[] measures = new Measure[e.contents.size()];
        Measure head = e.contents.get(0).accept(this);
        measures[0] = head;
        Measure previous = head;

        for (int i = 1; i < measures.length; i++) {
            Measure newMeasure = new Measure(e.contents.get(i).accept(this));
            System.out.println(newMeasure);
            System.out.println(previous);
            System.out.println(previous.next);
            System.out.println("-");
            previous.next = newMeasure;
            System.out.println(newMeasure);
            System.out.println(previous);
            System.out.println(previous.next);
            System.out.println("-");
            previous = newMeasure;
            System.out.println(newMeasure);
            System.out.println(previous);
            System.out.println(previous.next);
            System.out.println("-");
        }
        System.out.println();

        return head;
    }

    @Override
    public Measure xpVisitCall(Expression.CallExpression e) {
        
        return null;
    }

    @Override
    public Boolean stmtVisitAssignment(AssignmentStatement s) {
        int i = (int) Math.round(s.subject.index.accept(this).magnitude);
        Measure cursor = s.target.accept(this);

        while (cursor != null) {
            Variable.set(
                s.subject.variableName,
                i,
                new Measure(cursor)
            );
            i++;
            System.out.println(cursor);
            cursor = cursor.next;
        }

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
