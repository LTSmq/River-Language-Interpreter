package nodes.tokens;

import java.util.regex.Matcher;

// Assign class represents assigning a value to a variable
public class Assign extends Token {
    public Assign(Matcher match) { super(match); }

    @Override
    public String[] tokenNames() {
        return new String[] {"Assign"};
    }

    @Override
    public boolean equals(Token other) {
        return other instanceof Assign;
    }

}
