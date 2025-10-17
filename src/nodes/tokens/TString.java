package nodes.tokens;
import java.util.regex.Matcher;

public class TString extends Token {
    public String content;

    public TString(Matcher match)  {
        super(match);
        if (match == null) return;
        content = match.group("content");
    }

    @Override
    public String[] tokenNames() {
        return new String[] {"String"};
    }
}
