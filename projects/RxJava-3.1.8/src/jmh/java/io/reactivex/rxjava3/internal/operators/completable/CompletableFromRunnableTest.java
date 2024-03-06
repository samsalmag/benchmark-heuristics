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
package io.reactivex.rxjava3.internal.operators.completable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromRunnableTest extends RxJavaTest {

    @Test
    public void fromRunnable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Runnable run = new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        };
        Completable.fromRunnable(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Completable.fromRunnable(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromRunnableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable completable = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableThrows() {
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromRunnableDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.incrementAndGet();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void fromRunnableErrorsDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.incrementAndGet();
                throw new TestException();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Runnable run = mock(Runnable.class);
        Completable.fromRunnable(run).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void disposeWhileRunningComplete() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.fromRunnable(() -> {
            to.dispose();
        }).subscribeWith(to).assertEmpty();
    }

    @Test
    public void disposeWhileRunningError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestObserver<Void> to = new TestObserver<>();
            Completable.fromRunnable(() -> {
                to.dispose();
                throw new TestException();
            }).subscribeWith(to).assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnable, this.description("fromRunnable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableTwice, this.description("fromRunnableTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableInvokesLazy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableInvokesLazy, this.description("fromRunnableInvokesLazy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableThrows, this.description("fromRunnableThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableDisposed, this.description("fromRunnableDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableErrorsDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableErrorsDisposed, this.description("fromRunnableErrorsDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedUpfront, this.description("disposedUpfront"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileRunningComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeWhileRunningComplete, this.description("disposeWhileRunningComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileRunningError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeWhileRunningError, this.description("disposeWhileRunningError"));
        }

        private CompletableFromRunnableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableFromRunnableTest();
        }

        @java.lang.Override
        public CompletableFromRunnableTest implementation() {
            return this.implementation;
        }
    }
}
