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
import java.util.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDoFinallyTest extends RxJavaTest implements Action {

    int calls;

    @Override
    public void run() throws Exception {
        calls++;
    }

    @Test
    public void normalJust() {
        Flowable.just(1).doFinally(this).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmpty() {
        Flowable.empty().doFinally(this).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalError() {
        Flowable.error(new TestException()).doFinally(this).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTake() {
        Flowable.range(1, 10).doFinally(this).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.doFinally(FlowableDoFinallyTest.this);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.doFinally(FlowableDoFinallyTest.this).filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void syncFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doFinally(this).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundary() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Flowable.range(1, 5).doFinally(this).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doFinally(this).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundary() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doFinally(this).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void normalJustConditional() {
        Flowable.just(1).doFinally(this).filter(Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmptyConditional() {
        Flowable.empty().doFinally(this).filter(Functions.alwaysTrue()).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalErrorConditional() {
        Flowable.error(new TestException()).doFinally(this).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTakeConditional() {
        Flowable.range(1, 10).doFinally(this).filter(Functions.alwaysTrue()).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedConditional() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doFinally(this).compose(TestHelper.conditional()).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).hide().doFinally(this).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFusedConditional() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).hide().doFinally(this).compose(TestHelper.conditional()).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundaryConditional() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Flowable.range(1, 5).doFinally(this).compose(TestHelper.conditional()).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedConditional() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doFinally(this).compose(TestHelper.conditional()).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundaryConditional() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doFinally(this).compose(TestHelper.conditional()).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1).cancel();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void actionThrowsConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertResult(1).cancel();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void clearIsEmpty() {
        Flowable.range(1, 5).doFinally(this).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                qs.requestFusion(QueueFuseable.ANY);
                assertFalse(qs.isEmpty());
                try {
                    assertEquals(1, qs.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                qs.cancel();
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
        assertEquals(1, calls);
    }

    @Test
    public void clearIsEmptyConditional() {
        Flowable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                qs.requestFusion(QueueFuseable.ANY);
                assertFalse(qs.isEmpty());
                try {
                    assertEquals(1, qs.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                qs.cancel();
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
        assertEquals(1, calls);
    }

    @Test
    public void eventOrdering() {
        final List<String> list = new ArrayList<>();
        Flowable.error(new TestException()).doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                list.add("cancel");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onError", "finally"), list);
    }

    @Test
    public void eventOrdering2() {
        final List<String> list = new ArrayList<>();
        Flowable.just(1).doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                list.add("cancel");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onNext", "onComplete", "finally"), list);
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        TestHelper.rejectFlowableFusion().doFinally(() -> {
        }).subscribeWith(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE);
    }

    @Test
    public void fusionRejectedConditional() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        TestHelper.rejectFlowableFusion().doFinally(() -> {
        }).compose(TestHelper.conditional()).subscribeWith(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJust, this.description("normalJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmpty, this.description("normalEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalError, this.description("normalError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalTake, this.description("normalTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFused, this.description("syncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundary() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedBoundary, this.description("syncFusedBoundary"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFused, this.description("asyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundary() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedBoundary, this.description("asyncFusedBoundary"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJustConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJustConditional, this.description("normalJustConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmptyConditional, this.description("normalEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalErrorConditional, this.description("normalErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTakeConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalTakeConditional, this.description("normalTakeConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedConditional, this.description("syncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonFused, this.description("nonFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonFusedConditional, this.description("nonFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundaryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedBoundaryConditional, this.description("syncFusedBoundaryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedConditional, this.description("asyncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundaryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedBoundaryConditional, this.description("asyncFusedBoundaryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrows, this.description("actionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrowsConditional, this.description("actionThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearIsEmpty, this.description("clearIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearIsEmptyConditional, this.description("clearIsEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eventOrdering, this.description("eventOrdering"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eventOrdering2, this.description("eventOrdering2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRejected, this.description("fusionRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejectedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRejectedConditional, this.description("fusionRejectedConditional"));
        }

        private FlowableDoFinallyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableDoFinallyTest();
        }

        @java.lang.Override
        public FlowableDoFinallyTest implementation() {
            return this.implementation;
        }
    }
}
