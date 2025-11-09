package grammar.metatokens;

import java.util.List;
import java.util.HashSet;

public abstract class MetaToken {
    public final String lexeme;
    public MetaToken(String lexeme, String group) { this.lexeme = lexeme; }
    public List<String> groups() { return null; }

    @Override 
    public String toString() {
        return this.getClass().getSimpleName() + "( " + lexeme + " )";
    }
    
}
