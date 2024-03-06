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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableLastTest extends RxJavaTest {

    @Test
    public void lastWithElements() {
        Maybe<Integer> last = Observable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastWithNoElements() {
        Maybe<?> last = Observable.empty().lastElement();
        assertNull(last.blockingGet());
    }

    @Test
    public void lastMultiSubscribe() {
        Maybe<Integer> last = Observable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastViaObservable() {
        Observable.just(1, 2, 3).lastElement();
    }

    @Test
    public void last() {
        Maybe<Integer> o = Observable.just(1, 2, 3).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithOneElement() {
        Maybe<Integer> o = Observable.just(1).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndOneElement() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefault() {
        Single<Integer> o = Observable.just(1, 2, 3).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithOneElement() {
        Single<Integer> o = Observable.just(1).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().last(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(8);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndOneElement() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrErrorNoElement() {
        Observable.empty().lastOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void lastOrErrorOneElement() {
        Observable.just(1).lastOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void lastOrErrorMultipleElements() {
        Observable.just(1, 2, 3).lastOrError().test().assertNoErrors().assertValue(3);
    }

    @Test
    public void lastOrErrorError() {
        Observable.error(new RuntimeException("error")).lastOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.never().lastElement().toObservable());
        TestHelper.checkDisposed(Observable.never().lastElement());
        TestHelper.checkDisposed(Observable.just(1).lastOrError().toObservable());
        TestHelper.checkDisposed(Observable.just(1).lastOrError());
        TestHelper.checkDisposed(Observable.just(1).last(2).toObservable());
        TestHelper.checkDisposed(Observable.just(1).last(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastElement().toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastOrError().toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.last(2);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.last(2).toObservable();
            }
        });
    }

    @Test
    public void error() {
        Observable.error(new TestException()).lastElement().test().assertFailure(TestException.class);
    }

    @Test
    public void errorLastOrErrorObservable() {
        Observable.error(new TestException()).lastOrError().toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void emptyLastOrErrorObservable() {
        Observable.empty().lastOrError().toObservable().test().assertFailure(NoSuchElementException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithElements, this.description("lastWithElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithNoElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithNoElements, this.description("lastWithNoElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastMultiSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastMultiSubscribe, this.description("lastMultiSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastViaObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastViaObservable, this.description("lastViaObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_last() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::last, this.description("last"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithOneElement, this.description("lastWithOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithEmpty, this.description("lastWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithPredicate, this.description("lastWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicateAndOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithPredicateAndOneElement, this.description("lastWithPredicateAndOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithPredicateAndEmpty, this.description("lastWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefault, this.description("lastOrDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefaultWithOneElement, this.description("lastOrDefaultWithOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefaultWithEmpty, this.description("lastOrDefaultWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefaultWithPredicate, this.description("lastOrDefaultWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicateAndOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefaultWithPredicateAndOneElement, this.description("lastOrDefaultWithPredicateAndOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrDefaultWithPredicateAndEmpty, this.description("lastOrDefaultWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorNoElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrErrorNoElement, this.description("lastOrErrorNoElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrErrorOneElement, this.description("lastOrErrorOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorMultipleElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrErrorMultipleElements, this.description("lastOrErrorMultipleElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOrErrorError, this.description("lastOrErrorError"));
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
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorLastOrErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorLastOrErrorObservable, this.description("errorLastOrErrorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyLastOrErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyLastOrErrorObservable, this.description("emptyLastOrErrorObservable"));
        }

        private ObservableLastTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableLastTest();
        }

        @java.lang.Override
        public ObservableLastTest implementation() {
            return this.implementation;
        }
    }
}
