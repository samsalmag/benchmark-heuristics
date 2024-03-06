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
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableGenerateTest extends RxJavaTest {

    @Test
    public void statefulBiconsumer() {
        Observable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 10;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }).take(5).test().assertResult(10, 10, 10, 10, 10);
    }

    @Test
    public void stateSupplierThrows() {
        Observable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw new TestException();
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void generatorThrows() {
        Observable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                throw new TestException();
            }
        }, Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void disposerThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.generate(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new BiConsumer<Object, Emitter<Object>>() {

                @Override
                public void accept(Object s, Emitter<Object> e) throws Exception {
                    e.onComplete();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException();
                }
            }).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onComplete();
            }
        }, Functions.emptyConsumer()));
    }

    @Test
    public void nullError() {
        final int[] call = { 0 };
        Observable.generate(Functions.justSupplier(1), new BiConsumer<Integer, Emitter<Object>>() {

            @Override
            public void accept(Integer s, Emitter<Object> e) throws Exception {
                try {
                    e.onError(null);
                } catch (NullPointerException ex) {
                    call[0]++;
                }
            }
        }, Functions.emptyConsumer()).test().assertFailure(NullPointerException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void multipleOnNext() {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
            }
        }).test().assertFailure(IllegalStateException.class, 1);
    }

    @Test
    public void multipleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.generate(new Consumer<Emitter<Object>>() {

                @Override
                public void accept(Emitter<Object> e) throws Exception {
                    e.onError(new TestException("First"));
                    e.onError(new TestException("Second"));
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void multipleOnComplete() {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onComplete();
                e.onComplete();
            }
        }).test().assertResult();
    }

    @Test
    public void onNextAfterOnComplete() {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onComplete();
                e.onNext(1);
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_statefulBiconsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::statefulBiconsumer, this.description("statefulBiconsumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stateSupplierThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stateSupplierThrows, this.description("stateSupplierThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generatorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::generatorThrows, this.description("generatorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposerThrows, this.description("disposerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullError, this.description("nullError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleOnNext, this.description("multipleOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleOnError, this.description("multipleOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleOnComplete, this.description("multipleOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextAfterOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextAfterOnComplete, this.description("onNextAfterOnComplete"));
        }

        private ObservableGenerateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableGenerateTest();
        }

        @java.lang.Override
        public ObservableGenerateTest implementation() {
            return this.implementation;
        }
    }
}
