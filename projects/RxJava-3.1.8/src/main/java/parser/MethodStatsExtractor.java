package parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class that takes in a method of type MethodDeclaration and returns a Map<String, Integer> of method stats.
 * E.g. stats: number of loops, number of conditionals, etc.
 *
 * Example usage:
 * Map<String, Integer> stats = MethodStatsExtractor.getStats(method);
 * System.out.println(stats);
 *
 * @author Malte Åkvist
 */
public class MethodStatsExtractor {

    // Private constructor. Use methods directly since they static.
    private MethodStatsExtractor(){}

    /**
     * Get method stats from the provided method.
     *
     * @param method The method to extract stats from.
     * @return A map containing all extracted method stats, with the map key corresponding to a type of stat.
     */
    public static Map<String, Integer> getStats(MethodDeclaration method) {
        Map<String, Integer> methodStats = new HashMap<>();

        methodStats.put("Loops", countLoops(method));
        methodStats.put("Nested loops", countNestedLoops(method));
        methodStats.put("Conditionals", countConditionals(method));
        return methodStats;
    }

    /**
     * Counts conditionals in a method.
     *
     * @param method The method to count conditionals in.
     * @return Number of conditionals.
     */
    public static int countConditionals(MethodDeclaration method) {
        int ifCount = method.findAll(IfStmt.class).size(); // Count if and else if statements

        // Count switch case statements, excludes default case
        int switchCaseCount = method.findAll(SwitchStmt.class).stream()
                .flatMap(switchStmt -> switchStmt.getEntries().stream())
                .filter(switchEntry -> switchEntry.getLabels().isNonEmpty()) // Filter out default cases
                .mapToInt(switchEntry -> 1)
                .sum();

        return ifCount + switchCaseCount;
    }

    /**
     * Counts loops in a method.
     *
     * @param method The method to count loops in.
     * @return Number of loops.
     */
    public static int countLoops(MethodDeclaration method) {
        List<Node> loops = method.findAll(Node.class, n ->
                n instanceof ForStmt || n instanceof WhileStmt || n instanceof DoStmt);
        return loops.size();
    }

    /**
     * Counts nested loops in a method.
     *
     * @param method The method to count nested loops in.
     * @return Number of nested loops.
     */
    public static int countNestedLoops(MethodDeclaration method) {
        int depth = calculateDepth(method, 0);
        return (depth > 0) ? depth - 1 : 0;
    }

    // Counts depth of nested loops in a method
    private static int calculateDepth(Node node, int depth) {
        int maxDepth = depth;
        for (Node child : node.getChildNodes()) {
            int childDepth;
            if (child instanceof ForStmt || child instanceof WhileStmt || child instanceof DoStmt) {
                childDepth = calculateDepth(child, depth + 1);
            } else {
                childDepth = calculateDepth(child, depth);
            }
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth;
    }

    public static int countLinesOfCode(MethodDeclaration method) {
        int lines = 0;
        if (method.getBegin().isPresent()) {
            int startLine = method.getBegin().get().line;
            int endLine = method.getEnd().get().line;

            if (startLine != endLine) {
                BlockStmt body = method.getBody().orElse(null);
                if (body == null) return 0;
                String[] bodyLines = body.toString().split("\\r?\\n");

                for (String bodyLine : bodyLines) {
                    // Don't count some stuff
                    if (bodyLine.trim().isEmpty() ||
                        isJavaComment(bodyLine) ||
                        bodyLine.trim().equals("{") ||
                        bodyLine.trim().equals("}")) continue;
                    lines++;
                }
            }
        }
        return lines;
    }

    public static int countLogicalLinesOfCode(MethodDeclaration method) {
        if (!method.getBody().isPresent()) {
               return 0;
            }
        BlockStmt body = method.getBody().get();
        return countStatementsInBlock(body);
    }

    private static int countStatementsInBlock(Node node) {
        int count = 0;
        for (Node child : node.getChildNodes()) {
                if (child instanceof com.github.javaparser.ast.stmt.Statement) {
                        count++;
                    }
                count += countStatementsInBlock(child);
            }
        return count;
    }


    // Check if java comment
    private static boolean isJavaComment(String line) {
        // Regex to match Java comments (single-line and multi-line)
        String regex = "(//.*$)|(/\\*.*\\*/)";
        return Pattern.matches(regex, line.trim());
    }
}
