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
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeLastTimedTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void takeLastTimedWithNegativeCount() {
        Observable.just("one").takeLast(-1, 1, TimeUnit.SECONDS);
    }

    @Test
    public void takeLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Object> source = PublishSubject.create();
        // FIXME time unit now matters!
        Observable<Object> result = source.takeLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(o, times(1)).onNext(2);
        inOrder.verify(o, times(1)).onNext(3);
        inOrder.verify(o, times(1)).onNext(4);
        inOrder.verify(o, times(1)).onNext(5);
        inOrder.verify(o, times(1)).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedDelayCompletion() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Object> source = PublishSubject.create();
        // FIXME time unit now matters
        Observable<Object> result = source.takeLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(1250, TimeUnit.MILLISECONDS);
        // T: 2250ms
        source.onComplete();
        inOrder.verify(o, times(1)).onComplete();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedWithCapacity() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Object> source = PublishSubject.create();
        // FIXME time unit now matters!
        Observable<Object> result = source.takeLast(2, 1000, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(o, times(1)).onNext(4);
        inOrder.verify(o, times(1)).onNext(5);
        inOrder.verify(o, times(1)).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedThrowingSource() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Object> source = PublishSubject.create();
        Observable<Object> result = source.takeLast(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onError(new TestException());
        inOrder.verify(o, times(1)).onError(any(TestException.class));
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void takeLastTimedWithZeroCapacity() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Object> source = PublishSubject.create();
        Observable<Object> result = source.takeLast(0, 1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(o, times(1)).onComplete();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimeAndSize() {
        Observable.just(1, 2).takeLast(1, 1, TimeUnit.MINUTES).test().assertResult(2);
    }

    @Test
    public void takeLastTime() {
        Observable.just(1, 2).takeLast(1, TimeUnit.MINUTES).test().assertResult(1, 2);
    }

    @Test
    public void takeLastTimeDelayError() {
        Observable.just(1, 2).concatWith(Observable.<Integer>error(new TestException())).takeLast(1, TimeUnit.MINUTES, true).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void takeLastTimeDelayErrorCustomScheduler() {
        Observable.just(1, 2).concatWith(Observable.<Integer>error(new TestException())).takeLast(1, TimeUnit.MINUTES, Schedulers.io(), true).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().takeLast(1, TimeUnit.MINUTES));
    }

    @Test
    public void observeOn() {
        Observable.range(1, 1000).takeLast(1, TimeUnit.DAYS).take(500).observeOn(Schedulers.single(), true, 1).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void cancelCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.takeLast(1, TimeUnit.DAYS).test();
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
    public void lastWindowIsFixedInTime() {
        TimesteppingScheduler scheduler = new TimesteppingScheduler();
        scheduler.stepEnabled = false;
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.takeLast(2, TimeUnit.SECONDS, scheduler).test();
        ps.onNext(1);
        ps.onNext(2);
        ps.onNext(3);
        ps.onNext(4);
        scheduler.stepEnabled = true;
        ps.onComplete();
        to.assertResult(1, 2, 3, 4);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.takeLast(1, TimeUnit.SECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithNegativeCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::takeLastTimedWithNegativeCount, this.description("takeLastTimedWithNegativeCount"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimed, this.description("takeLastTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedDelayCompletion() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimedDelayCompletion, this.description("takeLastTimedDelayCompletion"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimedWithCapacity, this.description("takeLastTimedWithCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedThrowingSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimedThrowingSource, this.description("takeLastTimedThrowingSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithZeroCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimedWithZeroCapacity, this.description("takeLastTimedWithZeroCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeAndSize() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimeAndSize, this.description("takeLastTimeAndSize"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTime() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTime, this.description("takeLastTime"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimeDelayError, this.description("takeLastTimeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeDelayErrorCustomScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTimeDelayErrorCustomScheduler, this.description("takeLastTimeDelayErrorCustomScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observeOn, this.description("observeOn"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelCompleteRace, this.description("cancelCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWindowIsFixedInTime() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWindowIsFixedInTime, this.description("lastWindowIsFixedInTime"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ObservableTakeLastTimedTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableTakeLastTimedTest();
        }

        @java.lang.Override
        public ObservableTakeLastTimedTest implementation() {
            return this.implementation;
        }
    }
}
