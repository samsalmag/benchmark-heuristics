package parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.Node;

import java.util.HashMap;
import java.util.List;

// Class that takes in a method of type MethodDeclaration,
// and returns a hashmap of e.g. number of loops, nr of conditionals
// Example usage:
// HashMap<String, Integer> stats = ExtractMethodStats.getStats(method); // get statistics about method
// System.out.println(stats);
public class ExtractMethodStats {
    public static HashMap<String, Integer> getStats(MethodDeclaration method) {
        HashMap<String, Integer> methodStats = new HashMap<>();

        methodStats.put("Loops", countLoops(method));
        methodStats.put("Nested loops", countNestedLoops(method));
        methodStats.put("Conditionals", countConditionals(method));
        return methodStats;
    }

    private static int countConditionals(MethodDeclaration startMethod) {
        int ifCount = startMethod.findAll(IfStmt.class).size(); // Count if and else if statements

        // Count switch case statements, excludes default case
        int switchCaseCount = startMethod.findAll(SwitchStmt.class).stream()
                .flatMap(switchStmt -> switchStmt.getEntries().stream())
                .filter(switchEntry -> switchEntry.getLabels().isNonEmpty()) // Filter out default cases
                .mapToInt(switchEntry -> 1)
                .sum();

        return ifCount + switchCaseCount;
    }

    private static int countLoops(MethodDeclaration startMethod) {
        List<Node> loops = startMethod.findAll(Node.class, n ->
                n instanceof ForStmt || n instanceof WhileStmt || n instanceof DoStmt);
        return loops.size();
    }

    private static int countNestedLoops(MethodDeclaration startMethod) {
        return calculateDepth(startMethod, 0) - 1;
    }

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
}
