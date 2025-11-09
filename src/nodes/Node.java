package nodes;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class Node {
    public final Set<Node> children = new LinkedHashSet<>();
    public final Set<String> hints = new LinkedHashSet<>();

    public String category = "";
    
    public interface Visitor<T> {
        // Type checking performed by implementation instead of interface
        public <E extends Node> T visit(E expression);
    } 
    
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Node() {}
    public Node(Set<Node> children) {
        this();
        this.children.addAll(children);
    }

    public <C extends Node> ArrayList<C> getChildren(String givenHint, Class<C> nodeClass) {
        ArrayList<C> result = new ArrayList<>();
        for (Node child : children) {
            if (child.hints.contains(givenHint) && nodeClass.isInstance(child)) {
                result.add(nodeClass.cast(child));
            }
        }
        return result;
    }

    public ArrayList<Node> getChildren() {
        return new ArrayList<>(children);
    }

    public ArrayList<Node> getChildren(String givenHint) {
        return getChildren(givenHint, Node.class);
    }

    public Node getChild(String givenHint) {
        ArrayList<Node> hintedChildren = getChildren(givenHint);
        return (hintedChildren.isEmpty()) ? (Node) null : hintedChildren.get(0);
    }

    @Override
    public String toString() {
        if (category.equals("")) {
            return "?";
        }
        String result = category;
        if (!hints.isEmpty()) result += "::" + String.join(":", hints);
        return result;
    }
    

    public String asTree(int indentationLevel) {
        final String indentation = "---";
        String result = "";

        for (int i = 0; i < indentationLevel; i++) result += indentation;
        result += this;
        result += "\n";

        for (Node child : children) {
            result += child.asTree(indentationLevel + 1);
        }

        return result;
    }

    public String asTree() {
        return asTree(0);
    }

    public LinkedHashSet<Node> query(String hint, int depth) {
        LinkedHashSet<Node> results = new LinkedHashSet<>();

        for (Node child : children) {
            if (child.hints.contains(hint)) results.add(child);
            if (depth != 0) results.addAll(child.query(hint, depth - 1));
        }

        return results;
    }

    public LinkedHashSet<Node> query(String hint) { return query(hint, 0); }
}
