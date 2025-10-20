package nodes.tokens;
import java.util.regex.Matcher;
import data.OperationDirection;
import data.OperationType;

// Operators represent a binary function between two values
// This includes comparitor functions which compare true/false relations between values
// and arithmetic functions which perform a mathematical manipulation of values
abstract public class Operator extends Token {
    public OperationDirection direction;
    public Operator(Matcher match) { super(match); }

    public static class Comparator extends Operator {
        public static final String[] encodedGlyphs = {">", "!=", "<", ">=", "==", "<="};
        public boolean equalInclusive; 

        @Override
        public String[] tokenNames() {
            return new String[] {"Comparator"};
        }
        
        public Comparator(Matcher match) { 
            super(match); 
            String value = lexeme;

            int code = -1;
            for (int i = 0; i < encodedGlyphs.length; i++) {
                if (encodedGlyphs[i].equals(value)) {
                    code = i;
                    break;
                }
            }
            if (code == -1) return;
            
            equalInclusive = 1 == code / 3;
            direction = OperationDirection.values()[code % 3];
        } 
    }
    
    public static class Arithmetic extends Operator {
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
}
