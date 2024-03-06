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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFlatMapStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        Flowable.empty().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertResult();
    }

    @Test
    public void emptyHidden() {
        Flowable.empty().hide().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertResult();
    }

    @Test
    public void just() {
        Flowable.just(1).flatMapStream(v -> Stream.of(v + 1, v + 2, v + 3, v + 4, v + 5)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void justHidden() {
        Flowable.just(1).hide().flatMapStream(v -> Stream.of(v + 1, v + 2, v + 3, v + 4, v + 5)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierFusedError() {
        Flowable.fromCallable(() -> {
            throw new TestException();
        }).flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void errorHidden() {
        Flowable.error(new TestException()).hide().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void range() {
        Flowable.range(1, 5).flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 50, 51, 52, 53, 54);
    }

    @Test
    public void rangeHidden() {
        Flowable.range(1, 5).hide().flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 50, 51, 52, 53, 54);
    }

    @Test
    public void rangeToEmpty() {
        Flowable.range(1, 5).flatMapStream(v -> Stream.of()).test().assertResult();
    }

    @Test
    public void rangeTake() {
        Flowable.range(1, 5).flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).take(12).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31);
    }

    @Test
    public void rangeTakeHidden() {
        Flowable.range(1, 5).hide().flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).take(12).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31);
    }

    @Test
    public void upstreamCancelled() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        AtomicInteger calls = new AtomicInteger();
        TestSubscriber<Integer> ts = pp.flatMapStream(v -> Stream.of(v + 1, v + 2).onClose(() -> calls.getAndIncrement())).test(1);
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertValuesOnly(2);
        ts.cancel();
        assertFalse(pp.hasSubscribers());
        assertEquals(1, calls.get());
    }

    @Test
    public void upstreamCancelledCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.flatMapStream(v -> Stream.of(v + 1, v + 2).onClose(() -> {
                throw new TestException();
            })).test(1);
            assertTrue(pp.hasSubscribers());
            pp.onNext(1);
            ts.assertValuesOnly(2);
            ts.cancel();
            assertFalse(pp.hasSubscribers());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void crossMap() {
        Flowable.range(1, 1000).flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).test().assertValueCount(1_000_000).assertNoErrors().assertComplete();
    }

    @Test
    public void crossMapHidden() {
        Flowable.range(1, 1000).hide().flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).test().assertValueCount(1_000_000).assertNoErrors().assertComplete();
    }

    @Test
    public void crossMapBackpressured() {
        for (int n = 1; n < 2048; n *= 2) {
            Flowable.range(1, 1000).flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).rebatchRequests(n).test().withTag("rebatch: " + n).assertValueCount(1_000_000).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void crossMapBackpressuredHidden() {
        for (int n = 1; n < 2048; n *= 2) {
            Flowable.range(1, 1000).hide().flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).rebatchRequests(n).test().withTag("rebatch: " + n).assertValueCount(1_000_000).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.flatMapStream(v -> Stream.of(1, 2)));
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(UnicastProcessor.create().flatMapStream(v -> Stream.of(1, 2)));
    }

    @Test
    public void queueOverflow() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new TestException());
                }
            }.flatMapStream(v -> Stream.of(1, 2), 1).test(0).assertFailure(QueueOverflowException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void mapperThrows() {
        Flowable.just(1).hide().concatMapStream(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperNull() {
        Flowable.just(1).hide().concatMapStream(v -> null).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void streamNull() {
        Flowable.just(1).hide().concatMapStream(v -> Stream.of(1, null)).test().assertFailure(NullPointerException.class, 1);
    }

    @Test
    public void hasNextThrows() {
        Flowable.just(1).hide().concatMapStream(v -> Stream.generate(() -> {
            throw new TestException();
        })).test().assertFailure(TestException.class);
    }

    @Test
    public void hasNextThrowsLater() {
        AtomicInteger counter = new AtomicInteger();
        Flowable.just(1).hide().concatMapStream(v -> Stream.generate(() -> {
            if (counter.getAndIncrement() == 0) {
                return 1;
            }
            throw new TestException();
        })).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mapperThrowsWhenUpstreamErrors() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            AtomicInteger counter = new AtomicInteger();
            TestSubscriber<Integer> ts = pp.hide().concatMapStream(v -> {
                if (counter.getAndIncrement() == 0) {
                    return Stream.of(1, 2);
                }
                pp.onError(new IOException());
                throw new TestException();
            }).test();
            pp.onNext(1);
            pp.onNext(2);
            ts.assertFailure(IOException.class, 1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void rangeBackpressured() {
        Flowable.range(1, 5).hide().concatMapStream(v -> Stream.of(v), 1).test(0).assertEmpty().requestMore(5).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void cancelAfterIteratorNext() throws Exception {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                ts.cancel();
                return 1;
            }
        });
        Flowable.just(1).hide().concatMapStream(v -> stream).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void asyncUpstreamFused() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestSubscriber<Integer> ts = up.flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(up.hasSubscribers());
        up.onNext(1);
        ts.assertValuesOnly(1, 2);
        up.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void asyncUpstreamFusionBoundary() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestSubscriber<Integer> ts = up.map(v -> v + 1).flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(up.hasSubscribers());
        up.onNext(1);
        ts.assertValuesOnly(1, 2);
        up.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void fusedPollCrash() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestSubscriber<Integer> ts = up.map(v -> {
            throw new TestException();
        }).compose(TestHelper.flowableStripBoundary()).flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(up.hasSubscribers());
        up.onNext(1);
        assertFalse(up.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyHidden, this.description("emptyHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justHidden, this.description("justHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierFusedError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::supplierFusedError, this.description("supplierFusedError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorHidden, this.description("errorHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::range, this.description("range"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeHidden, this.description("rangeHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeToEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeToEmpty, this.description("rangeToEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeTake, this.description("rangeTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeTakeHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeTakeHidden, this.description("rangeTakeHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamCancelled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamCancelled, this.description("upstreamCancelled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamCancelledCloseCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamCancelledCloseCrash, this.description("upstreamCancelledCloseCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crossMap, this.description("crossMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMapHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crossMapHidden, this.description("crossMapHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMapBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crossMapBackpressured, this.description("crossMapBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMapBackpressuredHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crossMapBackpressuredHidden, this.description("crossMapBackpressuredHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribe, this.description("onSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueOverflow, this.description("queueOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrows, this.description("mapperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperNull, this.description("mapperNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::streamNull, this.description("streamNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrows, this.description("hasNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsLater() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrowsLater, this.description("hasNextThrowsLater"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsWhenUpstreamErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrowsWhenUpstreamErrors, this.description("mapperThrowsWhenUpstreamErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeBackpressured, this.description("rangeBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterIteratorNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAfterIteratorNext, this.description("cancelAfterIteratorNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncUpstreamFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncUpstreamFused, this.description("asyncUpstreamFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncUpstreamFusionBoundary() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncUpstreamFusionBoundary, this.description("asyncUpstreamFusionBoundary"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedPollCrash, this.description("fusedPollCrash"));
        }

        private FlowableFlatMapStreamTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFlatMapStreamTest();
        }

        @java.lang.Override
        public FlowableFlatMapStreamTest implementation() {
            return this.implementation;
        }
    }
}
