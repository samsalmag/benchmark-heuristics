/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.junitrule;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockitoutil.TestBase.filterLineNo;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.UnnecessaryStubbingException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.quality.Strictness;
import org.mockitousage.IMethods;
import org.mockitousage.strictness.ProductionCode;
import org.mockitoutil.SafeJUnitRule;

public class StrictJUnitRuleTest {

    @Rule
    public SafeJUnitRule rule = new SafeJUnitRule(MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS));

    @Mock
    IMethods mock;

    @Mock
    IMethods mock2;

    @Test
    public void ok_when_no_stubbings() throws Throwable {
        mock.simpleMethod();
        verify(mock).simpleMethod();
    }

    @Test
    public void ok_when_all_stubbings_used() throws Throwable {
        given(mock.simpleMethod(10)).willReturn("foo");
        mock.simpleMethod(10);
    }

    @Test
    public void ok_when_used_and_mismatched_argument() throws Throwable {
        given(mock.simpleMethod(10)).willReturn("foo");
        mock.simpleMethod(10);
        mock.simpleMethod(15);
    }

    @Test
    public void fails_when_unused_stubbings() throws Throwable {
        // expect
        rule.expectFailure(UnnecessaryStubbingException.class);
        // when
        given(mock.simpleMethod(10)).willReturn("foo");
        mock2.simpleMethod(10);
    }

    @Test
    public void test_failure_trumps_unused_stubbings() throws Throwable {
        // expect
        rule.expectFailure(AssertionError.class, "x");
        // when
        given(mock.simpleMethod(10)).willReturn("foo");
        mock.otherMethod();
        throw new AssertionError("x");
    }

    @Test
    public void why_do_return_syntax_is_useful() throws Throwable {
        // Trade-off of Mockito strictness documented in test
        // expect
        rule.expectFailure(PotentialStubbingProblem.class);
        // when
        when(mock.simpleMethod(10)).thenReturn("10");
        ProductionCode.simpleMethod(mock, 20);
    }

    @Test
    public void fails_fast_when_stubbing_invoked_with_different_argument() throws Throwable {
        // expect
        rule.expectFailure(new SafeJUnitRule.FailureAssert() {

            public void doAssert(Throwable t) {
                Assertions.assertThat(t).isInstanceOf(PotentialStubbingProblem.class);
                assertEquals(filterLineNo("\n" + "Strict stubbing argument mismatch. Please check:\n" + " - this invocation of 'simpleMethod' method:\n" + "    mock.simpleMethod(15);\n" + "    -> at org.mockitousage.strictness.ProductionCode.simpleMethod(ProductionCode.java:0)\n" + " - has following stubbing(s) with different arguments:\n" + "    1. mock.simpleMethod(20);\n" + "      -> at org.mockitousage.junitrule.StrictJUnitRuleTest.fails_fast_when_stubbing_invoked_with_different_argument(StrictJUnitRuleTest.java:0)\n" + "    2. mock.simpleMethod(30);\n" + "      -> at org.mockitousage.junitrule.StrictJUnitRuleTest.fails_fast_when_stubbing_invoked_with_different_argument(StrictJUnitRuleTest.java:0)\n" + "Typically, stubbing argument mismatch indicates user mistake when writing tests.\n" + "Mockito fails early so that you can debug potential problem easily.\n" + "However, there are legit scenarios when this exception generates false negative signal:\n" + "  - stubbing the same method multiple times using 'given().will()' or 'when().then()' API\n" + "    Please use 'will().given()' or 'doReturn().when()' API for stubbing.\n" + "  - stubbed method is intentionally invoked with different arguments by code under test\n" + "    Please use default or 'silent' JUnit Rule (equivalent of Strictness.LENIENT).\n" + "For more information see javadoc for PotentialStubbingProblem class."), filterLineNo(t.getMessage()));
            }
        });
        // when stubbings in the test code:
        // used
        willReturn("10").given(mock).simpleMethod(10);
        // unused
        willReturn("20").given(mock).simpleMethod(20);
        // unused
        willReturn("30").given(mock).simpleMethod(30);
        // then
        // ok, different method
        mock.otherMethod();
        // ok, stubbed with this argument
        mock.simpleMethod(10);
        // invocation in the code under test uses different argument and should fail immediately
        // this helps with debugging and is essential for Mockito strictness
        ProductionCode.simpleMethod(mock, 15);
    }

    @Test
    public void verify_no_more_interactions_ignores_stubs() throws Throwable {
        // when stubbing in test:
        given(mock.simpleMethod(10)).willReturn("foo");
        // and code under test does:
        // implicitly verifies the stubbing
        mock.simpleMethod(10);
        mock.otherMethod();
        // and in test we:
        verify(mock).otherMethod();
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void unused_stubs_with_multiple_mocks() throws Throwable {
        // expect
        rule.expectFailure(new SafeJUnitRule.FailureAssert() {

            public void doAssert(Throwable t) {
                assertEquals(filterLineNo("\n" + "Unnecessary stubbings detected.\n" + "Clean & maintainable test code requires zero unnecessary code.\n" + "Following stubbings are unnecessary (click to navigate to relevant line of code):\n" + "  1. -> at org.mockitousage.junitrule.StrictJUnitRuleTest.unused_stubs_with_multiple_mocks(StrictJUnitRuleTest.java:0)\n" + "  2. -> at org.mockitousage.junitrule.StrictJUnitRuleTest.unused_stubs_with_multiple_mocks(StrictJUnitRuleTest.java:0)\n" + "Please remove unnecessary stubbings or use 'lenient' strictness. More info: javadoc for UnnecessaryStubbingException class."), filterLineNo(t.getMessage()));
            }
        });
        // when test has
        given(mock.simpleMethod(10)).willReturn("foo");
        given(mock2.simpleMethod(20)).willReturn("foo");
        // used and should not be reported
        given(mock.otherMethod()).willReturn("foo");
        // and code has
        mock.otherMethod();
        mock2.booleanObjectReturningMethod();
    }

    @SuppressWarnings({ "MockitoUsage", "CheckReturnValue" })
    @Test
    public void rule_validates_mockito_usage() throws Throwable {
        // expect
        rule.expectFailure(UnfinishedVerificationException.class);
        // when test contains unfinished verification
        verify(mock);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ok_when_no_stubbings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ok_when_no_stubbings, this.description("ok_when_no_stubbings"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ok_when_all_stubbings_used() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ok_when_all_stubbings_used, this.description("ok_when_all_stubbings_used"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ok_when_used_and_mismatched_argument() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ok_when_used_and_mismatched_argument, this.description("ok_when_used_and_mismatched_argument"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fails_when_unused_stubbings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fails_when_unused_stubbings, this.description("fails_when_unused_stubbings"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_failure_trumps_unused_stubbings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_failure_trumps_unused_stubbings, this.description("test_failure_trumps_unused_stubbings"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_why_do_return_syntax_is_useful() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::why_do_return_syntax_is_useful, this.description("why_do_return_syntax_is_useful"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fails_fast_when_stubbing_invoked_with_different_argument() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fails_fast_when_stubbing_invoked_with_different_argument, this.description("fails_fast_when_stubbing_invoked_with_different_argument"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verify_no_more_interactions_ignores_stubs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::verify_no_more_interactions_ignores_stubs, this.description("verify_no_more_interactions_ignores_stubs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unused_stubs_with_multiple_mocks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unused_stubs_with_multiple_mocks, this.description("unused_stubs_with_multiple_mocks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rule_validates_mockito_usage() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rule_validates_mockito_usage, this.description("rule_validates_mockito_usage"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().rule, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private StrictJUnitRuleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StrictJUnitRuleTest();
        }

        @java.lang.Override
        public StrictJUnitRuleTest implementation() {
            return this.implementation;
        }
    }
}
