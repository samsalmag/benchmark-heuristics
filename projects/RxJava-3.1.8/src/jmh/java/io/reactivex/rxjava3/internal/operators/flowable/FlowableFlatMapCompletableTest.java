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
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapCompletableTest extends RxJavaTest {

    @Test
    public void normalFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable().test().assertResult();
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNullFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayErrorFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE).toFlowable().test().assertResult();
    }

    @Test
    public void normalAsyncFlowable() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).toFlowable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalAsyncFlowableMaxConcurrency() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }, false, 3).toFlowable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAllFlowable() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).<Integer>toFlowable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAllFlowable() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).<Integer>toFlowable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuterFlowable() {
        Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedFlowable() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void normal() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).test().assertResult();
    }

    @Test
    public void mapperThrows() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNull() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayError() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE).test().assertResult();
    }

    @Test
    public void normalAsync() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserverEx<Void> to = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAll() {
        TestObserverEx<Void> to = Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuter() {
        Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE).test().assertFailure(TestException.class);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }, false, 3).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void disposedFlowable() {
        TestHelper.checkDisposed(Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable());
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void fusedInternalsFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable().subscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                QueueSubscription<?> qs = (QueueSubscription<?>) s;
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertTrue(qs.isEmpty());
                qs.clear();
            }

            @Override
            public void onNext(Object t) {
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
    public void innerObserverFlowable() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).toFlowable().test();
    }

    @Test
    public void badSourceFlowable() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                }).toFlowable();
            }
        }, false, 1, null);
    }

    @Test
    public void innerObserver() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).test();
    }

    @Test
    public void delayErrorMaxConcurrency() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 2) {
                    return Completable.error(new TestException());
                }
                return Completable.complete();
            }
        }, true, 1).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void delayErrorMaxConcurrencyCompletable() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 2) {
                    return Completable.error(new TestException());
                }
                return Completable.complete();
            }
        }, true, 1).test().assertFailure(TestException.class);
    }

    @Test
    public void asyncMaxConcurrency() {
        for (int itemCount = 1; itemCount <= 100000; itemCount *= 10) {
            for (int concurrency = 1; concurrency <= 256; concurrency *= 2) {
                Flowable.range(1, itemCount).flatMapCompletable(Functions.justFunction(Completable.complete().subscribeOn(Schedulers.computation())), false, concurrency).test().withTag("itemCount=" + itemCount + ", concurrency=" + concurrency).awaitDone(5, TimeUnit.SECONDS).assertResult();
            }
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

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
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.flatMapCompletable(v -> Completable.never()).toFlowable());
    }

    @Test
    public void doubleOnSubscribeCompletable() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(f -> f.flatMapCompletable(v -> Completable.never()));
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp1 = PublishProcessor.create();
            TestSubscriber<Object> ts = new TestSubscriber<>();
            CountDownLatch cdl = new CountDownLatch(1);
            pp1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    ts.cancel();
                }, cdl);
                return Completable.complete();
            }).toFlowable().subscribe(ts);
            pp1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void cancelWhileMappingCompletable() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp1 = PublishProcessor.create();
            TestObserver<Void> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            pp1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).subscribe(to);
            pp1.onNext(1);
            cdl.await();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalFlowable, this.description("normalFlowable"));
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
        public void benchmark_normalDelayErrorFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorFlowable, this.description("normalDelayErrorFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncFlowable, this.description("normalAsyncFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncFlowableMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncFlowableMaxConcurrency, this.description("normalAsyncFlowableMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAllFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorAllFlowable, this.description("normalDelayErrorAllFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAllFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayInnerErrorAllFlowable, this.description("normalDelayInnerErrorAllFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuterFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalNonDelayErrorOuterFlowable, this.description("normalNonDelayErrorOuterFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFlowable, this.description("fusedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrows, this.description("mapperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperReturnsNull, this.description("mapperReturnsNull"));
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
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorAll, this.description("normalDelayErrorAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayInnerErrorAll, this.description("normalDelayInnerErrorAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalNonDelayErrorOuter, this.description("normalNonDelayErrorOuter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncMaxConcurrency, this.description("normalAsyncMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedFlowable, this.description("disposedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInternalsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedInternalsFlowable, this.description("fusedInternalsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserverFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerObserverFlowable, this.description("innerObserverFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceFlowable, this.description("badSourceFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerObserver, this.description("innerObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayErrorMaxConcurrency, this.description("delayErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorMaxConcurrencyCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayErrorMaxConcurrencyCompletable, this.description("delayErrorMaxConcurrencyCompletable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncMaxConcurrency, this.description("asyncMaxConcurrency"));
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribeCompletable, this.description("doubleOnSubscribeCompletable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMapping() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelWhileMapping, this.description("cancelWhileMapping"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMappingCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelWhileMappingCompletable, this.description("cancelWhileMappingCompletable"));
        }

        private FlowableFlatMapCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFlatMapCompletableTest();
        }

        @java.lang.Override
        public FlowableFlatMapCompletableTest implementation() {
            return this.implementation;
        }
    }
}
