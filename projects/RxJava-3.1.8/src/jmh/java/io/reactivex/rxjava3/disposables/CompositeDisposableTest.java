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
package io.reactivex.rxjava3.disposables;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompositeDisposableTest extends RxJavaTest {

    @Test
    public void success() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.dispose();
        assertEquals(2, counter.get());
    }

    @Test
    public void shouldUnsubscribeAll() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final CompositeDisposable cd = new CompositeDisposable();
        final int count = 10;
        final CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < count; i++) {
            cd.add(Disposable.fromRunnable(new Runnable() {

                @Override
                public void run() {
                    counter.incrementAndGet();
                }
            }));
        }
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        cd.dispose();
                    } catch (final InterruptedException e) {
                        fail(e.getMessage());
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        for (final Thread t : threads) {
            t.join();
        }
        assertEquals(count, counter.get());
    }

    @Test
    public void exception() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on first one");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        try {
            cd.dispose();
            fail("Expecting an exception");
        } catch (RuntimeException e) {
            // we expect this
            assertEquals(e.getMessage(), "failed on first one");
        }
        // we should still have disposed to the second one
        assertEquals(1, counter.get());
    }

    @Test
    public void compositeException() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on first one");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on second one too");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        try {
            cd.dispose();
            fail("Expecting an exception");
        } catch (CompositeException e) {
            // we expect this
            assertEquals(e.getExceptions().size(), 2);
        }
        // we should still have disposed to the second one
        assertEquals(1, counter.get());
    }

    @Test
    public void removeUnsubscribes() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(d1);
        cd.add(d2);
        cd.remove(d1);
        assertTrue(d1.isDisposed());
        assertFalse(d2.isDisposed());
    }

    @Test
    public void clear() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(d1);
        cd.add(d2);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        cd.clear();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        assertFalse(cd.isDisposed());
        Disposable d3 = Disposable.empty();
        cd.add(d3);
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(cd.isDisposed());
    }

    @Test
    public void unsubscribeIdempotence() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.dispose();
        cd.dispose();
        cd.dispose();
        // we should have only disposed once
        assertEquals(1, counter.get());
    }

    @Test
    public void unsubscribeIdempotenceConcurrently() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final CompositeDisposable cd = new CompositeDisposable();
        final int count = 10;
        final CountDownLatch start = new CountDownLatch(1);
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        cd.dispose();
                    } catch (final InterruptedException e) {
                        fail(e.getMessage());
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        for (final Thread t : threads) {
            t.join();
        }
        // we should have only disposed once
        assertEquals(1, counter.get());
    }

    @Test
    public void tryRemoveIfNotIn() {
        CompositeDisposable cd = new CompositeDisposable();
        CompositeDisposable cd1 = new CompositeDisposable();
        CompositeDisposable cd2 = new CompositeDisposable();
        cd.add(cd1);
        cd.remove(cd1);
        cd.add(cd2);
        // try removing agian
        cd.remove(cd1);
    }

    @Test(expected = NullPointerException.class)
    public void addingNullDisposableIllegal() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(null);
    }

    @Test
    public void initializeVarargs() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable(d1, d2);
        assertEquals(2, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        cd = new CompositeDisposable(d3, d4);
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertEquals(0, cd.size());
    }

    @Test
    public void initializeIterable() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable(Arrays.asList(d1, d2));
        assertEquals(2, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        cd = new CompositeDisposable(Arrays.asList(d3, d4));
        assertEquals(2, cd.size());
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertEquals(0, cd.size());
    }

    @Test
    public void addAll() {
        CompositeDisposable cd = new CompositeDisposable();
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        Disposable d3 = Disposable.empty();
        cd.addAll(d1, d2);
        cd.addAll(d3);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        assertFalse(d3.isDisposed());
        cd.clear();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        cd = new CompositeDisposable();
        cd.addAll(d1, d2);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        cd.dispose();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        assertEquals(0, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
    }

    @Test
    public void addAfterDisposed() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.dispose();
        Disposable d1 = Disposable.empty();
        assertFalse(cd.add(d1));
        assertTrue(d1.isDisposed());
        d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        assertFalse(cd.addAll(d1, d2));
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void delete() {
        CompositeDisposable cd = new CompositeDisposable();
        Disposable d1 = Disposable.empty();
        assertFalse(cd.delete(d1));
        Disposable d2 = Disposable.empty();
        cd.add(d2);
        assertFalse(cd.delete(d1));
        cd.dispose();
        assertFalse(cd.delete(d1));
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addAllRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void removeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void deleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void clearRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void addAllDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void removeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void deleteDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void clearDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void sizeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.size();
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void disposeThrowsIAE() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new IllegalArgumentException();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        assertTrue(d1.isDisposed());
    }

    @Test
    public void disposeThrowsError() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new AssertionError();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (AssertionError ex) {
            // expected
        }
        assertTrue(d1.isDisposed());
    }

    @Test
    public void disposeThrowsCheckedException() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new IOException();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (RuntimeException ex) {
            // expected
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + " should have thrown RuntimeException(IOException)");
            }
        }
        assertTrue(d1.isDisposed());
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void throwSneaky() throws E {
        throw (E) new IOException();
    }

    @Test
    public void disposeThrowsCheckedExceptionSneaky() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(new Disposable() {

            @Override
            public void dispose() {
                CompositeDisposableTest.<RuntimeException>throwSneaky();
            }

            @Override
            public boolean isDisposed() {
                // TODO Auto-generated method stub
                return false;
            }
        });
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (RuntimeException ex) {
            // expected
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + " should have thrown RuntimeException(IOException)");
            }
        }
        assertTrue(d1.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::success, this.description("success"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldUnsubscribeAll, this.description("shouldUnsubscribeAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exception() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::exception, this.description("exception"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::compositeException, this.description("compositeException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeUnsubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::removeUnsubscribes, this.description("removeUnsubscribes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clear() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clear, this.description("clear"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeIdempotence() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribeIdempotence, this.description("unsubscribeIdempotence"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeIdempotenceConcurrently() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribeIdempotenceConcurrently, this.description("unsubscribeIdempotenceConcurrently"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryRemoveIfNotIn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryRemoveIfNotIn, this.description("tryRemoveIfNotIn"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addingNullDisposableIllegal() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::addingNullDisposableIllegal, this.description("addingNullDisposableIllegal"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initializeVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::initializeVarargs, this.description("initializeVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initializeIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::initializeIterable, this.description("initializeIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAll, this.description("addAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAfterDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAfterDisposed, this.description("addAfterDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delete, this.description("delete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addRace, this.description("addRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAllRace, this.description("addAllRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::removeRace, this.description("removeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deleteRace, this.description("deleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearRace, this.description("clearRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addDisposeRace, this.description("addDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAllDisposeRace, this.description("addAllDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::removeDisposeRace, this.description("removeDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deleteDisposeRace, this.description("deleteDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearDisposeRace, this.description("clearDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sizeDisposeRace, this.description("sizeDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsIAE() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeThrowsIAE, this.description("disposeThrowsIAE"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeThrowsError, this.description("disposeThrowsError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsCheckedException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeThrowsCheckedException, this.description("disposeThrowsCheckedException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsCheckedExceptionSneaky() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeThrowsCheckedExceptionSneaky, this.description("disposeThrowsCheckedExceptionSneaky"));
        }

        private CompositeDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompositeDisposableTest();
        }

        @java.lang.Override
        public CompositeDisposableTest implementation() {
            return this.implementation;
        }
    }
}
