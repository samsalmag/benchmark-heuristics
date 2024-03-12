package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserAnonymousClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that parses a method recursively. The parser goes through all methods within the provided method as well, etc...
 * Is able to find method invocations, object instantiations, and package accesses.
 * Use MethodParser.parse() to first parse a method, then use getters to retrieve the information about it.
 *
 * @author Sam Salek
 */
public class MethodParser {

    // Display debug prints or not.
    private final boolean debug = false;

    private final int MAX_DEPTH = Integer.MAX_VALUE; // Maximum recursion depth
    private final String BASE_MAIN_PATH = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java\\";
    private final String BASE_TEST_PATH = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java\\";

    private final ParserConfiguration PARSER_CONFIG;
    private final CombinedTypeSolver TYPE_SOLVER;
    private final JavaParserAdapter PARSER;

    private final String filePath;
    private final String methodName;
    private final String projectTerm = "rxjava";    // This is used to figure out of method is within source code or a java lib.
                                                    // Should reflect a unique package name within the project.

    private final Map<String, Integer> methodCalls = new HashMap<>();
    private final Map<String, Integer> objectInstantiations = new HashMap<>();
    private final Map<String, Integer> packageAccesses = new HashMap<>();

    private boolean runComplete = false;

    private int numConditionals = 0;
    private int numLoops = 0;
    private int numNestedLoops = 0;

    /**
     * Creates a new instance of MethodParser.
     *
     *
     * @param filePath Path to the file where the method resides.
     * @param methodName Name of the method.
     */
    public MethodParser(String filePath, String methodName) {
        this.filePath = filePath;
        this.methodName = methodName;

        TYPE_SOLVER = new CombinedTypeSolver();
        PARSER_CONFIG = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(TYPE_SOLVER));

        TYPE_SOLVER.add(new ReflectionTypeSolver(false));
        TYPE_SOLVER.add(new JavaParserTypeSolver(new File("E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\main\\java"), PARSER_CONFIG));
        TYPE_SOLVER.add(new JavaParserTypeSolver(new File("E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java"), PARSER_CONFIG));

