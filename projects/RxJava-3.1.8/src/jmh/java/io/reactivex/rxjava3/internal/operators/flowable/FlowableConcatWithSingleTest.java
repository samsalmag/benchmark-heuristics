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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class FlowableConcatWithSingleTest extends RxJavaTest {

    @Test
    public void normal() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Single.just(100)).subscribe(ts);
        ts.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void backpressure() {
        Flowable.range(1, 5).concatWith(Single.just(100)).test(0).assertEmpty().requestMore(3).assertValues(1, 2, 3).requestMore(2).assertValues(1, 2, 3, 4, 5).requestMore(1).assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void mainError() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>error(new TestException()).concatWith(Single.just(100)).subscribe(ts);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Single.<Integer>error(new TestException())).subscribe(ts);
        ts.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void takeMain() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Single.just(100)).take(3).subscribe(ts);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void cancelOther() {
        SingleSubject<Object> other = SingleSubject.create();
        TestSubscriber<Object> ts = Flowable.empty().concatWith(other).test();
        assertTrue(other.hasObservers());
        ts.cancel();
        assertFalse(other.hasObservers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherError, this.description("otherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeMain, this.description("takeMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOther, this.description("cancelOther"));
        }

        private FlowableConcatWithSingleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableConcatWithSingleTest();
        }

        @java.lang.Override
        public FlowableConcatWithSingleTest implementation() {
            return this.implementation;
        }
    }
}
