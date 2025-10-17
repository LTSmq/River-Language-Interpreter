package processors;

import nodes.*;
import nodes.expressions.*;
import nodes.statements.*;
import nodes.tokens.*;


public class Parser {
    public Parser(String configSource) {
        MetaScanner.scan(configSource);
    }

    public Statement[] parse(Token[] tokens) {

        return null;
    } 
}