        PARSER = JavaParserAdapter.of(new JavaParser(PARSER_CONFIG));
    }

    /**
     * Runs the parser. Do this first before using any getters.
     */
    public void run() {
        try {
            // Parse the Java file
            CompilationUnit cu = PARSER.parse(new File(filePath));

            // Find the method to start from
            MethodDeclaration startMethod = cu.findAll(MethodDeclaration.class)
                    .stream()
                    .filter(method -> method.getNameAsString().equals(methodName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("ERROR: Method not found \"" + methodName + "\""));

            parseMethod(startMethod, 0);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (debug) {
            System.out.println("\n" + sortMapOutOfPlace(methodCalls));
            System.out.println("\n" + sortMapOutOfPlace(objectInstantiations));
            System.out.println("\n" + sortMapOutOfPlace(packageAccesses));
        }

        runComplete = true;
    }

    /**
     * Returns a ParsedMethod containing the data this parser extracted.
     *
     * @return A ParsedMethod instance.
     */
    public ParsedMethod toParsedMethod() {
        ParsedMethod parsedMethod = new ParsedMethod();
        String formattedPath = filePath.substring(filePath.indexOf("java\\") + "java\\".length()).trim();

        // Add data to parsed method instance.
        parsedMethod.setFilePath(formattedPath);
        parsedMethod.setMethodName(this.methodName);
        parsedMethod.setMethodCalls(getMethodCalls());
        parsedMethod.setObjectInstantiations(getObjectInstantiations());
        parsedMethod.setPackageAccesses(getPackageAccesses());
        parsedMethod.setNumConditionals(getNumConditionals());
        parsedMethod.setNumLoops(getNumLoops());
        parsedMethod.setNumNestedLoops(getNumNestedLoops());
        parsedMethod.setNumMethodCalls(getNumMethodCalls());

        return parsedMethod;
    }

    /**
     * Gets all method calls within the method, recursively.
     *
     * @return A map containing all method calls and how many times it occurred.
     */
    public Map<String, Integer> getMethodCalls() {
        if (!runComplete) throw new IllegalStateException("ERROR: Method not parsed yet! Use MethodParser.parse() first.");
        return new HashMap<>(methodCalls);
    }

    /**
     * Gets all object instantiations within the method, recursively.
     *
     * @return A map containing all object instantiations and how many times it occurred.
     */
    public Map<String, Integer> getObjectInstantiations() {
        if (!runComplete) throw new IllegalStateException("ERROR: Method not parsed yet! Use MethodParser.parse() first.");
        return new HashMap<>(objectInstantiations);
    }

    /**
     * Gets all package accesses within the method, recursively.
     *
     * @return A map containing all package accesses and how many times it occurred.
     */
    public Map<String, Integer> getPackageAccesses() {
        if (!runComplete) throw new IllegalStateException("ERROR: Method not parsed yet! Use MethodParser.parse() first.");
        return new HashMap<>(packageAccesses);
    }

    /**
     * Gets number of conditional statements in the method, recursively.
     *
     * @return An integer of number of conditionals.
     */
    public int getNumConditionals() {
        return numConditionals;
    }

    /**
     * Gets number of loops in the method, recursively.
     *
     * @return An integer of number of loops.
     */
    public int getNumLoops() {
        return numLoops;
    }

    /**
     * Gets number of nested loops in the method, recursively.
     *
     * @return An integer of number of nested loops.
     */
    public int getNumNestedLoops() {
        return numNestedLoops;
    }

    /**
     * Gets number of method calls in the method, recursively.
     *
     * @return An integer of number of method calls.
     */
    public int getNumMethodCalls() {
        return methodCalls.values().stream().mapToInt(v -> v).sum();
    }

    /**
     * Increments the value of a key inside a map by 1.
     *
     * @param map The map where the key to increment resides.
     * @param keyName Name of the key to increment.
     */
    private void incrementMapValue(Map<String, Integer> map, String keyName) {
        map.put(keyName, map.getOrDefault(keyName, 0) + 1);
    }

    /**
     * Sorts the provided map based on key.
     * Does it out of place (returns sorted map), so remember to save the return value.
     *
     * @param map The map to sort.
     * @return The sorted map.
     */
    private Map<String, Integer> sortMapOutOfPlace(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a,b)->b, LinkedHashMap::new));
    }

    /**
     * Finds all objects instantiations within a method.
     *
     * @param methodDeclaration The method to search object instantiations in.
     */
    private void findObjectInstantiations(MethodDeclaration methodDeclaration) {
        // Loop through all object instantiations...
        List<ObjectCreationExpr> objectCreationExprList = methodDeclaration.findAll(ObjectCreationExpr.class);
        for (ObjectCreationExpr creationExpr : objectCreationExprList) {
            ResolvedConstructorDeclaration resolvedConstructorDeclaration = creationExpr.resolve();
            String classPath;
            String packageName;

            // If the object instantiation is an anonymous class, then the package name is not readily available...
            // Do some magic to get package name of the class.
            if (resolvedConstructorDeclaration.declaringType() instanceof JavaParserAnonymousClassDeclaration) {
                JavaParserAnonymousClassDeclaration anonymousClassDeclaration = (JavaParserAnonymousClassDeclaration) resolvedConstructorDeclaration.declaringType();
                classPath = anonymousClassDeclaration.getSuperTypeDeclaration().getQualifiedName();
                packageName = anonymousClassDeclaration.getSuperTypeDeclaration().getPackageName();
            }
            // Else, do it very simply.
            else {
                classPath = resolvedConstructorDeclaration.declaringType().asReferenceType().getQualifiedName();
                packageName = resolvedConstructorDeclaration.declaringType().asReferenceType().getPackageName();
            }

            // Add to object instantiation map and package accesses map.
            incrementMapValue(objectInstantiations, classPath);
            incrementMapValue(packageAccesses, packageName);
        }
    }

    /**
     * Parses a method. Finds all method invocations, object instantiations, and package accesses within the provided method.
     *
     * @param methodDeclaration The method to parse.
     * @param depth The current depth of the parsing, since this method can be used recursively.
     *              Will not exceed MAX_DEPTH.
     */
    private void parseMethod(MethodDeclaration methodDeclaration, int depth) {
        if (depth >= MAX_DEPTH) {
            System.out.println("MAXIMUM DEPTH REACHED FOR METHOD \"" + methodDeclaration.getNameAsString() + "\"");
            return;
        }

        // Find any object instantiations inside the method
        findObjectInstantiations(methodDeclaration);

        // Count stats of method
        numConditionals += MethodStatsExtractor.countConditionals(methodDeclaration);
        numLoops += MethodStatsExtractor.countLoops(methodDeclaration);
        numNestedLoops += MethodStatsExtractor.countNestedLoops(methodDeclaration);

        // Loop through all method calls in the provided methodDeclaration variable.
        List<MethodCallExpr> methodCallExprList = methodDeclaration.findAll(MethodCallExpr.class);
        for (MethodCallExpr callExpr : methodCallExprList) {
            boolean javaLibFile = false;

            try {
                ResolvedMethodDeclaration resolvedMethodDeclaration = callExpr.resolve();

                MethodDeclaration calledMethodDeclaration = null;
                // If method call is from source code, we can get its method declaration directly.
                if (resolvedMethodDeclaration instanceof JavaParserMethodDeclaration) {
                    JavaParserMethodDeclaration javaParserMethodDeclaration = (JavaParserMethodDeclaration) resolvedMethodDeclaration;
                    calledMethodDeclaration = javaParserMethodDeclaration.getWrappedNode();
                }

                // If the method call is from a library (such as JRE), we cannot get its method declaration directly.
                // We need to jump through some hoops...
                else if (resolvedMethodDeclaration instanceof ReflectionMethodDeclaration) {
                    ReflectionMethodDeclaration reflectionMethodDeclaration = (ReflectionMethodDeclaration) resolvedMethodDeclaration;

                    // Check if the called method is located in a RxJava package.
                    // If true, get full path for the class that holds the called method. Then create a new compilation unit that parses that path.
                    // The parser finds and provides us the method declaration for the called method.
                    if (reflectionMethodDeclaration.toString().contains(projectTerm)) {
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

                // Add method call and package access to respective map.
                incrementMapValue(methodCalls, resolvedMethodDeclaration.getQualifiedName());
                incrementMapValue(packageAccesses, resolvedMethodDeclaration.getPackageName());

                // Continue finding method calls recursively if the called method is not from a java library.
                if (!javaLibFile) {
                    if (debug) System.out.println("METHOD INVOCATION: " + resolvedMethodDeclaration.getQualifiedName());
                    parseMethod(calledMethodDeclaration, depth + 1);
                }
                else {
                    if (debug) System.out.println("JAVA LIB FILE (STOPPING RECURSION): " + resolvedMethodDeclaration.getQualifiedName());
                }
            }
            catch (UnsolvedSymbolException e) {
                System.out.println("CLASS NOT FOUND: " + callExpr.getName());
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Separate each recursion section, so it is easier to distinguish and read the debug print.
            if (debug && depth == 0) System.out.println();
        }
    }
}
