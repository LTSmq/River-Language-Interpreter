
public class Token {
  /* Original:
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line; // [location]
  
    Token(TokenType type, String lexeme, Object literal, int line) {
      this.type = type;
      this.lexeme = lexeme;
      this.literal = literal;
      this.line = line;
    }
  
    public String toString() {
      return type + " " + lexeme + " " + literal;
    }
     */

    TokenType type;
    String lexeme;
    String pattern;

    public Token(TokenType given_type, String given_lexeme) {
      type = given_type;
      lexeme = given_lexeme;
    }

    public Token(TokenType given_type) {
      type = given_type;
      lexeme = "";
    }
    
    @Override
    public String toString() {
      return type.toString() + " -> " + lexeme;
    }
  }
  