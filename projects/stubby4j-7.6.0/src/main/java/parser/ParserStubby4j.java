package parser;

import com.samsalek.benchmarkheuristics.parser.BenchmarkParser;
import com.samsalek.benchmarkheuristics.parser.MethodParser;

public class ParserStubby4j {

    public static void main(String[] args) {

        MethodParser parser = new MethodParser(
            "projects\\stubby4j-7.6.0\\src\\main\\java\\",
            "projects\\stubby4j-7.6.0\\src\\main\\java\\",
            "stubby4j");

        BenchmarkParser parseAllBenchmarks = new BenchmarkParser("projects\\stubby4j-7.6.0\\src\\test\\java\\",
                                                            "benchmarks\\results\\stubby4j_RMAD.json");

        // Stubby4j has less than 100 benchmarks
        parseAllBenchmarks.parseBenchmarks(parser, 0, 99, "benchmarks\\results\\stubby4j_parsedBenchmarks1.json");
    }
}
