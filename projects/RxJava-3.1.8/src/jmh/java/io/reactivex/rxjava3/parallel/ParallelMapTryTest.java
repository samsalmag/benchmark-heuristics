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

import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelMapTryTest extends RxJavaTest implements Consumer<Object> {

    volatile int calls;

    @Override
    public void accept(Object t) throws Exception {
        calls++;
    }

    @Test
    public void mapNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).map(Functions.identity(), e).sequential().test().assertResult(1);
        }
    }

    @Test
    public void mapErrorNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).map(Functions.identity(), e).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void mapConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).map(Functions.identity(), e).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
        }
    }

    @Test
    public void mapErrorConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).map(Functions.identity(), e).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void mapFailWithError() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.ERROR).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void mapFailWithStop() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.STOP).sequential().test().assertResult();
    }

    @Test
    public void mapFailWithRetry() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            int count;

            @Override
            public Integer apply(Integer v) throws Exception {
                if (count++ == 1) {
                    return -1;
                }
                return 1 / v;
            }
        }, ParallelFailureHandling.RETRY).sequential().test().assertResult(-1, 1);
    }

    @Test
    public void mapFailWithRetryLimited() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailWithSkip() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.SKIP).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailHandlerThrows() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
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
    public void mapWrongParallelism() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).map(Functions.identity(), ParallelFailureHandling.ERROR));
    }

    @Test
    public void mapInvalidSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().map(Functions.identity(), ParallelFailureHandling.ERROR).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mapFailWithErrorConditional() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void mapFailWithStopConditional() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.STOP).filter(Functions.alwaysTrue()).sequential().test().assertResult();
    }

    @Test
    public void mapFailWithRetryConditional() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            int count;

            @Override
            public Integer apply(Integer v) throws Exception {
                if (count++ == 1) {
                    return -1;
                }
                return 1 / v;
            }
        }, ParallelFailureHandling.RETRY).filter(Functions.alwaysTrue()).sequential().test().assertResult(-1, 1);
    }

    @Test
    public void mapFailWithRetryLimitedConditional() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailWithSkipConditional() {
        Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
            }
        }, ParallelFailureHandling.SKIP).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailHandlerThrowsConditional() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return 1 / v;
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
    public void mapWrongParallelismConditional() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).map(Functions.identity(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()));
    }

    @Test
    public void mapInvalidSourceConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().map(Functions.identity(), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failureHandlingEnum() {
        TestHelper.checkEnum(ParallelFailureHandling.class);
    }

    @Test
    public void invalidSubscriberCount() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.range(1, 10).parallel().map(v -> v, ParallelFailureHandling.SKIP));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.map(v -> v, ParallelFailureHandling.ERROR));
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.map(v -> v, ParallelFailureHandling.ERROR).filter(v -> true));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapNoError, this.description("mapNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapErrorNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapErrorNoError, this.description("mapErrorNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapConditionalNoError, this.description("mapConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapErrorConditionalNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapErrorConditionalNoError, this.description("mapErrorConditionalNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithError, this.description("mapFailWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithStop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithStop, this.description("mapFailWithStop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetry() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithRetry, this.description("mapFailWithRetry"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryLimited() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithRetryLimited, this.description("mapFailWithRetryLimited"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithSkip() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithSkip, this.description("mapFailWithSkip"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailHandlerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailHandlerThrows, this.description("mapFailHandlerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapWrongParallelism() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapWrongParallelism, this.description("mapWrongParallelism"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapInvalidSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapInvalidSource, this.description("mapInvalidSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithErrorConditional, this.description("mapFailWithErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithStopConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithStopConditional, this.description("mapFailWithStopConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithRetryConditional, this.description("mapFailWithRetryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryLimitedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithRetryLimitedConditional, this.description("mapFailWithRetryLimitedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithSkipConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailWithSkipConditional, this.description("mapFailWithSkipConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailHandlerThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapFailHandlerThrowsConditional, this.description("mapFailHandlerThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapWrongParallelismConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapWrongParallelismConditional, this.description("mapWrongParallelismConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapInvalidSourceConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapInvalidSourceConditional, this.description("mapInvalidSourceConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failureHandlingEnum() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failureHandlingEnum, this.description("failureHandlingEnum"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidSubscriberCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::invalidSubscriberCount, this.description("invalidSubscriberCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ParallelMapTryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelMapTryTest();
        }

        @java.lang.Override
        public ParallelMapTryTest implementation() {
            return this.implementation;
        }
    }
}
