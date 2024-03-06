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
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableElementAtTest extends RxJavaTest {

    @Test
    public void elementAtFlowable() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1).toFlowable().blockingSingle().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndexFlowable() {
        Flowable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBoundsFlowable() {
        assertEquals(-100, Flowable.fromArray(1, 2).elementAt(2).toFlowable().blockingFirst(-100).intValue());
    }

    @Test
    public void elementAtOrDefaultFlowable() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1, 0).toFlowable().blockingSingle().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBoundsFlowable() {
        assertEquals(0, Flowable.fromArray(1, 2).elementAt(2, 0).toFlowable().blockingSingle().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndexFlowable() {
        Flowable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test
    public void elementAt() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1).blockingGet().intValue());
    }

    @Test
    public void elementAtConstrainsUpstreamRequests() {
        final List<Long> requests = new ArrayList<>();
        Flowable.fromArray(1, 2, 3, 4).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) throws Throwable {
                requests.add(n);
            }
        }).elementAt(2).blockingGet().intValue();
        assertEquals(Arrays.asList(3L), requests);
    }

    @Test
    public void elementAtWithDefaultConstrainsUpstreamRequests() {
        final List<Long> requests = new ArrayList<>();
        Flowable.fromArray(1, 2, 3, 4).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) throws Throwable {
                requests.add(n);
            }
        }).elementAt(2, 100).blockingGet().intValue();
        assertEquals(Arrays.asList(3L), requests);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndex() {
        Flowable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBounds() {
        assertNull(Flowable.fromArray(1, 2).elementAt(2).blockingGet());
    }

    @Test
    public void elementAtOrDefault() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1, 0).blockingGet().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBounds() {
        assertEquals(0, Flowable.fromArray(1, 2).elementAt(2, 0).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndex() {
        Flowable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrErrorNegativeIndex() {
        Flowable.empty().elementAtOrError(-1);
    }

    @Test
    public void elementAtOrErrorNoElement() {
        Flowable.empty().elementAtOrError(0).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorOneElement() {
        Flowable.just(1).elementAtOrError(0).test().assertNoErrors().assertValue(1);
    }

    @Test
    public void elementAtOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).elementAtOrError(1).test().assertNoErrors().assertValue(2);
    }

    @Test
    public void elementAtOrErrorInvalidIndex() {
        Flowable.just(1, 2, 3).elementAtOrError(3).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorError() {
        Flowable.error(new RuntimeException("error")).elementAtOrError(0).to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void elementAtIndex0OnEmptySource() {
        Flowable.empty().elementAt(0).test().assertResult();
    }

    @Test
    public void elementAtIndex0WithDefaultOnEmptySource() {
        Flowable.empty().elementAt(0, 5).test().assertResult(5);
    }

    @Test
    public void elementAtIndex1OnEmptySource() {
        Flowable.empty().elementAt(1).test().assertResult();
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySource() {
        Flowable.empty().elementAt(1, 10).test().assertResult(10);
    }

    @Test
    public void elementAtOrErrorIndex1OnEmptySource() {
        Flowable.empty().elementAtOrError(1).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Object>>() {

            @Override
            public Single<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0, 1);
            }
        });
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySourceObservable() {
        Flowable.empty().elementAt(1, 10).toFlowable().test().assertResult(10);
    }

    @Test
    public void errorFlowable() {
        Flowable.error(new TestException()).elementAt(1, 10).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).elementAt(1, 10).test().assertFailure(TestException.class);
        Flowable.error(new TestException()).elementAt(1).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.elementAt(0).toFlowable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0);
            }
        }, false, null, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0, 1);
            }
        }, false, null, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0).toFlowable();
            }
        }, false, null, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0, 1).toFlowable();
            }
        }, false, null, 1, 1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0).toFlowable());
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0, 1).toFlowable());
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0));
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0, 1));
    }

    @Test
    public void badSourceObservable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.elementAt(0).toFlowable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.elementAt(0, 1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtFlowable, this.description("elementAtFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithMinusIndexFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::elementAtWithMinusIndexFlowable, this.description("elementAtWithMinusIndexFlowable"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBoundsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtWithIndexOutOfBoundsFlowable, this.description("elementAtWithIndexOutOfBoundsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefaultFlowable, this.description("elementAtOrDefaultFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBoundsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefaultWithIndexOutOfBoundsFlowable, this.description("elementAtOrDefaultWithIndexOutOfBoundsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithMinusIndexFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::elementAtOrDefaultWithMinusIndexFlowable, this.description("elementAtOrDefaultWithMinusIndexFlowable"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAt, this.description("elementAt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtConstrainsUpstreamRequests() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtConstrainsUpstreamRequests, this.description("elementAtConstrainsUpstreamRequests"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithDefaultConstrainsUpstreamRequests() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtWithDefaultConstrainsUpstreamRequests, this.description("elementAtWithDefaultConstrainsUpstreamRequests"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithMinusIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::elementAtWithMinusIndex, this.description("elementAtWithMinusIndex"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBounds() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtWithIndexOutOfBounds, this.description("elementAtWithIndexOutOfBounds"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefault, this.description("elementAtOrDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBounds() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefaultWithIndexOutOfBounds, this.description("elementAtOrDefaultWithIndexOutOfBounds"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithMinusIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::elementAtOrDefaultWithMinusIndex, this.description("elementAtOrDefaultWithMinusIndex"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorNegativeIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::elementAtOrErrorNegativeIndex, this.description("elementAtOrErrorNegativeIndex"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorNoElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorNoElement, this.description("elementAtOrErrorNoElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorOneElement, this.description("elementAtOrErrorOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorMultipleElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorMultipleElements, this.description("elementAtOrErrorMultipleElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorInvalidIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorInvalidIndex, this.description("elementAtOrErrorInvalidIndex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorError, this.description("elementAtOrErrorError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex0OnEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtIndex0OnEmptySource, this.description("elementAtIndex0OnEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex0WithDefaultOnEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtIndex0WithDefaultOnEmptySource, this.description("elementAtIndex0WithDefaultOnEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1OnEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtIndex1OnEmptySource, this.description("elementAtIndex1OnEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1WithDefaultOnEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtIndex1WithDefaultOnEmptySource, this.description("elementAtIndex1WithDefaultOnEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorIndex1OnEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrErrorIndex1OnEmptySource, this.description("elementAtOrErrorIndex1OnEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1WithDefaultOnEmptySourceObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtIndex1WithDefaultOnEmptySourceObservable, this.description("elementAtIndex1WithDefaultOnEmptySourceObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorFlowable, this.description("errorFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceObservable, this.description("badSourceObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource2, this.description("badSource2"));
        }

        private FlowableElementAtTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableElementAtTest();
        }

        @java.lang.Override
        public FlowableElementAtTest implementation() {
            return this.implementation;
        }
    }
}
