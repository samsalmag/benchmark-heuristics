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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeCreateTest extends RxJavaTest {

    @Test
    public void callbackThrows() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void onSuccessNull() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                e.onSuccess(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onErrorNull() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                e.onError(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                e.onSuccess(1);
            }
        }));
    }

    @Test
    public void onSuccessThrows() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onSuccess(1);
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

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

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onErrorThrows() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

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

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onCompleteThrows() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onComplete();
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
    }

    @Test
    public void onSuccessThrows2() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                try {
                    e.onSuccess(1);
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

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

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onErrorThrows2() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                try {
                    e.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

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

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onCompleteThrows2() {
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> e) throws Exception {
                try {
                    e.onComplete();
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(e.isDisposed());
            }
        }).subscribe(new MaybeObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
    }

    @Test
    public void tryOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Boolean[] response = { null };
            Maybe.create(new MaybeOnSubscribe<Object>() {

                @Override
                public void subscribe(MaybeEmitter<Object> e) throws Exception {
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
        Maybe.create(new MaybeOnSubscribe<Object>() {

            @Override
            public void subscribe(MaybeEmitter<Object> emitter) throws Exception {
                assertTrue(emitter.toString().contains(MaybeCreate.Emitter.class.getSimpleName()));
            }
        }).test().assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callbackThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::callbackThrows, this.description("callbackThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessNull, this.description("onSuccessNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorNull, this.description("onErrorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessThrows, this.description("onSuccessThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorThrows, this.description("onErrorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteThrows, this.description("onCompleteThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessThrows2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessThrows2, this.description("onSuccessThrows2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorThrows2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorThrows2, this.description("onErrorThrows2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteThrows2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteThrows2, this.description("onCompleteThrows2"));
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

        private MaybeCreateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeCreateTest();
        }

        @java.lang.Override
        public MaybeCreateTest implementation() {
            return this.implementation;
        }
    }
}
