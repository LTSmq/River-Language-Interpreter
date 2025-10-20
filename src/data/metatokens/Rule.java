package data.metatokens;

import java.util.ArrayList;
import java.util.Collection;

public class Rule extends ArrayList<MetaToken>{
    public final String name;
    
    public Rule(String name, Collection<? extends MetaToken> initial) {
        super(initial);
        this.name = name;
    }

    public Rule(String name) {
        this(name, new ArrayList<>());
    }

    public ArrayList<OperandMetaToken> findHintedTokens(String matchHint) {
        ArrayList<OperandMetaToken> result = new ArrayList<>();

        for (MetaToken metaToken : this) {
            if (!(metaToken instanceof OperandMetaToken operand)) continue;
            if (operand.hints.contains(matchHint)) result.add(operand);
        }

        return result;
    } 
}
