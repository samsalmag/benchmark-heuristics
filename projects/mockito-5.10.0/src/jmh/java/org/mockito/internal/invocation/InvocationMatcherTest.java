/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.invocation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.internal.matchers.Any.ANY;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.NotNull;
import org.mockito.invocation.Invocation;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

@SuppressWarnings("unchecked")
public class InvocationMatcherTest extends TestBase {

    private InvocationMatcher simpleMethod;

    @Mock
    private IMethods mock;

    @Before
    public void setup() {
        simpleMethod = new InvocationBuilder().mock(mock).simpleMethod().toInvocationMatcher();
    }

    @Test
    public void should_be_a_citizen_of_hashes() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        Invocation invocationTwo = new InvocationBuilder().args("blah").toInvocation();
        Map<InvocationMatcher, String> map = new HashMap<InvocationMatcher, String>();
        map.put(new InvocationMatcher(invocation), "one");
        map.put(new InvocationMatcher(invocationTwo), "two");
        assertEquals(2, map.size());
    }

    @Test
    public void should_not_equal_if_number_of_arguments_differ() throws Exception {
        InvocationMatcher withOneArg = new InvocationMatcher(new InvocationBuilder().args("test").toInvocation());
        InvocationMatcher withTwoArgs = new InvocationMatcher(new InvocationBuilder().args("test", 100).toInvocation());
        assertFalse(withOneArg.equals(null));
        assertFalse(withOneArg.equals(withTwoArgs));
    }

    @Test
    public void should_to_string_with_matchers() throws Exception {
        ArgumentMatcher m = NotNull.NOT_NULL;
        InvocationMatcher notNull = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(m));
        ArgumentMatcher mTwo = new Equals('x');
        InvocationMatcher equals = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(mTwo));
        assertThat(notNull.toString()).contains("simpleMethod(notNull())");
        assertThat(equals.toString()).contains("simpleMethod('x')");
    }

    @Test
    public void should_know_if_is_similar_to() throws Exception {
        Invocation same = new InvocationBuilder().mock(mock).simpleMethod().toInvocation();
        assertTrue(simpleMethod.hasSimilarMethod(same));
        Invocation different = new InvocationBuilder().mock(mock).differentMethod().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(different));
    }

    @Test
    public void should_not_be_similar_to_verified_invocation() throws Exception {
        Invocation verified = new InvocationBuilder().simpleMethod().verified().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(verified));
    }

    @Test
    public void should_not_be_similar_if_mocks_are_different() throws Exception {
        Invocation onDifferentMock = new InvocationBuilder().simpleMethod().mock("different mock").toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(onDifferentMock));
    }

    @Test
    public void should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);
        String sameArg = "test";
        InvocationMatcher invocation = new InvocationBuilder().method(method).arg(sameArg).toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().method(overloadedMethod).arg(sameArg).toInvocation();
        assertFalse(invocation.hasSimilarMethod(overloadedInvocation));
    }

    @Test
    public void should_be_similar_if_is_overloaded_but_used_with_different_arg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);
        InvocationMatcher invocation = new InvocationBuilder().mock(mock).method(method).arg("foo").toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().mock(mock).method(overloadedMethod).arg("bar").toInvocation();
        assertTrue(invocation.hasSimilarMethod(overloadedInvocation));
    }

    @Test
    public void should_capture_arguments_from_invocation() throws Exception {
        // given
        Invocation invocation = new InvocationBuilder().args("1", 100).toInvocation();
        CapturingMatcher capturingMatcher = new CapturingMatcher(List.class);
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals("1"), capturingMatcher));
        // when
        invocationMatcher.captureArgumentsFrom(invocation);
        // then
        assertEquals(1, capturingMatcher.getAllValues().size());
        assertEquals(100, capturingMatcher.getLastValue());
    }

    @Test
    public void should_match_varargs_using_any_varargs() {
        // given
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, asList(ANY, ANY));
        // when
        boolean match = invocationMatcher.matches(invocation);
        // then
        assertTrue(match);
    }

    @Test
    public void should_capture_varargs_as_vararg() {
        // given
        mock.mixedVarargs(1, "a", "b");
        Invocation invocation = getLastInvocation();
        CapturingMatcher<String[]> m = new CapturingMatcher(String[].class);
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Arrays.<ArgumentMatcher>asList(new Equals(1), m));
        // when
        invocationMatcher.captureArgumentsFrom(invocation);
        // then
        Assertions.assertThat(m.getAllValues()).containsExactly(new String[] { "a", "b" });
    }

    // like using several time the captor in the vararg
    @Test
    public void should_capture_arguments_when_args_count_does_NOT_match() {
        // given
        mock.varargs();
        Invocation invocation = getLastInvocation();
        // when
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(ANY));
        // then
        invocationMatcher.captureArgumentsFrom(invocation);
    }

    @Test
    public void should_create_from_invocations() throws Exception {
        // given
        Invocation i = new InvocationBuilder().toInvocation();
        // when
        List<InvocationMatcher> out = InvocationMatcher.createFrom(asList(i));
        // then
        assertEquals(1, out.size());
        assertEquals(i, out.get(0).getInvocation());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_be_a_citizen_of_hashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_be_a_citizen_of_hashes, this.description("should_be_a_citizen_of_hashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_equal_if_number_of_arguments_differ() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_equal_if_number_of_arguments_differ, this.description("should_not_equal_if_number_of_arguments_differ"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_to_string_with_matchers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_to_string_with_matchers, this.description("should_to_string_with_matchers"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_know_if_is_similar_to() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_know_if_is_similar_to, this.description("should_know_if_is_similar_to"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_be_similar_to_verified_invocation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_be_similar_to_verified_invocation, this.description("should_not_be_similar_to_verified_invocation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_be_similar_if_mocks_are_different() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_be_similar_if_mocks_are_different, this.description("should_not_be_similar_if_mocks_are_different"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg, this.description("should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_be_similar_if_is_overloaded_but_used_with_different_arg() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_be_similar_if_is_overloaded_but_used_with_different_arg, this.description("should_be_similar_if_is_overloaded_but_used_with_different_arg"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_arguments_from_invocation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_arguments_from_invocation, this.description("should_capture_arguments_from_invocation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_match_varargs_using_any_varargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_match_varargs_using_any_varargs, this.description("should_match_varargs_using_any_varargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_varargs_as_vararg() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_varargs_as_vararg, this.description("should_capture_varargs_as_vararg"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_arguments_when_args_count_does_NOT_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_arguments_when_args_count_does_NOT_match, this.description("should_capture_arguments_when_args_count_does_NOT_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_create_from_invocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_create_from_invocations, this.description("should_create_from_invocations"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setup();
        }

        private InvocationMatcherTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new InvocationMatcherTest();
        }

        @java.lang.Override
        public InvocationMatcherTest implementation() {
            return this.implementation;
        }
    }
}
