/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package io.reactivex.rxjava3.parallel;

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelFilterTryTest extends RxJavaTest implements Consumer<Object> {

    volatile int calls;

    @Override
    public void accept(Object t) throws Exception {
        calls++;
    }

    @Test
    public void filterNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).filter(Functions.alwaysTrue(), e).sequential().test().assertResult(1);
        }
    }

    @Test
    public void filterFalse() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).filter(Functions.alwaysFalse(), e).sequential().test().assertResult();
        }
    }

    @Test
    public void filterFalseConditional() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).filter(Functions.alwaysFalse(), e).filter(Functions.alwaysTrue()).sequential().test().assertResult();
        }
    }

    @Test
    public void filterErrorNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).filter(Functions.alwaysTrue(), e).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void filterConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).filter(Functions.alwaysTrue(), e).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
        }
    }

    @Test
    public void filterErrorConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).filter(Functions.alwaysTrue(), e).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void filterFailWithError() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.ERROR).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void filterFailWithStop() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.STOP).sequential().test().assertResult();
    }

    @Test
    public void filterFailWithRetry() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            int count;

            @Override
            public boolean test(Integer v) throws Exception {
                if (count++ == 1) {
                    return true;
                }
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.RETRY).sequential().test().assertResult(0, 1);
    }

    @Test
    public void filterFailWithRetryLimited() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).sequential().test().assertResult(1);
    }

    @Test
    public void filterFailWithSkip() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.SKIP).sequential().test().assertResult(1);
    }

    @Test
    public void filterFailHandlerThrows() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                throw new TestException();
            }
        }).sequential().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, ArithmeticException.class, TestException.class);
    }

    @Test
    public void filterWrongParallelism() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).filter(Functions.alwaysTrue(), ParallelFailureHandling.ERROR));
    }

    @Test
    public void filterInvalidSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().filter(Functions.alwaysTrue(), ParallelFailureHandling.ERROR).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void filterFailWithErrorConditional() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void filterFailWithStopConditional() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.STOP).filter(Functions.alwaysTrue()).sequential().test().assertResult();
    }

    @Test
    public void filterFailWithRetryConditional() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            int count;

            @Override
            public boolean test(Integer v) throws Exception {
                if (count++ == 1) {
                    return true;
                }
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.RETRY).filter(Functions.alwaysTrue()).sequential().test().assertResult(0, 1);
    }

    @Test
    public void filterFailWithRetryLimitedConditional() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void filterFailWithSkipConditional() {
        Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, ParallelFailureHandling.SKIP).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void filterFailHandlerThrowsConditional() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return 1 / v > 0;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).sequential().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, ArithmeticException.class, TestException.class);
    }

    @Test
    public void filterWrongParallelismConditional() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).filter(Functions.alwaysTrue(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()));
    }

    @Test
    public void filterInvalidSourceConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().filter(Functions.alwaysTrue(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> ParallelFlowable.fromArray(f).filter(v -> true, ParallelFailureHandling.SKIP).sequential());
    }

    @Test
    public void doubleOnSubscribeConditional() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> ParallelFlowable.fromArray(f).filter(v -> true, ParallelFailureHandling.SKIP).filter(v -> true, ParallelFailureHandling.SKIP).sequential());
    }

    @Test
    public void conditionalFalseTrue() {
        Flowable.just(1).parallel().filter(v -> false, ParallelFailureHandling.SKIP).filter(v -> true, ParallelFailureHandling.SKIP).sequential().test().assertResult();
    }

    @Test
    public void conditionalTrueFalse() {
        Flowable.just(1).parallel().filter(v -> true, ParallelFailureHandling.SKIP).filter(v -> false, ParallelFailureHandling.SKIP).sequential().test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterNoError, this.description("filterNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFalse, this.description("filterFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFalseConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFalseConditional, this.description("filterFalseConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterErrorNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterErrorNoError, this.description("filterErrorNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterConditionalNoError, this.description("filterConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterErrorConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterErrorConditionalNoError, this.description("filterErrorConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithError, this.description("filterFailWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithStop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithStop, this.description("filterFailWithStop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithRetry() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithRetry, this.description("filterFailWithRetry"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithRetryLimited() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithRetryLimited, this.description("filterFailWithRetryLimited"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithSkip() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithSkip, this.description("filterFailWithSkip"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailHandlerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailHandlerThrows, this.description("filterFailHandlerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterWrongParallelism() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterWrongParallelism, this.description("filterWrongParallelism"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterInvalidSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterInvalidSource, this.description("filterInvalidSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithErrorConditional, this.description("filterFailWithErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithStopConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithStopConditional, this.description("filterFailWithStopConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithRetryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithRetryConditional, this.description("filterFailWithRetryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithRetryLimitedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithRetryLimitedConditional, this.description("filterFailWithRetryLimitedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailWithSkipConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailWithSkipConditional, this.description("filterFailWithSkipConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFailHandlerThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterFailHandlerThrowsConditional, this.description("filterFailHandlerThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterWrongParallelismConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterWrongParallelismConditional, this.description("filterWrongParallelismConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterInvalidSourceConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterInvalidSourceConditional, this.description("filterInvalidSourceConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribeConditional, this.description("doubleOnSubscribeConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFalseTrue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFalseTrue, this.description("conditionalFalseTrue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalTrueFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalTrueFalse, this.description("conditionalTrueFalse"));
        }

        private ParallelFilterTryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelFilterTryTest();
        }

        @java.lang.Override
        public ParallelFilterTryTest implementation() {
            return this.implementation;
        }
    }
}
