package parser;

import io.github.samsalmag.benchmarkheuristics.parser.BenchmarkParser;
import io.github.samsalmag.benchmarkheuristics.parser.Parser;

import java.util.Arrays;

public class ParserRxJava {

    public static void main(String[] args) {

        Parser parser = new Parser(1000,
                "projects\\RxJava-3.1.8\\src\\main\\java\\",
                "projects\\RxJava-3.1.8\\src\\test\\java\\",
                Arrays.asList("rxjava"),
                "projects\\RxJava-3.1.8\\dependencies\\");

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\RxJava-3.1.8\\src\\test\\java\\",
                                                            "benchmarks\\results\\run2\\rxjava-random_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\rxjava-random_parsedBenchmarks1.json");
        benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\rxjava-random_parsedBenchmarks2.json");
        benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\rxjava-random_parsedBenchmarks3.json");
    }
}
