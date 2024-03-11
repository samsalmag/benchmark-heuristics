package parser;

public class Main {

    public static void main(String[] args) {
        String filePath = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\schedulers\\InstantPeriodicTaskTest.java";
        String methodName = "dispose3";

        // String filePath = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\disposables\\DisposableTest.java";
        // String methodName = "fromAction";

        // String filePath = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\core\\Retry.java";
        // String methodName = "evaluate";

        MethodParser parser = new MethodParser(filePath, methodName);
        parser.run();
        print(parser);
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
        System.out.println("Conditionals: " + parser.getConditionals());
        System.out.println("Loops: " + parser.getLoops());
        System.out.println("Nested loops: " + parser.getNestedLoops());
        System.out.println("Method calls: " + parser.getMethodCallsAmount());
    }
}