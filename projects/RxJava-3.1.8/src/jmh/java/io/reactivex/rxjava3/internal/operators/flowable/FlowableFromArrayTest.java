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

import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.ScalarSupplier;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFromArrayTest extends RxJavaTest {

    Flowable<Integer> create(int n) {
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = i;
        }
        return Flowable.fromArray(array);
    }

    @Test
    public void simple() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        create(1000).subscribe(ts);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        create(1000).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.request(10);
        ts.assertNoErrors();
        ts.assertValueCount(10);
        ts.assertNotComplete();
        ts.request(1000);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void conditionalBackpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        create(1000).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.request(10);
        ts.assertNoErrors();
        ts.assertValueCount(10);
        ts.assertNotComplete();
        ts.request(1000);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void empty() {
        Assert.assertSame(Flowable.empty(), Flowable.fromArray(new Object[0]));
    }

    @Test
    public void just() {
        Flowable<Integer> source = Flowable.fromArray(new Integer[] { 1 });
        Assert.assertTrue(source.getClass().toString(), source instanceof ScalarSupplier);
    }

    @Test
    public void just10Arguments() {
        Flowable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1, 2, 3));
    }

    @Test
    public void conditionalOneIsNull() {
        Flowable.fromArray(new Integer[] { null, 1 }).filter(Functions.alwaysTrue()).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void conditionalOneIsNullSlowPath() {
        Flowable.fromArray(new Integer[] { null, 1 }).filter(Functions.alwaysTrue()).test(2L).assertFailure(NullPointerException.class);
    }

    @Test
    public void conditionalOneByOne() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(Functions.alwaysTrue()).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalFiltered() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test().assertResult(2, 4);
    }

    @Test
    public void conditionalSlowPathCancel() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(Functions.alwaysTrue()).subscribeWith(new TestSubscriber<Integer>(5L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        }).assertResult(1);
    }

    @Test
    public void conditionalSlowPathSkipCancel() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v < 2;
            }
        }).subscribeWith(new TestSubscriber<Integer>(5L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        }).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple, this.description("simple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalBackpressure, this.description("conditionalBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just10Arguments() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just10Arguments, this.description("just10Arguments"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalOneIsNull, this.description("conditionalOneIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneIsNullSlowPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalOneIsNullSlowPath, this.description("conditionalOneIsNullSlowPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneByOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalOneByOne, this.description("conditionalOneByOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFiltered() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFiltered, this.description("conditionalFiltered"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathCancel, this.description("conditionalSlowPathCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathSkipCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathSkipCancel, this.description("conditionalSlowPathSkipCancel"));
        }

        private FlowableFromArrayTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFromArrayTest();
        }

        @java.lang.Override
        public FlowableFromArrayTest implementation() {
            return this.implementation;
        }
    }
}
