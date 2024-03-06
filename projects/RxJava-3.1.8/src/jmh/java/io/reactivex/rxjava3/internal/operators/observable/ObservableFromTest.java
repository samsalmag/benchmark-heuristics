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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.ScalarSupplier;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromTest extends RxJavaTest {

    @Test
    public void fromFutureTimeout() throws Exception {
        Observable.fromFuture(Observable.never().toFuture(), 100, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void fromPublisher() {
        Observable.fromPublisher(Flowable.just(1)).test().assertResult(1);
    }

    @Test
    public void just10() {
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void fromArrayEmpty() {
        assertSame(Observable.empty(), Observable.fromArray());
    }

    @Test
    public void fromArraySingle() {
        assertTrue(Observable.fromArray(1) instanceof ScalarSupplier);
    }

    @Test
    public void fromPublisherDispose() {
        TestHelper.checkDisposed(Flowable.just(1).toObservable());
    }

    @Test
    public void fromPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToObservable(new Function<Flowable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.toObservable();
            }
        });
    }

    @Test
    public void fusionRejected() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.fromArray(1, 2, 3).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFutureTimeout, this.description("fromFutureTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisher, this.description("fromPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just10() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just10, this.description("just10"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArrayEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromArrayEmpty, this.description("fromArrayEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArraySingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromArraySingle, this.description("fromArraySingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisherDispose, this.description("fromPublisherDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisherDoubleOnSubscribe, this.description("fromPublisherDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRejected, this.description("fusionRejected"));
        }

        private ObservableFromTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableFromTest();
        }

        @java.lang.Override
        public ObservableFromTest implementation() {
            return this.implementation;
        }
    }
}
