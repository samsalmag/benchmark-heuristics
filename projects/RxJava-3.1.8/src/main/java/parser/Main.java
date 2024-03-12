package parser;

public class Main {

    public static void main(String[] args) {
        String filePath1 = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\schedulers\\InstantPeriodicTaskTest.java";
        String methodName1 = "dispose3";
        MethodParser parser1 = new MethodParser(filePath1, methodName1);
        parser1.run();

        String filePath2 = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\disposables\\DisposableTest.java";
        String methodName2 = "fromAction";
        MethodParser parser2 = new MethodParser(filePath2, methodName2);
        parser2.run();

        String filePath3 = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\core\\Retry.java";
        String methodName3 = "evaluate";
        MethodParser parser3 = new MethodParser(filePath3, methodName3);
        parser3.run();

        JsonCreator jsonCreator = new JsonCreator("E:\\Chalmers\\DATX05-MastersThesis\\test.json");
        jsonCreator.add(parser1.toParsedMethod(),
                        parser2.toParsedMethod(),
                        parser3.toParsedMethod());
        jsonCreator.createJson();
    }

    /**
     * Nicely prints the parsing of the provided MethodParser.
     *
     * @param parser The parser to print.
     */
    public static void print(MethodParser parser) {
        System.out.println("\nMETHOD CALLS");
        parser.getMethodCalls().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nOBJECT INSTANTIATIONS");
        parser.getObjectInstantiations().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nPACKAGE ACCESSES");
        parser.getPackageAccesses().forEach((k ,v) -> System.out.println(k + ": " + v));

        System.out.println("\nSTATS");
        System.out.println("Conditionals: " + parser.getNumConditionals());
        System.out.println("Loops: " + parser.getNumLoops());
        System.out.println("Nested loops: " + parser.getNumNestedLoops());
        System.out.println("Method calls: " + parser.getNumMethodCalls());
    }
}