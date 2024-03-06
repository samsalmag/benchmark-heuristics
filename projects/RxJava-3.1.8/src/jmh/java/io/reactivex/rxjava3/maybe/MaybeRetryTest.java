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
package io.reactivex.rxjava3.maybe;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.functions.Functions;

public class MaybeRetryTest extends RxJavaTest {

    @Test
    public void retryTimesPredicateWithMatchingPredicate() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                throw new IllegalArgumentException();
            }
        }).retry(Integer.MAX_VALUE, new Predicate<Throwable>() {

            @Override
            public boolean test(final Throwable throwable) throws Exception {
                return !(throwable instanceof IllegalArgumentException);
            }
        }).test().assertFailure(IllegalArgumentException.class);
        assertEquals(3, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(2, Functions.alwaysTrue()).test().assertResult(true);
        assertEquals(3, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithNotMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(1, Functions.alwaysTrue()).test().assertFailure(RuntimeException.class);
        assertEquals(2, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithZeroRetries() {
        final AtomicInteger atomicInteger = new AtomicInteger(2);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(0, Functions.alwaysTrue()).test().assertFailure(RuntimeException.class);
        assertEquals(1, numberOfSubscribeCalls.get());
    }

    @Test
    public void untilTrueJust() {
        Maybe.just(1).retryUntil(() -> true).test().assertResult(1);
    }

    @Test
    public void untilFalseJust() {
        Maybe.just(1).retryUntil(() -> false).test().assertResult(1);
    }

    @Test
    public void untilTrueEmpty() {
        Maybe.empty().retryUntil(() -> true).test().assertResult();
    }

    @Test
    public void untilFalseEmpty() {
        Maybe.empty().retryUntil(() -> false).test().assertResult();
    }

    @Test
    public void untilTrueError() {
        Maybe.error(new TestException()).retryUntil(() -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void untilFalseError() {
        AtomicInteger counter = new AtomicInteger();
        Maybe.defer(() -> {
            if (counter.getAndIncrement() == 0) {
                return Maybe.error(new TestException());
            }
            return Maybe.just(1);
        }).retryUntil(() -> false).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithMatchingPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryTimesPredicateWithMatchingPredicate, this.description("retryTimesPredicateWithMatchingPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithMatchingRetryAmount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryTimesPredicateWithMatchingRetryAmount, this.description("retryTimesPredicateWithMatchingRetryAmount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithNotMatchingRetryAmount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryTimesPredicateWithNotMatchingRetryAmount, this.description("retryTimesPredicateWithNotMatchingRetryAmount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithZeroRetries() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryTimesPredicateWithZeroRetries, this.description("retryTimesPredicateWithZeroRetries"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilTrueJust, this.description("untilTrueJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFalseJust, this.description("untilFalseJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilTrueEmpty, this.description("untilTrueEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFalseEmpty, this.description("untilFalseEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilTrueError, this.description("untilTrueError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFalseError, this.description("untilFalseError"));
        }

        private MaybeRetryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeRetryTest();
        }

        @java.lang.Override
        public MaybeRetryTest implementation() {
            return this.implementation;
        }
    }
}
