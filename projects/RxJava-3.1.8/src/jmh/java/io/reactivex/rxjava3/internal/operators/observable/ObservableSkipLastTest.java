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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipLastTest extends RxJavaTest {

    @Test
    public void skipLastEmpty() {
        Observable<String> o = Observable.<String>empty().skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLast1() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two", "three")).skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        InOrder inOrder = inOrder(observer);
        o.subscribe(observer);
        inOrder.verify(observer, never()).onNext("two");
        inOrder.verify(observer, never()).onNext("three");
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLast2() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two")).skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLastWithZeroCount() {
        Observable<String> w = Observable.just("one", "two");
        Observable<String> observable = w.skipLast(0);
        Observer<String> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLastWithBackpressure() {
        Observable<Integer> o = Observable.range(0, Flowable.bufferSize() * 2).skipLast(Flowable.bufferSize() + 10);
        TestObserver<Integer> to = new TestObserver<>();
        o.observeOn(Schedulers.computation()).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals((Flowable.bufferSize()) - 10, to.values().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void skipLastWithNegativeCount() {
        Observable.just("one").skipLast(-1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).skipLast(1));
    }

    @Test
    public void error() {
        Observable.error(new TestException()).skipLast(1).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.skipLast(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastEmpty, this.description("skipLastEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLast1, this.description("skipLast1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLast2, this.description("skipLast2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithZeroCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastWithZeroCount, this.description("skipLastWithZeroCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastWithBackpressure, this.description("skipLastWithBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithNegativeCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::skipLastWithNegativeCount, this.description("skipLastWithNegativeCount"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ObservableSkipLastTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSkipLastTest();
        }

        @java.lang.Override
        public ObservableSkipLastTest implementation() {
            return this.implementation;
        }
    }
}
