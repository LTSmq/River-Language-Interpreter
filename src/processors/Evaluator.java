package processors;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import data.*;

import nodes.Node;
import nodes.tokens.*;


public class Evaluator {
    final HashMap<String, HashMap<Integer, Variant>> variables = new HashMap<>();

    public String execute(Node statement) {
        switch (statement.category) {
            case "PROCEDURE" -> {
                String output = "";
                Set<Node> sequence = statement.query("content");

                for (Node subStatement : sequence) {
                    output += execute(subStatement);
                }
                return output;
            }

            case "STATEMENT" -> {
                return execute(getHint(statement, "type"));
            }

            case "OUTPUT_STATEMENT" -> {
                return evaluate(getHint(statement, "interior")).toString();
            }

            case "ASSIGN_STATEMENT" -> {
                Node destination = getHint(statement, "destination");
                Variable var  = parseVar(destination);

                Node source = getHint(statement, "source");
                Variant sourceValue = evaluate(source);

                List<Node> operationChildren = statement.getChildren("operation");
                Node operation = (operationChildren.isEmpty()) ? null : operationChildren.get(0);

                if (operation != null) {
                    if (!(operation instanceof Arithmetic arithmetic)) throw new IllegalArgumentException("Operator is not arithmetic");
                    sourceValue = getVar(var.name, var.index).operate(sourceValue, arithmetic.type, arithmetic.direction);
                }

                setVar(var.name, var.index, sourceValue);
                return "";
            }

            case "PRINT_STATEMENT" -> {
                Node content = getHint(statement, "string", 2);
                if (!(content instanceof TString tokenString)) throw new IllegalArgumentException("Hint 'contents' not a string as expected: " + content.asTree());
                return tokenString.content;
            }

            case "IF_STATEMENT" -> {
                return executeConditonalBlock(getHint(statement, "conditionalBlock"), false);
            }

            case "WHILE_STATEMENT" -> {
                return executeConditonalBlock(getHint(statement, "conditionalBlock"), true);
            }

            default -> { throw new IllegalArgumentException("Not a statement: \n" + statement.asTree()); }
        }
    }

    public Variant evaluate(Node expression) { 

        switch (expression.category) {
            case "EXPRESSION" -> {
                return evaluate(getHint(expression, "type"));
            }

            case "LITERAL" -> {
                Node literal = expression.getChildren().get(0);
                switch (literal) {
                    case TMeasure measure: return measure.measure;
                    case TString string: return new DString(string.content);
                    default: throw new IllegalArgumentException("Unrecognised literal: " + literal.asTree());
                }
            }

            case "OPERAND" -> {
                Node operand = expression.getChildren().get(0);
                return evaluate(operand);
            }

            case "VARIABLE" -> {
                Variable var = parseVar(expression);
                return getVar(var.name, var.index);
            }

            case "OPERATION_EXPRESSION" -> {
                Variant operand = evaluate(getHint(expression, "operand", 1));
                Variant source = evaluate(getHint(expression, "source"));

                Node operation = getHint(expression, "operation");

                if (!(operation instanceof Arithmetic arithmetic)) throw new IllegalStateException("Operator is not arithmetic");
                return operand.operate(source, arithmetic.type, arithmetic.direction);
            }

            case "GROUP_EXPRESSION" -> {
                return evaluate(getHint(expression, "interior"));
            }

            default -> { throw new IllegalArgumentException("Not an expression: \n" + expression.asTree()); }
        }


    }

    static Node getHint(Node parent, String hint, int depth) {
        Set<Node> hintSet = parent.query(hint, depth);
        if (hintSet.isEmpty()) { throw new IllegalArgumentException("Hint '" + hint + "' not found: " + parent.asTree()); }
        return hintSet.iterator().next();
    }

    static Node getHint(Node parent, String hint) {
        return getHint(parent, hint, 0);
    }

    Variant getVar(String name, int index) {
        if (!variables.containsKey(name)) return Variant.DEFAULT;
        if (!variables.get(name).containsKey(index)) return Variant.DEFAULT;

        return variables.get(name).get(index);
    }

    Variant getVar(String name) { return getVar(name ,0); }

    void setVar(String name, int index, Variant value) {
        if (!variables.containsKey(name)) {
            variables.put(name, new HashMap<>());
        }
        variables.get(name).put(index, value);
    }

    void setVar(String name, Variant value) {
        setVar(name, 0, value);
    }

    String executeConditonalBlock(Node conditionalBlock, boolean repeat) {
        Node condition = getHint(conditionalBlock, "condition");
        Node procedure = getHint(conditionalBlock, "procedure", 1);
        String output = "";

        while (evaluate(condition).isTrue()) {
            for (Node statement : procedure.getChildren()) {
                output += execute(statement);
            }

            if (!repeat) break;
        }

        return output;
    }

    public Variable parseVar(Node varNode) {
        String name = ((Token) varNode.getChildren("name").get(0)).lexeme;
        List<Node> indexExpressions = varNode.getChildren("index");
        int index = 0;
        if (!indexExpressions.isEmpty()) {
            Variant value = evaluate(indexExpressions.get(0));
            if (!(value instanceof Measure m)) throw new IllegalArgumentException("Not a measure for index: " + indexExpressions.get(0));
            index = (int) Math.round(m.magnitude);
        }

        return new Variable(name, index);
    }

    class Variable {
        public String name;
        public int index;

        public Variable(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}


