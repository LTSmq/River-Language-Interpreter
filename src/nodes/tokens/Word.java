package nodes.tokens;
import java.util.regex.Matcher;

public class Word extends Token {
    public Word(Matcher match) { 
        super(match);
     }
     
     @Override
     public String[] tokenNames() {
         return new String[] {"Word"};
     }
} 