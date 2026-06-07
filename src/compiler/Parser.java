package compiler;

import java.util.*;
import java.util.function.Supplier;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;
    private final List<String> errors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        if (!match(TokenType.FORMULA_START)) {
            errors.add("Formula must start with '='");
            return null;
        }

        ASTNode expr = expression();
        if (!match(TokenType.EOF)) {
            errors.add("Unexpected trailing token: " + currentLexeme());
        }
        return expr == null ? null : new FormulaNode(expr);
    }

    private ASTNode expression() {
        return comparison();
    }

    private ASTNode comparison() {
        return binaryOp(() -> additive(), TokenType.GT, TokenType.LT, TokenType.GE,
                TokenType.LE, TokenType.EQ, TokenType.NEQ);
    }

    private ASTNode additive() {
        return binaryOp(() -> multiplicative(), TokenType.PLUS, TokenType.MINUS);
    }

    private ASTNode multiplicative() {
        return binaryOp(() -> primary(), TokenType.MULTIPLY, TokenType.DIVIDE);
    }

    private ASTNode binaryOp(Supplier<ASTNode> lower, TokenType... operators) {
        ASTNode left = lower.get();

        while (matchAny(operators)) {
            String op = previous().lexeme;
            ASTNode right = lower.get();
            left = new BinaryNode(op, left, right);
        }
        return left;
    }

    private ASTNode primary() {
        if (match(TokenType.NUMBER))
            return new NumberNode(previous().lexeme);

        if (match(TokenType.CELL_REF)) {
            String startCell = previous().lexeme;
            if (match(TokenType.COLON)) {
                if (!match(TokenType.CELL_REF)) {
                    errors.add("Range end must be a cell reference after ':'");
                    return null;
                }
                String endCell = previous().lexeme;
                validateRange(startCell, endCell);
                return new RangeNode(startCell, endCell);
            }
            return new CellNode(startCell);
        }

        if (match(TokenType.FUNCTION))
            return functionCall();

        if (match(TokenType.LPAREN)) {
            ASTNode e = expression();
            if (!match(TokenType.RPAREN))
                errors.add("Missing ')'");
            return e;
        }

        errors.add("Expected number, cell reference, function, or '('");
        return null;
    }

    private ASTNode functionCall() {
        String name = previous().lexeme;

        if (!match(TokenType.LPAREN)) {
            errors.add("Expected '(' after function");
            return null;
        }

        List<ASTNode> args = new ArrayList<>();

        if (!check(TokenType.RPAREN)) {
            do {
                args.add(expression());
            } while (match(TokenType.COMMA));
        }

        if (!match(TokenType.RPAREN))
            errors.add("Expected ',' or ')' in function call");

        return new FuncNode(name, args);
    }

    private void validateRange(String start, String end) {
        int splitStart = Lexer.findCellSplitPoint(start);
        int splitEnd = Lexer.findCellSplitPoint(end);
        String startCol = start.substring(0, splitStart);
        String endCol = end.substring(0, splitEnd);
        int startRow = Integer.parseInt(start.substring(splitStart));
        int endRow = Integer.parseInt(end.substring(splitEnd));

        if (compareCols(startCol, endCol) > 0 || startRow > endRow) {
            errors.add("Malformed range: start cell must come before end cell (" + start + ":" + end + ")");
        }
    }

    private int compareCols(String a, String b) {
        return Integer.compare(colToNumber(a), colToNumber(b));
    }

    private int colToNumber(String col) {
        int result = 0;
        for (int i = 0; i < col.length(); i++) {
            result = result * 26 + (col.charAt(i) - 'A' + 1);
        }
        return result;
    }

    private boolean match(TokenType t) {
        if (check(t)) { pos++; return true; }
        return false;
    }

    private boolean matchAny(TokenType... types) {
        for (TokenType t : types)
            if (match(t)) return true;
        return false;
    }

    private boolean check(TokenType t) {
        return pos < tokens.size() && tokens.get(pos).type == t;
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private String currentLexeme() {
        if (pos >= tokens.size()) return "<end>";
        return tokens.get(pos).lexeme;
    }

    public List<String> getErrors() {
        return errors;
    }
}