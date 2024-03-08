package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class Parser {

    private final int MAX_DEPTH = 3; // Maximum recursion depth

    private final ParserConfiguration PARSER_CONFIG;
    private final CombinedTypeSolver TYPE_SOLVER;
    private final JavaParserAdapter PARSER;

    private final String BASE_MAIN_PATH = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java\\";
    private final String BASE_TEST_PATH = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java\\";

    private String filePath;
    private String methodName;

    public Parser(String filePath, String methodName) {
        this.filePath = filePath;
        this.methodName = methodName;

        TYPE_SOLVER = new CombinedTypeSolver();
        PARSER_CONFIG = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(TYPE_SOLVER));

        TYPE_SOLVER.add(new ReflectionTypeSolver(false));
        //TYPE_SOLVER.add(new JarTypeSolver("C:\\Program Files (x86)\\Java\\jre1.8.0_301\\lib\\rt.jar"));
        TYPE_SOLVER.add(new JavaParserTypeSolver(new File("E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java"), PARSER_CONFIG));
        TYPE_SOLVER.add(new JavaParserTypeSolver(new File("E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java"), PARSER_CONFIG));

        PARSER = JavaParserAdapter.of(new JavaParser(PARSER_CONFIG));
    }

    public void run() {
        try {
            // Parse the Java file
            CompilationUnit cu = PARSER.parse(new File(filePath));

            // Find the method to start from
            MethodDeclaration startMethod = cu.findAll(MethodDeclaration.class)
                    .stream()
                    .filter(method -> method.getNameAsString().equals(methodName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Method not found: " + methodName));

            // Start recursively extracting method invocations from the start method
            System.out.println("Starting extraction from method: " + methodName + "\n");

            extractMethodInvocations(startMethod, 0);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractMethodInvocations(MethodDeclaration methodDeclaration, int depth) {
        if (depth >= MAX_DEPTH) {
            System.out.println("Maximum depth reached for method: " + methodDeclaration.getNameAsString());
            return;
        }

        // Loop through all method calls in the provided methodDeclaration variable.
        List<MethodCallExpr> methodCallExprList = methodDeclaration.findAll(MethodCallExpr.class);
        for (MethodCallExpr callExpr : methodCallExprList) {
            boolean javaLibFile = false;

            try {
                ResolvedMethodDeclaration resolvedMethodDeclaration = callExpr.resolve();

                MethodDeclaration calledMethodDeclaration = null;
                // If method call is from source code, we can get its method declaration directly.
                if (resolvedMethodDeclaration instanceof JavaParserMethodDeclaration javaParserMethodDeclaration) {
                    calledMethodDeclaration = javaParserMethodDeclaration.getWrappedNode();
                }

                // If the method call is from a library (such as JRE), we cannot get its method declaration directly.
                // We need to jump through some hoops...
                else if (resolvedMethodDeclaration instanceof ReflectionMethodDeclaration reflectionMethodDeclaration) {

                    // Check if the called method is located in a RxJava package.
                    // If true, get full path for the class that holds the called method. Then create a new compilation unit that parses that path.
                    // The parser finds and provides us the method declaration for the called method.
                    if (reflectionMethodDeclaration.toString().contains("rxjava")) {
                        String classPath = (reflectionMethodDeclaration.getPackageName() + "." + reflectionMethodDeclaration.getClassName()).replace(".", "\\");
                        String fullPath = BASE_MAIN_PATH + classPath + ".java";
                        CompilationUnit methodCu = PARSER.parse(new File(fullPath));

                        calledMethodDeclaration = methodCu
                                .findAll(MethodDeclaration.class)
                                .stream()
                                .filter(m -> m.getNameAsString()
                                        .equals(reflectionMethodDeclaration.getName()))
                                .findFirst()
                                .get();
                    }

                    // If not true, then called method is probably from a file in some java library.
                    else {
                        javaLibFile = true;
                    }
                }

                // Continue finding method calls recursively if the called method is not from a java library.
                if (!javaLibFile) {
                    System.out.println("METHOD INVOCATION: " + resolvedMethodDeclaration.getQualifiedName());
                    extractMethodInvocations(calledMethodDeclaration, depth + 1);
                }
                else {
                    System.out.println("JAVA LIB FILE (STOPPING RECURSION): " + resolvedMethodDeclaration.getQualifiedName());
                }
            }
            catch (UnsolvedSymbolException e) {
                System.out.println("CLASS NOT FOUND: " + callExpr.getName());
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Separate each recursion section, so it is easier to distinguish and read the print.
            if (depth == 0) {
                System.out.println();
            }
        }
    }
}
