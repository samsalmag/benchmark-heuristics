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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableCollectWithCollectorTest extends RxJavaTest {

    @Test
    public void basic() {
        Observable.range(1, 5).collect(Collectors.toList()).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void empty() {
        Observable.empty().collect(Collectors.toList()).test().assertResult(Collections.emptyList());
    }

    @Test
    public void error() {
        Observable.error(new TestException()).collect(Collectors.toList()).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrash() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                throw new TestException();
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrash() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrash() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> {
                    throw new TestException();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignals() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
                }
            };
            source.collect(new Collector<Integer, Integer, Integer>() {

                @Override
                public Supplier<Integer> supplier() {
                    return () -> 1;
                }

                @Override
                public BiConsumer<Integer, Integer> accumulator() {
                    return (a, b) -> {
                        throw new TestException();
                    };
                }

                @Override
                public BinaryOperator<Integer> combiner() {
                    return (a, b) -> a + b;
                }

                @Override
                public Function<Integer, Integer> finisher() {
                    return a -> a;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Collections.emptySet();
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().collect(Collectors.toList()));
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(f -> f.collect(Collectors.toList()));
    }

    @Test
    public void basicToObservable() {
        Observable.range(1, 5).collect(Collectors.toList()).toObservable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void emptyToObservable() {
        Observable.empty().collect(Collectors.toList()).toObservable().test().assertResult(Collections.emptyList());
    }

    @Test
    public void errorToObservable() {
        Observable.error(new TestException()).collect(Collectors.toList()).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrashToObservable() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                throw new TestException();
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrashToObservable() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toObservable().test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrashToObservable() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> {
                    throw new TestException();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignalsToObservable() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
                }
            };
            source.collect(new Collector<Integer, Integer, Integer>() {

                @Override
                public Supplier<Integer> supplier() {
                    return () -> 1;
                }

                @Override
                public BiConsumer<Integer, Integer> accumulator() {
                    return (a, b) -> {
                        throw new TestException();
                    };
                }

                @Override
                public BinaryOperator<Integer> combiner() {
                    return (a, b) -> a + b;
                }

                @Override
                public Function<Integer, Integer> finisher() {
                    return a -> a;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Collections.emptySet();
                }
            }).toObservable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void disposeToObservable() {
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()).toObservable());
    }

    @Test
    public void onSubscribeToObservable() {
        TestHelper.checkDoubleOnSubscribeObservable(f -> f.collect(Collectors.toList()).toObservable());
    }

    @Test
    public void toObservableTake() {
        Observable.range(1, 5).collect(Collectors.toList()).toObservable().take(1).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void disposeBeforeEnd() {
        TestObserver<List<Integer>> to = Observable.range(1, 5).concatWith(Observable.never()).collect(Collectors.toList()).test();
        to.dispose();
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basic, this.description("basic"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorSupplierCrash, this.description("collectorSupplierCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorCrash, this.description("collectorAccumulatorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFinisherCrash, this.description("collectorFinisherCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorDropSignals, this.description("collectorAccumulatorDropSignals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribe, this.description("onSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicToObservable, this.description("basicToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyToObservable, this.description("emptyToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorToObservable, this.description("errorToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrashToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorSupplierCrashToObservable, this.description("collectorSupplierCrashToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrashToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorCrashToObservable, this.description("collectorAccumulatorCrashToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrashToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFinisherCrashToObservable, this.description("collectorFinisherCrashToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignalsToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorDropSignalsToObservable, this.description("collectorAccumulatorDropSignalsToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeToObservable, this.description("disposeToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeToObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribeToObservable, this.description("onSubscribeToObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableTake, this.description("toObservableTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeBeforeEnd() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeBeforeEnd, this.description("disposeBeforeEnd"));
        }

        private ObservableCollectWithCollectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableCollectWithCollectorTest();
        }

        @java.lang.Override
        public ObservableCollectWithCollectorTest implementation() {
            return this.implementation;
        }
    }
}
