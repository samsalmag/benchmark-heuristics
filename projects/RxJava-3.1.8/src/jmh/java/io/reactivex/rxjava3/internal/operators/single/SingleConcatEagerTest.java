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
package io.reactivex.rxjava3.internal.operators.single;

import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class SingleConcatEagerTest {

    @Test
    public void iterableNormal() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void iterableNormalMaxConcurrency() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void iterableError() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableErrorMaxConcurrency() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void publisherNormal() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void publisherNormalMaxConcurrency() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void publisherError() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableDelayError() {
        Single.concatEagerDelayError(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void iterableDelayErrorMaxConcurrency() {
        Single.concatEagerDelayError(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayError() {
        Single.concatEagerDelayError(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayErrorMaxConcurrency() {
        Single.concatEagerDelayError(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableNormal, this.description("iterableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableNormalMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableNormalMaxConcurrency, this.description("iterableNormalMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableError, this.description("iterableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableErrorMaxConcurrency, this.description("iterableErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publisherNormal, this.description("publisherNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherNormalMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publisherNormalMaxConcurrency, this.description("publisherNormalMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publisherError, this.description("publisherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableDelayError, this.description("iterableDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iterableDelayErrorMaxConcurrency, this.description("iterableDelayErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publisherDelayError, this.description("publisherDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publisherDelayErrorMaxConcurrency, this.description("publisherDelayErrorMaxConcurrency"));
        }

        private SingleConcatEagerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleConcatEagerTest();
        }

        @java.lang.Override
        public SingleConcatEagerTest implementation() {
            return this.implementation;
        }
    }
}
