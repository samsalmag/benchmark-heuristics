package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.MethodAmbiguityException;
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

    private final boolean debug = false;    // Display debug prints.

    public static List<String> ambigousList = new ArrayList<>();
    public static List<String> noSuchElementList = new ArrayList<>();
    public static List<String> unsupportedOperationList = new ArrayList<>();
    public static List<String> concurrentModificationList = new ArrayList<>();
    public static List<String> otherExceptionList = new ArrayList<>();

    private final ParserConfiguration PARSER_CONFIG;
    private final CombinedTypeSolver TYPE_SOLVER;
    private final JavaParserAdapter PARSER;

    private final int maxDepth;           // Maximum recursion depth
    private final String baseMainPath;
    private final String baseTestPath;
    private final String projectTerm;     // This is used to figure out of method is within source code or a java lib.
                                          // Should reflect a unique package name within the project, e.g., "rxjava" for project RxJava.

    private ParsedMethod parsedMethod;
    private Map<String, Integer> methodCalls = new HashMap<>();
    private Map<String, Integer> objectInstantiations = new HashMap<>();
    private Map<String, Integer> packageAccesses = new HashMap<>();

    private boolean runComplete;

    public MethodParser(int maxDepth, String baseMainPath, String baseTestPath, String projectTerm, File... typeSolverPaths) {
        this.maxDepth = maxDepth;
        this.baseMainPath = baseMainPath;
        this.baseTestPath = baseTestPath;
        this.projectTerm = projectTerm;

        TYPE_SOLVER = new CombinedTypeSolver();
        PARSER_CONFIG = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(TYPE_SOLVER));

        TYPE_SOLVER.add(new ReflectionTypeSolver(false));
        Arrays.stream(typeSolverPaths).forEach(t -> TYPE_SOLVER.add(new JavaParserTypeSolver(t, PARSER_CONFIG)));

        PARSER = JavaParserAdapter.of(new JavaParser(PARSER_CONFIG));
    }


    public MethodParser(int maxDepth, String baseMainPath, String baseTestPath, String projectTerm) {
        this(maxDepth, baseMainPath, baseTestPath, projectTerm,
                new File("projects\\RxJava-3.1.8\\src\\main\\java").getAbsoluteFile(),
                new File("projects\\RxJava-3.1.8\\src\\test\\java").getAbsoluteFile());
    }

    /**
     * Runs the parser.
     */
    public ParsedMethod parse(String filePath, String methodName) {
        resetValues();

        // Add to ParsedMethod
        String formattedPath = filePath.substring(filePath.indexOf("java\\") + "java\\".length()).trim();
        parsedMethod.setFilePath(formattedPath);
        parsedMethod.setMethodName(methodName);

        // Parse the Java file
        try {
            CompilationUnit cu = PARSER.parse(new File(filePath));

            // Find the method to start from
            MethodDeclaration startMethod = cu.findAll(MethodDeclaration.class)
                    .stream()
                    .filter(method -> method.getNameAsString().equals(methodName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("ERROR: Method not found \"" + methodName + "\""));

            try {
                parseMethod(startMethod, 0);
            } catch (MethodAmbiguityException e) {
                System.out.println("AMBIGUOUS METHOD CALL!!!#!#!");
                ambigousList.add(filePath);
            }
            catch (NoSuchElementException e) {
                System.out.println("NO SUCH ELEMENT EXCEPTION!!");
                noSuchElementList.add(filePath);
            }
            catch (UnsupportedOperationException e) {
                System.out.println("UnsupportedOperationException EXCEPTION!!");
                unsupportedOperationList.add(filePath);
            }
            catch (ConcurrentModificationException e) {
                System.out.println("concurrentModificationList EXCEPTION!!");
                concurrentModificationList.add(filePath);
            }
            catch (Exception e) {
                System.out.println("Other EXCEPTION!!");
                otherExceptionList.add(filePath);
            }

        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Add last bits to ParsedMethod
        parsedMethod.setMethodCalls(new HashMap<>(methodCalls));
        parsedMethod.setObjectInstantiations(new HashMap<>(objectInstantiations));
        parsedMethod.setPackageAccesses(new HashMap<>(packageAccesses));

        runComplete = true;

        if (debug) {
            System.out.println("\n" + sortMapOutOfPlace(methodCalls));
            System.out.println("\n" + sortMapOutOfPlace(objectInstantiations));
            System.out.println("\n" + sortMapOutOfPlace(packageAccesses));
        }

        return parsedMethod;
    }

    private void resetValues() {
        this.parsedMethod = new ParsedMethod();
        this.methodCalls = new HashMap<>();
        this.objectInstantiations = new HashMap<>();
        this.packageAccesses = new HashMap<>();
        runComplete = false;
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
        if (depth >= maxDepth) {
            System.out.println("MAXIMUM DEPTH REACHED FOR METHOD \"" + methodDeclaration.getNameAsString() + "\"");
            return;
        }

        if (depth == 0) { // is JUnit test if depth is 0
            parsedMethod.incrementLinesOfCodeJunitTest(MethodStatsExtractor.countLinesOfCode(methodDeclaration));
            parsedMethod.incrementLogicalLinesOfCodeJunitTest(MethodStatsExtractor.countLogicalLinesOfCode(methodDeclaration));
        }

        // Find any object instantiations inside the method
        findObjectInstantiations(methodDeclaration);

        // Count stats of method
        parsedMethod.incrementNumConditionals(MethodStatsExtractor.countConditionals(methodDeclaration));
        parsedMethod.incrementNumLoops(MethodStatsExtractor.countLoops(methodDeclaration));
        parsedMethod.incrementNumNestedLoops(MethodStatsExtractor.countNestedLoops(methodDeclaration));
        parsedMethod.incrementLinesOfCode(MethodStatsExtractor.countLinesOfCode(methodDeclaration));
        parsedMethod.incrementLogicalLinesOfCode(MethodStatsExtractor.countLogicalLinesOfCode(methodDeclaration));

        // Loop through all method calls in the provided methodDeclaration variable.
        List<MethodCallExpr> methodCallExprList = methodDeclaration.findAll(MethodCallExpr.class);
        parsedMethod.incrementNumMethodCalls(methodCallExprList.size());  // Add stat to parsed method
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
                        String packageName = reflectionMethodDeclaration.getPackageName();
                        String className = reflectionMethodDeclaration.getClassName();

                        // If there are dots in the class name, assume the last part is the nested class name and ignore it for the path
                        String[] classNameParts = className.split("\\.");
                        if (classNameParts.length > 1) {
                            className = classNameParts[0]; // Use only the top-level class name
                        }

                        String classPath = (packageName + "." + className).replace(".", "\\");
                        String fullPath = baseMainPath + classPath + ".java";
                        CompilationUnit methodCu = PARSER.parse(new File(fullPath).getAbsoluteFile());

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
                    parsedMethod.incrementNumRecursiveMethodCalls(1); // increase non java lib method call stats
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

    public ParsedMethod getParsedMethod() {
        if (!runComplete) throw new IllegalStateException("ERROR: Method parsing not complete yet! Use MethodParser.parse() first or wait for parsing to complete.");
        return parsedMethod;
    }
}
