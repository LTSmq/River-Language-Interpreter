package processors;

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


public class Parser {

    final Set<Rule> rules;

    static GrammarNode generateGrammarTree(List<MetaToken> postfixMetaTokenSequence) {
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

    public Parser(String configSource) {
        rules = MetaScanner.getRulesPostfix(configSource);
    }

    public Statement[] parse(Token[] tokens) {
        ArrayList<Statement> result = new ArrayList<>();

        return result.toArray(Statement[]::new);
    } 

}


class GrammarNode {
    private final MetaToken metaToken;
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

}  