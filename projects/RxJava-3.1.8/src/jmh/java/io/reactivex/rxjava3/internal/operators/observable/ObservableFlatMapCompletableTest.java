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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFlatMapCompletableTest extends RxJavaTest {

    @Test
    public void normalObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable().test().assertResult();
    }

    @Test
    public void mapperThrowsObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperReturnsNullObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void normalDelayErrorObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true).toObservable().test().assertResult();
    }

    @Test
    public void normalAsyncObservable() {
        Observable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Observable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).toObservable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAllObservable() {
        TestObserverEx<Integer> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).<Integer>toObservable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAllObservable() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).<Integer>toObservable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuterObservable() {
        Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedObservable() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toObservable().subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposedObservable() {
        TestHelper.checkDisposed(Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable());
    }

    @Test
    public void normal() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).test().assertResult();
    }

    @Test
    public void mapperThrows() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Void> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperReturnsNull() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Void> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void normalDelayError() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true).test().assertResult();
    }

    @Test
    public void normalAsync() {
        Observable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Observable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserverEx<Void> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAll() {
        TestObserverEx<Void> to = Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuter() {
        Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void fused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toObservable().subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void innerObserver() {
        Observable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

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
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return o.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void fusedInternalsObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable().subscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
                QueueDisposable<?> qd = (QueueDisposable<?>) d;
                try {
                    assertNull(qd.poll());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertTrue(qd.isEmpty());
                qd.clear();
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
    public void innerObserverObservable() {
        Observable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

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
        }).toObservable().test();
    }

    @Test
    public void badSourceObservable() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return o.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                }).toObservable();
            }
        }, false, 1, null);
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.flatMapCompletable(v -> Completable.never()).toObservable());
    }

    @Test
    public void doubleOnSubscribeCompletable() {
        TestHelper.checkDoubleOnSubscribeObservableToCompletable(o -> o.flatMapCompletable(v -> Completable.never()));
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Object> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).toObservable().subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void cancelWhileMappingCompletable() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Void> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalObservable, this.description("normalObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrowsObservable, this.description("mapperThrowsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNullObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperReturnsNullObservable, this.description("mapperReturnsNullObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorObservable, this.description("normalDelayErrorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalAsyncObservable, this.description("normalAsyncObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAllObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayErrorAllObservable, this.description("normalDelayErrorAllObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAllObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayInnerErrorAllObservable, this.description("normalDelayInnerErrorAllObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuterObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalNonDelayErrorOuterObservable, this.description("normalNonDelayErrorOuterObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedObservable, this.description("fusedObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedObservable, this.description("disposedObservable"));
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
        public void benchmark_innerObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerObserver, this.description("innerObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInternalsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedInternalsObservable, this.description("fusedInternalsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserverObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerObserverObservable, this.description("innerObserverObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceObservable, this.description("badSourceObservable"));
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

        private ObservableFlatMapCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableFlatMapCompletableTest();
        }

        @java.lang.Override
        public ObservableFlatMapCompletableTest implementation() {
            return this.implementation;
        }
    }
}
