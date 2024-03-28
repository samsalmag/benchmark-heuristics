package parser;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
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

    private final String baseTestPath;
    private final List<SimpleEntry<String, Double>> benchmarkMap;

    public ParseAllBenchmarks(String baseTestPath, String benchmarkJsonPath) {
        this.baseTestPath = baseTestPath;
        this.benchmarkMap = readJson(benchmarkJsonPath);
    }

    public void setupShutdownHook(JsonCreator jsonCreator) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                jsonCreator.createJson();
                System.out.println("Shutdown hook ran, created json file");
            }));
    }

    public void parseBenchmarks(String outputPath) {
        parseBenchmarks(0, benchmarkMap.size() - 1, outputPath);
    }

    public void parseBenchmarks(int firstBenchmarkIndex, int lastBenchmarkIndex, String outputPath) {
        // Check so indexes are in range, correct them if they are not
        if (firstBenchmarkIndex < 0) firstBenchmarkIndex = 0;
        if (lastBenchmarkIndex > benchmarkMap.size() - 1) lastBenchmarkIndex = benchmarkMap.size() - 1;


        JsonCreator jsonCreator = new JsonCreator(outputPath);
        setupShutdownHook(jsonCreator);
        int successfulIndex = 0;
        int iterationIndex = 0;

        try {
            for (int i = firstBenchmarkIndex; i <= lastBenchmarkIndex; i++) {
                iterationIndex += 1;

                String benchmark = benchmarkMap.get(i).getKey();
                int last_Index = benchmark.lastIndexOf('_');
                String method = benchmark.substring(last_Index + 1);

                int secondLastDotIndex = benchmark.lastIndexOf('.', benchmark.lastIndexOf('.') - 1);
                // Get the class path, each directory is currently separated by dots so have to use paths library to resolve path
                String classPathDots = benchmark.substring(0, secondLastDotIndex);
                String[] parts = classPathDots.split("\\.");
                Path classPath = Paths.get("", parts);

                Path base = Paths.get(baseTestPath);
                Path resolvedPath = base.resolve(classPath); // concatenate base path with class path
                Path benchmarkPath = Paths.get(resolvedPath + ".java"); // add .java to file extension
                System.out.println(benchmarkPath);
                System.out.println(method);
                MethodParser parser = new MethodParser(Integer.MAX_VALUE,
                        "projects\\RxJava-3.1.8\\src\\main\\java\\",
                        "projects\\RxJava-3.1.8\\src\\main\\java\\",
                        "rxjava");

                ParsedMethod parsed = parser.parse(benchmarkPath.toString(), method);
                System.out.println("INDEX, RUN METHODS: " + (i + 1) + "/" + benchmarkMap.size());
                if (MethodParser.ambigousList.contains(benchmarkPath.toString()) ||
                        MethodParser.otherExceptionList.contains(benchmarkPath.toString()) ||
                        MethodParser.unsupportedOperationList.contains(benchmarkPath.toString()) ||
                        MethodParser.concurrentModificationList.contains(benchmarkPath.toString()) ||
                        MethodParser.noSuchElementList.contains(benchmarkPath.toString())) {
                    System.out.println("INDEX, SUCCESSFUL RAN METHODS: " + successfulIndex + "/" + iterationIndex);
                    continue;
                }
                Double RMAD = benchmarkMap.get(i).getValue();
                parsed.setRMAD(RMAD);
                jsonCreator.add(parsed);
                successfulIndex += 1;
                System.out.println("INDEX, SUCCESSFUL RAN METHODS: " + successfulIndex + "/" + iterationIndex + "\n");
            }
        }
        finally {
            System.out.println("Ambigous list, size: " + MethodParser.ambigousList.size() + " list:" + MethodParser.ambigousList);
            System.out.println("noSuchElementList list, size: " + MethodParser.noSuchElementList.size() + " list:" + MethodParser.noSuchElementList);
            System.out.println("unsupportedOperationList list, size: " + MethodParser.unsupportedOperationList.size() + " list:" + MethodParser.unsupportedOperationList);
            System.out.println("concurrentModificationList list, size: " + MethodParser.concurrentModificationList.size() + " list:" + MethodParser.concurrentModificationList);
            System.out.println("Other list, size: " + MethodParser.otherExceptionList.size() + " list:" + MethodParser.otherExceptionList);

            // Create json file even if there's an exception
            jsonCreator.createJson();
        }
    }

    /**
     * Reads the given JSON file and converts it into a list of SimpleEntry<String, Double> objects,
     * which is essentially a list of pairs.
     *
     * @return List of SimpleEntry<String, Double> objects.
     */
    private List<SimpleEntry<String, Double>> readJson(String jsonPath) {
        Gson gson = new Gson();
        List<SimpleEntry<String, Double>> results = new ArrayList<>();

        Type listType = new TypeToken<List<List<Object>>>(){}.getType();

        try (FileReader reader = new FileReader(new File(jsonPath).getAbsoluteFile())) {
            List<List<Object>> rawData = gson.fromJson(reader, listType);

            for (List<Object> entry : rawData) {
                String methodName = (String) entry.get(0);
                double value = ((Number) entry.get(1)).doubleValue();
                results.add(new SimpleEntry<>(methodName, value));
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /*
    // REMOVE THIS LATER, TAKEN FROM Main.java
    public static void print(ParsedMethod parsedMethod) {
        System.out.println("\nMETHOD CALLS");
        parsedMethod.getMethodCalls().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nOBJECT INSTANTIATIONS");
        parsedMethod.getObjectInstantiations().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nPACKAGE ACCESSES");
        parsedMethod.getPackageAccesses().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nSTATS");
        System.out.println("Conditionals: " + parsedMethod.getNumConditionals());
        System.out.println("Loops: " + parsedMethod.getNumLoops());
        System.out.println("Nested loops: " + parsedMethod.getNumNestedLoops());
        System.out.println("Method calls: " + parsedMethod.getNumMethodCalls());
        System.out.println("Recursive method calls: " + parsedMethod.getRecursiveMethodCalls());
        System.out.println("Lines of code: " + parsedMethod.getLinesOfCode());
        System.out.println("Logical lines of code: " + parsedMethod.getLogicalLinesOfCode());
        System.out.println("Lines of code (junit test): " + parsedMethod.getLinesOfCodeJunitTest());
        System.out.println("Logical lines of code (junit test): " + parsedMethod.getLogicalLinesOfCodeJunitTest());
        System.out.println("RMAD: " + parsedMethod.getRMAD());
    }
    */
}
