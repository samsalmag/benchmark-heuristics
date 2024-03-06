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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.parallel.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ParallelMapOptionalTest extends RxJavaTest {

    @Test
    public void doubleFilter() {
        Flowable.range(1, 10).parallel().mapOptional(Optional::of).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().assertResult(6);
    }

    @Test
    public void doubleFilterAsync() {
        Flowable.range(1, 10).parallel().runOn(Schedulers.computation()).mapOptional(Optional::of).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertResult(6);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleError2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).parallel().mapOptional(Optional::of).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrash() {
        Flowable.just(1).parallel().mapOptional(v -> {
            throw new TestException();
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional() {
        Flowable.just(1).parallel().mapOptional(v -> {
            throw new TestException();
        }).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional2() {
        Flowable.just(1).parallel().runOn(Schedulers.computation()).mapOptional(v -> {
            throw new TestException();
        }).filter(Functions.alwaysTrue()).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void allNone() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty()).sequential().test().assertResult();
    }

    @Test
    public void allNoneConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty()).filter(v -> true).sequential().test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty()).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void mixedConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty()).filter(v -> true).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void invalidSubscriberCount() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.range(1, 10).parallel().mapOptional(Optional::of));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of));
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of).filter(v -> true));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleFilter, this.description("doubleFilter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilterAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleFilterAsync, this.description("doubleFilterAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError, this.description("doubleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError2, this.description("doubleError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrash, this.description("mapCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrashConditional, this.description("mapCrashConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrashConditional2, this.description("mapCrashConditional2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allNone, this.description("allNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allNoneConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allNoneConditional, this.description("allNoneConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixed, this.description("mixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixedConditional, this.description("mixedConditional"));
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

        private ParallelMapOptionalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelMapOptionalTest();
        }

        @java.lang.Override
        public ParallelMapOptionalTest implementation() {
            return this.implementation;
        }
    }
}
