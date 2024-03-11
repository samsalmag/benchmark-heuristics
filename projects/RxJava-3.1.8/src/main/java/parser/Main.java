package parser;

public class Main {

    public static void main(String[] args) {
        String filePath = "E:\\Chalmers\\DATX05-MastersThesis\\benchmark-heuristics\\projects\\RxJava-3.1.8\\src\\test\\java\\io\\reactivex\\rxjava3\\internal\\schedulers\\InstantPeriodicTaskTest.java";
        String methodName = "dispose3";

        MethodParser parser = new MethodParser(filePath, methodName);
        parser.run();
        System.out.println(parser.getMethodCalls());
        System.out.println(parser.getObjectInstantiations());
        System.out.println(parser.getPackageAccesses());
    }
}