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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ScheduledRunnableTest extends RxJavaTest {

    @Test
    public void dispose() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        set.dispose();
        assertTrue(run.isDisposed());
    }

    @Test
    public void disposeRun() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        run.dispose();
        run.dispose();
        assertTrue(run.isDisposed());
    }

    @Test
    public void setFutureCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, 0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void setFutureRunRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, 0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.run();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r1);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void runDispose() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.call();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void pluginCrash() {
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                throw new TestException("Second");
            }
        });
        CompositeDisposable set = new CompositeDisposable();
        final ScheduledRunnable run = new ScheduledRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException("First");
            }
        }, set);
        set.add(run);
        try {
            run.run();
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Second", ex.getMessage());
        } finally {
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
        assertTrue(run.isDisposed());
        assertEquals(0, set.size());
    }

    @Test
    public void crashReported() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(new Runnable() {

                @Override
                public void run() {
                    throw new TestException("First");
                }
            }, set);
            set.add(run);
            try {
                run.run();
                fail("Should have thrown!");
            } catch (TestException expected) {
                // expected
            }
            assertTrue(run.isDisposed());
            assertEquals(0, set.size());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withoutParentDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.call();
    }

    @Test
    public void withParentDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, new CompositeDisposable());
        run.dispose();
        run.call();
    }

    @Test
    public void withFutureDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.dispose();
        run.call();
    }

    @Test
    public void withFutureDisposed2() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.call();
    }

    @Test
    public void withFutureDisposed3() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.set(2, Thread.currentThread());
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.call();
    }

    @Test
    public void runFuture() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Void> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.call();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void syncWorkerCancelRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final CompositeDisposable set = new CompositeDisposable();
            final AtomicBoolean interrupted = new AtomicBoolean();
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicInteger syncb = new AtomicInteger(2);
            Runnable r0 = new Runnable() {

                @Override
                public void run() {
                    set.dispose();
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    if (syncb.decrementAndGet() != 0) {
                        while (syncb.get() != 0) {
                        }
                    }
                    for (int j = 0; j < 1000; j++) {
                        if (Thread.currentThread().isInterrupted()) {
                            interrupted.set(true);
                            break;
                        }
                    }
                }
            };
            final ScheduledRunnable run = new ScheduledRunnable(r0, set);
            set.add(run);
            final FutureTask<Void> ft = new FutureTask<>(run, null);
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    run.setFuture(ft);
                    if (syncb.decrementAndGet() != 0) {
                        while (syncb.get() != 0) {
                        }
                    }
                }
            };
            TestHelper.race(ft, r2);
            assertFalse("The task was interrupted", interrupted.get());
        }
    }

    @Test
    public void disposeAfterRun() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.run();
        assertEquals(ScheduledRunnable.DONE, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.DONE, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void syncDisposeIdempotent() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.set(ScheduledRunnable.THREAD_INDEX, Thread.currentThread());
        run.dispose();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.run();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void asyncDisposeIdempotent() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.run();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void noParentIsDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        assertFalse(run.isDisposed());
        run.run();
        assertTrue(run.isDisposed());
    }

    @Test
    public void withParentIsDisposed() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        run.run();
        assertTrue(run.isDisposed());
        assertFalse(set.remove(run));
    }

    @Test
    public void toStringStates() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable task = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        assertEquals("ScheduledRunnable[Waiting]", task.toString());
        task.set(ScheduledRunnable.THREAD_INDEX, Thread.currentThread());
        assertEquals("ScheduledRunnable[Running on " + Thread.currentThread() + "]", task.toString());
        task.dispose();
        assertEquals("ScheduledRunnable[Disposed(Sync)]", task.toString());
        task.set(ScheduledRunnable.FUTURE_INDEX, ScheduledRunnable.DONE);
        assertEquals("ScheduledRunnable[Finished]", task.toString());
        task = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        task.dispose();
        assertEquals("ScheduledRunnable[Disposed(Async)]", task.toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRun, this.description("disposeRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setFutureCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setFutureCancelRace, this.description("setFutureCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setFutureRunRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setFutureRunRace, this.description("setFutureRunRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runDispose, this.description("runDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pluginCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::pluginCrash, this.description("pluginCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashReported() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crashReported, this.description("crashReported"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withoutParentDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withoutParentDisposed, this.description("withoutParentDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withParentDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withParentDisposed, this.description("withParentDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFutureDisposed, this.description("withFutureDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFutureDisposed2, this.description("withFutureDisposed2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFutureDisposed3, this.description("withFutureDisposed3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runFuture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runFuture, this.description("runFuture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncWorkerCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncWorkerCancelRace, this.description("syncWorkerCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeAfterRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeAfterRun, this.description("disposeAfterRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncDisposeIdempotent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncDisposeIdempotent, this.description("syncDisposeIdempotent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncDisposeIdempotent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncDisposeIdempotent, this.description("asyncDisposeIdempotent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noParentIsDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noParentIsDisposed, this.description("noParentIsDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withParentIsDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withParentIsDisposed, this.description("withParentIsDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toStringStates() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toStringStates, this.description("toStringStates"));
        }

        private ScheduledRunnableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScheduledRunnableTest();
        }

        @java.lang.Override
        public ScheduledRunnableTest implementation() {
            return this.implementation;
        }
    }
}
