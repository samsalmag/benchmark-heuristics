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
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableCollectWithCollectorTest extends RxJavaTest {

    @Test
    public void basic() {
        Flowable.range(1, 5).collect(Collectors.toList()).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void empty() {
        Flowable.empty().collect(Collectors.toList()).test().assertResult(Collections.emptyList());
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).collect(Collectors.toList()).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrash() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
            Flowable<Integer> source = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
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
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()));
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(f -> f.collect(Collectors.toList()));
    }

    @Test
    public void basicToFlowable() {
        Flowable.range(1, 5).collect(Collectors.toList()).toFlowable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void emptyToFlowable() {
        Flowable.empty().collect(Collectors.toList()).toFlowable().test().assertResult(Collections.emptyList());
    }

    @Test
    public void errorToFlowable() {
        Flowable.error(new TestException()).collect(Collectors.toList()).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrashToFlowable() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        }).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrashToFlowable() {
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
        }).toFlowable().test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrashToFlowable() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        }).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignalsToFlowable() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable<Integer> source = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
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
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void disposeToFlowable() {
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()).toFlowable());
    }

    @Test
    public void onSubscribeToFlowable() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.collect(Collectors.toList()).toFlowable());
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
        public void benchmark_basicToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicToFlowable, this.description("basicToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyToFlowable, this.description("emptyToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorToFlowable, this.description("errorToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrashToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorSupplierCrashToFlowable, this.description("collectorSupplierCrashToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrashToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorCrashToFlowable, this.description("collectorAccumulatorCrashToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrashToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFinisherCrashToFlowable, this.description("collectorFinisherCrashToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignalsToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorAccumulatorDropSignalsToFlowable, this.description("collectorAccumulatorDropSignalsToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeToFlowable, this.description("disposeToFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeToFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribeToFlowable, this.description("onSubscribeToFlowable"));
        }

        private FlowableCollectWithCollectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableCollectWithCollectorTest();
        }

        @java.lang.Override
        public FlowableCollectWithCollectorTest implementation() {
            return this.implementation;
        }
    }
}
