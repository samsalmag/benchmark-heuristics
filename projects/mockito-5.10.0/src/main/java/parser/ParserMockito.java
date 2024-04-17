package parser;

import io.github.samsalmag.benchmarkheuristics.parser.BenchmarkParser;
import io.github.samsalmag.benchmarkheuristics.parser.Parser;

public class ParserMockito {

    public static void main(String[] args) {

        Parser parser = new Parser(
            "projects\\mockito-5.10.0\\src\\main\\java\\",
            "projects\\mockito-5.10.0\\src\\main\\java\\",
            "mockito");

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\mockito-5.10.0\\src\\test\\java\\",
                                                            "benchmarks\\results\\run2\\mockito-random_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\mockito_parsedBenchmarks1_run2.json");
        benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\mockito_parsedBenchmarks2_run2.json");
        benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\mockito_parsedBenchmarks2_run2.json");
    }
}
