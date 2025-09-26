//> Scanning token-type

/*
 * token    -> (\w+)|([^\w\s])
 * 
 * ADD      -> +
 * SUBTRACT -> -
 * MULTIPLY -> *
 * DIVIDE   -> /
 * 
 * MEASURE  -> \b(\d+(?:\.\d+)?)\s*(p?[km]?m\d*)?(p?[dhs]?\d*)?\b
 * VARIABLE -> \b((?:[_a-z])[_a-z0-9]*)\b
 * 
 * DELIMITER          -> [;\n]
 * KEYWORD            -> \b([_A-Z][_A-Z0-9]*)\b
 * OPEN_PARENTHESIS   -> (
 * CLOSE_PARENTHESIS  -> )
 * 
 */

public enum TokenType {
  // Debug
  UNRECOGNISED,

  // Operators
  ASSIGN, OPERATOR, COMPARATOR,

  // Values
  MEASURE, VARIABLE, ARRAY, 

  // Structure
  KEYWORD, STATEMENT_DELIMITER, ARGUMENT_DELIMITER, 

  // Stacks
  OPEN_PARENTHESIS, CLOSE_PARENTHESIS,
  OPEN_BLOCK,       CLOSE_BLOCK,
  OPEN_BRACKET,     CLOSE_BRACKET,

}
