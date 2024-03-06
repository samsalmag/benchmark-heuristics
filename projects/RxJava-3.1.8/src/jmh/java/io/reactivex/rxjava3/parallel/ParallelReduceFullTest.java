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

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ParallelReduceFullTest extends RxJavaTest {

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.parallel().reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void error() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.<Integer>error(new TestException()).parallel().reduce(new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).test().assertFailure(TestException.class);
            assertTrue(errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ParallelFlowable.fromArray(Flowable.<Integer>error(new IOException()), Flowable.<Integer>error(new TestException())).reduce(new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).test().assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void empty() {
        Flowable.<Integer>empty().parallel().reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertResult();
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().reduce(new BiFunction<Object, Object, Object>() {

                @Override
                public Object apply(Object a, Object b) throws Exception {
                    return "" + a + b;
                }
            }).test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void reducerCrash() {
        Flowable.range(1, 4).parallel(2).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                if (b == 3) {
                    throw new TestException();
                }
                return a + b;
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void reducerCrash2() {
        Flowable.range(1, 4).parallel(2).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                if (a == 1 + 3) {
                    throw new TestException();
                }
                return a + b;
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallelToFlowable(pf -> pf.reduce((a, b) -> a));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error2, this.description("error2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError, this.description("doubleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reducerCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reducerCrash, this.description("reducerCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reducerCrash2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reducerCrash2, this.description("reducerCrash2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ParallelReduceFullTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelReduceFullTest();
        }

        @java.lang.Override
        public ParallelReduceFullTest implementation() {
            return this.implementation;
        }
    }
}
