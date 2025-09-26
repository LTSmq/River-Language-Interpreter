import java.util.regex.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Scanner {
    public static final HashMap<TokenType, String> tokenPatterns; 
    private static final String TOKEN_EXTRACT_PATTERN = "([\\w\\.]+)|([^\\w\\s])+";
    private boolean log;
    static {
        tokenPatterns = new HashMap<>();
        
        tokenPatterns.put(TokenType.ASSIGN,                 "=");
        tokenPatterns.put(TokenType.OPERATOR,               "(?:[\\+\\*\\-\\/])|(?:(?:[!=<>]=)|(?:[<>]))");

        tokenPatterns.put(TokenType.MEASURE,                "\\b(\\d+(?:\\.\\d+)?)\\s*(p?[km]?m\\d*)?(p?[dhs]?\\d*)?\\b");
        tokenPatterns.put(TokenType.VARIABLE,               "\\b((?:[_a-z])[_a-z0-9]*)\\b");
        tokenPatterns.put(TokenType.KEYWORD,                "\\b((?:[_A-Z])[_A-Z0-9]*)\\b");

        tokenPatterns.put(TokenType.ARGUMENT_DELIMITER,     ",");
        tokenPatterns.put(TokenType.STATEMENT_DELIMITER,    "[;]");
        tokenPatterns.put(TokenType.OPEN_PARENTHESIS,       "\\(");
        tokenPatterns.put(TokenType.CLOSE_PARENTHESIS,      "\\)");
        tokenPatterns.put(TokenType.OPEN_BLOCK,             "\\{");
        tokenPatterns.put(TokenType.CLOSE_BLOCK,            "\\}");
        tokenPatterns.put(TokenType.OPEN_BRACKET,           "\\[");
        tokenPatterns.put(TokenType.CLOSE_BRACKET,          "\\]");
    }

    public Scanner() {};
    public Scanner(boolean use_logging) {
        log = use_logging;
    }

    public List<Token> getTokens(String source) {
        log("Initialising output array for token result...");
        List<Token> scanned = new ArrayList<Token>();
        
        log("Initialising generic token extraction matcher...");
        Pattern extractionPattern = Pattern.compile(TOKEN_EXTRACT_PATTERN);
        Matcher extractionMatcher = extractionPattern.matcher(source);
        
        log("Finding generic tokens...");
        List<String> matchedStrings = new ArrayList<String>();
        while (extractionMatcher.find()) {
            String match = extractionMatcher.group();
            log("Found -> '" + match + "'");
            matchedStrings.add(match);
        }

        log("Beginning token classification...");
        for (String matchedString : matchedStrings) {
            TokenType token_type = TokenType.UNRECOGNISED;

            for (TokenType type: tokenPatterns.keySet()) {
                boolean matches = matchedString.matches(tokenPatterns.get(type));
                if (matches) {
                    token_type = type;
                    break;
                }
            }
            scanned.add(new Token(token_type, matchedString));
            log("\"" + matchedString + "\" classified as " + token_type);
        }

        log("Produced list of " + scanned.size() + " tokens");
        return scanned;
    }

    private void log(String description) {
        if (log) System.out.println("Scanner:\t" + description);
    }
}