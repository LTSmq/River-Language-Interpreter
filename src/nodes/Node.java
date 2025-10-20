package nodes;

import java.util.Map;


public abstract class Node {
    public Map<String, Node> children;

    public interface Visitor<T> {
        // Type checking performed by implementation instead of interface
        public <E extends Node> T visit(E expression);
    } 

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Node() {}
    public Node(Map<String, Node> children) {
        this();
        this.children = children;
    }

}
