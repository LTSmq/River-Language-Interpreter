import java.util.List;

import java.util.ArrayList;


public class Parser{

    static List<Statement> parse(List<Token> sourceTokens) {
        List<List<Token>> segmentedTokens = segment(sourceTokens);
        List<Statement> result = new ArrayList<>();

        for (List<Token> statementTokens : segmentedTokens) {
            result.add(stmtEvaluate(statementTokens));
        }
        Log.progress(Log.Source.PARSER, "Found " + result.size() + " statements in script");
        return result;
    }

    static List<List<Token>> segment(List<Token> tokens) {
        List<List<Token>> result = new ArrayList<>();
        
        List<Token> buffer = new ArrayList<>();
        int blockStack = 0;

        for (Token token : tokens) {
            if (token.type == TokenType.OPEN_BLOCK) {
                blockStack++;
            }
            else if (token.type == TokenType.CLOSE_BLOCK) {
                blockStack--;
            }

            if (blockStack == 0 && token.type == TokenType.STATEMENT_DELIMITER) {
                if (buffer.size() > 0) {
                    result.add(buffer);
                    buffer = new ArrayList<>();
                }
            }

            else {
                buffer.add(token);
            }
        }

        return result;
    }

    static List<Token> lookAheadGrouping(List<Token> content, TokenType openingToken, TokenType closingToken) {
        List<Token> result = new ArrayList<>();
        int stack = 0;
        int i = 0;
        Token cursor = content.get(i);
        System.out.println(content);
        while (stack > 0 || cursor.type != closingToken) {
            result.add(cursor);
            System.out.println("+" + cursor);
            i++;
            if (i >= content.size()) {
                Log.error(Log.Source.PARSER, "Group lookahead could not close; missing closing brackets");
            }
            cursor = content.get(i);
            if (cursor.type == openingToken) stack++;
            if (cursor.type == closingToken) stack--;
        }
        System.out.println("=" + result);
        return result;
    }

    static Expression xpEvaluate(List<Token> expressionTokens) {
        if (expressionTokens.isEmpty()) return null;
        Token leadingToken = expressionTokens.get(0);

        Expression result = null;
        if (expressionTokens.size() == 1) {
            Token atomicToken = leadingToken;
            if (atomicToken.type == TokenType.VARIABLE){
                return new Expression.VariableExpression(atomicToken.lexeme);
            }
            else if (atomicToken.type == TokenType.MEASURE) {
                return new Expression.LiteralExpression(Measure.parse(atomicToken.lexeme));
            }
            Log.error(Log.Source.PARSER, "Unrecognised atomic token " + atomicToken.lexeme);
        }
        
        if (leadingToken.type == TokenType.OPEN_PARENTHESIS) {
            int parenthesisStack = 0;
            int i = 1;
            Token cursor = expressionTokens.get(i);
            List<Token> interior = new ArrayList<>();
            while (parenthesisStack > 0 || cursor.type != TokenType.CLOSE_PARENTHESIS) {
                interior.add(cursor);
                i++;
                cursor = expressionTokens.get(i);
                if      (cursor.type == TokenType.OPEN_PARENTHESIS)     parenthesisStack++;
                else if (cursor.type == TokenType.CLOSE_PARENTHESIS)    parenthesisStack--;
            }
            return xpEvaluate(interior);
        }
        
        Token nextToken = expressionTokens.get(1);
        if (nextToken.type == TokenType.OPERATOR) {
            if (expressionTokens.size() == 2) {
                Log.error(Log.Source.PARSER, "Expected token after operator");
            }

            Expression.AtomicExpression subject = null;
            if (leadingToken.type == TokenType.VARIABLE) {
                subject = new Expression.VariableExpression(leadingToken.lexeme);
            }
            else if (leadingToken.type == TokenType.MEASURE) {
                subject = new Expression.LiteralExpression(Measure.parse(leadingToken.lexeme));
            }

            Expression.OperationExpression.Operation operation = Expression.OperationExpression.parseOperation(nextToken.lexeme);
            
            return new Expression.OperationExpression(subject, operation, xpEvaluate(expressionTokens.subList(2, expressionTokens.size())));
        }

        String grammarExpression = "";
        for (Token token : expressionTokens) grammarExpression += token + " ";
        Log.error(Log.Source.PARSER, "Unrecognised expression grammar: " + grammarExpression);
        return result;
    }
    
    static Statement stmtEvaluate(List<Token> statementTokens) {
        Statement result = null;

        if (statementTokens.size() <= 1) return null;  // No valid statements have only 1 token
        Token leadingToken = statementTokens.get(0);
        Token nextToken = statementTokens.get(1);
        if (leadingToken.type == TokenType.VARIABLE) {
            if (nextToken.type == TokenType.ASSIGN) {
                List<Token> targetTokens = new ArrayList<>();
                targetTokens.addAll(statementTokens.subList(2, statementTokens.size()));
                return new AssignmentStatement(
                    new Expression.VariableExpression(leadingToken.lexeme), 
                    xpEvaluate(targetTokens)
                );
            }

        }
        if (leadingToken.type == TokenType.KEYWORD) {
            if (IfStatement.keywords.contains(leadingToken.lexeme)){

                if (nextToken.type == TokenType.OPEN_PARENTHESIS) {
                    List<Token> entryConditionTokens = lookAheadGrouping(
                        statementTokens.subList(2, statementTokens.size()), TokenType.OPEN_PARENTHESIS, TokenType.CLOSE_PARENTHESIS);
                    Expression entryCondition = xpEvaluate(entryConditionTokens);
                    List<Token> blockTokens = lookAheadGrouping(statementTokens.subList(4 + entryConditionTokens.size(), statementTokens.size()),
                        TokenType.OPEN_BLOCK , TokenType.CLOSE_BLOCK);
                    
                    return new IfStatement(entryCondition, parse(blockTokens), leadingToken.lexeme.equals("WHILE"));
                }
            }
        }
        return result;
    }

}
