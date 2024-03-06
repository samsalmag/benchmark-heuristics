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
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapMaybeTest extends RxJavaTest {

    @Test
    public void normal() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalEmpty() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void normalDelayError() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, true, Integer.MAX_VALUE).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(10).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 3).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(10).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency1() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNullFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayErrorAll() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalBackpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent1Backpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, false, 1).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent2Backpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, false, 2).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void takeAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).take(2).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(2).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void take() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).take(2).test().assertResult(1, 2);
    }

    @Test
    public void middleError() {
        Flowable.fromArray(new String[] { "1", "a", "2" }).flatMapMaybe(new Function<String, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final String s) throws NumberFormatException {
                // return Maybe.just(Integer.valueOf(s)); //This works
                return Maybe.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws NumberFormatException {
                        return Integer.valueOf(s);
                    }
                });
            }
        }).test().assertFailure(NumberFormatException.class, 1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty();
            }
        }));
    }

    @Test
    public void asyncFlatten() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1).subscribeOn(Schedulers.computation());
            }
        }).take(500).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void asyncFlattenNone() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void asyncFlattenNoneMaxConcurrency() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }, false, 128).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void asyncFlattenErrorMaxConcurrency() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>error(new TestException()).subscribeOn(Schedulers.computation());
            }
        }, true, 128).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(CompositeException.class);
    }

    @Test
    public void successError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return pp.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void completeError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return pp.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).test();
        pp.onComplete();
        ts.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Object> f) throws Exception {
                return f.flatMapMaybe(Functions.justFunction(Maybe.just(2)));
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onError(new TestException("Second"));
                }
            }.flatMapMaybe(Functions.justFunction(Maybe.just(2))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).flatMapMaybe(Functions.justFunction(new Maybe<Integer>() {

                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            })).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emissionQueueTrigger() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp2.onNext(2);
                    pp2.onComplete();
                }
            }
        };
        Flowable.just(pp1, pp2).flatMapMaybe(new Function<PublishProcessor<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(ts);
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void emissionQueueTrigger2() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        final PublishProcessor<Integer> pp3 = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp2.onNext(2);
                    pp2.onComplete();
                }
            }
        };
        Flowable.just(pp1, pp2, pp3).flatMapMaybe(new Function<PublishProcessor<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(ts);
        pp1.onNext(1);
        pp1.onComplete();
        pp3.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void disposeInner() {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(1).flatMapMaybe(new Function<Integer, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Integer v) throws Exception {
                return new Maybe<Object>() {

                    @Override
                    protected void subscribeActual(MaybeObserver<? super Object> observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ts.cancel();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void innerSuccessCompletesAfterMain() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapMaybe(Functions.justFunction(pp.singleElement())).test();
        pp.onNext(2);
        pp.onComplete();
        ts.assertResult(2);
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.just(2))).test(0L).assertEmpty();
        ts.request(1);
        ts.assertResult(2);
    }

    @Test
    public void error() {
        Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.<Integer>error(new TestException()))).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void errorDelayed() {
        Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.<Integer>error(new TestException())), true, 16).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void requestCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>never()).flatMapMaybe(Functions.justFunction(Maybe.just(2))).test(0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMapMaybe(v -> Maybe.never()));
    }

    @Test
    public void successRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            MaybeSubject<Integer> ss1 = MaybeSubject.create();
            MaybeSubject<Integer> ss2 = MaybeSubject.create();
            TestSubscriber<Integer> ts = Flowable.just(ss1, ss2).flatMapMaybe(v -> v).test();
            TestHelper.race(() -> ss1.onSuccess(1), () -> ss2.onSuccess(1));
            ts.assertResult(1, 1);
        }
    }

    @Test
    public void successCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            MaybeSubject<Integer> ss1 = MaybeSubject.create();
            MaybeSubject<Integer> ss2 = MaybeSubject.create();
            TestSubscriber<Integer> ts = Flowable.just(ss1, ss2).flatMapMaybe(v -> v).test();
            TestHelper.race(() -> ss1.onSuccess(1), () -> ss2.onComplete());
            ts.assertResult(1);
        }
    }

    @Test
    public void successShortcut() {
        MaybeSubject<Integer> ss1 = MaybeSubject.create();
        TestSubscriber<Integer> ts = Flowable.just(ss1).hide().flatMapMaybe(v -> v).test();
        ss1.onSuccess(1);
        ts.assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmpty, this.description("normalEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayError, this.description("normalDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsync, this.description("normalAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncMaxConcurrency, this.description("normalAsyncMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncMaxConcurrency1, this.description("normalAsyncMaxConcurrency1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrowsFlowable, this.description("mapperThrowsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNullFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperReturnsNullFlowable, this.description("mapperReturnsNullFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorAll, this.description("normalDelayErrorAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalBackpressured, this.description("normalBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaxConcurrent1Backpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalMaxConcurrent1Backpressured, this.description("normalMaxConcurrent1Backpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaxConcurrent2Backpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalMaxConcurrent2Backpressured, this.description("normalMaxConcurrent2Backpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeAsync, this.description("takeAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_middleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::middleError, this.description("middleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlatten() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFlatten, this.description("asyncFlatten"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFlattenNone, this.description("asyncFlattenNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenNoneMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFlattenNoneMaxConcurrency, this.description("asyncFlattenNoneMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFlattenErrorMaxConcurrency, this.description("asyncFlattenErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successError, this.description("successError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeError, this.description("completeError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badInnerSource, this.description("badInnerSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionQueueTrigger() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emissionQueueTrigger, this.description("emissionQueueTrigger"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionQueueTrigger2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emissionQueueTrigger2, this.description("emissionQueueTrigger2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInner() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeInner, this.description("disposeInner"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerSuccessCompletesAfterMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerSuccessCompletesAfterMain, this.description("innerSuccessCompletesAfterMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed, this.description("errorDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCancelRace, this.description("requestCancelRace"));
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
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successRace, this.description("successRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successCompleteRace, this.description("successCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successShortcut() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successShortcut, this.description("successShortcut"));
        }

        private FlowableFlatMapMaybeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFlatMapMaybeTest();
        }

        @java.lang.Override
        public FlowableFlatMapMaybeTest implementation() {
            return this.implementation;
        }
    }
}
