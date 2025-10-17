package nodes;

import java.util.List;

import nodes.tokens.Token;

public abstract class Node {
    public interface Visitor<T> {
        // Type checking performed by implementation instead of interface
        public <E extends Node> T visit(E expression);
    } 

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public List<Token> getTokens() {
        return null;
    }
}
