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
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class TrampolineSchedulerInternalTest extends RxJavaTest {

    @Test
    @SuppressUndeliverable
    public void scheduleDirectInterrupt() {
        Thread.currentThread().interrupt();
        final int[] calls = { 0 };
        assertSame(EmptyDisposable.INSTANCE, Schedulers.trampoline().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                calls[0]++;
            }
        }, 1, TimeUnit.SECONDS));
        assertTrue(Thread.interrupted());
        assertEquals(0, calls[0]);
    }

    @Test
    public void dispose() {
        Worker w = Schedulers.trampoline().createWorker();
        assertFalse(w.isDisposed());
        w.dispose();
        assertTrue(w.isDisposed());
        assertEquals(EmptyDisposable.INSTANCE, w.schedule(Functions.EMPTY_RUNNABLE));
    }

    @Test
    public void reentrantScheduleDispose() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }).dispose();
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void reentrantScheduleShutdown() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }, 1, TimeUnit.MILLISECONDS);
                    w.dispose();
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void reentrantScheduleShutdown2() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.dispose();
                    assertSame(EmptyDisposable.INSTANCE, w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }, 1, TimeUnit.MILLISECONDS));
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    @SuppressUndeliverable
    public void reentrantScheduleInterrupt() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0 };
            Thread.currentThread().interrupt();
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                }
            }, 1, TimeUnit.DAYS);
            assertTrue(Thread.interrupted());
            assertEquals(0, calls[0]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void sleepingRunnableDisposedOnRun() {
        TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, 0);
        w.dispose();
        run.run();
        verify(r, never()).run();
    }

    @Test
    public void sleepingRunnableNoDelayRun() {
        TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, 0);
        run.run();
        verify(r).run();
    }

    @Test
    public void sleepingRunnableDisposedOnDelayedRun() {
        final TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, System.currentTimeMillis() + 200);
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                w.dispose();
            }
        }, 100, TimeUnit.MILLISECONDS);
        run.run();
        verify(r, never()).run();
    }

    @Test
    public void submitAndDisposeNextTask() {
        Scheduler.Worker w = Schedulers.trampoline().createWorker();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            Runnable run = mock(Runnable.class);
            AtomicInteger sync = new AtomicInteger(2);
            w.schedule(() -> {
                Disposable d = w.schedule(run);
                Schedulers.single().scheduleDirect(() -> {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    d.dispose();
                });
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
            });
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectInterrupt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scheduleDirectInterrupt, this.description("scheduleDirectInterrupt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantScheduleDispose, this.description("reentrantScheduleDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleShutdown() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantScheduleShutdown, this.description("reentrantScheduleShutdown"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleShutdown2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantScheduleShutdown2, this.description("reentrantScheduleShutdown2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleInterrupt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reentrantScheduleInterrupt, this.description("reentrantScheduleInterrupt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableDisposedOnRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sleepingRunnableDisposedOnRun, this.description("sleepingRunnableDisposedOnRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableNoDelayRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sleepingRunnableNoDelayRun, this.description("sleepingRunnableNoDelayRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableDisposedOnDelayedRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sleepingRunnableDisposedOnDelayedRun, this.description("sleepingRunnableDisposedOnDelayedRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_submitAndDisposeNextTask() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::submitAndDisposeNextTask, this.description("submitAndDisposeNextTask"));
        }

        private TrampolineSchedulerInternalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TrampolineSchedulerInternalTest();
        }

        @java.lang.Override
        public TrampolineSchedulerInternalTest implementation() {
            return this.implementation;
        }
    }
}
