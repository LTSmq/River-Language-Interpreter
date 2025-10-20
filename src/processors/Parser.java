package processors;

import java.util.Set;

import nodes.*;
import nodes.expressions.*;
import nodes.statements.*;
import nodes.tokens.*;

import data.metatokens.*;


public class Parser {
    final Set<Rule> rules;

    public Parser(String configSource) {
        rules = MetaScanner.getRulesPostfix(configSource);
        for (Rule rule : rules) {
            System.out.println(rule.name + ":");
            System.out.print("\t");
            for (MetaToken mt : rule) {
                System.out.print(mt.lexeme + " ");
            }
            System.out.println();
        }
    }

    public Statement[] parse(Token[] tokens) {

        return null;
    } 
}

