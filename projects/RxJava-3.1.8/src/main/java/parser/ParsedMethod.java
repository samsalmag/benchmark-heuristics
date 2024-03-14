package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class for a parsed method. Only stores data.
 *
 * @author Sam Salek
 */
public class ParsedMethod {

    private String filePath;
    private String methodName;

    private Map<String, Integer> methodCalls = new HashMap<>();
    private Map<String, Integer> objectInstantiations = new HashMap<>();
    private Map<String, Integer> packageAccesses = new HashMap<>();

    private int numConditionals;
    private int numLoops;
    private int numNestedLoops;
    private int numMethodCalls;
    private int linesOfCode;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, Integer> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(Map<String, Integer> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public Map<String, Integer> getObjectInstantiations() {
        return objectInstantiations;
    }

    public void setObjectInstantiations(Map<String, Integer> objectInstantiations) {
        this.objectInstantiations = objectInstantiations;
    }

    public Map<String, Integer> getPackageAccesses() {
        return packageAccesses;
    }

    public void setPackageAccesses(Map<String, Integer> packageAccesses) {
        this.packageAccesses = packageAccesses;
    }

    public int getNumConditionals() {
        return numConditionals;
    }

    public void setNumConditionals(int numConditionals) {
        this.numConditionals = numConditionals;
    }

    public int getNumLoops() {
        return numLoops;
    }

    public void setNumLoops(int numLoops) {
        this.numLoops = numLoops;
    }

    public int getNumNestedLoops() {
        return numNestedLoops;
    }

    public void setNumNestedLoops(int numNestedLoops) {
        this.numNestedLoops = numNestedLoops;
    }

    public int getNumMethodCalls() {
        return numMethodCalls;
    }

    public void setNumMethodCalls(int numMethodCalls) {
        this.numMethodCalls = numMethodCalls;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }
}
