/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.verify;
import static org.mockito.junit.MockitoJUnit.rule;
import static org.mockitoutil.Stopwatch.createNotStarted;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.exceptions.verification.MoreThanAllowedActualInvocations;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.internal.verification.DummyVerificationMode;
import org.mockito.junit.MockitoRule;
import org.mockito.verification.VerificationMode;
import org.mockitousage.IMethods;
import org.mockitoutil.Stopwatch;
import org.mockitoutil.async.AsyncTesting;

public class VerificationWithAfterTest {

    @Rule
    public MockitoRule mockito = rule();

    @Mock
    private IMethods mock;

    private Runnable callMock = new Runnable() {

        public void run() {
            mock.oneArg('1');
        }
    };

    private AsyncTesting async = new AsyncTesting();

    private Stopwatch watch = createNotStarted();

    @After
    public void tearDown() {
        async.cleanUp();
    }

    @Test
    public void should_verify_with_after() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(1000, callMock);
        // then
        verify(mock, after(300)).oneArg('1');
    }

    @Test
    public void should_verify_with_after_and_fail() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(40, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(600)).oneArg('1');
            }
        }).isInstanceOf(TooManyActualInvocations.class);
    }

    @Test
    public void should_verify_with_time_x() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        async.runAfter(600, callMock);
        // then
        verify(mock, after(300).times(2)).oneArg('1');
    }

    @Test
    public void should_verify_with_time_x_and_fail() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(40, callMock);
        async.runAfter(80, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(300).times(2)).oneArg('1');
            }
        }).isInstanceOf(TooManyActualInvocations.class);
    }

    @Test
    public void should_verify_with_at_least() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        // then
        verify(mock, after(300).atLeastOnce()).oneArg('1');
    }

    @Test
    public void should_verify_with_at_least_and_fail() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        async.runAfter(600, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(300).atLeast(3)).oneArg('1');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining(// TODO specific exception
        "Wanted *at least* 3 times");
    }

    @Test
    public void should_verify_with_at_most() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        async.runAfter(600, callMock);
        // then
        verify(mock, after(300).atMost(2)).oneArg('1');
    }

    @Test
    public void should_verify_with_at_most_and_fail() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        async.runAfter(600, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(300).atMost(1)).oneArg('1');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining(// TODO specific exception
        "Wanted at most 1 time but was 2");
    }

    @Test
    public void should_verify_with_never() {
        // given
        async.runAfter(500, callMock);
        // then
        verify(mock, after(50).never()).oneArg('1');
    }

    @Test
    public void should_verify_with_never_and_fail() {
        // given
        async.runAfter(10, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(300).never()).oneArg('1');
            }
        }).isInstanceOf(MoreThanAllowedActualInvocations.class).hasMessageContaining("Wanted at most 0 times but was 1");
    }

    @Test
    public void should_verify_with_only() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(600, callMock);
        // then
        verify(mock, after(300).only()).oneArg('1');
    }

    @Test
    public void should_verify_with_only_and_fail() {
        // given
        async.runAfter(10, callMock);
        async.runAfter(50, callMock);
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(300).only()).oneArg('1');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining(// TODO specific exception
        "No interactions wanted here");
    }

    @Test
    public void should_fail_early_when_at_most_is_used() {
        watch.start();
        // when
        async.runAfter(50, callMock);
        async.runAfter(100, callMock);
        // then
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            public void call() {
                verify(mock, after(10000).atMost(1)).oneArg('1');
            }
        }).isInstanceOf(MoreThanAllowedActualInvocations.class);
        // using generous number to avoid timing issues
        watch.assertElapsedTimeIsLessThan(2000, MILLISECONDS);
    }

    @Test
    public void should_fail_early_when_never_is_used() {
        watch.start();
        // when
        async.runAfter(50, callMock);
        // then
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            public void call() {
                verify(mock, after(10000).never()).oneArg('1');
            }
        }).isInstanceOf(MoreThanAllowedActualInvocations.class);
        // using generous number to avoid timing issues
        watch.assertElapsedTimeIsLessThan(2000, MILLISECONDS);
    }

    @Test
    // TODO nice to have
    @Ignore
    public void should_fail_early_when_only_is_used() {
        watch.start();
        // when
        async.runAfter(50, callMock);
        async.runAfter(100, callMock);
        // then
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            public void call() {
                verify(mock, after(10000).only()).oneArg('1');
            }
        }).isInstanceOf(NoInteractionsWanted.class);
        // using generous number to avoid timing issues
        watch.assertElapsedTimeIsLessThan(2000, MILLISECONDS);
    }

    @Test
    // TODO nice to have
    @Ignore
    public void should_fail_early_when_time_x_is_used() {
        watch.start();
        // when
        async.runAfter(50, callMock);
        async.runAfter(100, callMock);
        // then
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            public void call() {
                verify(mock, after(10000).times(1)).oneArg('1');
            }
        }).isInstanceOf(NoInteractionsWanted.class);
        // using generous number to avoid timing issues
        watch.assertElapsedTimeIsLessThan(2000, MILLISECONDS);
    }

    @Test
    public void should_return_formatted_output_from_toString_when_created_with_factory_method() {
        VerificationMode after = after(3);
        assertThat(after).hasToString("Wanted after 3 ms: [Wanted invocations count: 1]");
    }

    @Test
    public void should_return_formatted_output_from_toString_using_wrapped_verification_mode() {
        org.mockito.verification.After after = new org.mockito.verification.After(10, new DummyVerificationMode());
        assertThat(after).hasToString("Wanted after 10 ms: [Dummy verification mode]");
    }

    @Test
    public void should_return_formatted_output_from_toString_when_chaining_other_verification_mode() {
        VerificationMode afterAndOnly = after(5).only();
        assertThat(afterAndOnly).hasToString("Wanted after 5 ms: [Wanted invocations count: 1 and no other method invoked]");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_after() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_after, this.description("should_verify_with_after"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_after_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_after_and_fail, this.description("should_verify_with_after_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_time_x() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_time_x, this.description("should_verify_with_time_x"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_time_x_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_time_x_and_fail, this.description("should_verify_with_time_x_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_at_least() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_at_least, this.description("should_verify_with_at_least"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_at_least_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_at_least_and_fail, this.description("should_verify_with_at_least_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_at_most() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_at_most, this.description("should_verify_with_at_most"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_at_most_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_at_most_and_fail, this.description("should_verify_with_at_most_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_never() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_never, this.description("should_verify_with_never"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_never_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_never_and_fail, this.description("should_verify_with_never_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_only() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_only, this.description("should_verify_with_only"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_verify_with_only_and_fail() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_verify_with_only_and_fail, this.description("should_verify_with_only_and_fail"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_fail_early_when_at_most_is_used() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_fail_early_when_at_most_is_used, this.description("should_fail_early_when_at_most_is_used"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_fail_early_when_never_is_used() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_fail_early_when_never_is_used, this.description("should_fail_early_when_never_is_used"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_formatted_output_from_toString_when_created_with_factory_method() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_formatted_output_from_toString_when_created_with_factory_method, this.description("should_return_formatted_output_from_toString_when_created_with_factory_method"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_formatted_output_from_toString_using_wrapped_verification_mode() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_formatted_output_from_toString_using_wrapped_verification_mode, this.description("should_return_formatted_output_from_toString_using_wrapped_verification_mode"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_formatted_output_from_toString_when_chaining_other_verification_mode() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_formatted_output_from_toString_when_chaining_other_verification_mode, this.description("should_return_formatted_output_from_toString_when_chaining_other_verification_mode"));
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().tearDown();
            super.after();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().mockito, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private VerificationWithAfterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new VerificationWithAfterTest();
        }

        @java.lang.Override
        public VerificationWithAfterTest implementation() {
            return this.implementation;
        }
    }
}
