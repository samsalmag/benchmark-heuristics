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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSingleTest extends RxJavaTest {

    @Test
    public void singleObservable() {
        Observable<Integer> o = Observable.just(1).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultObservable() {
        Observable<Integer> o = Observable.just(1).single(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2).single(3).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().single(1).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527Observable() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        }).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void single() {
        Maybe<Integer> o = Observable.just(1).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElements() {
        Maybe<Integer> o = Observable.just(1, 2).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElements() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefault() {
        Single<Integer> o = Observable.just(1).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElements() {
        Single<Integer> o = Observable.just(1, 2).single(3);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().single(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElements() {
        Single<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleElementOperatorDoNotSwallowExceptionWhenDone() {
        final Throwable exception = new RuntimeException("some error");
        final AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    error.set(throwable);
                }
            });
            Observable.unsafeCreate(new ObservableSource<Integer>() {

                @Override
                public void subscribe(final Observer<? super Integer> observer) {
                    observer.onComplete();
                    observer.onError(exception);
                }
            }).singleElement().test().assertComplete();
            assertSame(exception, error.get().getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleOrErrorNoElement() {
        Observable.empty().singleOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void singleOrErrorOneElement() {
        Observable.just(1).singleOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void singleOrErrorMultipleElements() {
        Observable.just(1, 2, 3).singleOrError().test().assertNoValues().assertError(IllegalArgumentException.class);
    }

    @Test
    public void singleOrErrorError() {
        Observable.error(new RuntimeException("error")).singleOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.singleOrError();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.singleElement();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.singleOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.singleElement();
            }
        });
    }

    @Test
    public void singleOrError() {
        Observable.empty().singleOrError().toObservable().test().assertFailure(NoSuchElementException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleObservable, this.description("singleObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElementsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithTooManyElementsObservable, this.description("singleWithTooManyElementsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithEmptyObservable, this.description("singleWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateObservable, this.description("singleWithPredicateObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElementsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndTooManyElementsObservable, this.description("singleWithPredicateAndTooManyElementsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndEmptyObservable, this.description("singleWithPredicateAndEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultObservable, this.description("singleOrDefaultObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElementsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithTooManyElementsObservable, this.description("singleOrDefaultWithTooManyElementsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithEmptyObservable, this.description("singleOrDefaultWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateObservable, this.description("singleOrDefaultWithPredicateObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElementsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndTooManyElementsObservable, this.description("singleOrDefaultWithPredicateAndTooManyElementsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndEmptyObservable, this.description("singleOrDefaultWithPredicateAndEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1527Observable, this.description("issue1527Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single, this.description("single"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithTooManyElements, this.description("singleWithTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithEmpty, this.description("singleWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicate, this.description("singleWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndTooManyElements, this.description("singleWithPredicateAndTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndEmpty, this.description("singleWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefault, this.description("singleOrDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithTooManyElements, this.description("singleOrDefaultWithTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithEmpty, this.description("singleOrDefaultWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicate, this.description("singleOrDefaultWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndTooManyElements, this.description("singleOrDefaultWithPredicateAndTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndEmpty, this.description("singleOrDefaultWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1527, this.description("issue1527"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleElementOperatorDoNotSwallowExceptionWhenDone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleElementOperatorDoNotSwallowExceptionWhenDone, this.description("singleElementOperatorDoNotSwallowExceptionWhenDone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorNoElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorNoElement, this.description("singleOrErrorNoElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorOneElement, this.description("singleOrErrorOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorMultipleElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorMultipleElements, this.description("singleOrErrorMultipleElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorError, this.description("singleOrErrorError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrError, this.description("singleOrError"));
        }

        private ObservableSingleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSingleTest();
        }

        @java.lang.Override
        public ObservableSingleTest implementation() {
            return this.implementation;
        }
    }
}
