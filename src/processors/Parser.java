package processors;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import nodes.*;
import nodes.expressions.*;
import nodes.statements.*;
import nodes.tokens.*;

import data.metatokens.*;
import javax.management.RuntimeErrorException;


public class Parser {

    final Set<Rule> rules;

    public Parser(String configSource) {
        rules = MetaScanner.getRulesPostfix(configSource);
    }

    public Statement[] parse(Token[] tokens) {
        ArrayList<Statement> result = new ArrayList<>();
        LinkedList<Token> tokenQueue = new LinkedList<>(Arrays.asList(tokens));

        while (!tokenQueue.isEmpty()) {
            Token cursor = tokenQueue.removeLast();
            for (Rule rule : rules) {

            }
        }
        
        return result.toArray(Statement[]::new);
    } 

}


class GrammarNode {
    public static GrammarNode generateGrammarTree(List<MetaToken> postfixMetaTokenSequence) {
        // Initialise storage
        LinkedList<GrammarNode> grammarNodeStack = new LinkedList<>();

        // Iterate through meta tokens
        for (MetaToken metaToken : postfixMetaTokenSequence) {
            // Convert meta token to grammar node
            GrammarNode grammarNode = new GrammarNode(metaToken);

            // Place all arguments "ary" from stack into operator node's children (binary => arguments = 2, unary => arguments = 1)
            if (metaToken instanceof OperatorMetaToken operator) {
                int ary = operator.ary();
                
                // Add children in reverse order of stack to ensure left->right precedence
                ArrayList<GrammarNode> unstackedArguments = new ArrayList<>();
                for (int i = 0; i < ary; i++) unstackedArguments.add(grammarNodeStack.pop());
                for (GrammarNode argument : unstackedArguments.reversed()) grammarNode.addChild(argument);

            }
            // Add node to stack
            grammarNodeStack.add(grammarNode);
        }

        return grammarNodeStack.get(0);
    }

    public final MetaToken metaToken;
    private final ArrayList<GrammarNode> children = new ArrayList<>();

    public GrammarNode(MetaToken metaToken, List<GrammarNode> initialChildren) {
        if (initialChildren != null) children.addAll(initialChildren);
        this.metaToken = metaToken;
    }
    
    public GrammarNode(MetaToken metaToken) {
        this(metaToken, (List<GrammarNode>) null);
    }

    public void addChild(GrammarNode node) {
        if (node != null) children.add(node);
    }

    public List<GrammarNode> getChildren() {
        return children;
    }

} 


enum AcceptCode {
    ACCEPTED,       // The token is valid in the sequence, but the rule has not been satisfied
    FULFILLED,      // The token has completed the rule, but more tokens are acceptable
    TERMINATED,     // The token has completed the rule and the rule expects no more tokens
    REJECTED,       // The token is incompatible with the rule
}


class RuleInterpreter {
    final Rule rule;
    LinkedList<OperandMetaToken> operandStack = new LinkedList<>();
    Set<String> expectedContinue = new LinkedHashSet<>();  // The token name to continue the matching (i.e. maintain current operation in rule)
    Set<String> expectedEscape = new LinkedHashSet<>();  // The token name to progress the pattern (i.e. get next operation in rule)
    private int ruleHead;

    public RuleInterpreter(Rule rule) {
        this.rule = rule;
    }

    private void escape() {
        MetaToken nextMetaToken = rule.get(ruleHead);
        while (nextMetaToken instanceof OperandMetaToken operand) {
            operandStack.addLast(operand);
            ruleHead++;
            nextMetaToken = rule.get(ruleHead);
        }

        assert nextMetaToken instanceof OperatorMetaToken;
        OperatorMetaToken operator = (OperatorMetaToken) nextMetaToken;

        switch (operator) {
            case PostfixMetaToken postfix -> {
                List<String> continueNames = new ArrayList<>() {{ add(operandStack.pop().lexeme); }};
                List<String> escapeNames = new ArrayList<>();
                
                if (!operandStack.isEmpty()) escapeNames.add(operandStack.peek().lexeme);
                switch (postfix.type) {
                    case ONE_PLUS -> {
                        
                    }
                    
                    case ZERO_ONE -> {
                    
                    }
                    
                    case ZERO_PLUS -> {
                    
                    }
                    
                    default -> {}
                    
                }
            }
            case InfixMetaToken infix -> {

            }
            default -> {}
        }
    }

    public boolean determine(List<Token> tokenSequence) {

        for (Token token : tokenSequence) {

        }
        return false;
    }
}