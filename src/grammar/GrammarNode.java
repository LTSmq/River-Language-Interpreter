package grammar;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import grammar.metatokens.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import nodes.Node;
import nodes.tokens.Token;


public class GrammarNode{
    public final MetaToken metaToken;
    private final ArrayList<GrammarNode> children = new ArrayList<>();
    public final HashMap<Node, Integer> tokensConsumed = new HashMap<>();
    static final ParseStrategy GENERAL_PARSE_STRATEGY = new GenericParseStreategy();
    
    public static GrammarNode generateGrammarTree(Rule rule) {  // Rule extends List<MetaToken>
        // Initialise storage
        LinkedList<GrammarNode> grammarNodeStack = new LinkedList<>();
        
        // Iterate through meta tokens
        for (MetaToken metaToken : rule) {
            // Convert meta token to grammar node
            GrammarNode grammarNode = new GrammarNode(metaToken);

            // Place all arguments "ary" from stack into operator node's children (binary => arguments = 2, unary => arguments = 1)
            if (metaToken instanceof OperatorMetaToken operator) {
                int ary = operator.ary();
                
                // Add children in reverse order of stack to ensure left->right precedence
                ArrayList<GrammarNode> unstackedArguments = new ArrayList<>();
                for (int i = 0; i < ary; i++) unstackedArguments.add(grammarNodeStack.pollLast());
                for (GrammarNode argument : unstackedArguments.reversed()) grammarNode.addChild(argument);

            }
            // Add node to stack
            grammarNodeStack.add(grammarNode);
        }

        assert grammarNodeStack.size() == 1;

        GrammarNode result = grammarNodeStack.get(0);
        result.collapseBinary();
        return result;
    }

    public Node parse(Token[] tokens, int head,  Map<String, GrammarNode> context) {
        Node result = GENERAL_PARSE_STRATEGY.parse(this, tokens, head, context);
        if (result != null) {
            
        }
        return result;
    }

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

    boolean represents(Token token) {
        return Arrays.asList(token.tokenNames()).contains(metaToken.lexeme);
    }

    public List<GrammarNode> getChildren() {
        return new ArrayList<>(children);
    }
    public String asTree(int indentationLevel) {
        final String indentation = "----";
        String result = "";

        for (int i = 0; i < indentationLevel; i++) result += indentation;
        result += this.getClass().getSimpleName() + "(" + metaToken.toString() + ")";
        result += "\n";

        for (GrammarNode child : children) {
            result += child.asTree(indentationLevel + 1);
        }

        return result;
    }

    public String asTree() {
        return asTree(0);
    }

    // Assumes all infix binary operators are expandable to associative operators
    public void collapseBinary() {
        LinkedHashSet<GrammarNode> newChildren = new LinkedHashSet<>();
        for (GrammarNode child : getChildren()) {
            child.collapseBinary();
            InfixMetaToken infix = (metaToken instanceof InfixMetaToken imt) ? imt : null;
            InfixMetaToken childInfix = (child.metaToken instanceof InfixMetaToken imt) ? imt : null;

            if ((infix == null) || (childInfix == null) || (childInfix.type != infix.type)) {
                newChildren.add(child);
                continue;
            }

            for (GrammarNode descendant : child.getChildren()) {
                newChildren.add(descendant);
            }


        }
        children.clear();
        children.addAll(newChildren);
    }
    
    public void dumpHints(Node to) {
        if (!(metaToken instanceof OperandMetaToken operand)) return;
        to.hints.addAll(operand.hints);
    }
}


interface ParseStrategy {
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context);
}


class GenericParseStreategy implements ParseStrategy { 
    static final LinkedHashMap<String, ParseStrategy> OPERATIONS = new LinkedHashMap<>() {{
        put(".", new ConcatParseStrategy());
        put("|", new OrParseStrategy());
        put("*", new PlusStrategy(false));
        put("+", new PlusStrategy(true));
        put("?", new OptionalStrategy());
    }};

