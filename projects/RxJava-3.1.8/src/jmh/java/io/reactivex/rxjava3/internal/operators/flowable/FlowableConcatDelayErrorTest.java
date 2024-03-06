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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestSubscriberEx;

public class FlowableConcatDelayErrorTest extends RxJavaTest {

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        source.concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1, 2, 2, 3);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void mainErrors() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        source.concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.assertValues(1, 2, 2, 3);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 1, 2, 1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
    }

    @Test
    public void singleInnerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).// prevent scalar optimization
        hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerNull() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).// prevent scalar optimization
        hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return null;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(NullPointerException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerThrows() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).// prevent scalar optimization
        hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                throw new TestException();
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerWithEmpty() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.<Integer>empty() : Flowable.range(1, 2);
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void innerWithScalar() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.just(3) : Flowable.range(1, 2);
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 3, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(3);
        ts.assertValues(1, 2, 2, 3);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 2, 3, 3, 4);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    static <T> Flowable<T> withError(Flowable<T> source) {
        return source.concatWith(Flowable.<T>error(new TestException()));
    }

    @Test
    public void concatDelayErrorFlowable() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void concatDelayErrorFlowableError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.concatDelayError(withError(Flowable.just(withError(Flowable.just(1)), withError(Flowable.just(2))))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
        CompositeException ce = (CompositeException) ts.errors().get(0);
        List<Throwable> cex = ce.getExceptions();
        assertEquals(3, cex.size());
        assertTrue(cex.get(0).toString(), cex.get(0) instanceof TestException);
        assertTrue(cex.get(1).toString(), cex.get(1) instanceof TestException);
        assertTrue(cex.get(2).toString(), cex.get(2) instanceof TestException);
    }

    @Test
    public void concatDelayErrorIterable() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.concatDelayError(Arrays.asList(Flowable.just(1), Flowable.just(2))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void concatDelayErrorIterableError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.concatDelayError(Arrays.asList(withError(Flowable.just(1)), withError(Flowable.just(2)))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
        assertEquals(2, ((CompositeException) ts.errors().get(0)).getExceptions().size());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainCompletes, this.description("mainCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainErrors, this.description("mainErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerErrors, this.description("innerErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleInnerErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleInnerErrors, this.description("singleInnerErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerNull, this.description("innerNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerThrows, this.description("innerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerWithEmpty, this.description("innerWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithScalar() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerWithScalar, this.description("innerWithScalar"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatDelayErrorFlowable, this.description("concatDelayErrorFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatDelayErrorFlowableError, this.description("concatDelayErrorFlowableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatDelayErrorIterable, this.description("concatDelayErrorIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorIterableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatDelayErrorIterableError, this.description("concatDelayErrorIterableError"));
        }

        private FlowableConcatDelayErrorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableConcatDelayErrorTest();
        }

        @java.lang.Override
        public FlowableConcatDelayErrorTest implementation() {
            return this.implementation;
        }
    }
}
