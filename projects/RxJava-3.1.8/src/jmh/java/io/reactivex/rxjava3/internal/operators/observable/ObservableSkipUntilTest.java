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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipUntilTest extends RxJavaTest {

    Observer<Object> observer;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
    }

    @Test
    public void normal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onNext(4);
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void otherNeverFires() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(Observable.never());
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, never()).onNext(any());
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void otherEmpty() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(Observable.empty());
        m.subscribe(observer);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
    }

    @Test
    public void otherFiresAndCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onNext(4);
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void sourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void otherThrowsImmediately() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onError(new RuntimeException("Forced failure"));
        verify(observer, never()).onNext(any());
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().skipUntil(PublishSubject.create()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.skipUntil(Observable.never());
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return Observable.never().skipUntil(o);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal1, this.description("normal1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherNeverFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherNeverFires, this.description("otherNeverFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherEmpty, this.description("otherEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherFiresAndCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherFiresAndCompletes, this.description("otherFiresAndCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sourceThrows, this.description("sourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherThrowsImmediately() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherThrowsImmediately, this.description("otherThrowsImmediately"));
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

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableSkipUntilTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSkipUntilTest();
        }

        @java.lang.Override
        public ObservableSkipUntilTest implementation() {
            return this.implementation;
        }
    }
}
