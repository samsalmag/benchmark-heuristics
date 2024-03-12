package parser;

import java.util.HashMap;
import java.util.Map;

public class ParsedMethod {

    private String methodName;

    private Map<String, Integer> methodCalls = new HashMap<>();
    private Map<String, Integer> objectInstantiations = new HashMap<>();
    private Map<String, Integer> packageAccesses = new HashMap<>();

    private int numConditionals;
    private int numLoops;
    private int numNestedLoops;
    private int numMethodCalls;

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

    public void setNumConditionals(int numConditionals) {
        this.numConditionals = numConditionals;
    }

    public void setNumLoops(int numLoops) {
        this.numLoops = numLoops;
    }

    public void setNumNestedLoops(int numNestedLoops) {
        this.numNestedLoops = numNestedLoops;
    }

    public void setNumMethodCalls(int numMethodCalls) {
        this.numMethodCalls = numMethodCalls;
    }
}