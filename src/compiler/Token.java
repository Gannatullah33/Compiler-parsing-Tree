package compiler;

 enum TokenType {
    FORMULA_START("="),
    NUMBER("NUMBER"),
    CELL_REF("CELL_REF"),
    FUNCTION("FUNCTION"),

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),

    GT(">"),
    LT("<"),
    GE(">="),
    LE("<="),
    EQ("=="),
    NEQ("!="),

    LPAREN("("),
    RPAREN(")"),
    COMMA(","),
    COLON(":"),

    EOF("EOF");

    private final String display;

    TokenType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return type + " -> '" + lexeme + "' (" + line + ":" + column + ")";
    }
}