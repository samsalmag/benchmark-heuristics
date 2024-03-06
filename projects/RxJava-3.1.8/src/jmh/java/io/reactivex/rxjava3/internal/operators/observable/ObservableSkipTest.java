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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableSkipTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void skipNegativeElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(-99);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipZeroElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(0);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipOneElement() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(1);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipTwoElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(2);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipEmptyStream() {
        Observable<String> w = Observable.empty();
        Observable<String> skip = w.skip(1);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipMultipleObservers() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(2);
        Observer<String> observer1 = TestHelper.mockObserver();
        skip.subscribe(observer1);
        Observer<String> observer2 = TestHelper.mockObserver();
        skip.subscribe(observer2);
        verify(observer1, times(1)).onNext(any(String.class));
        verify(observer1, never()).onError(any(Throwable.class));
        verify(observer1, times(1)).onComplete();
        verify(observer2, times(1)).onNext(any(String.class));
        verify(observer2, never()).onError(any(Throwable.class));
        verify(observer2, times(1)).onComplete();
    }

    @Test
    public void skipError() {
        Exception e = new Exception();
        Observable<String> ok = Observable.just("one");
        Observable<String> error = Observable.error(e);
        Observable<String> skip = Observable.concat(ok, error).skip(100);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, times(1)).onError(e);
        verify(observer, never()).onComplete();
    }

    @Test
    public void requestOverflowDoesNotOccur() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.range(1, 10).skip(5).subscribe(to);
        to.assertTerminated();
        to.assertComplete();
        to.assertNoErrors();
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), to.values());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).skip(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.skip(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipNegativeElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::skipNegativeElements, this.description("skipNegativeElements"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipZeroElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipZeroElements, this.description("skipZeroElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipOneElement, this.description("skipOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipTwoElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipTwoElements, this.description("skipTwoElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmptyStream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipEmptyStream, this.description("skipEmptyStream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipMultipleObservers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipMultipleObservers, this.description("skipMultipleObservers"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipError, this.description("skipError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflowDoesNotOccur() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestOverflowDoesNotOccur, this.description("requestOverflowDoesNotOccur"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ObservableSkipTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSkipTest();
        }

        @java.lang.Override
        public ObservableSkipTest implementation() {
            return this.implementation;
        }
    }
}
