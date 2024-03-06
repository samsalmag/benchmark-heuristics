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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableReduceTest extends RxJavaTest {

    Observer<Object> observer;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
        singleObserver = TestHelper.mockSingleObserver();
    }

    BiFunction<Integer, Integer, Integer> sum = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    @Test
    public void aggregateAsIntSumObservable() {
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer).onNext(1 + 2 + 3 + 4 + 5);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void aggregateAsIntSumSourceThrowsObservable() {
        Observable<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5), Observable.<Integer>error(new TestException())).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumAccumulatorThrowsObservable() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sumErr).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumResultSelectorThrowsObservable() {
        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).toObservable().map(error);
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithNoInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(sum).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void backpressureWithInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(0, sum).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void aggregateAsIntSum() {
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver).onSuccess(1 + 2 + 3 + 4 + 5);
        verify(singleObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void aggregateAsIntSumSourceThrows() {
        Single<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5), Observable.<Integer>error(new TestException())).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumAccumulatorThrows() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sumErr).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumResultSelectorThrows() {
        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(error);
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithNoInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void backpressureWithInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Single<Integer> reduced = source.reduce(0, sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void reduceWithSingle() {
        Observable.range(1, 5).reduceWith(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 0;
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertResult(15);
    }

    @Test
    public void reduceMaybeDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.reduce(new BiFunction<Object, Object, Object>() {

                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void reduceMaybeCheckDisposed() {
        TestHelper.checkDisposed(Observable.just(new Object()).reduce(new BiFunction<Object, Object, Object>() {

            @Override
            public Object apply(Object a, Object b) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void reduceMaybeBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.reduce(new BiFunction<Object, Object, Object>() {

                @Override
                public Object apply(Object a, Object b) throws Exception {
                    return a;
                }
            }).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void seedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Observable<Integer> o) throws Exception {
                return o.reduce(0, new BiFunction<Integer, Integer, Integer>() {

                    @Override
                    public Integer apply(Integer a, Integer b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void seedDisposed() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().reduce(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void seedBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.reduce(0, new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a;
                }
            }).test().assertResult(0);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumObservable, this.description("aggregateAsIntSumObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumSourceThrowsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumSourceThrowsObservable, this.description("aggregateAsIntSumSourceThrowsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumAccumulatorThrowsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumAccumulatorThrowsObservable, this.description("aggregateAsIntSumAccumulatorThrowsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumResultSelectorThrowsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumResultSelectorThrowsObservable, this.description("aggregateAsIntSumResultSelectorThrowsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithNoInitialValueObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithNoInitialValueObservable, this.description("backpressureWithNoInitialValueObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValueObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithInitialValueObservable, this.description("backpressureWithInitialValueObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSum() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSum, this.description("aggregateAsIntSum"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumSourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumSourceThrows, this.description("aggregateAsIntSumSourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumAccumulatorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumAccumulatorThrows, this.description("aggregateAsIntSumAccumulatorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumResultSelectorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::aggregateAsIntSumResultSelectorThrows, this.description("aggregateAsIntSumResultSelectorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithNoInitialValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithNoInitialValue, this.description("backpressureWithNoInitialValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithInitialValue, this.description("backpressureWithInitialValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithSingle, this.description("reduceWithSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceMaybeDoubleOnSubscribe, this.description("reduceMaybeDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeCheckDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceMaybeCheckDisposed, this.description("reduceMaybeCheckDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeBadSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceMaybeBadSource, this.description("reduceMaybeBadSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::seedDoubleOnSubscribe, this.description("seedDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::seedDisposed, this.description("seedDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedBadSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::seedBadSource, this.description("seedBadSource"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableReduceTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableReduceTest();
        }

        @java.lang.Override
        public ObservableReduceTest implementation() {
            return this.implementation;
        }
    }
}
