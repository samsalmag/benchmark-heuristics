package parser;

import io.github.samsalmag.benchmarkheuristics.parser.BenchmarkParser;
import io.github.samsalmag.benchmarkheuristics.parser.Parser;

public class ParserStubby4j {

    public static void main(String[] args) {

        Parser parser = new Parser(
            "projects\\stubby4j-7.6.0\\src\\main\\java\\",
            "projects\\stubby4j-7.6.0\\src\\main\\java\\",
            "stubby4j");

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\stubby4j-7.6.0\\src\\test\\java\\",
                                                            "benchmarks\\results\\stubby4j_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
        benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
        benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
    }
}
