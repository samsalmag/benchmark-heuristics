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
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromActionTest extends RxJavaTest {

    @Test
    public void fromAction() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Observable.fromAction(new Action() {

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
        Observable.fromAction(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Observable.fromAction(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromActionInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Observable<Object> source = Observable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        source.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromActionThrows() {
        Observable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void callable() throws Throwable {
        final int[] counter = { 0 };
        Observable<Void> m = Observable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter[0]++;
            }
        });
        assertTrue(m.getClass().toString(), m instanceof Supplier);
        assertNull(((Supplier<Void>) m).get());
        assertEquals(1, counter[0]);
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestObserver<Object> to = Observable.fromAction(new Action() {

                @Override
                public void run() throws Exception {
                    cdl1.countDown();
                    cdl2.await(5, TimeUnit.SECONDS);
                }
            }).subscribeOn(Schedulers.single()).test();
            assertTrue(cdl1.await(5, TimeUnit.SECONDS));
            to.dispose();
            int timeout = 10;
            while (timeout-- > 0 && errors.isEmpty()) {
                Thread.sleep(100);
            }
            TestHelper.assertUndeliverable(errors, 0, InterruptedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Action run = mock(Action.class);
        Observable.fromAction(run).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void cancelWhileRunning() {
        final TestObserver<Object> to = new TestObserver<>();
        Observable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.dispose();
            }
        }).subscribeWith(to).assertEmpty();
        assertTrue(to.isDisposed());
    }

    @Test
    public void asyncFused() throws Throwable {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ASYNC);
        Action action = mock(Action.class);
        Observable.fromAction(action).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult();
        verify(action).run();
    }

    @Test
    public void syncFusedRejected() throws Throwable {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.SYNC);
        Action action = mock(Action.class);
        Observable.fromAction(action).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult();
        verify(action).run();
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
        public void benchmark_callable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::callable, this.description("callable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrorLoss() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noErrorLoss, this.description("noErrorLoss"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedUpfront, this.description("disposedUpfront"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileRunning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelWhileRunning, this.description("cancelWhileRunning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFused, this.description("asyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedRejected, this.description("syncFusedRejected"));
        }

        private ObservableFromActionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableFromActionTest();
        }

        @java.lang.Override
        public ObservableFromActionTest implementation() {
            return this.implementation;
        }
    }
}
