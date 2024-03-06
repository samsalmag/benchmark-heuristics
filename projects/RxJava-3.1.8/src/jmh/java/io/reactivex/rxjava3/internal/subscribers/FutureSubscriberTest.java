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
package io.reactivex.rxjava3.internal.subscribers;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class FutureSubscriberTest extends RxJavaTest {

    FutureSubscriber<Integer> fs;

    @Before
    public void before() {
        fs = new FutureSubscriber<>();
    }

    @Test
    public void cancel() throws Exception {
        assertFalse(fs.isDone());
        assertFalse(fs.isCancelled());
        fs.cancel();
        fs.cancel();
        fs.request(10);
        fs.request(-99);
        fs.cancel(false);
        assertTrue(fs.isDone());
        assertTrue(fs.isCancelled());
        try {
            fs.get();
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
        try {
            fs.get(1, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
    }

    @Test
    public void onError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fs.onError(new TestException("One"));
            fs.onError(new TestException("Two"));
            try {
                fs.get(5, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
                assertEquals("One", ex.getCause().getMessage());
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNext() throws Exception {
        fs.onNext(1);
        fs.onComplete();
        assertEquals(1, fs.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void onSubscribe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BooleanSubscription s = new BooleanSubscription();
            fs.onSubscribe(s);
            BooleanSubscription s2 = new BooleanSubscription();
            fs.onSubscribe(s2);
            assertFalse(s.isCancelled());
            assertTrue(s2.isCancelled());
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Subscription already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void await() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fs.onNext(1);
                fs.onComplete();
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, fs.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    @SuppressUndeliverable
    public void onErrorCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    fs.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void onCompleteCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            if (i % 3 == 0) {
                fs.onSubscribe(new BooleanSubscription());
            }
            if (i % 2 == 0) {
                fs.onNext(1);
            }
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    fs.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void onErrorOnComplete() throws Exception {
        fs.onError(new TestException("One"));
        fs.onComplete();
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            assertEquals("One", ex.getCause().getMessage());
        }
    }

    @Test
    @SuppressUndeliverable
    public void onCompleteOnError() throws Exception {
        fs.onComplete();
        fs.onError(new TestException("One"));
        try {
            assertNull(fs.get(5, TimeUnit.MILLISECONDS));
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof NoSuchElementException);
        }
    }

    @Test
    @SuppressUndeliverable
    public void cancelOnError() throws Exception {
        fs.cancel(true);
        fs.onError(new TestException("One"));
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
    }

    @Test
    @SuppressUndeliverable
    public void cancelOnComplete() throws Exception {
        fs.cancel(true);
        fs.onComplete();
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
    }

    @Test
    public void onNextThenOnCompleteTwice() throws Exception {
        fs.onNext(1);
        fs.onComplete();
        fs.onComplete();
        assertEquals(1, fs.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void completeAsync() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fs.onNext(1);
                fs.onComplete();
            }
        }, 500, TimeUnit.MILLISECONDS);
        assertEquals(1, fs.get().intValue());
    }

    @Test
    public void getTimedOut() throws Exception {
        try {
            fs.get(1, TimeUnit.NANOSECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(1, TimeUnit.NANOSECONDS), expected.getMessage());
        }
    }

    @Test
    public void onNextCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fs.onNext(1);
            fs.onComplete();
            fs.onError(new TestException("One"));
            assertEquals((Integer) 1, fs.get(5, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
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
        public void benchmark_onError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onError, this.description("onError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNext, this.description("onNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribe, this.description("onSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelRace, this.description("cancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_await() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::await, this.description("await"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCancelRace, this.description("onErrorCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteCancelRace, this.description("onCompleteCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorOnComplete, this.description("onErrorOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnError, this.description("onCompleteOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnError, this.description("cancelOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnComplete, this.description("cancelOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThenOnCompleteTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextThenOnCompleteTwice, this.description("onNextThenOnCompleteTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeAsync, this.description("completeAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getTimedOut() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getTimedOut, this.description("getTimedOut"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCompleteOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextCompleteOnError, this.description("onNextCompleteOnError"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FutureSubscriberTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FutureSubscriberTest();
        }

        @java.lang.Override
        public FutureSubscriberTest implementation() {
            return this.implementation;
        }
    }
}
