package processors;

import java.util.Set;

import grammar.Rule;

import java.util.Map;
import java.util.LinkedHashMap;

import nodes.*;
import nodes.tokens.*;

import grammar.GrammarNode;
import grammar.MetaScanner;


public class Parser {

    final Map<String, GrammarNode> ruleNodes = new LinkedHashMap<>();

    public Parser(String configSource) {
        Set<Rule> rules = MetaScanner.getRulesPostfix(configSource);
        for (Rule rule : rules) ruleNodes.put(rule.name, GrammarNode.generateGrammarTree(rule));
    }

    public Node parse(Token[] tokens) {
        
        GrammarNode programRule = ruleNodes.get("PROCEDURE");
        
        return programRule.parse(tokens, 0, ruleNodes);
    } 

    public String grammarTree() {
        String str = "";
        for (String ruleName : ruleNodes.keySet()) {
            GrammarNode grammar = ruleNodes.get(ruleName);
            str = str + ruleName + "\n";
            str = str + grammar.asTree();
            str = str + "\n";
        }
        return str;
    }
}
