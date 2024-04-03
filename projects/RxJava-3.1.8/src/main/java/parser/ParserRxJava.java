package parser;

import com.samsalek.benchmarkheuristics.parser.BenchmarkParser;
import com.samsalek.benchmarkheuristics.parser.MethodParser;

public class ParserRxJava {

    public static void main(String[] args) {

        MethodParser parser = new MethodParser(
                "projects\\RxJava-3.1.8\\src\\main\\java\\",
                "projects\\RxJava-3.1.8\\src\\main\\java\\",
                "rxjava");

        BenchmarkParser parseAllBenchmarks = new BenchmarkParser("projects\\RxJava-3.1.8\\src\\test\\java\\",
                                                            "benchmarks\\results\\rxjava_RMAD.json");

        // RxJava has less than 1000 benchmarks
        parseAllBenchmarks.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\rxjava_parsedBenchmarks1.json");
        parseAllBenchmarks.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\rxjava_parsedBenchmarks2.json");
        parseAllBenchmarks.parseBenchmarks(parser, 500, 1000, "benchmarks\\results\\rxjava_parsedBenchmarks3.json");
    }
}
