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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableIgnoreElementsTest extends RxJavaTest {

    @Test
    public void withEmptyFlowable() {
        assertTrue(Flowable.empty().ignoreElements().toFlowable().isEmpty().blockingGet());
    }

    @Test
    public void withNonEmptyFlowable() {
        assertTrue(Flowable.just(1, 2, 3).ignoreElements().toFlowable().isEmpty().blockingGet());
    }

    @Test
    public void upstreamIsProcessedButIgnoredFlowable() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        long count = Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().toFlowable().count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count);
    }

    @Test
    public void completedOkFlowable() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        Flowable.range(1, 10).ignoreElements().toFlowable().subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertTerminated();
    }

    @Test
    public void errorReceivedFlowable() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        TestException ex = new TestException("boo");
        Flowable.error(ex).ignoreElements().toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertTerminated();
        ts.assertError(TestException.class);
        ts.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstreamFlowable() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Flowable.range(1, 10).concatWith(Flowable.<Integer>never()).doOnCancel(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().toFlowable().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void doesNotHangAndProcessesAllUsingBackpressureFlowable() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final AtomicInteger count = new AtomicInteger(0);
        int num = 10;
        Flowable.range(1, num).// 
        doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).// 
        ignoreElements().<Integer>toFlowable().// 
        doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).// 
        subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
            }
        });
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count.get());
    }

    @Test
    public void withEmpty() {
        Flowable.empty().ignoreElements().blockingAwait();
    }

    @Test
    public void withNonEmpty() {
        Flowable.just(1, 2, 3).ignoreElements().blockingAwait();
    }

    @Test
    public void upstreamIsProcessedButIgnored() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().blockingAwait();
        assertEquals(num, upstreamCount.get());
    }

    @Test
    public void completedOk() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Flowable.range(1, 10).ignoreElements().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceived() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Flowable.error(ex).ignoreElements().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Flowable.range(1, 10).concatWith(Flowable.<Integer>never()).doOnCancel(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void doesNotHangAndProcessesAllUsingBackpressure() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final AtomicInteger count = new AtomicInteger(0);
        int num = 10;
        Flowable.range(1, num).// 
        doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).// 
        ignoreElements().// 
        subscribe(new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }
        });
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count.get());
    }

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.ignoreElements().<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1).hide().ignoreElements().<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void fusedAPICalls() {
        Flowable.just(1).hide().ignoreElements().<Integer>toFlowable().subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new AssertionError(ex);
                }
                assertTrue(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new AssertionError(ex);
                }
                try {
                    qs.offer(1);
                    fail("Should have thrown!");
                } catch (UnsupportedOperationException ex) {
                    // expected
                }
                try {
                    qs.offer(1, 2);
                    fail("Should have thrown!");
                } catch (UnsupportedOperationException ex) {
                    // expected
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).ignoreElements());
        TestHelper.checkDisposed(Flowable.just(1).ignoreElements().toFlowable());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.ignoreElements().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return f.ignoreElements();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmptyFlowable, this.description("withEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withNonEmptyFlowable, this.description("withNonEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnoredFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamIsProcessedButIgnoredFlowable, this.description("upstreamIsProcessedButIgnoredFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOkFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completedOkFlowable, this.description("completedOkFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceivedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorReceivedFlowable, this.description("errorReceivedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstreamFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribesFromUpstreamFlowable, this.description("unsubscribesFromUpstreamFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotHangAndProcessesAllUsingBackpressureFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesNotHangAndProcessesAllUsingBackpressureFlowable, this.description("doesNotHangAndProcessesAllUsingBackpressureFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty, this.description("withEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withNonEmpty, this.description("withNonEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnored() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamIsProcessedButIgnored, this.description("upstreamIsProcessedButIgnored"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOk() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completedOk, this.description("completedOk"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceived() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorReceived, this.description("errorReceived"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribesFromUpstream, this.description("unsubscribesFromUpstream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotHangAndProcessesAllUsingBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesNotHangAndProcessesAllUsingBackpressure, this.description("doesNotHangAndProcessesAllUsingBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAPICalls() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedAPICalls, this.description("fusedAPICalls"));
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

        private FlowableIgnoreElementsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableIgnoreElementsTest();
        }

        @java.lang.Override
        public FlowableIgnoreElementsTest implementation() {
            return this.implementation;
        }
    }
}
