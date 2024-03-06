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
package io.reactivex.rxjava3.schedulers;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.RxThreadFactory;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ExecutorSchedulerInterruptibleTest extends AbstractSchedulerConcurrencyTests {

    static final Executor executor = Executors.newFixedThreadPool(2, new RxThreadFactory("TestCustomPool"));

    @Override
    protected Scheduler getScheduler() {
        return Schedulers.from(executor, true);
    }

    @Test
    public final void handledErrorIsNotDeliveredToThreadHandler() throws InterruptedException {
        SchedulerTestHelper.handledErrorIsNotDeliveredToThreadHandler(getScheduler());
    }

    @Test
    public void cancelledTaskRetention() throws InterruptedException {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Scheduler s = Schedulers.from(exec, true);
        try {
            Scheduler.Worker w = s.createWorker();
            try {
                ExecutorSchedulerTest.cancelledRetention(w, false);
            } finally {
                w.dispose();
            }
            w = s.createWorker();
            try {
                ExecutorSchedulerTest.cancelledRetention(w, true);
            } finally {
                w.dispose();
            }
        } finally {
            exec.shutdownNow();
        }
    }

    /**
     * A simple executor which queues tasks and executes them one-by-one if executeOne() is called.
     */
    static final class TestExecutor implements Executor {

        final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void execute(Runnable command) {
            queue.offer(command);
        }

        public void executeOne() {
            Runnable r = queue.poll();
            if (r != null) {
                r.run();
            }
        }

        public void executeAll() {
            Runnable r;
            while ((r = queue.poll()) != null) {
                r.run();
            }
        }
    }

    @Test
    public void cancelledTasksDontRun() {
        final AtomicInteger calls = new AtomicInteger();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        };
        TestExecutor exec = new TestExecutor();
        Scheduler custom = Schedulers.from(exec, true);
        Worker w = custom.createWorker();
        try {
            Disposable d1 = w.schedule(task);
            Disposable d2 = w.schedule(task);
            Disposable d3 = w.schedule(task);
            d1.dispose();
            d2.dispose();
            d3.dispose();
            exec.executeAll();
            assertEquals(0, calls.get());
        } finally {
            w.dispose();
        }
    }

    @Test
    public void cancelledWorkerDoesntRunTasks() {
        final AtomicInteger calls = new AtomicInteger();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        };
        TestExecutor exec = new TestExecutor();
        Scheduler custom = Schedulers.from(exec, true);
        Worker w = custom.createWorker();
        try {
            w.schedule(task);
            w.schedule(task);
            w.schedule(task);
        } finally {
            w.dispose();
        }
        exec.executeAll();
        assertEquals(0, calls.get());
    }

    @Test
    public void plainExecutor() throws Exception {
        Scheduler s = Schedulers.from(new Executor() {

            @Override
            public void execute(Runnable r) {
                r.run();
            }
        }, true);
        final CountDownLatch cdl = new CountDownLatch(5);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                cdl.countDown();
            }
        };
        s.scheduleDirect(r);
        s.scheduleDirect(r, 50, TimeUnit.MILLISECONDS);
        Disposable d = s.schedulePeriodicallyDirect(r, 10, 10, TimeUnit.MILLISECONDS);
        try {
            assertTrue(cdl.await(5, TimeUnit.SECONDS));
        } finally {
            d.dispose();
        }
        assertTrue(d.isDisposed());
    }

    @Test
    public void rejectingExecutor() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.shutdown();
        Scheduler s = Schedulers.from(exec, true);
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            assertSame(EmptyDisposable.INSTANCE, s.scheduleDirect(Functions.EMPTY_RUNNABLE));
            assertSame(EmptyDisposable.INSTANCE, s.scheduleDirect(Functions.EMPTY_RUNNABLE, 10, TimeUnit.MILLISECONDS));
            assertSame(EmptyDisposable.INSTANCE, s.schedulePeriodicallyDirect(Functions.EMPTY_RUNNABLE, 10, 10, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, RejectedExecutionException.class);
            TestHelper.assertUndeliverable(errors, 1, RejectedExecutionException.class);
            TestHelper.assertUndeliverable(errors, 2, RejectedExecutionException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void rejectingExecutorWorker() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.shutdown();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Worker s = Schedulers.from(exec, true).createWorker();
            assertSame(EmptyDisposable.INSTANCE, s.schedule(Functions.EMPTY_RUNNABLE));
            s = Schedulers.from(exec, true).createWorker();
            assertSame(EmptyDisposable.INSTANCE, s.schedule(Functions.EMPTY_RUNNABLE, 10, TimeUnit.MILLISECONDS));
            s = Schedulers.from(exec, true).createWorker();
            assertSame(EmptyDisposable.INSTANCE, s.schedulePeriodically(Functions.EMPTY_RUNNABLE, 10, 10, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, RejectedExecutionException.class);
            TestHelper.assertUndeliverable(errors, 1, RejectedExecutionException.class);
            TestHelper.assertUndeliverable(errors, 2, RejectedExecutionException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void reuseScheduledExecutor() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            Scheduler s = Schedulers.from(exec, true);
            final CountDownLatch cdl = new CountDownLatch(8);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    cdl.countDown();
                }
            };
            s.scheduleDirect(r);
            s.scheduleDirect(r, 10, TimeUnit.MILLISECONDS);
            Disposable d = s.schedulePeriodicallyDirect(r, 10, 10, TimeUnit.MILLISECONDS);
            try {
                assertTrue(cdl.await(5, TimeUnit.SECONDS));
            } finally {
                d.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void reuseScheduledExecutorAsWorker() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Worker s = Schedulers.from(exec, true).createWorker();
        assertFalse(s.isDisposed());
        try {
            final CountDownLatch cdl = new CountDownLatch(8);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    cdl.countDown();
                }
            };
            s.schedule(r);
            s.schedule(r, 10, TimeUnit.MILLISECONDS);
            Disposable d = s.schedulePeriodically(r, 10, 10, TimeUnit.MILLISECONDS);
            try {
                assertTrue(cdl.await(5, TimeUnit.SECONDS));
            } finally {
                d.dispose();
            }
        } finally {
            s.dispose();
            exec.shutdown();
        }
        assertTrue(s.isDisposed());
    }

    @Test
    public void disposeRace() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        final Scheduler s = Schedulers.from(exec, true);
        try {
            for (int i = 0; i < 500; i++) {
                final Worker w = s.createWorker();
                final AtomicInteger c = new AtomicInteger(2);
                w.schedule(new Runnable() {

                    @Override
                    public void run() {
                        c.decrementAndGet();
                        while (c.get() != 0) {
                        }
                    }
                });
                c.decrementAndGet();
                while (c.get() != 0) {
                }
                w.dispose();
            }
        } finally {
            exec.shutdownNow();
        }
    }

    @Test
    public void runnableDisposed() {
        final Scheduler s = Schedulers.from(new Executor() {

            @Override
            public void execute(Runnable r) {
                r.run();
            }
        }, true);
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE);
        assertTrue(d.isDisposed());
    }

    @Test
    public void runnableDisposedAsync() throws Exception {
        final Scheduler s = Schedulers.from(new Executor() {

            @Override
            public void execute(Runnable r) {
                new Thread(r).start();
            }
        }, true);
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsync2() throws Exception {
        final Scheduler s = Schedulers.from(executor, true);
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncCrash() throws Exception {
        final Scheduler s = Schedulers.from(new Executor() {

            @Override
            public void execute(Runnable r) {
                new Thread(r).start();
            }
        }, true);
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
        final Scheduler s = Schedulers.from(new Executor() {

            @Override
            public void execute(Runnable r) {
                new Thread(r).start();
            }
        }, true);
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE, 1, TimeUnit.MILLISECONDS);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncTimed2() throws Exception {
        ExecutorService executorScheduler = Executors.newScheduledThreadPool(1, new RxThreadFactory("TestCustomPoolTimed"));
        try {
            final Scheduler s = Schedulers.from(executorScheduler, true);
            Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE, 1, TimeUnit.MILLISECONDS);
            while (!d.isDisposed()) {
                Thread.sleep(1);
            }
        } finally {
            executorScheduler.shutdownNow();
        }
    }

    @Test
    public void unwrapScheduleDirectTaskAfterDispose() {
        Scheduler scheduler = getScheduler();
        final CountDownLatch cdl = new CountDownLatch(1);
        Runnable countDownRunnable = new Runnable() {

            @Override
            public void run() {
                cdl.countDown();
            }
        };
        Disposable disposable = scheduler.scheduleDirect(countDownRunnable, 100, TimeUnit.MILLISECONDS);
        SchedulerRunnableIntrospection wrapper = (SchedulerRunnableIntrospection) disposable;
        assertSame(countDownRunnable, wrapper.getWrappedRunnable());
        disposable.dispose();
        assertSame(Functions.EMPTY_RUNNABLE, wrapper.getWrappedRunnable());
    }

    @Test
    public void interruptibleDirectTask() throws Exception {
        Scheduler scheduler = getScheduler();
        final AtomicInteger sync = new AtomicInteger(2);
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        Disposable d = scheduler.scheduleDirect(new Runnable() {

            @Override
            public void run() {
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    isInterrupted.set(true);
                }
            }
        });
        if (sync.decrementAndGet() != 0) {
            while (sync.get() != 0) {
            }
        }
        Thread.sleep(500);
        d.dispose();
        int i = 20;
        while (i-- > 0 && !isInterrupted.get()) {
            Thread.sleep(50);
        }
        assertTrue("Interruption did not propagate", isInterrupted.get());
    }

    @Test
    public void interruptibleWorkerTask() throws Exception {
        Scheduler scheduler = getScheduler();
        Worker worker = scheduler.createWorker();
        try {
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = worker.schedule(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            });
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertTrue("Interruption did not propagate", isInterrupted.get());
        } finally {
            worker.dispose();
        }
    }

    @Test
    public void interruptibleDirectTaskScheduledExecutor() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, true);
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = scheduler.scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            });
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertTrue("Interruption did not propagate", isInterrupted.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void interruptibleWorkerTaskScheduledExecutor() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, true);
            Worker worker = scheduler.createWorker();
            try {
                final AtomicInteger sync = new AtomicInteger(2);
                final AtomicBoolean isInterrupted = new AtomicBoolean();
                Disposable d = worker.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            isInterrupted.set(true);
                        }
                    }
                });
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                Thread.sleep(500);
                d.dispose();
                int i = 20;
                while (i-- > 0 && !isInterrupted.get()) {
                    Thread.sleep(50);
                }
                assertTrue("Interruption did not propagate", isInterrupted.get());
            } finally {
                worker.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleDirectTask() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = scheduler.scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            });
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertFalse("Interruption happened", isInterrupted.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleWorkerTask() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            Worker worker = scheduler.createWorker();
            try {
                final AtomicInteger sync = new AtomicInteger(2);
                final AtomicBoolean isInterrupted = new AtomicBoolean();
                Disposable d = worker.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            isInterrupted.set(true);
                        }
                    }
                });
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                Thread.sleep(500);
                d.dispose();
                int i = 20;
                while (i-- > 0 && !isInterrupted.get()) {
                    Thread.sleep(50);
                }
                assertFalse("Interruption happened", isInterrupted.get());
            } finally {
                worker.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleDirectTaskScheduledExecutor() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = scheduler.scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            });
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertFalse("Interruption happened", isInterrupted.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleWorkerTaskScheduledExecutor() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            Worker worker = scheduler.createWorker();
            try {
                final AtomicInteger sync = new AtomicInteger(2);
                final AtomicBoolean isInterrupted = new AtomicBoolean();
                Disposable d = worker.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            isInterrupted.set(true);
                        }
                    }
                });
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                Thread.sleep(500);
                d.dispose();
                int i = 20;
                while (i-- > 0 && !isInterrupted.get()) {
                    Thread.sleep(50);
                }
                assertFalse("Interruption happened", isInterrupted.get());
            } finally {
                worker.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleDirectTaskTimed() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = scheduler.scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            }, 1, TimeUnit.MILLISECONDS);
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertFalse("Interruption happened", isInterrupted.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleWorkerTaskTimed() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            Worker worker = scheduler.createWorker();
            try {
                final AtomicInteger sync = new AtomicInteger(2);
                final AtomicBoolean isInterrupted = new AtomicBoolean();
                Disposable d = worker.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            isInterrupted.set(true);
                        }
                    }
                }, 1, TimeUnit.MILLISECONDS);
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                Thread.sleep(500);
                d.dispose();
                int i = 20;
                while (i-- > 0 && !isInterrupted.get()) {
                    Thread.sleep(50);
                }
                assertFalse("Interruption happened", isInterrupted.get());
            } finally {
                worker.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleDirectTaskScheduledExecutorTimed() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicBoolean isInterrupted = new AtomicBoolean();
            Disposable d = scheduler.scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                }
            }, 1, TimeUnit.MILLISECONDS);
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) {
                }
            }
            Thread.sleep(500);
            d.dispose();
            int i = 20;
            while (i-- > 0 && !isInterrupted.get()) {
                Thread.sleep(50);
            }
            assertFalse("Interruption happened", isInterrupted.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void nonInterruptibleWorkerTaskScheduledExecutorTimed() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        try {
            Scheduler scheduler = Schedulers.from(exec, false);
            Worker worker = scheduler.createWorker();
            try {
                final AtomicInteger sync = new AtomicInteger(2);
                final AtomicBoolean isInterrupted = new AtomicBoolean();
                Disposable d = worker.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            isInterrupted.set(true);
                        }
                    }
                }, 1, TimeUnit.MILLISECONDS);
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
                Thread.sleep(500);
                d.dispose();
                int i = 20;
                while (i-- > 0 && !isInterrupted.get()) {
                    Thread.sleep(50);
                }
                assertFalse("Interruption happened", isInterrupted.get());
            } finally {
                worker.dispose();
            }
        } finally {
            exec.shutdown();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.schedulers.AbstractSchedulerConcurrencyTests._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_handledErrorIsNotDeliveredToThreadHandler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::handledErrorIsNotDeliveredToThreadHandler, this.description("handledErrorIsNotDeliveredToThreadHandler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledTaskRetention() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelledTaskRetention, this.description("cancelledTaskRetention"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledTasksDontRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelledTasksDontRun, this.description("cancelledTasksDontRun"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledWorkerDoesntRunTasks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelledWorkerDoesntRunTasks, this.description("cancelledWorkerDoesntRunTasks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_plainExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::plainExecutor, this.description("plainExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectingExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rejectingExecutor, this.description("rejectingExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectingExecutorWorker() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rejectingExecutorWorker, this.description("rejectingExecutorWorker"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reuseScheduledExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reuseScheduledExecutor, this.description("reuseScheduledExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reuseScheduledExecutorAsWorker() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reuseScheduledExecutorAsWorker, this.description("reuseScheduledExecutorAsWorker"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposed, this.description("runnableDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsync, this.description("runnableDisposedAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsync2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsync2, this.description("runnableDisposedAsync2"));
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
        public void benchmark_runnableDisposedAsyncTimed2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableDisposedAsyncTimed2, this.description("runnableDisposedAsyncTimed2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unwrapScheduleDirectTaskAfterDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unwrapScheduleDirectTaskAfterDispose, this.description("unwrapScheduleDirectTaskAfterDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptibleDirectTask() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptibleDirectTask, this.description("interruptibleDirectTask"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptibleWorkerTask() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptibleWorkerTask, this.description("interruptibleWorkerTask"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptibleDirectTaskScheduledExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptibleDirectTaskScheduledExecutor, this.description("interruptibleDirectTaskScheduledExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptibleWorkerTaskScheduledExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptibleWorkerTaskScheduledExecutor, this.description("interruptibleWorkerTaskScheduledExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleDirectTask() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleDirectTask, this.description("nonInterruptibleDirectTask"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleWorkerTask() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleWorkerTask, this.description("nonInterruptibleWorkerTask"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleDirectTaskScheduledExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleDirectTaskScheduledExecutor, this.description("nonInterruptibleDirectTaskScheduledExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleWorkerTaskScheduledExecutor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleWorkerTaskScheduledExecutor, this.description("nonInterruptibleWorkerTaskScheduledExecutor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleDirectTaskTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleDirectTaskTimed, this.description("nonInterruptibleDirectTaskTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleWorkerTaskTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleWorkerTaskTimed, this.description("nonInterruptibleWorkerTaskTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleDirectTaskScheduledExecutorTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleDirectTaskScheduledExecutorTimed, this.description("nonInterruptibleDirectTaskScheduledExecutorTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonInterruptibleWorkerTaskScheduledExecutorTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonInterruptibleWorkerTaskScheduledExecutorTimed, this.description("nonInterruptibleWorkerTaskScheduledExecutorTimed"));
        }

        private ExecutorSchedulerInterruptibleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ExecutorSchedulerInterruptibleTest();
        }

        @java.lang.Override
        public ExecutorSchedulerInterruptibleTest implementation() {
            return this.implementation;
        }
    }
}
