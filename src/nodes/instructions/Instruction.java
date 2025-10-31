package nodes.instructions;

import grammar.Rule;
import nodes.Node;


public abstract class Instruction extends Node {
    public final Rule rule;

    public Instruction(Rule rule, Set<Node> children) {
        
    }
}