    int indent = 0;
    @Override
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context) {
        switch (gn.metaToken) {
            case LiteralMetaToken literal -> {
                // Leaf case
                Token leading = tokens[head];
                if (gn.represents(leading)) {
                    gn.tokensConsumed.put(leading, 1);
                    gn.dumpHints(leading);
                    return leading;
                }
                
                // Recursion case
                else if (context.keySet().contains(literal.lexeme)) {
                    for (int i = 0; i < indent; i++) System.out.print("----");
                    System.out.println("START: " + literal.lexeme);
                    GrammarNode template = context.get(literal.lexeme);
                    indent++;
                    Node response = template.parse(tokens, head, context);
                    indent --;
                    for (int i = 0; i < indent; i++) System.out.print("----");
                    System.out.println("END: " + literal.lexeme + " (" + (response != null) + ")");
                    if (response == null) return null;
                    
                    response.category = literal.lexeme;
                    
                    gn.tokensConsumed.put(response, template.tokensConsumed.get(response));
                    gn.dumpHints(response);
                    return response;
                }
                
                else return null;
                
            }

            // Operation case
            case OperatorMetaToken operator -> {
                assert OPERATIONS.keySet().contains(operator.lexeme);
                return OPERATIONS.get(operator.lexeme).parse(gn, tokens, head, context);
            }

            // Bad case
            default -> {
                return (Node) null;
            }
        }
    }
}


class ConcatParseStrategy implements ParseStrategy {
    // Technically handles arbitrary child count even though program structure has 2 children max
    @Override
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context) {
        LinkedHashSet<Node> nodeChildren = new LinkedHashSet<>();
        int start = head;

        for (GrammarNode template : gn.getChildren()) {
            Node response = template.parse(tokens, head, context);
            if (response == null) return null;

            nodeChildren.add(response);
            head += template.tokensConsumed.get(response);
        }

        Node result = new Node(nodeChildren);
        result.category = "<Concat>";
        gn.tokensConsumed.put(result, head - start);

        return result;
    }
}


class OrParseStrategy implements ParseStrategy {
    // Technically handles arbitrary child count even though program structure has 2 children max
    @Override
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context) {
        for (GrammarNode template : gn.getChildren()) {
            Node response = template.parse(tokens, head, context);
            if (response == null) continue;
            
            LinkedHashSet<Node> wrap = new LinkedHashSet<>();
            wrap.add(response);
            Node wrapper = new Node(wrap);
            gn.tokensConsumed.put(wrapper, template.tokensConsumed.get(response));
            wrapper.category = "<Any>";
            return wrapper;

        }

        return null;
    }
}


class PlusStrategy implements ParseStrategy {
    final boolean onePlus;

    public PlusStrategy(boolean onePlus) {
        this.onePlus = onePlus;
    }

    @Override
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context) {
        GrammarNode template = gn.getChildren().get(0);
        Node response = template.parse(tokens, head, context);
        if (onePlus && response == null) return null;

        int start = head;
        LinkedHashSet<Node> nodeChildren = new LinkedHashSet<>();
        while (response != null) {
            nodeChildren.add(response);
            head += template.tokensConsumed.get(response);
            if (head < tokens.length) response = template.parse(tokens, head, context); 
            else break;
        }

        Node result = new Node(nodeChildren);
        result.category = (onePlus) ? "<OnePlus>" : "<ZeroPlus>"; 
        gn.tokensConsumed.put(result, head - start);
        return result;
    }
}


class OptionalStrategy implements ParseStrategy {
    @Override
    public Node parse(GrammarNode gn, Token[] tokens, int head, Map<String, GrammarNode> context) {
        int tokensConsumed = 0;
        LinkedHashSet<Node> nodeChildren = new LinkedHashSet<>();

        GrammarNode template = gn.getChildren().get(0);
        Node response = template.parse(tokens, head, context);
        if (response != null) {
            tokensConsumed = template.tokensConsumed.get(response);
            nodeChildren.add(response);
        }

        Node result = new Node(nodeChildren);
        gn.tokensConsumed.put(result, tokensConsumed);
        result.category = "<Optional>";
        return result;
    }
}
