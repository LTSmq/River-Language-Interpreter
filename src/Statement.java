import java.util.ArrayList;
import java.util.List;


public interface Statement extends SyntaxNode {
    static interface Visitor<Type> {
        Type stmtVisitAssignment(AssignmentStatement s);
        Type stmtVisitCall(CallStatement s);
        Type stmtVisitIf(IfStatement s);
    } 

    public <Type> Type accept(Visitor<Type> v);
}


class AssignmentStatement implements Statement {
    Expression.VariableExpression subject;
    Expression target;

    public List<SyntaxNode> children() {
        return new ArrayList<SyntaxNode>() {{
            add(subject);
            add(target);
        }};
    }
    public <Type> Type accept(Visitor<Type> v) {
        return v.stmtVisitAssignment(this);
    }

    AssignmentStatement(Expression.VariableExpression varEx, Expression tarEx) {
        subject = varEx;
        target = tarEx;
    }
}


class CallStatement implements Statement {
    Expression.CallExpression content;

    public List<SyntaxNode> children() {
        return new ArrayList<SyntaxNode>() {{
           add(content); 
        }};
    }
    public <Type> Type accept(Visitor<Type> v) {
        return v.stmtVisitCall(this);
    }
}


abstract class BlockStatement implements Statement {
    List<Statement> statements;
    public List<SyntaxNode> children() {
        return new ArrayList<>(statements);
    } 
}


class IfStatement extends BlockStatement {
    public static final List<String> keywords;
    static {
        keywords = new ArrayList<>();
        keywords.add("IF");
        keywords.add("WHILE");
    }
    Expression condition;
    boolean whileLoop = false;
    public <Type> Type accept(Visitor<Type> v) {
        return v.stmtVisitIf(this);
    }
    public IfStatement(Expression entryCondition, List<Statement> nestedStatements) {
        condition = entryCondition;
        statements = nestedStatements;
    }

    public IfStatement(Expression entryCondition, List<Statement> nestedStatements, boolean isWhileLoop) {
        condition = entryCondition;
        statements = nestedStatements;
        whileLoop = isWhileLoop;
    }
}

