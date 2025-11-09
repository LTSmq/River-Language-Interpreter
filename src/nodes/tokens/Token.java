package nodes.tokens;
import java.util.regex.Matcher;

import nodes.Node;

abstract public class Token extends Node {
    public String lexeme = "\0";

    public Token(Matcher match) {
        if (match == null) return;
        lexeme = match.group();
    };

    // Virtual method used in sampling to find which names in the config file correspond to which classes
    public String[] tokenNames() {
        return new String[] {};
    }

    public boolean equals(Token other) {
        return this.lexeme.equals(other.lexeme);  // Default
    }
    @Override
    public String toString() {
        String hintString = "";
        if (!hints.isEmpty())  hintString += "::" + String.join(":", hints);
        return this.getClass().getSimpleName() + "(\"" + lexeme + "\")" + hintString;
    }
}
