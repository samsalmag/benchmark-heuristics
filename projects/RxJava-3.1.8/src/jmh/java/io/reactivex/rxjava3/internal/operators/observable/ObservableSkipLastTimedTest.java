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
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipLastTimedTest extends RxJavaTest {

    @Test
    public void skipLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        // FIXME the timeunit now matters due to rounding
        Observable<Integer> result = source.skipLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        scheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o, never()).onNext(4);
        inOrder.verify(o, never()).onNext(5);
        inOrder.verify(o, never()).onNext(6);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedErrorBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o, never()).onNext(any());
    }

    @Test
    public void skipLastTimedCompleteBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedWhenAllElementsAreValid() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void skipLastTimedDefaultScheduler() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedDefaultSchedulerDelayError() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedCustomSchedulerDelayError() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, Schedulers.io(), true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().skipLast(1, TimeUnit.DAYS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.skipLast(1, TimeUnit.DAYS);
            }
        });
    }

    @Test
    public void onCompleteDisposeRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onCompleteDisposeDelayErrorRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void errorDelayed() {
        Observable.error(new TestException()).skipLast(1, TimeUnit.DAYS, new TestScheduler(), true).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Observable.just(1).skipLast(0, TimeUnit.SECONDS).take(1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void onNextDisposeRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onNextOnCompleteDisposeDelayErrorRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void skipLastTimedDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        // FIXME the timeunit now matters due to rounding
        Observable<Integer> result = source.skipLast(1000, TimeUnit.MILLISECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        scheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o, never()).onNext(4);
        inOrder.verify(o, never()).onNext(5);
        inOrder.verify(o, never()).onNext(6);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedErrorBeforeTimeDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o, never()).onNext(any());
    }

    @Test
    public void skipLastTimedCompleteBeforeTimeDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedWhenAllElementsAreValidDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.MILLISECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimed, this.description("skipLastTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedErrorBeforeTime() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedErrorBeforeTime, this.description("skipLastTimedErrorBeforeTime"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCompleteBeforeTime() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedCompleteBeforeTime, this.description("skipLastTimedCompleteBeforeTime"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedWhenAllElementsAreValid() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedWhenAllElementsAreValid, this.description("skipLastTimedWhenAllElementsAreValid"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedDefaultScheduler, this.description("skipLastTimedDefaultScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultSchedulerDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedDefaultSchedulerDelayError, this.description("skipLastTimedDefaultSchedulerDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCustomSchedulerDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedCustomSchedulerDelayError, this.description("skipLastTimedCustomSchedulerDelayError"));
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
        public void benchmark_onCompleteDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteDisposeRace, this.description("onCompleteDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteDisposeDelayErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteDisposeDelayErrorRace, this.description("onCompleteDisposeDelayErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed, this.description("errorDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextDisposeRace, this.description("onNextDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnCompleteDisposeDelayErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextOnCompleteDisposeDelayErrorRace, this.description("onNextOnCompleteDisposeDelayErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedDelayError, this.description("skipLastTimedDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedErrorBeforeTimeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedErrorBeforeTimeDelayError, this.description("skipLastTimedErrorBeforeTimeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCompleteBeforeTimeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedCompleteBeforeTimeDelayError, this.description("skipLastTimedCompleteBeforeTimeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedWhenAllElementsAreValidDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipLastTimedWhenAllElementsAreValidDelayError, this.description("skipLastTimedWhenAllElementsAreValidDelayError"));
        }

        private ObservableSkipLastTimedTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableSkipLastTimedTest();
        }

        @java.lang.Override
        public ObservableSkipLastTimedTest implementation() {
            return this.implementation;
        }
    }
}
