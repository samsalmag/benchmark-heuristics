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
package io.reactivex.rxjava3.internal.operators.maybe;

import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class MaybeConcatEagerTest {

    @Test
    public void iterableNormal() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.empty(), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void iterableNormalMaxConcurrency() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.empty(), Maybe.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void iterableError() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableErrorMaxConcurrency() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void publisherNormal() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.empty(), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void publisherNormalMaxConcurrency() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.empty(), Maybe.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void publisherError() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableDelayError() {
        Maybe.concatEagerDelayError(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void iterableDelayErrorMaxConcurrency() {
        Maybe.concatEagerDelayError(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayError() {
        Maybe.concatEagerDelayError(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayErrorMaxConcurrency() {
        Maybe.concatEagerDelayError(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
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

        private MaybeConcatEagerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeConcatEagerTest();
        }

        @java.lang.Override
        public MaybeConcatEagerTest implementation() {
            return this.implementation;
        }
    }
}
