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
package io.reactivex.rxjava3.internal.operators.mixed;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableConcatMapCompletableTest extends RxJavaTest {

    @Test
    public void simple() {
        Flowable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertResult();
    }

    @Test
    public void simple2() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }))).test().assertResult();
        assertEquals(5, counter.get());
    }

    @Test
    public void simpleLongPrefetch() {
        Flowable.range(1, 1024).concatMapCompletable(Functions.justFunction(Completable.complete()), 32).test().assertResult();
    }

    @Test
    public void simpleLongPrefetchHidden() {
        Flowable.range(1, 1024).hide().concatMapCompletable(Functions.justFunction(Completable.complete()), 32).test().assertResult();
    }

    @Test
    public void mainError() {
        Flowable.<Integer>error(new TestException()).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Flowable.just(1).concatMapCompletable(Functions.justFunction(Completable.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayed() {
        TestObserverEx<Void> to = Flowable.range(1, 5).concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        assertEquals(5, ((CompositeException) to.errors().get(0)).getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        Flowable.just(1).concatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void immediateError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        pp.onError(new TestException());
        assertFalse(cs.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void immediateError2() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertFalse(pp.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void boundaryError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletableDelayError(Functions.justFunction(cs), false).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        pp.onError(new TestException());
        assertTrue(cs.hasObservers());
        to.assertEmpty();
        cs.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void endError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final CompletableSubject cs = CompletableSubject.create();
        final CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 1) {
                    return cs;
                }
                return cs2;
            }
        }, true, 32).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertTrue(pp.hasSubscribers());
        pp.onNext(2);
        to.assertEmpty();
        cs2.onComplete();
        assertTrue(pp.hasSubscribers());
        to.assertEmpty();
        pp.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return f.concatMapCompletable(Functions.justFunction(Completable.complete()));
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.never().concatMapCompletable(Functions.justFunction(Completable.complete())));
    }

    @Test
    public void queueOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new TestException());
                }
            }.concatMapCompletable(Functions.justFunction(Completable.never()), 1).test().assertFailure(QueueOverflowException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void immediateOuterInnerErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final CompletableSubject cs = CompletableSubject.create();
                TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
                pp.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        cs.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertError(new Predicate<Throwable>() {

                    @Override
                    public boolean test(Throwable e) throws Exception {
                        return e instanceof TestException || e instanceof CompositeException;
                    }
                }).assertNotComplete();
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void disposeInDrainLoop() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final CompletableSubject cs = CompletableSubject.create();
            final TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(2);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onComplete();
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void doneButNotEmpty() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CompletableSubject cs = CompletableSubject.create();
        final TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        pp.onNext(1);
        pp.onNext(2);
        pp.onComplete();
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletableDelayError(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletableDelayError(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void basicNonFused() {
        Flowable.range(1, 5).hide().concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicSyncFused() {
        Flowable.range(1, 5).concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicAsyncFused() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectFlowableFusion().concatMapCompletable(v -> Completable.complete().hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Flowable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.flowableStripBoundary()).concatMapCompletable(v -> Completable.complete().hide()).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple, this.description("simple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple2, this.description("simple2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleLongPrefetch, this.description("simpleLongPrefetch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetchHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleLongPrefetchHidden, this.description("simpleLongPrefetchHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerError, this.description("innerError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerErrorDelayed, this.description("innerErrorDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperCrash, this.description("mapperCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::immediateError, this.description("immediateError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::immediateError2, this.description("immediateError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryError, this.description("boundaryError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_endError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::endError, this.description("endError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueOverflow, this.description("queueOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateOuterInnerErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::immediateOuterInnerErrorRace, this.description("immediateOuterInnerErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInDrainLoop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeInDrainLoop, this.description("disposeInDrainLoop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doneButNotEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doneButNotEmpty, this.description("doneButNotEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::undeliverableUponCancel, this.description("undeliverableUponCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::undeliverableUponCancelDelayError, this.description("undeliverableUponCancelDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::undeliverableUponCancelDelayErrorTillEnd, this.description("undeliverableUponCancelDelayErrorTillEnd"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicNonFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicNonFused, this.description("basicNonFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicSyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicSyncFused, this.description("basicSyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicAsyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicAsyncFused, this.description("basicAsyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicFusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicFusionRejected, this.description("basicFusionRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedPollCrash, this.description("fusedPollCrash"));
        }

        private FlowableConcatMapCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableConcatMapCompletableTest();
        }

        @java.lang.Override
        public FlowableConcatMapCompletableTest implementation() {
            return this.implementation;
        }
    }
}
