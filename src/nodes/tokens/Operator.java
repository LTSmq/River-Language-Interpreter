package nodes.tokens;
import java.util.regex.Matcher;
import data.OperationDirection;
import data.OperationType;

// Operators represent a binary function between two values
// This includes comparitor functions which compare true/false relations between values
// and arithmetic functions which perform a mathematical manipulation of values
abstract public class Operator extends Token {
    public OperationDirection direction;
    public Operator(Matcher match) { super(match); }


 
}
