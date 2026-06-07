package compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 2 && "--file".equals(args[0])) {
            runFormulaFile(args[1]);
            return;
        }

        List<String> tests = List.of(
                "=A1 + B2",
                "=(A1 + B1) * 2",
                "=SUM(A1, B2, 5)",
                "=IF(A1 > 10, B1, 0)",
                "=SUM(A1:A5)",
                "=MAX(SUM(A1,2), MIN(B1,3))",

                "=SUM(A1 B2)",
                "=A1+",
                "=A5:A1",
                "A1+B2",
                "=SUM(A1:)"
        );

        for (String input : tests) {
            analyzeFormula(input);
        }
    }

    private static void runFormulaFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            int index = 1;
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    index++;
                    continue;
                }
                System.out.println("\n--- Formula line " + index + " ---");
                analyzeFormula(line.trim());
                index++;
            }
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e.getMessage());
        }
    }

    private static void analyzeFormula(String input) {
        System.out.println("\nINPUT: " + input);

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token t : tokens) {
            System.out.println(t);
        }

        if (hasErrors(lexer.getErrors(), "LEXER")) {
            return;
        }

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        if (hasErrors(parser.getErrors(), "PARSER")) {
            return;
        }

        System.out.println("AST:");
        ast.print(0);
        System.out.println("AST JSON:");
        System.out.println(ast.toJson(0));
    }

    private static boolean hasErrors(List<String> errors, String label) {
        if (!errors.isEmpty()) {
            System.out.println(label + " ERRORS: " + errors);
            return true;
        }
        return false;
    }
}