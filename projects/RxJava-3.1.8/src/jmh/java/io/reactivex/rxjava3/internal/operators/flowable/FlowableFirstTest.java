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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.NoSuchElementException;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFirstTest extends RxJavaTest {

    Subscriber<String> w;

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
        w = TestHelper.mockSubscriber();
        wo = TestHelper.mockSingleObserver();
        wm = TestHelper.mockMaybeObserver();
    }

    @Test
    public void firstOrElseOfNoneFlowable() {
        Flowable<String> src = Flowable.empty();
        src.first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseOfSomeFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("a");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.filter(IS_D).first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfSomeFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("d");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3).first(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).first(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().first(1).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrElseOfNone() {
        Flowable<String> src = Flowable.empty();
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseOfSome() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("a");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicate() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfSome() {
        Flowable<String> src = Flowable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("d");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void first() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElement() {
        Maybe<Integer> maybe = Flowable.just(1).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmpty() {
        Maybe<Integer> maybe = Flowable.<Integer>empty().firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicate() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElement() {
        Maybe<Integer> maybe = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmpty() {
        Maybe<Integer> maybe = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefault() {
        Single<Integer> single = Flowable.just(1, 2, 3).first(4);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElement() {
        Single<Integer> single = Flowable.just(1).first(2);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmpty() {
        Single<Integer> single = Flowable.<Integer>empty().first(1);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicate() {
        Single<Integer> single = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElement() {
        Single<Integer> single = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmpty() {
        Single<Integer> single = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrErrorNoElement() {
        Flowable.empty().firstOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElement() {
        Flowable.just(1).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorError() {
        Flowable.error(new RuntimeException("error")).firstOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void firstOrErrorNoElementFlowable() {
        Flowable.empty().firstOrError().toFlowable().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElementFlowable() {
        Flowable.just(1).firstOrError().toFlowable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElementsFlowable() {
        Flowable.just(1, 2, 3).firstOrError().toFlowable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorErrorFlowable() {
        Flowable.error(new RuntimeException("error")).firstOrError().toFlowable().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNoneFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfNoneFlowable, this.description("firstOrElseOfNoneFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSomeFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseOfSomeFlowable, this.description("firstOrElseOfSomeFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable, this.description("firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSomeFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrElseWithPredicateOfSomeFlowable, this.description("firstOrElseWithPredicateOfSomeFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstFlowable, this.description("firstFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithOneElementFlowable, this.description("firstWithOneElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithEmptyFlowable, this.description("firstWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateFlowable, this.description("firstWithPredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndOneElementFlowable, this.description("firstWithPredicateAndOneElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateAndEmptyFlowable, this.description("firstWithPredicateAndEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultFlowable, this.description("firstOrDefaultFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithOneElementFlowable, this.description("firstOrDefaultWithOneElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithEmptyFlowable, this.description("firstOrDefaultWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateFlowable, this.description("firstOrDefaultWithPredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndOneElementFlowable, this.description("firstOrDefaultWithPredicateAndOneElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrDefaultWithPredicateAndEmptyFlowable, this.description("firstOrDefaultWithPredicateAndEmptyFlowable"));
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
        public void benchmark_firstOrErrorNoElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorNoElementFlowable, this.description("firstOrErrorNoElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElementFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorOneElementFlowable, this.description("firstOrErrorOneElementFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorMultipleElementsFlowable, this.description("firstOrErrorMultipleElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorErrorFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOrErrorErrorFlowable, this.description("firstOrErrorErrorFlowable"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FlowableFirstTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFirstTest();
        }

        @java.lang.Override
        public FlowableFirstTest implementation() {
            return this.implementation;
        }
    }
}
