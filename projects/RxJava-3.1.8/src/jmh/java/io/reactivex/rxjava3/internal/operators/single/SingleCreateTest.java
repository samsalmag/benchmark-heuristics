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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class SingleCreateTest extends RxJavaTest {

    @Test
    @SuppressUndeliverable
    public void basic() {
        final Disposable d = Disposable.empty();
        Single.<Integer>create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> e) throws Exception {
                e.setDisposable(d);
                e.onSuccess(1);
                e.onError(new TestException());
                e.onSuccess(2);
                e.onError(new TestException());
            }
        }).test().assertResult(1);
        assertTrue(d.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicWithCancellable() {
        final Disposable d1 = Disposable.empty();
        final Disposable d2 = Disposable.empty();
        Single.<Integer>create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> e) throws Exception {
                e.setDisposable(d1);
                e.setCancellable(new Cancellable() {

                    @Override
                    public void cancel() throws Exception {
                        d2.dispose();
                    }
                });
                e.onSuccess(1);
                e.onError(new TestException());
                e.onSuccess(2);
                e.onError(new TestException());
            }
        }).test().assertResult(1);
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicWithError() {
        final Disposable d = Disposable.empty();
        Single.<Integer>create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> e) throws Exception {
                e.setDisposable(d);
                e.onError(new TestException());
                e.onSuccess(2);
                e.onError(new TestException());
            }
        }).test().assertFailure(TestException.class);
        assertTrue(d.isDisposed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsafeCreate() {
        Single.unsafeCreate(Single.just(1));
    }

    @Test
    public void createCallbackThrows() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                s.onSuccess(1);
            }
        }));
    }

    @Test
    public void createNullSuccess() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                s.onSuccess(null);
                s.onSuccess(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void createNullError() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                s.onError(null);
                s.onError(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void createConsumerThrows() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                try {
                    s.onSuccess(1);
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
            }
        }).subscribe(new SingleObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
                throw new TestException();
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    @Test
    public void createConsumerThrowsResource() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                Disposable d = Disposable.empty();
                s.setDisposable(d);
                try {
                    s.onSuccess(1);
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
            }
        }).subscribe(new SingleObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
                throw new TestException();
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    @Test
    public void createConsumerThrowsOnError() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                try {
                    s.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
            }
        }).subscribe(new SingleObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }
        });
    }

    @Test
    public void createConsumerThrowsResourceOnError() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> s) throws Exception {
                Disposable d = Disposable.empty();
                s.setDisposable(d);
                try {
                    s.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
            }
        }).subscribe(new SingleObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }
        });
    }

    @Test
    public void tryOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Boolean[] response = { null };
            Single.create(new SingleOnSubscribe<Object>() {

                @Override
                public void subscribe(SingleEmitter<Object> e) throws Exception {
                    e.onSuccess(1);
                    response[0] = e.tryOnError(new TestException());
                }
            }).test().assertResult(1);
            assertFalse(response[0]);
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emitterHasToString() {
        Single.create(new SingleOnSubscribe<Object>() {

            @Override
            public void subscribe(SingleEmitter<Object> emitter) throws Exception {
                assertTrue(emitter.toString().contains(SingleCreate.Emitter.class.getSimpleName()));
            }
        }).test().assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basic, this.description("basic"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithCancellable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicWithCancellable, this.description("basicWithCancellable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicWithError, this.description("basicWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreate() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::unsafeCreate, this.description("unsafeCreate"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createCallbackThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createCallbackThrows, this.description("createCallbackThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createNullSuccess, this.description("createNullSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createNullError, this.description("createNullError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createConsumerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createConsumerThrows, this.description("createConsumerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createConsumerThrowsResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createConsumerThrowsResource, this.description("createConsumerThrowsResource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createConsumerThrowsOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createConsumerThrowsOnError, this.description("createConsumerThrowsOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createConsumerThrowsResourceOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createConsumerThrowsResourceOnError, this.description("createConsumerThrowsResourceOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryOnError, this.description("tryOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitterHasToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitterHasToString, this.description("emitterHasToString"));
        }

        private SingleCreateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleCreateTest();
        }

        @java.lang.Override
        public SingleCreateTest implementation() {
            return this.implementation;
        }
    }
}
