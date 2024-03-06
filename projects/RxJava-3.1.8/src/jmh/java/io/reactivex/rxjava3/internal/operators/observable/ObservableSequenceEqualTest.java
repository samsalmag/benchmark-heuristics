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

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.BiPredicate;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSequenceEqualTest extends RxJavaTest {

    @Test
    public void observable1() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, true);
    }

    @Test
    public void observable2() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three", "four")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void observable3() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three", "four"), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withError1Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.just("one", "two", "three")).toObservable();
        verifyError(o);
    }

    @Test
    public void withError2Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException()))).toObservable();
        verifyError(o);
    }

    @Test
    public void withError3Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException()))).toObservable();
        verifyError(o);
    }

    @Test
    public void withEmpty1Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withEmpty2Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.<String>empty()).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withEmpty3Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.<String>empty()).toObservable();
        verifyResult(o, true);
    }

    @Test
    public void withEqualityErrorObservable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one"), Observable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        }).toObservable();
        verifyError(o);
    }

    private void verifyResult(Single<Boolean> o, boolean result) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(result);
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Observable<Boolean> observable) {
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Single<Boolean> single) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void prefetchObservable() {
        Observable.sequenceEqual(Observable.range(1, 20), Observable.range(1, 20), 2).toObservable().test().assertResult(true);
    }

    @Test
    public void disposedObservable() {
        TestHelper.checkDisposed(Observable.sequenceEqual(Observable.just(1), Observable.just(2)).toObservable());
    }

    @Test
    public void one() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three"));
        verifyResult(o, true);
    }

    @Test
    public void two() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three", "four"));
        verifyResult(o, false);
    }

    @Test
    public void three() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three", "four"), Observable.just("one", "two", "three"));
        verifyResult(o, false);
    }

    @Test
    public void withError1() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.just("one", "two", "three"));
        verifyError(o);
    }

    @Test
    public void withError2() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())));
        verifyError(o);
    }

    @Test
    public void withError3() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())));
        verifyError(o);
    }

    @Test
    public void withEmpty1() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.just("one", "two", "three"));
        verifyResult(o, false);
    }

    @Test
    public void withEmpty2() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.<String>empty());
        verifyResult(o, false);
    }

    @Test
    public void withEmpty3() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.<String>empty());
        verifyResult(o, true);
    }

    @Test
    public void withEqualityError() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one"), Observable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        });
        verifyError(o);
    }

    private void verifyResult(Observable<Boolean> o, boolean result) {
        Observer<Boolean> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(result);
        inOrder.verify(observer).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void prefetch() {
        Observable.sequenceEqual(Observable.range(1, 20), Observable.range(1, 20), 2).test().assertResult(true);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.sequenceEqual(Observable.just(1), Observable.just(2)));
    }

    @Test
    public void simpleInequal() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(2)).test().assertResult(false);
    }

    @Test
    public void simpleInequalObservable() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(2)).toObservable().test().assertResult(false);
    }

    @Test
    public void onNextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Boolean> to = Observable.sequenceEqual(Observable.never(), ps).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void onNextCancelRaceObservable() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Boolean> to = Observable.sequenceEqual(Observable.never(), ps).toObservable().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void firstCompletesBeforeSecond() {
        Observable.sequenceEqual(Observable.just(1), Observable.empty()).test().assertResult(false);
    }

    @Test
    public void secondCompletesBeforeFirst() {
        Observable.sequenceEqual(Observable.empty(), Observable.just(1)).test().assertResult(false);
    }

    @Test
    public void bothEmpty() {
        Observable.sequenceEqual(Observable.empty(), Observable.empty()).test().assertResult(true);
    }

    @Test
    public void bothJust() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(1)).test().assertResult(true);
    }

    @Test
    public void bothCompleteWhileComparing() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Boolean> to = Observable.sequenceEqual(ps1, ps2, (a, b) -> {
            ps1.onNext(1);
            ps1.onComplete();
            ps2.onNext(1);
            ps2.onComplete();
            return a.equals(b);
        }).test();
        ps1.onNext(0);
        ps2.onNext(0);
        to.assertResult(true);
    }

    @Test
    public void bothCompleteWhileComparingAsObservable() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Boolean> to = Observable.sequenceEqual(ps1, ps2, (a, b) -> {
            ps1.onNext(1);
            ps1.onComplete();
            ps2.onNext(1);
            ps2.onComplete();
            return a.equals(b);
        }).toObservable().test();
        ps1.onNext(0);
        ps2.onNext(0);
        to.assertResult(true);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observable1, this.description("observable1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observable2, this.description("observable2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observable3, this.description("observable3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError1Observable, this.description("withError1Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError2Observable, this.description("withError2Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError3Observable, this.description("withError3Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty1Observable, this.description("withEmpty1Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty2Observable, this.description("withEmpty2Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3Observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty3Observable, this.description("withEmpty3Observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEqualityErrorObservable, this.description("withEqualityErrorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetchObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::prefetchObservable, this.description("prefetchObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedObservable, this.description("disposedObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_one() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::one, this.description("one"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_two() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::two, this.description("two"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_three() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::three, this.description("three"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError1, this.description("withError1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError2, this.description("withError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError3, this.description("withError3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty1, this.description("withEmpty1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty2, this.description("withEmpty2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty3, this.description("withEmpty3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEqualityError, this.description("withEqualityError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::prefetch, this.description("prefetch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleInequal, this.description("simpleInequal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequalObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleInequalObservable, this.description("simpleInequalObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextCancelRace, this.description("onNextCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRaceObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextCancelRaceObservable, this.description("onNextCancelRaceObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletesBeforeSecond() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstCompletesBeforeSecond, this.description("firstCompletesBeforeSecond"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondCompletesBeforeFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::secondCompletesBeforeFirst, this.description("secondCompletesBeforeFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothEmpty, this.description("bothEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothJust, this.description("bothJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothCompleteWhileComparing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothCompleteWhileComparing, this.description("bothCompleteWhileComparing"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothCompleteWhileComparingAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothCompleteWhileComparingAsObservable, this.description("bothCompleteWhileComparingAsObservable"));
        }

        private ObservableSequenceEqualTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSequenceEqualTest();
        }

        @java.lang.Override
        public ObservableSequenceEqualTest implementation() {
            return this.implementation;
        }
    }
}
