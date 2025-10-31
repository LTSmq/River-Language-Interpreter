package nodes.tokens;
import java.util.regex.Matcher;

// Delimiters represent a means to end a stream such as signifying th end of a statement or argument expression
public class Delimiter extends Token{
    public enum Type{
        UNRECOGNISED,
        STATEMENT,
        ARGUMENT,
        OUTPUT,
    }

    public Type type;

    @Override
    public String[] tokenNames() {
        return new String[] {"StatementDelimiter", "ArgumentDelimiter", "OutputDelimiter"};
    }
    
    public Delimiter(Matcher match) { 
        super(match);
        String value = lexeme;
        switch (value) {
            case ";" -> type = Type.STATEMENT;
            case "," -> type = Type.ARGUMENT;
            case "'" -> type = Type.OUTPUT;
            default -> type = Type.UNRECOGNISED;
        }
    }

    @Override
    public boolean equals(Token other) {
        return other instanceof Delimiter delimiter && delimiter.type == this.type;
    }
}
