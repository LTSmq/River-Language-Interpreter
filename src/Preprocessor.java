import java.util.regex.*;

public class Preprocessor {
    static final Pattern compoundPattern = Pattern.compile("(?<leftOperand>[_a-z][_a-z0-9]*)\\s*(?<operator>[\\+\\-\\*\\/])=(?<rightOperand>[^;]*);");
    static final Pattern definitionPattern = Pattern.compile("DEFINE\\s+(?<constant>[_a-zA-Z][_a-zA-Z0-9]*)\\s+(?<value>\\S+)");
    public static String process(String content) {
        String processed = content;

        int lastIndex = 0;
        Matcher definitionFinder = definitionPattern.matcher(processed);
        while (definitionFinder.find()) {
            String keyword = definitionFinder.group("constant");
            String substitution = definitionFinder.group("value");
            
            processed = processed.replace(definitionFinder.group(), "");
            processed = processed.replace(keyword, substitution);
        }

        char[] buffer = new char[processed.length()];
        int parenthesisStack = 0;
        int bracketStack = 0;

        for (int i = 0; i < buffer.length; i++) {
            char c = processed.charAt(i);
            switch (c) {
                case '[': bracketStack++;
                break;

                case ']': bracketStack--;
                break;

                case '(': parenthesisStack++;
                break;

                case ')': parenthesisStack--;
                break;
            }
            if (c == '\n' && bracketStack + parenthesisStack == 0) {
                buffer[i] = ';';
            }
            else {
                buffer[i] = c;
            }
        }

        processed = new String(buffer);
        processed = processed.replaceAll("};*", "} ; ");

        char[] separation_values = new char[] {';', ',', '(', ')', '[', ']', '{', '}'};
        for (char value : separation_values) {
            String str = new String(new char[] {value});
            processed = processed.replace(str, " " + str);
        }
        Matcher compoundMatcher = compoundPattern.matcher(processed);
        StringBuilder sb = new StringBuilder();

        while (compoundMatcher.find()) {
            int matchStart = compoundMatcher.start();
            int matchEnd = compoundMatcher.end();
            String leftOperand = compoundMatcher.group("leftOperand");
            String rightOperand = compoundMatcher.group("rightOperand");
            String opeator = compoundMatcher.group("operator");

            String reconstructed = leftOperand + " = " + leftOperand + " " + opeator +  " " + rightOperand + ";";

            sb.append(processed, lastIndex, matchStart);
            sb.append(reconstructed);
            lastIndex = matchEnd;

        }
        sb.append(processed.substring(lastIndex));
        processed = sb.toString();

        return processed;
    } 


    String convert(Matcher matcher) {  // Virtual
        return "";
    }

}
