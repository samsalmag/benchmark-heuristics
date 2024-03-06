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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableMergeWithSingleTest extends RxJavaTest {

    @Test
    public void normal() {
        Flowable.range(1, 5).mergeWith(Single.just(100)).test().assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void normalLong() {
        Flowable.range(1, 512).mergeWith(Single.just(100)).test().assertValueCount(513).assertComplete();
    }

    @Test
    public void normalLongRequestExact() {
        Flowable.range(1, 512).mergeWith(Single.just(100)).test(513).assertValueCount(513).assertComplete();
    }

    @Test
    public void take() {
        Flowable.range(1, 5).mergeWith(Single.just(100)).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(cs.hasObservers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
    }

    @Test
    public void normalBackpressured() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.range(1, 5).mergeWith(Single.just(100)).subscribe(ts);
        ts.assertEmpty().requestMore(2).assertValues(100, 1).requestMore(2).assertValues(100, 1, 2, 3).requestMore(2).assertResult(100, 1, 2, 3, 4, 5);
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).mergeWith(Single.just(100)).test().assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        Flowable.never().mergeWith(Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void completeRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final SingleSubject<Integer> cs = SingleSubject.create();
            TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onSuccess(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1, 1);
        }
    }

    @Test
    public void onNextSlowPath() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onNext(2);
                }
            }
        });
        pp.onNext(1);
        cs.onSuccess(3);
        pp.onNext(4);
        pp.onComplete();
        ts.assertResult(1, 2, 3, 4);
    }

    @Test
    public void onSuccessSlowPath() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onNext(3);
        pp.onComplete();
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void onSuccessSlowPathBackpressured() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>(1) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onNext(3);
        pp.onComplete();
        ts.request(2);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void onSuccessFastPathBackpressuredRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final SingleSubject<Integer> cs = SingleSubject.create();
            final TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<>(0));
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cs.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(2);
                }
            };
            TestHelper.race(r1, r2);
            pp.onNext(2);
            pp.onComplete();
            ts.assertResult(1, 2);
        }
    }

    @Test
    public void onErrorMainOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Subscriber<?>> subscriber = new AtomicReference<>();
            TestSubscriber<Integer> ts = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    subscriber.set(s);
                }
            }.mergeWith(Single.<Integer>error(new IOException())).test();
            subscriber.get().onError(new TestException());
            ts.assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorOtherOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.error(new IOException()).mergeWith(Single.error(new TestException())).test().assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextRequestRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final SingleSubject<Integer> cs = SingleSubject.create();
            final TestSubscriber<Integer> ts = pp.mergeWith(cs).test(0);
            pp.onNext(0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(3);
                }
            };
            TestHelper.race(r1, r2);
            cs.onSuccess(1);
            pp.onComplete();
            ts.assertResult(0, 1, 1);
        }
    }

    @Test
    public void doubleOnSubscribeMain() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.mergeWith(Single.just(1));
            }
        });
    }

    @Test
    public void noRequestOnError() {
        Flowable.empty().mergeWith(Single.error(new TestException())).test(0).assertFailure(TestException.class);
    }

    @Test
    public void drainExactRequestCancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).take(2).subscribeWith(new TestSubscriber<Integer>(2) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onComplete();
        ts.request(2);
        ts.assertResult(1, 2);
    }

    @Test
    public void drainRequestWhenLimitReached() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    for (int i = 0; i < Flowable.bufferSize() - 1; i++) {
                        pp.onNext(i + 2);
                    }
                }
            }
        });
        cs.onSuccess(1);
        pp.onComplete();
        ts.request(2);
        ts.assertValueCount(Flowable.bufferSize());
        ts.assertComplete();
    }

    @Test
    public void cancelOtherOnMainError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(ss).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(ss.hasObservers());
        pp.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", ss.hasObservers());
    }

    @Test
    public void cancelMainOnOtherError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(ss).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(ss.hasObservers());
        ss.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", ss.hasObservers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.mergeWith(Single.just(1).hide());
            }
        });
    }

    @Test
    public void drainMoreWorkBeforeCancel() {
        SingleSubject<Integer> ss = SingleSubject.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).mergeWith(ss).doOnNext(v -> {
            if (v == 1) {
                ss.onSuccess(6);
                ts.cancel();
            }
        }).subscribe(ts);
        ts.assertValuesOnly(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLong() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalLong, this.description("normalLong"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLongRequestExact() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalLongRequestExact, this.description("normalLongRequestExact"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalBackpressured, this.description("normalBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherError, this.description("otherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeRace, this.description("completeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextSlowPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextSlowPath, this.description("onNextSlowPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessSlowPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessSlowPath, this.description("onSuccessSlowPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessSlowPathBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessSlowPathBackpressured, this.description("onSuccessSlowPathBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessFastPathBackpressuredRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessFastPathBackpressuredRace, this.description("onSuccessFastPathBackpressuredRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMainOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorMainOverflow, this.description("onErrorMainOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOtherOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorOtherOverflow, this.description("onErrorOtherOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextRequestRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextRequestRace, this.description("onNextRequestRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribeMain, this.description("doubleOnSubscribeMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noRequestOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noRequestOnError, this.description("noRequestOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainExactRequestCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainExactRequestCancel, this.description("drainExactRequestCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainRequestWhenLimitReached() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainRequestWhenLimitReached, this.description("drainRequestWhenLimitReached"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOtherOnMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOtherOnMainError, this.description("cancelOtherOnMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMainOnOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelMainOnOtherError, this.description("cancelMainOnOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::undeliverableUponCancel, this.description("undeliverableUponCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMoreWorkBeforeCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainMoreWorkBeforeCancel, this.description("drainMoreWorkBeforeCancel"));
        }

        private FlowableMergeWithSingleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableMergeWithSingleTest();
        }

        @java.lang.Override
        public FlowableMergeWithSingleTest implementation() {
            return this.implementation;
        }
    }
}
