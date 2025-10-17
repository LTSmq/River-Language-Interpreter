package nodes.tokens;
import java.util.regex.Matcher;

// Keywords represent readable strings that have reserved functionality
// This language intends enforce all keywords to be capitalised and all dynamic words to be lowercase
public abstract class KWord extends Token {
    public KWord(Matcher match) { super(match); }

    public abstract static class Conditional extends KWord{
        public Conditional(Matcher match) { super(match); }
    }

    public static class If extends Conditional {
        public If(Matcher match) { super(match); }

        @Override 
        public String[] tokenNames() {
            return new String[] {"KeywordIf"};
        }
    }

    public static class While extends Conditional {
        public While(Matcher match) { super(match); }
        
        @Override 
        public String[] tokenNames() {
            return new String[] {"KeywordWhile"};
        }
    }
}