package compiler;

import java.util.List;

abstract class ASTNode {
    abstract void print(int indent);
    abstract String toJson(int indent);

    protected String indent(int level) {
        return "  ".repeat(level);
    }
}

class NumberNode extends ASTNode {
    int value;

    NumberNode(String v) { value = Integer.parseInt(v); }

    void print(int i) {
        System.out.println(indent(i) + "Number: " + value);
    }

    @Override
    String toJson(int indent) {
        return "{ \"type\": \"Number\", \"value\": " + value + " }";
    }
}

class CellNode extends ASTNode {
    String name;

    CellNode(String n) { name = n; }

    void print(int i) {
        System.out.println(indent(i) + "Cell: " + name);
    }

    @Override
    String toJson(int indent) {
        return "{ \"type\": \"Cell\", \"name\": \"" + name + "\" }";
    }
}

class RangeNode extends ASTNode {
    String start;
    String end;

    RangeNode(String start, String end) {
        this.start = start;
        this.end = end;
    }

    void print(int i) {
        System.out.println(indent(i) + "Range: " + start + ":" + end);
    }

    @Override
    String toJson(int indent) {
        return "{ \"type\": \"Range\", \"start\": \"" + start + "\", \"end\": \"" + end + "\" }";
    }
}

class BinaryNode extends ASTNode {
    String op;
    ASTNode left, right;

    BinaryNode(String o, ASTNode l, ASTNode r) {
        op = o; left = l; right = r;
    }

    void print(int i) {
        System.out.println(indent(i) + "Op: " + op);
        left.print(i+1);
        right.print(i+1);
    }

    @Override
    String toJson(int indent) {
        String childIndent = indent(indent + 1);
        String baseIndent = indent(indent);
        return "{\n" +
                childIndent + "\"type\": \"Binary\",\n" +
                childIndent + "\"op\": \"" + op + "\",\n" +
                childIndent + "\"left\": " + left.toJson(indent + 1) + ",\n" +
                childIndent + "\"right\": " + right.toJson(indent + 1) + "\n" +
                baseIndent + "}";
    }
}

class FuncNode extends ASTNode {
    String name;
    List<ASTNode> args;

    FuncNode(String n, List<ASTNode> a) {
        name = n; args = a;
    }

    void print(int i) {
        System.out.println(indent(i) + "Func: " + name);
        for (ASTNode a : args) a.print(i+1);
    }

    @Override
    String toJson(int indent) {
        String childIndent = indent(indent + 1);
        String baseIndent = indent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(childIndent).append("\"type\": \"Function\",\n")
                .append(childIndent).append("\"name\": \"").append(name).append("\",\n")
                .append(childIndent).append("\"args\": [");

        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(args.get(i).toJson(indent + 2));
        }
        sb.append("]\n")
                .append(baseIndent).append("}");
        return sb.toString();
    }
}

class FormulaNode extends ASTNode {
    ASTNode expr;

    FormulaNode(ASTNode e) { expr = e; }

    void print(int i) {
        System.out.println("Formula");
        expr.print(1);
    }

    @Override
    String toJson(int indent) {
        String childIndent = indent(indent + 1);
        String baseIndent = indent(indent);
        return "{\n" +
                childIndent + "\"type\": \"Formula\",\n" +
                childIndent + "\"expression\": " + expr.toJson(indent + 1) + "\n" +
                baseIndent + "}";
    }
}