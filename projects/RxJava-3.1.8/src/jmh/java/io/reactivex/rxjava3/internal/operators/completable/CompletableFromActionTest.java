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
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromActionTest extends RxJavaTest {

    @Test
    public void fromAction() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromActionTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Action run = new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        };
        Completable.fromAction(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Completable.fromAction(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromActionInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable completable = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromActionThrows() {
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromActionDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                calls.incrementAndGet();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void fromActionErrorsDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                calls.incrementAndGet();
                throw new TestException();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Action run = mock(Action.class);
        Completable.fromAction(run).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void disposeWhileRunningComplete() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.fromAction(() -> {
            to.dispose();
        }).subscribeWith(to).assertEmpty();
    }

    @Test
    public void disposeWhileRunningError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestObserver<Void> to = new TestObserver<>();
            Completable.fromAction(() -> {
                to.dispose();
                throw new TestException();
            }).subscribeWith(to).assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromAction, this.description("fromAction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionTwice, this.description("fromActionTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionInvokesLazy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionInvokesLazy, this.description("fromActionInvokesLazy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionThrows, this.description("fromActionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionDisposed, this.description("fromActionDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionErrorsDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionErrorsDisposed, this.description("fromActionErrorsDisposed"));
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

        private CompletableFromActionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableFromActionTest();
        }

        @java.lang.Override
        public CompletableFromActionTest implementation() {
            return this.implementation;
        }
    }
}
