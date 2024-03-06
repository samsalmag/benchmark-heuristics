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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.NoSuchElementException;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableFirstTest extends RxJavaTest {

    Observer<String> w;

    SingleObserver<Object> wo;

    MaybeObserver<Object> wm;

    private static final Predicate<String> IS_D = new Predicate<String>() {

        @Override
        public boolean test(String value) {
            return "d".equals(value);
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockObserver();
        wo = TestHelper.mockSingleObserver();
        wm = TestHelper.mockMaybeObserver();
    }

    @Test
    public void firstOrElseOfNoneObservable() {
        Observable<String> src = Observable.empty();
        src.first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseOfSomeObservable() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("a");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicateObservable() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.filter(IS_D).first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfSomeObservable() {
        Observable<String> src = Observable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("d");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElementObservable() {
        Observable<Integer> o = Observable.just(1).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElementObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3).first(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElementObservable() {
        Observable<Integer> o = Observable.just(1).first(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().first(1).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElementObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrElseOfNone() {
        Observable<String> src = Observable.empty();
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseOfSome() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("a");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicate() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfSome() {
        Observable<String> src = Observable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("d");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void first() {
        Maybe<Integer> o = Observable.just(1, 2, 3).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElement() {
        Maybe<Integer> o = Observable.just(1).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElement() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefault() {
        Single<Integer> o = Observable.just(1, 2, 3).first(4);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElement() {
        Single<Integer> o = Observable.just(1).first(2);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().first(1);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElement() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrErrorNoElement() {
        Observable.empty().firstOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElement() {
        Observable.just(1).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElements() {
        Observable.just(1, 2, 3).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorError() {
        Observable.error(new RuntimeException("error")).firstOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void firstOrErrorNoElementObservable() {
        Observable.empty().firstOrError().toObservable().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElementObservable() {
        Observable.just(1).firstOrError().toObservable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElementsObservable() {
        Observable.just(1, 2, 3).firstOrError().toObservable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorErrorObservable() {
        Observable.error(new RuntimeException("error")).firstOrError().toObservable().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNoneObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfNoneObservable, this.description("firstOrElseOfNoneObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSomeObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfSomeObservable, this.description("firstOrElseOfSomeObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicateObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfNoneMatchingThePredicateObservable, this.description("firstOrElseWithPredicateOfNoneMatchingThePredicateObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSomeObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfSomeObservable, this.description("firstOrElseWithPredicateOfSomeObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstObservable, this.description("firstObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithOneElementObservable, this.description("firstWithOneElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithEmptyObservable, this.description("firstWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateObservable, this.description("firstWithPredicateObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndOneElementObservable, this.description("firstWithPredicateAndOneElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndEmptyObservable, this.description("firstWithPredicateAndEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultObservable, this.description("firstOrDefaultObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithOneElementObservable, this.description("firstOrDefaultWithOneElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithEmptyObservable, this.description("firstOrDefaultWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateObservable, this.description("firstOrDefaultWithPredicateObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndOneElementObservable, this.description("firstOrDefaultWithPredicateAndOneElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndEmptyObservable, this.description("firstOrDefaultWithPredicateAndEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfNone, this.description("firstOrElseOfNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfSome, this.description("firstOrElseOfSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfNoneMatchingThePredicate, this.description("firstOrElseWithPredicateOfNoneMatchingThePredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfSome, this.description("firstOrElseWithPredicateOfSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_first() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::first, this.description("first"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithOneElement, this.description("firstWithOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithEmpty, this.description("firstWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicate, this.description("firstWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndOneElement, this.description("firstWithPredicateAndOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndEmpty, this.description("firstWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefault, this.description("firstOrDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithOneElement, this.description("firstOrDefaultWithOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithEmpty, this.description("firstOrDefaultWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicate, this.description("firstOrDefaultWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndOneElement, this.description("firstOrDefaultWithPredicateAndOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndEmpty, this.description("firstOrDefaultWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorNoElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorNoElement, this.description("firstOrErrorNoElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorOneElement, this.description("firstOrErrorOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorMultipleElements, this.description("firstOrErrorMultipleElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorError, this.description("firstOrErrorError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorNoElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorNoElementObservable, this.description("firstOrErrorNoElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElementObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorOneElementObservable, this.description("firstOrErrorOneElementObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElementsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorMultipleElementsObservable, this.description("firstOrErrorMultipleElementsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorErrorObservable, this.description("firstOrErrorErrorObservable"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableFirstTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableFirstTest();
        }

        @java.lang.Override
        public ObservableFirstTest implementation() {
            return this.implementation;
        }
    }
}
