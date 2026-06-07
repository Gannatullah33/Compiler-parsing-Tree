package compiler;

import java.util.*;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private static final Set<String> FUNCTIONS = Set.of("SUM", "MAX", "MIN", "IF");
    private static final Map<String, TokenType> TWO_CHAR_OPS = Map.of(
        ">=", TokenType.GE,
        "<=", TokenType.LE,
        "!=", TokenType.NEQ,
        "==", TokenType.EQ
    );
    private static final Map<Character, TokenType> ONE_CHAR_OPS = Map.of(
        '+', TokenType.PLUS,
        '-', TokenType.MINUS,
        '*', TokenType.MULTIPLY,
        '/', TokenType.DIVIDE,
        '>', TokenType.GT,
        '<', TokenType.LT,
        '(', TokenType.LPAREN,
        ')', TokenType.RPAREN,
        ',', TokenType.COMMA,
        ':', TokenType.COLON
    );

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            // formula start
            if (c == '=' && pos == 0) {
                add(TokenType.FORMULA_START, "=");
                advance();
                continue;
            }

            if (Character.isDigit(c)) {
                number();
                continue;
            }

            if (Character.isLetter(c)) {
                identifier();
                continue;
            }

            // two-char operators
            boolean matched = false;
            for (String op : TWO_CHAR_OPS.keySet()) {
                if (input.startsWith(op, pos)) {
                    add(TWO_CHAR_OPS.get(op), op);
                    pos += op.length();
                    col += op.length();
                    matched = true;
                    break;
                }
            }
            if (matched) continue;

            // one-char operators
            if (ONE_CHAR_OPS.containsKey(c)) {
                add(ONE_CHAR_OPS.get(c), String.valueOf(c));
                advance();
                continue;
            }

            errors.add("Invalid char: " + c + " at " + line + ":" + col);
            advance();
        }

        add(TokenType.EOF, "");
        return tokens;
    }

    private void number() {
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            advance();
        }
        add(TokenType.NUMBER, input.substring(start, pos));
    }

    private void identifier() {
        int start = pos;
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            advance();
        }

        String text = input.substring(start, pos).toUpperCase();

        if (FUNCTIONS.contains(text)) {
            add(TokenType.FUNCTION, text);
        } else if (isCell(text)) {
            add(TokenType.CELL_REF, text);
        } else {
            errors.add("Invalid identifier: " + text);
        }
    }

    private boolean isCell(String s) {
        int i = findCellSplitPoint(s);
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        return i == s.length() && i > 1;
    }

    private static int findCellSplitPoint(String cell) {
        int i = 0;
        while (i < cell.length() && Character.isLetter(cell.charAt(i))) i++;
        return i;
    }

    private void add(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, line, col));
    }

    private void advance() {
        pos++;
        col++;
    }

    public List<String> getErrors() {
        return errors;
    }
}