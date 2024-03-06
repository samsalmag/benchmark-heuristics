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
import java.util.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableElementAtTest extends RxJavaTest {

    @Test
    public void elementAtObservable() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAtWithIndexOutOfBoundsObservable() {
        assertEquals(-99, Observable.fromArray(1, 2).elementAt(2).toObservable().blockingSingle(-99).intValue());
    }

    @Test
    public void elementAtOrDefaultObservable() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1, 0).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBoundsObservable() {
        assertEquals(0, Observable.fromArray(1, 2).elementAt(2, 0).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAt() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndex() {
        Observable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBounds() {
        assertNull(Observable.fromArray(1, 2).elementAt(2).blockingGet());
    }

    @Test
    public void elementAtOrDefault() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1, 0).blockingGet().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBounds() {
        assertEquals(0, Observable.fromArray(1, 2).elementAt(2, 0).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndex() {
        Observable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrErrorNegativeIndex() {
        Observable.empty().elementAtOrError(-1);
    }

    @Test
    public void elementAtOrErrorNoElement() {
        Observable.empty().elementAtOrError(0).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorOneElement() {
        Observable.just(1).elementAtOrError(0).test().assertNoErrors().assertValue(1);
    }

    @Test
    public void elementAtOrErrorMultipleElements() {
        Observable.just(1, 2, 3).elementAtOrError(1).test().assertNoErrors().assertValue(2);
    }

    @Test
    public void elementAtOrErrorInvalidIndex() {
        Observable.just(1, 2, 3).elementAtOrError(3).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorError() {
        Observable.error(new RuntimeException("error")).elementAtOrError(0).to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void elementAtIndex0OnEmptySource() {
        Observable.empty().elementAt(0).test().assertResult();
    }

    @Test
    public void elementAtIndex0WithDefaultOnEmptySource() {
        Observable.empty().elementAt(0, 5).test().assertResult(5);
    }

    @Test
    public void elementAtIndex1OnEmptySource() {
        Observable.empty().elementAt(1).test().assertResult();
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySource() {
        Observable.empty().elementAt(1, 10).test().assertResult(10);
    }

    @Test
    public void elementAtOrErrorIndex1OnEmptySource() {
        Observable.empty().elementAtOrError(1).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0).toObservable());
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0));
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0, 1).toObservable());
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0, 1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0).toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0, 1);
            }
        });
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySourceObservable() {
        Observable.empty().elementAt(1, 10).toObservable().test().assertResult(10);
    }

    @Test
    public void errorObservable() {
        Observable.error(new TestException()).elementAt(1, 10).toObservable().test().assertFailure(TestException.class);
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
            }.elementAt(0).toObservable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource() {
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
            }.elementAt(0).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource2() {
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
            }.elementAt(0, 1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtObservable, this.description("elementAtObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBoundsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtWithIndexOutOfBoundsObservable, this.description("elementAtWithIndexOutOfBoundsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefaultObservable, this.description("elementAtOrDefaultObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBoundsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAtOrDefaultWithIndexOutOfBoundsObservable, this.description("elementAtOrDefaultWithIndexOutOfBoundsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::elementAt, this.description("elementAt"));
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
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
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
        public void benchmark_errorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorObservable, this.description("errorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceObservable, this.description("badSourceObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource2, this.description("badSource2"));
        }

        private ObservableElementAtTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableElementAtTest();
        }

        @java.lang.Override
        public ObservableElementAtTest implementation() {
            return this.implementation;
        }
    }
}
