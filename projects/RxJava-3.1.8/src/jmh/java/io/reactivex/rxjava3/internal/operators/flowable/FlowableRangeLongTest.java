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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableRangeLongTest extends RxJavaTest {

    @Test
    public void rangeStartAt2Count3() {
        Subscriber<Long> subscriber = TestHelper.mockSubscriber();
        Flowable.rangeLong(2, 3).subscribe(subscriber);
        verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, times(1)).onNext(3L);
        verify(subscriber, times(1)).onNext(4L);
        verify(subscriber, never()).onNext(5L);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void rangeUnsubscribe() {
        Subscriber<Long> subscriber = TestHelper.mockSubscriber();
        final AtomicInteger count = new AtomicInteger();
        Flowable.rangeLong(1, 1000).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                count.incrementAndGet();
            }
        }).take(3).subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1L);
        verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, times(1)).onNext(3L);
        verify(subscriber, never()).onNext(4L);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        assertEquals(3, count.get());
    }

    @Test
    public void rangeWithZero() {
        Flowable.rangeLong(1, 0);
    }

    @Test
    public void rangeWithOverflow2() {
        Flowable.rangeLong(Long.MAX_VALUE, 0);
    }

    @Test
    public void rangeWithOverflow3() {
        Flowable.rangeLong(1, Long.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeWithOverflow4() {
        Flowable.rangeLong(2, Long.MAX_VALUE);
    }

    @Test
    public void rangeWithOverflow5() {
        assertFalse(Flowable.rangeLong(Long.MIN_VALUE, 0).blockingIterable().iterator().hasNext());
    }

    @Test
    public void backpressureViaRequest() {
        Flowable<Long> f = Flowable.rangeLong(1, Flowable.bufferSize());
        TestSubscriberEx<Long> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        ts.request(1);
        f.subscribe(ts);
        ts.assertValue(1L);
        ts.request(2);
        ts.assertValues(1L, 2L, 3L);
        ts.request(3);
        ts.assertValues(1L, 2L, 3L, 4L, 5L, 6L);
        ts.request(Flowable.bufferSize());
        ts.assertTerminated();
    }

    @Test
    public void noBackpressure() {
        ArrayList<Long> list = new ArrayList<>(Flowable.bufferSize() * 2);
        for (long i = 1; i <= Flowable.bufferSize() * 2 + 1; i++) {
            list.add(i);
        }
        Flowable<Long> f = Flowable.rangeLong(1, list.size());
        TestSubscriberEx<Long> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        // infinite
        ts.request(Long.MAX_VALUE);
        f.subscribe(ts);
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    void withBackpressureOneByOne(long start) {
        Flowable<Long> source = Flowable.rangeLong(start, 100);
        TestSubscriberEx<Long> ts = new TestSubscriberEx<>(0L);
        ts.request(1);
        source.subscribe(ts);
        List<Long> list = new ArrayList<>(100);
        for (long i = 0; i < 100; i++) {
            list.add(i + start);
            ts.request(1);
        }
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    void withBackpressureAllAtOnce(long start) {
        Flowable<Long> source = Flowable.rangeLong(start, 100);
        TestSubscriberEx<Long> ts = new TestSubscriberEx<>(0L);
        ts.request(100);
        source.subscribe(ts);
        List<Long> list = new ArrayList<>(100);
        for (long i = 0; i < 100; i++) {
            list.add(i + start);
        }
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    @Test
    public void withBackpressure1() {
        for (long i = 0; i < 100; i++) {
            withBackpressureOneByOne(i);
        }
    }

    @Test
    public void withBackpressureAllAtOnce() {
        for (long i = 0; i < 100; i++) {
            withBackpressureAllAtOnce(i);
        }
    }

    @Test
    public void withBackpressureRequestWayMore() {
        Flowable<Long> source = Flowable.rangeLong(50, 100);
        TestSubscriberEx<Long> ts = new TestSubscriberEx<>(0L);
        ts.request(150);
        source.subscribe(ts);
        List<Long> list = new ArrayList<>(100);
        for (long i = 0; i < 100; i++) {
            list.add(i + 50);
        }
        // and then some
        ts.request(50);
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    @Test
    public void requestOverflow() {
        final AtomicInteger count = new AtomicInteger();
        int n = 10;
        Flowable.rangeLong(1, n).subscribe(new DefaultSubscriber<Long>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(Long t) {
                count.incrementAndGet();
                request(Long.MAX_VALUE - 1);
            }
        });
        assertEquals(n, count.get());
    }

    @Test
    public void emptyRangeSendsOnCompleteEagerlyWithRequestZero() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Flowable.rangeLong(1, 0).subscribe(new DefaultSubscriber<Long>() {

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
        TestSubscriber<Long> ts = new TestSubscriber<>();
        Flowable.rangeLong(Long.MAX_VALUE - 1L, 2L).subscribe(ts);
        ts.assertComplete();
        ts.assertNoErrors();
        ts.assertValues(Long.MAX_VALUE - 1L, Long.MAX_VALUE);
    }

    @Test
    public void nearMaxValueWithBackpressure() {
        TestSubscriber<Long> ts = new TestSubscriber<>(3L);
        Flowable.rangeLong(Long.MAX_VALUE - 1L, 2L).subscribe(ts);
        ts.assertComplete();
        ts.assertNoErrors();
        ts.assertValues(Long.MAX_VALUE - 1L, Long.MAX_VALUE);
    }

    @Test
    public void negativeCount() {
        try {
            Flowable.rangeLong(1L, -1L);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void countOne() {
        Flowable.rangeLong(5495454L, 1L).test().assertResult(5495454L);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Long> ts = new TestSubscriberEx<Long>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.rangeLong(1, 2).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1L, 2L);
    }

    @Test
    public void fusedReject() {
        TestSubscriberEx<Long> ts = new TestSubscriberEx<Long>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.rangeLong(1, 2).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1L, 2L);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.rangeLong(1, 2));
    }

    @Test
    public void fusedClearIsEmpty() {
        TestHelper.checkFusedIsEmptyClear(Flowable.rangeLong(1, 2));
    }

    @Test
    public void noOverflow() {
        Flowable.rangeLong(Long.MAX_VALUE - 1, 2);
        Flowable.rangeLong(Long.MIN_VALUE, 2);
        Flowable.rangeLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Test
    public void conditionalNormal() {
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).test().assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.rangeLong(1L, 5L));
        TestHelper.assertBadRequestReported(Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()));
    }

    @Test
    public void conditionalNormalSlowpath() {
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).test(5).assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void conditionalSlowPathTakeExact() {
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).take(5).test().assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void slowPathTakeExact() {
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).take(5).test().assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void conditionalSlowPathRebatch() {
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).rebatchRequests(1).test().assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void slowPathRebatch() {
        Flowable.rangeLong(1L, 5L).rebatchRequests(1).test().assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void slowPathCancel() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>(2L) {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.rangeLong(1L, 5L).subscribe(ts);
        ts.assertResult(1L);
    }

    @Test
    public void fastPathCancel() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.rangeLong(1L, 5L).subscribe(ts);
        ts.assertResult(1L);
    }

    @Test
    public void conditionalSlowPathCancel() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>(1L) {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1L);
    }

    @Test
    public void conditionalFastPathCancel() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1L);
    }

    @Test
    public void conditionalRequestOneByOne() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>(1L) {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.rangeLong(1L, 5L).filter(new Predicate<Long>() {

            @Override
            public boolean test(Long v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertResult(2L, 4L);
    }

    @Test
    public void conditionalRequestOneByOne2() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>(1L) {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.rangeLong(1L, 5L).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void fastPathCancelExact() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                if (t == 5L) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.rangeLong(1L, 5L).subscribe(ts);
        ts.assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void conditionalFastPathCancelExact() {
        TestSubscriber<Long> ts = new TestSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                super.onNext(t);
                if (t == 5L) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.rangeLong(1L, 5L).filter(new Predicate<Long>() {

            @Override
            public boolean test(Long v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertResult(2L, 4L);
    }

    @Test
    public void slowPathCancelBeforeComplete() {
        Flowable.rangeLong(1, 2).take(2).test().assertResult(1L, 2L);
    }

    @Test
    public void conditionalFastPathCancelBeforeComplete() {
        TestSubscriber<Long> ts = new TestSubscriber<>();
        Flowable.rangeLong(1, 2).compose(TestHelper.conditional()).doOnNext(v -> {
            if (v == 2L) {
                ts.cancel();
            }
        }).subscribe(ts);
        ts.assertValuesOnly(1L, 2L);
    }

    @Test
    public void conditionalSlowPathTake() {
        TestSubscriber<Long> ts = new TestSubscriber<>(4);
        Flowable.rangeLong(1, 3).compose(TestHelper.conditional()).doOnNext(v -> {
            if (v == 2L) {
                ts.cancel();
            }
        }).subscribe(ts);
        ts.assertValuesOnly(1L, 2L);
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
        public void benchmark_backpressureViaRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureViaRequest, this.description("backpressureViaRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noBackpressure, this.description("noBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressure1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withBackpressure1, this.description("withBackpressure1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressureAllAtOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withBackpressureAllAtOnce, this.description("withBackpressureAllAtOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressureRequestWayMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withBackpressureRequestWayMore, this.description("withBackpressureRequestWayMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestOverflow, this.description("requestOverflow"));
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
        public void benchmark_nearMaxValueWithBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nearMaxValueWithBackpressure, this.description("nearMaxValueWithBackpressure"));
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noOverflow, this.description("noOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalNormal, this.description("conditionalNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormalSlowpath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalNormalSlowpath, this.description("conditionalNormalSlowpath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathTakeExact() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathTakeExact, this.description("conditionalSlowPathTakeExact"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathTakeExact() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::slowPathTakeExact, this.description("slowPathTakeExact"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathRebatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathRebatch, this.description("conditionalSlowPathRebatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathRebatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::slowPathRebatch, this.description("slowPathRebatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::slowPathCancel, this.description("slowPathCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fastPathCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fastPathCancel, this.description("fastPathCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathCancel, this.description("conditionalSlowPathCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPathCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFastPathCancel, this.description("conditionalFastPathCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalRequestOneByOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalRequestOneByOne, this.description("conditionalRequestOneByOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalRequestOneByOne2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalRequestOneByOne2, this.description("conditionalRequestOneByOne2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fastPathCancelExact() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fastPathCancelExact, this.description("fastPathCancelExact"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPathCancelExact() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFastPathCancelExact, this.description("conditionalFastPathCancelExact"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathCancelBeforeComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::slowPathCancelBeforeComplete, this.description("slowPathCancelBeforeComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPathCancelBeforeComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFastPathCancelBeforeComplete, this.description("conditionalFastPathCancelBeforeComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalSlowPathTake, this.description("conditionalSlowPathTake"));
        }

        private FlowableRangeLongTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableRangeLongTest();
        }

        @java.lang.Override
        public FlowableRangeLongTest implementation() {
            return this.implementation;
        }
    }
}
