package parser;

public class Main {

    public static void main(String[] args) {
        ParseAllBenchmarks parseAllBenchmarks = new ParseAllBenchmarks("C:\\Users\\super\\IntelliJ-projects\\MASTER\\benchmark-heuristics\\benchmarks\\results\\rxjava_RMAD.json");
        parseAllBenchmarks.parseBenchmarks();

        MethodParser parser = new MethodParser(Integer.MAX_VALUE,
                                    "projects\\RxJava-3.1.8\\src\\main\\java\\",
                                     "projects\\RxJava-3.1.8\\src\\main\\java\\",
                                      "rxjava");

        String filePath1 = "projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\schedulers\\InstantPeriodicTaskTest.java";
        String methodName1 = "dispose3";
        parser.parse(filePath1, methodName1);

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
        */

        JsonCreator jsonCreator = new JsonCreator("E:\\Chalmers\\DATX05-MastersThesis\\test.json");
        //jsonCreator.add(parser1.toParsedMethod(),
        //                parser2.toParsedMethod(),
        //                parser3.toParsedMethod());
        //jsonCreator.createJson();


        print(parser.parse(filePath1, methodName1));
    }

    /**
     * Nicely prints the parsing of the provided ParsedMethod.
     *
     * @param parsedMethod The parsedMethod to print.
     */
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
        System.out.println("Lines of code: " + parsedMethod.getLinesOfCode());
    }
}