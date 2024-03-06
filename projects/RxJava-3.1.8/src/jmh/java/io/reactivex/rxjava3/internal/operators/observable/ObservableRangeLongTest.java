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
import java.util.ArrayList;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableRangeLongTest extends RxJavaTest {

    @Test
    public void rangeStartAt2Count3() {
        Observer<Long> observer = TestHelper.mockObserver();
        Observable.rangeLong(2, 3).subscribe(observer);
        verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onNext(3L);
        verify(observer, times(1)).onNext(4L);
        verify(observer, never()).onNext(5L);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void rangeUnsubscribe() {
        Observer<Long> observer = TestHelper.mockObserver();
        final AtomicInteger count = new AtomicInteger();
        Observable.rangeLong(1, 1000).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                count.incrementAndGet();
            }
        }).take(3).subscribe(observer);
        verify(observer, times(1)).onNext(1L);
        verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onNext(3L);
        verify(observer, never()).onNext(4L);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        assertEquals(3, count.get());
    }

    @Test
    public void rangeWithZero() {
        Observable.rangeLong(1L, 0L);
    }

    @Test
    public void rangeWithOverflow2() {
        Observable.rangeLong(Long.MAX_VALUE, 0L);
    }

    @Test
    public void rangeWithOverflow3() {
        Observable.rangeLong(1L, Long.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeWithOverflow4() {
        Observable.rangeLong(2L, Long.MAX_VALUE);
    }

    @Test
    public void rangeWithOverflow5() {
        assertFalse(Observable.rangeLong(Long.MIN_VALUE, 0).blockingIterable().iterator().hasNext());
    }

    @Test
    public void noBackpressure() {
        ArrayList<Long> list = new ArrayList<>(Flowable.bufferSize() * 2);
        for (long i = 1; i <= Flowable.bufferSize() * 2 + 1; i++) {
            list.add(i);
        }
        Observable<Long> o = Observable.rangeLong(1, list.size());
        TestObserverEx<Long> to = new TestObserverEx<>();
        o.subscribe(to);
        to.assertValueSequence(list);
        to.assertTerminated();
    }

    @Test
    public void emptyRangeSendsOnCompleteEagerlyWithRequestZero() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Observable.rangeLong(1L, 0L).subscribe(new DefaultObserver<Long>() {

            @Override
            public void onStart() {
                // request(0);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Long t) {
            }
        });
        assertTrue(completed.get());
    }

    @Test
    public void nearMaxValueWithoutBackpressure() {
        TestObserver<Long> to = new TestObserver<>();
        Observable.rangeLong(Long.MAX_VALUE - 1L, 2L).subscribe(to);
        to.assertComplete();
        to.assertNoErrors();
        to.assertValues(Long.MAX_VALUE - 1, Long.MAX_VALUE);
    }

    @Test
    public void negativeCount() {
        try {
            Observable.rangeLong(1L, -1L);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void countOne() {
        Observable.rangeLong(5495454L, 1L).test().assertResult(5495454L);
    }

    @Test
    public void noOverflow() {
        Observable.rangeLong(Long.MAX_VALUE - 1, 2);
        Observable.rangeLong(Long.MIN_VALUE, 2);
        Observable.rangeLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Test
    public void fused() {
        TestObserverEx<Long> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.rangeLong(1, 2).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1L, 2L);
    }

    @Test
    public void fusedReject() {
        TestObserverEx<Long> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.rangeLong(1, 2).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1L, 2L);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.rangeLong(1, 2));
    }

    @Test
    public void fusedClearIsEmpty() {
        TestHelper.checkFusedIsEmptyClear(Observable.rangeLong(1, 2));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeStartAt2Count3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeStartAt2Count3, this.description("rangeStartAt2Count3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeUnsubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeUnsubscribe, this.description("rangeUnsubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithZero() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeWithZero, this.description("rangeWithZero"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeWithOverflow2, this.description("rangeWithOverflow2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeWithOverflow3, this.description("rangeWithOverflow3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow4() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::rangeWithOverflow4, this.description("rangeWithOverflow4"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow5() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeWithOverflow5, this.description("rangeWithOverflow5"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noBackpressure, this.description("noBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyRangeSendsOnCompleteEagerlyWithRequestZero() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyRangeSendsOnCompleteEagerlyWithRequestZero, this.description("emptyRangeSendsOnCompleteEagerlyWithRequestZero"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nearMaxValueWithoutBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nearMaxValueWithoutBackpressure, this.description("nearMaxValueWithoutBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_negativeCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::negativeCount, this.description("negativeCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countOne, this.description("countOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noOverflow, this.description("noOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedReject, this.description("fusedReject"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedClearIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedClearIsEmpty, this.description("fusedClearIsEmpty"));
        }

        private ObservableRangeLongTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableRangeLongTest();
        }

        @java.lang.Override
        public ObservableRangeLongTest implementation() {
            return this.implementation;
        }
    }
}
