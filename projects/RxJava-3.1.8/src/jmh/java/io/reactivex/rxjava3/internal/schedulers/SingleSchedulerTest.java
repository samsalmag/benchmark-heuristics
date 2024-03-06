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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.SingleScheduler.ScheduledWorker;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.*;

public class SingleSchedulerTest extends AbstractSchedulerTests {

    @Test
    @SuppressUndeliverable
    public void shutdownRejects() {
        final int[] calls = { 0 };
        Runnable r = new Runnable() {

            @Override
            public void run() {
                calls[0]++;
            }
        };
        Scheduler s = new SingleScheduler();
        s.shutdown();
        assertEquals(Disposable.disposed(), s.scheduleDirect(r));
        assertEquals(Disposable.disposed(), s.scheduleDirect(r, 1, TimeUnit.SECONDS));
        assertEquals(Disposable.disposed(), s.schedulePeriodicallyDirect(r, 1, 1, TimeUnit.SECONDS));
        Worker w = s.createWorker();
        ((ScheduledWorker) w).executor.shutdownNow();
        assertEquals(Disposable.disposed(), w.schedule(r));
        assertEquals(Disposable.disposed(), w.schedule(r, 1, TimeUnit.SECONDS));
        assertEquals(Disposable.disposed(), w.schedulePeriodically(r, 1, 1, TimeUnit.SECONDS));
        assertEquals(0, calls[0]);
        w.dispose();
        assertTrue(w.isDisposed());
    }

    @Test
    public void startRace() {
        final Scheduler s = new SingleScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            s.shutdown();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.start();
                }
            };
            TestHelper.race(r1, r1);
        }
    }

    @Test
    public void runnableDisposedAsync() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncCrash() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(new Runnable() {

            @Override
            public void run() {
                throw new IllegalStateException();
            }
        });
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncTimed() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE, 1, TimeUnit.MILLISECONDS);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Override
    protected Scheduler getScheduler() {
        return Schedulers.single();
    }

    @Test
    public void zeroPeriodRejectedExecution() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Scheduler s = RxJavaPlugins.createSingleScheduler(new RxThreadFactory("Test"));
            s.shutdown();
            Runnable run = mock(Runnable.class);
            s.schedulePeriodicallyDirect(run, 1, 0, TimeUnit.MILLISECONDS);
            Thread.sleep(100);
            verify(run, never()).run();
            TestHelper.assertUndeliverable(errors, 0, RejectedExecutionException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.schedulers.AbstractSchedulerTests._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shutdownRejects() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shutdownRejects, this.description("shutdownRejects"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startRace, this.description("startRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsync, this.description("runnableDisposedAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsyncCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsyncCrash, this.description("runnableDisposedAsyncCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsyncTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsyncTimed, this.description("runnableDisposedAsyncTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zeroPeriodRejectedExecution() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zeroPeriodRejectedExecution, this.description("zeroPeriodRejectedExecution"));
        }

        private SingleSchedulerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleSchedulerTest();
        }

        @java.lang.Override
        public SingleSchedulerTest implementation() {
            return this.implementation;
        }
    }
}
