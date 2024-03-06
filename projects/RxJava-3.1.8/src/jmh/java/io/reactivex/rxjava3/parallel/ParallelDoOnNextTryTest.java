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

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelDoOnNextTryTest extends RxJavaTest implements Consumer<Object> {

    volatile int calls;

    @Override
    public void accept(Object t) throws Exception {
        calls++;
    }

    @Test
    public void doOnNextNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).doOnNext(this, e).sequential().test().assertResult(1);
            assertEquals(calls, 1);
            calls = 0;
        }
    }

    @Test
    public void doOnNextErrorNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).doOnNext(this, e).sequential().test().assertFailure(TestException.class);
            assertEquals(calls, 0);
        }
    }

    @Test
    public void doOnNextConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).doOnNext(this, e).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
            assertEquals(calls, 1);
            calls = 0;
        }
    }

    @Test
    public void doOnNextErrorConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).doOnNext(this, e).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
            assertEquals(calls, 0);
        }
    }

    @Test
    public void doOnNextFailWithError() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.ERROR).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void doOnNextFailWithStop() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.STOP).sequential().test().assertResult();
    }

    @Test
    public void doOnNextFailWithRetry() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            int count;

            @Override
            public void accept(Integer v) throws Exception {
                if (count++ == 1) {
                    return;
                }
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.RETRY).sequential().test().assertResult(0, 1);
    }

    @Test
    public void doOnNextFailWithRetryLimited() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).sequential().test().assertResult(1);
    }

    @Test
    public void doOnNextFailWithSkip() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.SKIP).sequential().test().assertResult(1);
    }

    @Test
    public void doOnNextFailHandlerThrows() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
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
    public void doOnNextWrongParallelism() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).doOnNext(Functions.emptyConsumer(), ParallelFailureHandling.ERROR));
    }

    @Test
    public void filterInvalidSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().doOnNext(Functions.emptyConsumer(), ParallelFailureHandling.ERROR).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnNextFailWithErrorConditional() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void doOnNextFailWithStopConditional() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.STOP).filter(Functions.alwaysTrue()).sequential().test().assertResult();
    }

    @Test
    public void doOnNextFailWithRetryConditional() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            int count;

            @Override
            public void accept(Integer v) throws Exception {
                if (count++ == 1) {
                    return;
                }
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.RETRY).filter(Functions.alwaysTrue()).sequential().test().assertResult(0, 1);
    }

    @Test
    public void doOnNextFailWithRetryLimitedConditional() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void doOnNextFailWithSkipConditional() {
        Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
            }
        }, ParallelFailureHandling.SKIP).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void doOnNextFailHandlerThrowsConditional() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (1 / v < 0) {
                    System.out.println("Should not happen!");
                }
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
    public void doOnNextWrongParallelismConditional() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).doOnNext(Functions.emptyConsumer(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()));
    }

    @Test
    public void filterInvalidSourceConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().doOnNext(Functions.emptyConsumer(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> ParallelFlowable.fromArray(f).doOnNext(v -> {
        }, ParallelFailureHandling.SKIP).sequential());
    }

    @Test
    public void doubleOnSubscribeConditional() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> ParallelFlowable.fromArray(f).doOnNext(v -> {
        }, ParallelFailureHandling.SKIP).filter(v -> true, ParallelFailureHandling.SKIP).sequential());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextNoError, this.description("doOnNextNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextErrorNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextErrorNoError, this.description("doOnNextErrorNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextConditionalNoError, this.description("doOnNextConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextErrorConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextErrorConditionalNoError, this.description("doOnNextErrorConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithError, this.description("doOnNextFailWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithStop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithStop, this.description("doOnNextFailWithStop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithRetry() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithRetry, this.description("doOnNextFailWithRetry"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithRetryLimited() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithRetryLimited, this.description("doOnNextFailWithRetryLimited"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithSkip() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithSkip, this.description("doOnNextFailWithSkip"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailHandlerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailHandlerThrows, this.description("doOnNextFailHandlerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextWrongParallelism() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextWrongParallelism, this.description("doOnNextWrongParallelism"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterInvalidSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterInvalidSource, this.description("filterInvalidSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithErrorConditional, this.description("doOnNextFailWithErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithStopConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithStopConditional, this.description("doOnNextFailWithStopConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithRetryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithRetryConditional, this.description("doOnNextFailWithRetryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithRetryLimitedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithRetryLimitedConditional, this.description("doOnNextFailWithRetryLimitedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailWithSkipConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailWithSkipConditional, this.description("doOnNextFailWithSkipConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextFailHandlerThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextFailHandlerThrowsConditional, this.description("doOnNextFailHandlerThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnNextWrongParallelismConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnNextWrongParallelismConditional, this.description("doOnNextWrongParallelismConditional"));
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

        private ParallelDoOnNextTryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelDoOnNextTryTest();
        }

        @java.lang.Override
        public ParallelDoOnNextTryTest implementation() {
            return this.implementation;
        }
    }
}
