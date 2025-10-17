package nodes;

import nodes.statements.Statement;

public class Program extends Node {
    public Statement[] statements;
    
    public Program(Statement[] statements) {
        this.statements = statements;
    }
}
