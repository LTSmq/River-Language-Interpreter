package nodes.tokens;

import java.util.regex.Matcher;

// Bracket class represents a grouping of tokens, including expressions and blocks
public class Bracket extends Token {
    // Coded glyphs are provided in order of the enumerations to resolve by modulation instead of mapping
    private static final String[] codedGlyphs = new String[] {"(", ")", "[", "]", "{", "}"};

    public enum Type { ROUND, SQUARE, CURLY }
    public Type type;
    
    public enum Direction { OPEN, CLOSE }
    public Direction direction;

    @Override
    public String[] tokenNames() {
        return new String[] {"OpenExpression", "CloseExpression", "OpenSubscript", "CloseSubscript", "OpenBlock", "CloseBlock"};
    }

    public Bracket(Matcher match) {
        super(match);
        String value = lexeme;
        int code = -1;
        
        for (int i = 0; i < codedGlyphs.length; i++) {
            if (codedGlyphs[i].equals(value)) {
                code = i;
                break;
            }
        }

        if (code == -1) return;  // Perhaps add an exception here...
        
        type = Type.values()[code / 2];
        direction = Direction.values()[code % 2];
    }

    public int findTerminusIndex(Token[] nextTokens) {
        if (this.direction != Direction.OPEN) return -1;
        if (nextTokens.length <= 0) return -1;

        int stack = 0;
        int cursor = 0;
        Token token = nextTokens[cursor]; 

        while (
                (stack > 0)                                         // "Internal brackets have not been closed"
            ||  !(token instanceof Bracket)                         // "The token is not a bracket"
            ||  (((Bracket) token).type != this.type)               // "The token is a bracket but not the same type"
            ||  (((Bracket) token).direction != Direction.CLOSE)    // "The token is a bracket with same type but not a closing bracket"
        ) {
            cursor++;
            if (cursor >= nextTokens.length) return -1;

            token = nextTokens[cursor];
            Bracket bracket = (Bracket) token;
            if (bracket == null) continue;

            switch (bracket.direction) {
                case Direction.OPEN     -> stack++;
                case Direction.CLOSE    -> stack--;
            }

        }

        return cursor;
    }

    @Override
    public boolean equals(Token other) {
        return other instanceof Bracket bracket && bracket.type == this.type;
    }
}
