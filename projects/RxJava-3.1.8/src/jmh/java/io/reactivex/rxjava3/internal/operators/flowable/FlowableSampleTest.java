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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSampleTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Subscriber<Long> subscriber;

    private Subscriber<Object> subscriber2;

    @Before
    public // due to mocking
    void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        subscriber = TestHelper.mockSubscriber();
        subscriber2 = TestHelper.mockSubscriber();
    }

    @Test
    public void sample() {
        Flowable<Long> source = Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber1) {
                subscriber1.onSubscribe(new BooleanSubscription());
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onNext(1L);
                    }
                }, 1, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onNext(2L);
                    }
                }, 2, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onComplete();
                    }
                }, 3, TimeUnit.SECONDS);
            }
        });
        Flowable<Long> sampled = source.sample(400L, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(800L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(any(Long.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1200L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        verify(subscriber, never()).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1600L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        verify(subscriber, never()).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        inOrder.verify(subscriber, never()).onNext(2L);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNormal() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onNext(4);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNoDuplicates() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onNext(4);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerTerminatingEarly() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onComplete();
        source.onNext(3);
        source.onNext(4);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, times(1)).onComplete();
        inOrder.verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmitAndTerminate() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onComplete();
        sampler.onNext(2);
        sampler.onComplete();
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onComplete();
        inOrder.verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmptySource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onComplete();
        sampler.onNext(1);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerSourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onError(new RuntimeException("Forced failure!"));
        sampler.onNext(1);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onError(any(Throwable.class));
        verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sampleWithSamplerThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        sampler.onNext(1);
        sampler.onError(new RuntimeException("Forced failure!"));
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onNext(1);
        inOrder.verify(subscriber2, times(1)).onError(any(RuntimeException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sampleUnsubscribe() {
        final Subscription s = mock(Subscription.class);
        Flowable<Integer> f = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(s);
            }
        });
        f.throttleLast(1, TimeUnit.MILLISECONDS).subscribe().dispose();
        verify(s).cancel();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().sample(1, TimeUnit.SECONDS, new TestScheduler()));
        TestHelper.checkDisposed(PublishProcessor.create().sample(Flowable.never()));
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).sample(1, TimeUnit.SECONDS).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureOverflow() {
        BehaviorProcessor.createDefault(1).sample(1, TimeUnit.MILLISECONDS).test(0L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void backpressureOverflowWithOtherPublisher() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp1.sample(pp2).test(0L);
        pp1.onNext(1);
        pp2.onNext(2);
        ts.assertFailure(MissingBackpressureException.class);
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void emitLastTimed() {
        Flowable.just(1).sample(1, TimeUnit.DAYS, true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedEmpty() {
        Flowable.empty().sample(1, TimeUnit.DAYS, true).test().assertResult();
    }

    @Test
    public void emitLastTimedCustomScheduler() {
        Flowable.just(1).sample(1, TimeUnit.DAYS, Schedulers.single(), true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler scheduler = new TestScheduler();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(1, TimeUnit.SECONDS, scheduler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void emitLastOther() {
        Flowable.just(1).sample(Flowable.timer(1, TimeUnit.DAYS), true).test().assertResult(1);
    }

    @Test
    public void emitLastOtherEmpty() {
        Flowable.empty().sample(Flowable.timer(1, TimeUnit.DAYS), true).test().assertResult();
    }

    @Test
    public void emitLastOtherRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final PublishProcessor<Integer> sampler = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(sampler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void emitLastOtherCompleteCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final PublishProcessor<Integer> sampler = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(sampler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.sample(1, TimeUnit.SECONDS);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.sample(PublishProcessor.create());
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().sample(PublishProcessor.create()));
    }

    @Test
    public void badRequestTimed() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().sample(1, TimeUnit.MINUTES));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sample() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sample, this.description("sample"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerNormal, this.description("sampleWithSamplerNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNoDuplicates() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerNoDuplicates, this.description("sampleWithSamplerNoDuplicates"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerTerminatingEarly() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerTerminatingEarly, this.description("sampleWithSamplerTerminatingEarly"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmitAndTerminate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerEmitAndTerminate, this.description("sampleWithSamplerEmitAndTerminate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerEmptySource, this.description("sampleWithSamplerEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerSourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerSourceThrows, this.description("sampleWithSamplerSourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleWithSamplerThrows, this.description("sampleWithSamplerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleUnsubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sampleUnsubscribe, this.description("sampleUnsubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureOverflow, this.description("backpressureOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflowWithOtherPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureOverflowWithOtherPublisher, this.description("backpressureOverflowWithOtherPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastTimed, this.description("emitLastTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastTimedEmpty, this.description("emitLastTimedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedCustomScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastTimedCustomScheduler, this.description("emitLastTimedCustomScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedRunCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastTimedRunCompleteRace, this.description("emitLastTimedRunCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastOther, this.description("emitLastOther"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastOtherEmpty, this.description("emitLastOtherEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherRunCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastOtherRunCompleteRace, this.description("emitLastOtherRunCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherCompleteCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitLastOtherCompleteCompleteRace, this.description("emitLastOtherCompleteCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequestTimed, this.description("badRequestTimed"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FlowableSampleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableSampleTest();
        }

        @java.lang.Override
        public FlowableSampleTest implementation() {
            return this.implementation;
        }
    }
}
