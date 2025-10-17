package processors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Constructor;

import java.util.regex.Pattern;

import nodes.tokens.*;

import java.util.regex.Matcher;

public class Scanner {
    // Pattern to extract other patterns from given configuration
    private static final Pattern TOKEN_PATTERN_EXTRACTOR = Pattern.compile("(?<name>[\\w\\-]+)\\s+(?<regex>\\S[^\\n]*)");

    // Map correlating pattern names in the configuration to their respective class constructors
    private static final LinkedHashMap<String, Constructor<? extends Token>> tokenConstructors;

    // Pattern to extract individual tokens from source, determined by configuration file during instance initialisation
    private final Pattern tokenExtractor;

    // Static initialisation
    static {
        // Initialise constructor map
        tokenConstructors = new LinkedHashMap<>();

        // Load all usable classes in order of precedence
        LinkedHashSet<Class<? extends Token>> tokenClasses = new LinkedHashSet<>() {{
            add(TString.class);
            add(TMeasure.class);
            add(Word.class);
            add(KWord.If.class);
            add(KWord.While.class);
            add(Operator.Comparator.class);
            add(Operator.Arithmetic.class);
            add(Assign.class);
            add(Bracket.class);
            add(Delimiter.class);
        }};

        // Iterate through classes
        for (Class<? extends Token> tokenClass : tokenClasses) {
            // Begin try block to handle bad method calls (should not occur)
            try {
                // Generate constructor object
                Constructor<? extends Token> tokenConstructor = tokenClass.getDeclaredConstructor(Matcher.class);
                
                // Get applicable token names from sample token
                Token sample = tokenConstructor.newInstance((Matcher) null);
                String[] tokenNames = sample.tokenNames();

                // Load into constructor map
                for (String tokenName : tokenNames) tokenConstructors.put(tokenName, tokenConstructor);
            }

            // Output construction errors
            catch (Exception e) {e.printStackTrace();}
        }
    }

    public Scanner(String configSource) {
        // Create matcher for source
        Matcher tokenMatcher = TOKEN_PATTERN_EXTRACTOR.matcher(configSource);

        // Create set for token cases in extraction pattern
        LinkedHashSet<String> extractionCases = new LinkedHashSet<>();

        // Format matches into case set
        while (tokenMatcher.find()) {
            String name = tokenMatcher.group("name");
            String regex = tokenMatcher.group("regex").replace("\r", "");  // I'm not too sure why there are return characters
            String case_ = "(?<" + name + ">" + regex + ")";

            extractionCases.add(case_);
        }

        // Generate pattern by joining cases on OR regex switch
        String tokenExtractionString = String.join("|", extractionCases);
        tokenExtractor = Pattern.compile(tokenExtractionString);
    }

    // Scanning procedure that converts a source-code string intro a series of tokens
    public List<Token> scan(String source) {
        // Reserve memory for tokens
        List<Token> tokens = new ArrayList<>();

        // Create token extractor
        Matcher match = tokenExtractor.matcher(source);

        // Iterate through all matches
        while (match.find()) {
            // Iterate through constructor names to find a matching pattern name
            for (String patternName : tokenConstructors.keySet()) { if (match.group(patternName) != null) {
                try {
                    // Use constructor object to manufacture token using match
                    Token token = tokenConstructors.get(patternName).newInstance(match);

                    // Add to result
                    tokens.add(token);
                } catch (Exception  e) { e.printStackTrace(); }
                break;
            }}
        }

        // Scan complete
        return tokens;
    }

}
