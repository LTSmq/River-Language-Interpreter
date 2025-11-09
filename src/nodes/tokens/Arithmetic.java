package nodes.tokens;

import java.util.regex.Matcher;

import data.OperationDirection;
import data.OperationType;

public class Arithmetic extends Operator {
    public static final String[] encodedGlyphs = {"+", "\0", "-", "*", "\0", "-", "^", "\0", "~"};
    public OperationType type;

    @Override 
    public boolean equals(Token other) {
        return (
                other instanceof Arithmetic operator
            &&  operator.direction == this.direction
            &&  operator.type == this.type
        );
    }

    @Override
    public String[] tokenNames() {
        return new String[] {"Arithmetic"};
    }

    public Arithmetic(Matcher match) {
        super(match);
        String value = lexeme;
        int code = -1;
        for (int i = 0; i < encodedGlyphs.length; i++) {
            if (value.equals(encodedGlyphs[i])) {
                code = i;
                break;
            }
        }

        if (code == -1) return;

        type = OperationType.values()[code / 3];
        direction = OperationDirection.values()[code % 3];
    }


}