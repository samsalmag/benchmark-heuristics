import com.samsalek.benchmarkheuristics.parser.BenchmarkParser;
import com.samsalek.benchmarkheuristics.parser.MethodParser;

public class Parser {

    public static void main(String[] args) {

        MethodParser parser = new MethodParser(Integer.MAX_VALUE,
                "projects\\RxJava-3.1.8\\src\\main\\java\\",
                "projects\\RxJava-3.1.8\\src\\main\\java\\",
                "rxjava");

        boolean parseBenchmarks = true;

        if (parseBenchmarks) {
            BenchmarkParser parseAllBenchmarks = new BenchmarkParser("projects\\RxJava-3.1.8\\src\\test\\java\\",
                    "benchmarks\\results\\rxjava_RMAD.json");

            // parseAllBenchmarks.parseBenchmarks(parser, 0, 260, "benchmarks\\results\\parsedBenchmarks_test1.json");
            // parseAllBenchmarks.parseBenchmarks(parser, 261, 520, "benchmarks\\results\\parsedBenchmarks_test2.json");
            // parseAllBenchmarks.parseBenchmarks(parser, 521, 780, "benchmarks\\results\\parsedBenchmarks_test3.json");

            parseAllBenchmarks.parseBenchmarks(parser, 70, 100, "benchmarks\\results\\parsedBenchmarks_test1.json");
        }
        else {

            /*
            // A lot of clas not found errors
            String filePath1 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\operators\\completable\\CompletableTimeoutTest.java";
            String methodName1 = "timeoutException";
            parser.parse(filePath1, methodName1);
            */

            String filePath1 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\operators\\maybe\\MaybeConcatMapSingleTest.java";
            String methodName1 = "flatMapSingleElementError";
            parser.parse(filePath1, methodName1).print();

            /*
            String filePath2 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\operators\\flowable\\FlowableOnBackpressureReduceWithTest.java";
            String methodName2 = "exceptionFromSupplier";
            parser.parse(filePath2, methodName2);

            print(parser.parse(filePath2, methodName2));

            String filePath3 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\observable\\ObservableFuseableTest.java";
            String methodName3 = "syncRange";
            parser.parse(filePath3, methodName3);

            print(parser.parse(filePath3, methodName3));

             */
        }

         /*
            String filePath1 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\schedulers\\InstantPeriodicTaskTest.java";
            String methodName1 = "dispose3";
            parser.parse(filePath1, methodName1);

            print(parser.parse(filePath1, methodName1));
             */

        /*
        String filePath2 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\disposables\\DisposableTest.java";
        String methodName2 = "fromAction";
        parser.parse(filePath2, methodName2);

        String filePath3 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\core\\Retry.java";
        String methodName3 = "evaluate";
        parser.parse(filePath3, methodName3);

        /* Unsolved exception
        String filePath3 = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\jdk8\\ParallelMapTryOptionalTest.java";
        String methodName3 = "mapFailWithSkip";
        MethodParser parser3 = new MethodParser(filePath3, methodName3);
        parser3.run();


        JsonCreator jsonCreator = new JsonCreator("E:\\Chalmers\\DATX05-MastersThesis\\test.json");
        //jsonCreator.add(parser1.toParsedMethod(),
        //                parser2.toParsedMethod(),
        //                parser3.toParsedMethod());
        //jsonCreator.createJson();


        print(parser.parse(filePath1, methodName1));
        */
    }
}
