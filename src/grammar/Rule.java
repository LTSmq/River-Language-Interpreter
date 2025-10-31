package grammar;

import java.util.LinkedList;
import java.util.Collection;

import grammar.metatokens.*;

public class Rule extends LinkedList<MetaToken>{
    public final String name;
    
    public Rule(String name, Collection<? extends MetaToken> initial) {
        super(initial);
        this.name = name;
    }

    public Rule(String name) {
        this(name, new LinkedList<>());
    }

    public LinkedList<OperandMetaToken> findHintedTokens(String matchHint) {
        LinkedList<OperandMetaToken> result = new LinkedList<>();

        for (MetaToken metaToken : this) {
            if (!(metaToken instanceof OperandMetaToken operand)) continue;
            if (operand.hints.contains(matchHint)) result.add(operand);
        }

        return result;
    } 
}
