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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest.EmptyDisposingObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableConcatMapTest extends RxJavaTest {

    @Test
    public void asyncFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Integer> to = us.concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test();
        us.onNext(1);
        us.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.<Integer>just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }));
    }

    @Test
    public void dispose2() {
        TestHelper.checkDisposed(Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }));
    }

    @Test
    public void mainError() {
        Observable.<Integer>error(new TestException()).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.<Integer>just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mainErrorDelayed() {
        Observable.<Integer>error(new TestException()).concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError() {
        Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError2() {
        Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.concatMap(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.range(v, 2);
                }
            }).test().assertResult(1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceDelayError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.range(v, 2);
                }
            }).test().assertResult(1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void normalDelayErrors() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void normalDelayErrorsTillTheEnd() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }, true, 16).test().assertResult(1, 2);
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                TestObserver<Integer> to = ps1.concatMap(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        return ps2;
                    }
                }).test();
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertError(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedPollThrows() {
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedPollThrowsDelayError() {
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperThrowsDelayError() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void badInnerDelayError() {
        @SuppressWarnings("rawtypes")
        final Observer[] o = { null };
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return new Observable<Integer>() {

                        @Override
                        protected void subscribeActual(Observer<? super Integer> observer) {
                            o[0] = observer;
                            observer.onSubscribe(Disposable.empty());
                            observer.onComplete();
                        }
                    };
                }
            }).test().assertResult();
            o[0].onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void concatReportsDisposedOnComplete() {
        final Disposable[] disposable = { null };
        Observable.fromArray(Observable.just(1), Observable.just(2)).hide().concatMap(Functions.<Observable<Integer>>identity()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    @Test
    public void concatReportsDisposedOnError() {
        final Disposable[] disposable = { null };
        Observable.fromArray(Observable.just(1), Observable.<Integer>error(new TestException())).hide().concatMap(Functions.<Observable<Integer>>identity()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    @Test
    public void reentrantNoOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Integer> to = ps.concatMap(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Exception {
                    return Observable.just(v + 1);
                }
            }, 1).subscribeWith(new TestObserver<Integer>() {

                @Override
                public void onNext(Integer t) {
                    super.onNext(t);
                    if (t == 1) {
                        for (int i = 1; i < 10; i++) {
                            ps.onNext(i);
                        }
                        ps.onComplete();
                    }
                }
            });
            ps.onNext(0);
            if (!errors.isEmpty()) {
                to.onError(new CompositeException(errors));
            }
            to.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void reentrantNoOverflowHidden() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.concatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Exception {
                return Observable.just(v + 1).hide();
            }
        }, 1).subscribeWith(new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    for (int i = 1; i < 10; i++) {
                        ps.onNext(i);
                    }
                    ps.onComplete();
                }
            }
        });
        ps.onNext(0);
        to.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void noCancelPrevious() {
        final AtomicInteger counter = new AtomicInteger();
        Observable.range(1, 5).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.just(v).doOnDispose(new Action() {

                    @Override
                    public void run() throws Exception {
                        counter.getAndIncrement();
                    }
                });
            }
        }).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(0, counter.get());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMap(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.concatMap(v -> Observable.never()));
    }

    @Test
    public void doubleOnSubscribeDelayError() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.concatMapDelayError(v -> Observable.never()));
    }

    @Test
    public void scalarXMap() {
        Observable.fromCallable(() -> 1).concatMap(v -> Observable.just(2).hide()).test().assertResult(2);
    }

    @Test
    public void rejectedFusion() {
        TestHelper.rejectObservableFusion().concatMap(v -> Observable.never()).test();
    }

    @Test
    public void rejectedFusionDelayError() {
        TestHelper.rejectObservableFusion().concatMapDelayError(v -> Observable.never()).test();
    }

    @Test
    public void asyncFusedDelayError() {
        UnicastSubject<Integer> uc = UnicastSubject.create();
        TestObserver<Integer> to = uc.concatMapDelayError(v -> Observable.just(v).hide()).test();
        uc.onNext(1);
        uc.onComplete();
        to.assertResult(1);
    }

    @Test
    public void scalarInnerJustDelayError() {
        Observable.just(1).hide().concatMapDelayError(v -> Observable.just(v)).test().assertResult(1);
    }

    @Test
    public void scalarInnerEmptyDelayError() {
        Observable.just(1).hide().concatMapDelayError(v -> Observable.empty()).test().assertResult();
    }

    @Test
    public void scalarInnerJustDisposeDelayError() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.just(1).hide().concatMapDelayError(v -> Observable.fromCallable(() -> {
            to.dispose();
            return 1;
        })).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void scalarInnerEmptyDisposeDelayError() {
        TestObserver<Object> to = new TestObserver<>();
        Observable.just(1).hide().concatMapDelayError(v -> new EmptyDisposingObservable(to)).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void delayErrorInnerActive() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.range(1, 5).hide().concatMapDelayError(v -> ps).test();
        ps.onComplete();
        to.assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFused, this.description("asyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose2, this.description("dispose2"));
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
        public void benchmark_mainErrorDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainErrorDelayed, this.description("mainErrorDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerErrorDelayError, this.description("innerErrorDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerErrorDelayError2, this.description("innerErrorDelayError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceDelayError, this.description("badSourceDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrors, this.description("normalDelayErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorsTillTheEnd() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorsTillTheEnd, this.description("normalDelayErrorsTillTheEnd"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorRace, this.description("onErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrows, this.description("mapperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedPollThrows, this.description("fusedPollThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollThrowsDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedPollThrowsDelayError, this.description("fusedPollThrowsDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrowsDelayError, this.description("mapperThrowsDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badInnerDelayError, this.description("badInnerDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatReportsDisposedOnComplete, this.description("concatReportsDisposedOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatReportsDisposedOnError, this.description("concatReportsDisposedOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantNoOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantNoOverflow, this.description("reentrantNoOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantNoOverflowHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantNoOverflowHidden, this.description("reentrantNoOverflowHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPrevious() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noCancelPrevious, this.description("noCancelPrevious"));
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribeDelayError, this.description("doubleOnSubscribeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarXMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarXMap, this.description("scalarXMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectedFusion() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rejectedFusion, this.description("rejectedFusion"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectedFusionDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rejectedFusionDelayError, this.description("rejectedFusionDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedDelayError, this.description("asyncFusedDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarInnerJustDelayError, this.description("scalarInnerJustDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerEmptyDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarInnerEmptyDelayError, this.description("scalarInnerEmptyDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDisposeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarInnerJustDisposeDelayError, this.description("scalarInnerJustDisposeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerEmptyDisposeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarInnerEmptyDisposeDelayError, this.description("scalarInnerEmptyDisposeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorInnerActive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayErrorInnerActive, this.description("delayErrorInnerActive"));
        }

        private ObservableConcatMapTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableConcatMapTest();
        }

        @java.lang.Override
        public ObservableConcatMapTest implementation() {
            return this.implementation;
        }
    }
}
