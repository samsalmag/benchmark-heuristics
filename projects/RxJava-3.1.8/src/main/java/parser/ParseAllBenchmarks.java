package parser;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jdk.internal.org.jline.utils.OSUtils;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that uses method and package name of the benchmarks in a benchmarkDict json file to
 * parse them (extracts stats, e.g. nested loops) and generates a new json file containing the results.
 * The structure of the read json file is a list of Pair<String, Double>, where String is the full
 * benchmark name, and Double is the RMAD value for the benchmark.
 *
 * @author Malte Åkvist
 */
public class ParseAllBenchmarks {

    private final String BASE_TEST_PATH = "C:\\Users\\super\\IntelliJ-projects\\MASTER\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\";
    private List<SimpleEntry<String, Double>> benchmarkMap;
    private String benchmarkJsonPath;

    public ParseAllBenchmarks(String benchmarkJsonPath) {
        this.benchmarkJsonPath = benchmarkJsonPath;
        this.benchmarkMap = readJson();
    }

    public void parseBenchmarks() {
        for (int i = 0; i < benchmarkMap.size(); i++) {
            if (i >= 0) {

                String benchmark = benchmarkMap.get(i).getKey();
                int last_Index = benchmark.lastIndexOf('_');
                String method = benchmark.substring(last_Index + 1);

                int secondLastDotIndex = benchmark.lastIndexOf('.', benchmark.lastIndexOf('.') - 1);
                // Get the class path, each directory is currently separated by dots so have to use paths library to resolve path
                String classPathDots = benchmark.substring(0, secondLastDotIndex);
                String[] parts = classPathDots.split("\\.");
                Path classPath = Paths.get("", parts);

                Path base = Paths.get(BASE_TEST_PATH);
                Path resolvedPath = base.resolve(classPath); // concatenate base path with class path
                Path benchmarkPath = Paths.get(resolvedPath + ".java"); // add .java to file extension
                System.out.println(benchmarkPath);
                System.out.println(method);
                System.out.println("INDEX, RUN METHODS: " + i);
                MethodParser parser1 = new MethodParser(benchmarkPath.toString(), method);
                parser1.run();
                // print(parser1);

                System.out.println("Ambigous list, size: " + MethodParser.ambigousList.size() + " list:" + MethodParser.ambigousList);
                System.out.println("noSuchElementList list, size: " + MethodParser.noSuchElementList.size() + " list:" + MethodParser.noSuchElementList);
                System.out.println("unsupportedOperationList list, size: " + MethodParser.unsupportedOperationList.size() + " list:" + MethodParser.unsupportedOperationList);
                System.out.println("concurrentModificationList list, size: " + MethodParser.concurrentModificationList.size() + " list:" + MethodParser.concurrentModificationList);
            }
        }
    }

    /**
     * REMOVE THIS LATER, TAKEN FROM Main.java
     */
    public static void print(MethodParser parser) {
        System.out.println("\nMETHOD CALLS");
        parser.getMethodCalls().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nOBJECT INSTANTIATIONS");
        parser.getObjectInstantiations().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nPACKAGE ACCESSES");
        parser.getPackageAccesses().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nSTATS");
        System.out.println("Conditionals: " + parser.getNumConditionals());
        System.out.println("Loops: " + parser.getNumLoops());
        System.out.println("Nested loops: " + parser.getNumNestedLoops());
        System.out.println("Method calls: " + parser.getNumMethodCalls());
    }

    /**
     * Reads the given JSON file and converts it into a list of SimpleEntry<String, Double> objects,
     * which is essentially a list of pairs.
     */
    private List<SimpleEntry<String, Double>> readJson() {
        Gson gson = new Gson();
        List<SimpleEntry<String, Double>> results = new ArrayList<>();

        Type listType = new TypeToken<List<List<Object>>>(){}.getType();

        try (FileReader reader = new FileReader(benchmarkJsonPath)) {
            List<List<Object>> rawData = gson.fromJson(reader, listType);

            for (List<Object> entry : rawData) {
                String methodName = (String) entry.get(0);
                double value = ((Number) entry.get(1)).doubleValue();
                results.add(new SimpleEntry<>(methodName, value));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
