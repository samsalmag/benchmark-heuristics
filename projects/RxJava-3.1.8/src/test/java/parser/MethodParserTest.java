package parser;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class MethodParserTest {

    private static MethodParser PARSER;

    @BeforeClass
    public static void init() {
        PARSER = new MethodParser(Integer.MAX_VALUE, "src\\main\\java\\", "src\\test\\java\\", "rxjava",
                new File("src\\main\\java"),
                new File("src\\test\\java"));
    }

    @Test
    public void numLoopsTest_shouldBeZero() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "empty";
        PARSER.parse(filePath, methodName);

        int expectedValue = 0;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeOne() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "oneLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeTwo_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeTwo_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeThree_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeThree_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeLoop_twoNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeFour_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "fourLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 4;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numLoopsTest_shouldBeFour_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "fourLoop_threeNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 4;
        int actualValue = PARSER.getParsedMethod().getNumLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numNestedLoopsTest_shouldBeOne_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumNestedLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numNestedLoopsTest_shouldBeOne_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumNestedLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numNestedLoopsTest_shouldBeOne_3() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "fourLoop_oneNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumNestedLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numNestedLoopsTest_shouldBeTwo() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeLoop_twoNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumNestedLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numNestedLoopsTest_shouldBeThree() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "fourLoop_threeNestedLoop";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumNestedLoops();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeOne() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "oneMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeTwo() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeThree() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeFour() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "fourMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 4;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeEight() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "eightMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 8;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeNine() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "nineMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 9;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numMethodCallsTest_shouldBeThirty() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "thirtyMethodCall";
        PARSER.parse(filePath, methodName);

        int expectedValue = 30;
        int actualValue = PARSER.getParsedMethod().getNumMethodCalls();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeOne_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "oneIf_1";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeOne_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "oneIf_2";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeOne_3() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "oneSwitchCase";
        PARSER.parse(filePath, methodName);

        int expectedValue = 1;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeTwo_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoIf_1";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeTwo_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoIf_2";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeTwo_3() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoIf_3";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeTwo_4() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "twoSwitchCase";
        PARSER.parse(filePath, methodName);

        int expectedValue = 2;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeThree_1() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeIf_1";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeThree_2() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeIf_2";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void numConditionalsTest_shouldBeThree_3() {
        String filePath = new File("src/test/java/parser/Stub.java").getAbsolutePath();
        String methodName = "threeSwitchCase";
        PARSER.parse(filePath, methodName);

        int expectedValue = 3;
        int actualValue = PARSER.getParsedMethod().getNumConditionals();

        assertEquals(expectedValue, actualValue);
    }
}
