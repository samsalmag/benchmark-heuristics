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
package io.reactivex.rxjava3.internal.observers;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FutureSingleObserverTest extends RxJavaTest {

    @Test
    public void cancel() {
        final Future<?> f = Single.never().toFuture();
        assertFalse(f.isCancelled());
        assertFalse(f.isDone());
        f.cancel(true);
        assertTrue(f.isCancelled());
        assertTrue(f.isDone());
        try {
            f.get();
            fail("Should have thrown!");
        } catch (CancellationException ex) {
            // expected
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        } catch (ExecutionException ex) {
            throw new AssertionError(ex);
        }
        try {
            f.get(5, TimeUnit.SECONDS);
            fail("Should have thrown!");
        } catch (CancellationException ex) {
            // expected
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        } catch (ExecutionException ex) {
            throw new AssertionError(ex);
        } catch (TimeoutException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Future<?> f = Single.never().toFuture();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    f.cancel(true);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void timeout() throws Exception {
        Future<?> f = Single.never().toFuture();
        try {
            f.get(100, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(100, TimeUnit.MILLISECONDS), expected.getMessage());
        }
    }

    @Test
    public void dispose() {
        Future<Integer> f = Single.just(1).toFuture();
        ((Disposable) f).dispose();
        assertTrue(((Disposable) f).isDisposed());
    }

    @Test
    public void errorGetWithTimeout() throws Exception {
        Future<?> f = Single.error(new TestException()).toFuture();
        try {
            f.get(5, TimeUnit.SECONDS);
            fail("Should have thrown");
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof TestException);
        }
    }

    @Test
    public void normalGetWitHTimeout() throws Exception {
        Future<Integer> f = Single.just(1).toFuture();
        assertEquals(1, f.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void getAwait() throws Exception {
        Future<Integer> f = Single.just(1).delay(100, TimeUnit.MILLISECONDS).toFuture();
        assertEquals(1, f.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void onSuccessCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final Future<?> f = ps.single(-99).toFuture();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    f.cancel(true);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onErrorCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Future<?> f = ps.single(-99).toFuture();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        f.cancel(true);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelRace, this.description("cancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeout, this.description("timeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorGetWithTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorGetWithTimeout, this.description("errorGetWithTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalGetWitHTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalGetWitHTimeout, this.description("normalGetWitHTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getAwait() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getAwait, this.description("getAwait"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessCancelRace, this.description("onSuccessCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCancelRace, this.description("onErrorCancelRace"));
        }

        private FutureSingleObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FutureSingleObserverTest();
        }

        @java.lang.Override
        public FutureSingleObserverTest implementation() {
            return this.implementation;
        }
    }
}
