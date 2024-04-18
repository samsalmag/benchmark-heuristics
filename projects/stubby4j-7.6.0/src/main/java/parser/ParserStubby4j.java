package parser;

import io.github.samsalmag.benchmarkheuristics.parser.BenchmarkParser;
import io.github.samsalmag.benchmarkheuristics.parser.Parser;

import java.util.Arrays;

public class ParserStubby4j {

    public static void main(String[] args) {

        Parser parser = new Parser(1000,
            "projects\\stubby4j-7.6.0\\src\\main\\java\\",
            "projects\\stubby4j-7.6.0\\src\\test\\java\\",
                Arrays.asList("stubby4j"),
                null);

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\stubby4j-7.6.0\\src\\test\\java\\",
                                                            "benchmarks\\results\\stubby4j_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
        benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
        benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
    }
}
