package parser;

import com.samsalek.benchmarkheuristics.parser.BenchmarkParser;
import com.samsalek.benchmarkheuristics.parser.MethodParser;

public class ParserMockito {

    public static void main(String[] args) {

        MethodParser parser = new MethodParser(
            "projects\\mockito-5.10.0\\src\\main\\java\\",
            "projects\\mockito-5.10.0\\src\\main\\java\\",
            "mockito");

        BenchmarkParser parseAllBenchmarks = new BenchmarkParser("projects\\mockito-5.10.0\\src\\test\\java\\",
                                                            "benchmarks\\results\\mockito_RMAD.json");

        // Mockito has less than 500 benchmarks
        parseAllBenchmarks.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\mockito_parsedBenchmarks1.json");
        parseAllBenchmarks.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\mockito_parsedBenchmarks2.json");
    }
}
