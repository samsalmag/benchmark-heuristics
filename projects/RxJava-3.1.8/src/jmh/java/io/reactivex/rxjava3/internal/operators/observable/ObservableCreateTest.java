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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableCreateTest extends RxJavaTest {

    @Test
    @SuppressUndeliverable
    public void basic() {
        final Disposable d = Disposable.empty();
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.setDisposable(d);
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
                e.onError(new TestException());
                e.onNext(4);
                e.onError(new TestException());
                e.onComplete();
            }
        }).test().assertResult(1, 2, 3);
        assertTrue(d.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicWithCancellable() {
        final Disposable d1 = Disposable.empty();
        final Disposable d2 = Disposable.empty();
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.setDisposable(d1);
                e.setCancellable(new Cancellable() {

                    @Override
                    public void cancel() throws Exception {
                        d2.dispose();
                    }
                });
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
                e.onError(new TestException());
                e.onNext(4);
                e.onError(new TestException());
                e.onComplete();
            }
        }).test().assertResult(1, 2, 3);
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicWithError() {
        final Disposable d = Disposable.empty();
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.setDisposable(d);
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onError(new TestException());
                e.onComplete();
                e.onNext(4);
                e.onError(new TestException());
            }
        }).test().assertFailure(TestException.class, 1, 2, 3);
        assertTrue(d.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicSerialized() {
        final Disposable d = Disposable.empty();
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e = e.serialize();
                e.setDisposable(d);
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
                e.onError(new TestException());
                e.onNext(4);
                e.onError(new TestException());
                e.onComplete();
            }
        }).test().assertResult(1, 2, 3);
        assertTrue(d.isDisposed());
    }

    @Test
    @SuppressUndeliverable
    public void basicWithErrorSerialized() {
        final Disposable d = Disposable.empty();
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e = e.serialize();
                e.setDisposable(d);
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onError(new TestException());
                e.onComplete();
                e.onNext(4);
                e.onError(new TestException());
            }
        }).test().assertFailure(TestException.class, 1, 2, 3);
        assertTrue(d.isDisposed());
    }

    @Test
    public void wrap() {
        Observable.wrap(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
                observer.onNext(3);
                observer.onNext(4);
                observer.onNext(5);
                observer.onComplete();
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void unsafe() {
        Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
                observer.onNext(3);
                observer.onNext(4);
                observer.onNext(5);
                observer.onComplete();
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsafeWithObservable() {
        Observable.unsafeCreate(Observable.just(1));
    }

    @Test
    @SuppressUndeliverable
    public void createNullValue() {
        final Throwable[] error = { null };
        Observable.create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                try {
                    e.onNext(null);
                    e.onNext(1);
                    e.onError(new TestException());
                    e.onComplete();
                } catch (Throwable ex) {
                    error[0] = ex;
                }
            }
        }).test().assertFailure(NullPointerException.class);
        assertNull(error[0]);
    }

    @Test
    @SuppressUndeliverable
    public void createNullValueSerialized() {
        final Throwable[] error = { null };
        Observable.create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e = e.serialize();
                try {
                    e.onNext(null);
                    e.onNext(1);
                    e.onError(new TestException());
                    e.onComplete();
                } catch (Throwable ex) {
                    error[0] = ex;
                }
            }
        }).test().assertFailure(NullPointerException.class);
        assertNull(error[0]);
    }

    @Test
    public void callbackThrows() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void nullValue() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                e.onNext(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void nullThrowable() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                e.onError(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void nullValueSync() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                e.serialize().onNext(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void nullThrowableSync() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                e.serialize().onError(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onErrorCrash() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
            }
        }).subscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
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
    public void onCompleteCrash() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onComplete();
                    fail("Should have thrown");
                } catch (TestException ex) {
                    // expected
                }
                assertTrue(d.isDisposed());
            }
        }).subscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
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
    public void serialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.create(new ObservableOnSubscribe<Object>() {

                @Override
                public void subscribe(ObservableEmitter<Object> e) throws Exception {
                    ObservableEmitter<Object> f = e.serialize();
                    assertSame(f, f.serialize());
                    assertFalse(f.isDisposed());
                    final int[] calls = { 0 };
                    f.setCancellable(new Cancellable() {

                        @Override
                        public void cancel() throws Exception {
                            calls[0]++;
                        }
                    });
                    e.onComplete();
                    assertTrue(f.isDisposed());
                    assertEquals(1, calls[0]);
                }
            }).test().assertResult();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void serializedConcurrentOnNext() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                final ObservableEmitter<Object> f = e.serialize();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                            f.onNext(1);
                        }
                    }
                };
                TestHelper.race(r1, r1);
            }
        }).take(TestHelper.RACE_DEFAULT_LOOPS).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertValueCount(TestHelper.RACE_DEFAULT_LOOPS).assertComplete().assertNoErrors();
    }

    @Test
    public void serializedConcurrentOnNextOnError() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                final ObservableEmitter<Object> f = e.serialize();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 1000; i++) {
                            f.onNext(1);
                        }
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 100; i++) {
                            f.onNext(1);
                        }
                        f.onError(new TestException());
                    }
                };
                TestHelper.race(r1, r2);
            }
        }).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertNotComplete().assertError(TestException.class);
    }

    @Test
    public void serializedConcurrentOnNextOnComplete() {
        TestObserverEx<Object> to = Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                final ObservableEmitter<Object> f = e.serialize();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 1000; i++) {
                            f.onNext(1);
                        }
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 100; i++) {
                            f.onNext(1);
                        }
                        f.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
            }
        }).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertComplete().assertNoErrors();
        int c = to.values().size();
        assertTrue("" + c, c >= 100);
    }

    @Test
    public void onErrorRace() {
        Observable<Object> source = Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                final ObservableEmitter<Object> f = e.serialize();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        f.onError(null);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        f.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
            }
        });
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                source.test().assertFailure(Throwable.class);
            }
        } finally {
            RxJavaPlugins.reset();
        }
        assertFalse(errors.isEmpty());
    }

    @Test
    public void onCompleteRace() {
        Observable<Object> source = Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                final ObservableEmitter<Object> f = e.serialize();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        f.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        f.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
            }
        });
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            source.test().assertResult();
        }
    }

    @Test
    public void tryOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Boolean[] response = { null };
            Observable.create(new ObservableOnSubscribe<Object>() {

                @Override
                public void subscribe(ObservableEmitter<Object> e) throws Exception {
                    e.onNext(1);
                    response[0] = e.tryOnError(new TestException());
                }
            }).take(1).test().assertResult(1);
            assertFalse(response[0]);
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryOnErrorSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Boolean[] response = { null };
            Observable.create(new ObservableOnSubscribe<Object>() {

                @Override
                public void subscribe(ObservableEmitter<Object> e) throws Exception {
                    e = e.serialize();
                    e.onNext(1);
                    response[0] = e.tryOnError(new TestException());
                }
            }).take(1).test().assertResult(1);
            assertFalse(response[0]);
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emitterHasToString() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                assertTrue(emitter.toString().contains(ObservableCreate.CreateEmitter.class.getSimpleName()));
                assertTrue(emitter.serialize().toString().contains(ObservableCreate.CreateEmitter.class.getSimpleName()));
            }
        }).test().assertEmpty();
    }

    @Test
    public void emptySerialized() {
        Observable.create(emitter -> emitter.serialize().onComplete()).test().assertResult();
    }

    @Test
    public void serializedDisposedBeforeOnNext() {
        TestObserver<Object> to = new TestObserver<>();
        Observable.create(emitter -> {
            to.dispose();
            emitter.serialize().onNext(1);
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void serializedOnNextAfterComplete() {
        TestObserver<Object> to = new TestObserver<>();
        Observable.create(emitter -> {
            emitter = emitter.serialize();
            emitter.onComplete();
            emitter.onNext(1);
        }).subscribe(to);
        to.assertResult();
    }

    @Test
    public void serializedEnqueueAndDrainRace() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            AtomicReference<ObservableEmitter<Integer>> ref = new AtomicReference<>();
            CountDownLatch cdl = new CountDownLatch(1);
            Observable.<Integer>create(emitter -> {
                emitter = emitter.serialize();
                ref.set(emitter);
                emitter.onNext(1);
            }).doOnNext(v -> {
                if (v == 1) {
                    TestHelper.raceOther(() -> {
                        ref.get().onNext(2);
                    }, cdl);
                    ref.get().onNext(3);
                }
            }).subscribe(to);
            cdl.await();
            to.assertValueCount(3);
        }
    }

    @Test
    public void serializedDrainDoneButNotEmpty() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            AtomicReference<ObservableEmitter<Integer>> ref = new AtomicReference<>();
            CountDownLatch cdl = new CountDownLatch(1);
            Observable.<Integer>create(emitter -> {
                emitter = emitter.serialize();
                ref.set(emitter);
                emitter.onNext(1);
            }).doOnNext(v -> {
                if (v == 1) {
                    TestHelper.raceOther(() -> {
                        ref.get().onNext(2);
                        ref.get().onComplete();
                    }, cdl);
                    ref.get().onNext(3);
                }
            }).subscribe(to);
            cdl.await();
        }
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
        public void benchmark_basicSerialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicSerialized, this.description("basicSerialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithErrorSerialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basicWithErrorSerialized, this.description("basicWithErrorSerialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::wrap, this.description("wrap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsafe, this.description("unsafe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeWithObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::unsafeWithObservable, this.description("unsafeWithObservable"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createNullValue, this.description("createNullValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueSerialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createNullValueSerialized, this.description("createNullValueSerialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callbackThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::callbackThrows, this.description("callbackThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullValue, this.description("nullValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullThrowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullThrowable, this.description("nullThrowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullValueSync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullValueSync, this.description("nullValueSync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullThrowableSync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullThrowableSync, this.description("nullThrowableSync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCrash, this.description("onErrorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteCrash, this.description("onCompleteCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serialized, this.description("serialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedConcurrentOnNext, this.description("serializedConcurrentOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNextOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedConcurrentOnNextOnError, this.description("serializedConcurrentOnNextOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNextOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedConcurrentOnNextOnComplete, this.description("serializedConcurrentOnNextOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorRace, this.description("onErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteRace, this.description("onCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryOnError, this.description("tryOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnErrorSerialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryOnErrorSerialized, this.description("tryOnErrorSerialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitterHasToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emitterHasToString, this.description("emitterHasToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySerialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptySerialized, this.description("emptySerialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedDisposedBeforeOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedDisposedBeforeOnNext, this.description("serializedDisposedBeforeOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedOnNextAfterComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedOnNextAfterComplete, this.description("serializedOnNextAfterComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedEnqueueAndDrainRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedEnqueueAndDrainRace, this.description("serializedEnqueueAndDrainRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedDrainDoneButNotEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serializedDrainDoneButNotEmpty, this.description("serializedDrainDoneButNotEmpty"));
        }

        private ObservableCreateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableCreateTest();
        }

        @java.lang.Override
        public ObservableCreateTest implementation() {
            return this.implementation;
        }
    }
}
