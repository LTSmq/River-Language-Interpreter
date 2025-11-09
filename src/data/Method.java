package data;

import nodes.Node;

public class Method implements Variant{
    public Node procedure;
    public Method() {}
    public Method(Node procedure) { this.procedure = procedure; }

    @Override
    public boolean isTrue() {
        return procedure != null;
    }
}
