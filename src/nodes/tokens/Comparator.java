package nodes.tokens;

import java.util.regex.Matcher;

import data.OperationDirection;

public class Comparator extends Operator {
    public static final String[] encodedGlyphs = {">", "!=", "<", ">=", "==", "<="};
    public boolean equalInclusive; 

    @Override
    public String[] tokenNames() {
        return new String[] { "Comparator" };
    }

    @Override
    public boolean equals(Token other) {
        return (
                other instanceof Comparator comparator
            &&  comparator.direction == this.direction
            &&  comparator.equalInclusive == this.equalInclusive
        );
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