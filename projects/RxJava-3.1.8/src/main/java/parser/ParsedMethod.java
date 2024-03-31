package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class for a parsed method. Only stores data.
 *
 * @author Sam Salek
 */
public class ParsedMethod {

    private transient String filePath;
    private transient String methodName;

    private Map<String, Integer> methodCalls = new HashMap<>();
    private Map<String, Integer> objectInstantiations = new HashMap<>();
    private Map<String, Integer> packageAccesses = new HashMap<>();

    private int numConditionals;
    private int numLoops;
    private int numNestedLoops;
    private int numMethodCalls;
    private int numRecursiveMethodCalls;    // Non java lib calls
    private int linesOfCode;                // Counts physical lines of code
    private int linesOfCodeJunitTest;
    private int logicalLinesOfCode;
    private int logicalLinesOfCodeJunitTest;


    public void incrementNumConditionals(int incrementAmount) {
        numConditionals += incrementAmount;
    }

    public void incrementNumLoops(int incrementAmount) {
        numLoops += incrementAmount;
    }

    public void incrementNumNestedLoops(int incrementAmount) {
        numNestedLoops += incrementAmount;
    }

    public void incrementNumMethodCalls(int incrementAmount) {
        numMethodCalls += incrementAmount;
    }

    public void incrementNumRecursiveMethodCalls(int incrementAmount) {
        numRecursiveMethodCalls += incrementAmount;
    }

    public void incrementLinesOfCode(int incrementAmount) {
        linesOfCode += incrementAmount;
    }

    public void incrementLinesOfCodeJunitTest(int incrementAmount) {
        linesOfCodeJunitTest += incrementAmount;
    }

    public void incrementLogicalLinesOfCode(int incrementAmount) {
        logicalLinesOfCode += incrementAmount;
    }

    public void incrementLogicalLinesOfCodeJunitTest(int incrementAmount) {
        logicalLinesOfCodeJunitTest += incrementAmount;
    }

    /**
     * Nicely prints the parsing data of this ParsedMethod.
     *
     */
    public void print() {
        System.out.println("\nMETHOD CALLS");
        this.getMethodCalls().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nOBJECT INSTANTIATIONS");
        this.getObjectInstantiations().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nPACKAGE ACCESSES");
        this.getPackageAccesses().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nSTATS");
        System.out.println("Conditionals: " + this.getNumConditionals());
        System.out.println("Loops: " + this.getNumLoops());
        System.out.println("Nested loops: " + this.getNumNestedLoops());
        System.out.println("Number of method calls: " + this.getNumMethodCalls());
        System.out.println("Number of recursive method calls: " + this.getNumRecursiveMethodCalls());
        System.out.println("Lines of code: " + this.getLinesOfCode());
        System.out.println("Lines of code (JUnit test): " + this.getLinesOfCodeJunitTest());
        System.out.println("Logical lines of code: " + this.getLogicalLinesOfCode());
        System.out.println("Logical lines of code (JUnit test): " + this.getLogicalLinesOfCodeJunitTest());
    }

    // --------------------------------------- //
    // --------------- SETTERS --------------- //
    // --------------------------------------- //

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setMethodCalls(Map<String, Integer> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public void setObjectInstantiations(Map<String, Integer> objectInstantiations) {
        this.objectInstantiations = objectInstantiations;
    }

    public void setPackageAccesses(Map<String, Integer> packageAccesses) {
        this.packageAccesses = packageAccesses;
    }

    // --------------------------------------- //
    // --------------- GETTERS --------------- //
    // --------------------------------------- //

    public String getFilePath() {
        return filePath;
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<String, Integer> getMethodCalls() {
        return methodCalls;
    }

    public Map<String, Integer> getObjectInstantiations() {
        return objectInstantiations;
    }

    public Map<String, Integer> getPackageAccesses() {
        return packageAccesses;
    }

    public int getNumConditionals() {
        return numConditionals;
    }

    public int getNumLoops() {
        return numLoops;
    }

    public int getNumNestedLoops() {
        return numNestedLoops;
    }

    public int getNumMethodCalls() {
        return numMethodCalls;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public int getLinesOfCodeJunitTest() {
        return linesOfCodeJunitTest;
    }

    public int getLogicalLinesOfCode() {
        return logicalLinesOfCode;
    }

    public int getLogicalLinesOfCodeJunitTest() {
        return logicalLinesOfCodeJunitTest;
    }

    public int getNumRecursiveMethodCalls() {
        return numRecursiveMethodCalls;
    }
}
