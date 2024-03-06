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
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.internal.disposables.SequentialDisposable;

@RunWith(MockitoJUnitRunner.class)
public class SequentialDisposableTest extends RxJavaTest {

    private SequentialDisposable serialDisposable;

    @Before
    public void setUp() {
        serialDisposable = new SequentialDisposable();
    }

    @Test
    public void unsubscribingWithoutUnderlyingDoesNothing() {
        serialDisposable.dispose();
    }

    @Test
    public void getDisposableShouldReturnset() {
        final Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        assertSame(underlying, serialDisposable.get());
        final Disposable another = mock(Disposable.class);
        serialDisposable.update(another);
        assertSame(another, serialDisposable.get());
    }

    @Test
    public void notDisposedWhenReplaced() {
        final Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        serialDisposable.replace(Disposable.empty());
        serialDisposable.dispose();
        verify(underlying, never()).dispose();
    }

    @Test
    public void unsubscribingTwiceDoesUnsubscribeOnce() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        serialDisposable.dispose();
        verify(underlying).dispose();
        serialDisposable.dispose();
        verifyNoMoreInteractions(underlying);
    }

    @Test
    public void settingSameDisposableTwiceDoesUnsubscribeIt() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        verifyNoInteractions(underlying);
        serialDisposable.update(underlying);
        verify(underlying).dispose();
    }

    @Test
    public void unsubscribingWithSingleUnderlyingUnsubscribes() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        underlying.dispose();
        verify(underlying).dispose();
    }

    @Test
    public void replacingFirstUnderlyingCausesUnsubscription() {
        Disposable first = mock(Disposable.class);
        serialDisposable.update(first);
        Disposable second = mock(Disposable.class);
        serialDisposable.update(second);
        verify(first).dispose();
    }

    @Test
    public void whenUnsubscribingSecondUnderlyingUnsubscribed() {
        Disposable first = mock(Disposable.class);
        serialDisposable.update(first);
        Disposable second = mock(Disposable.class);
        serialDisposable.update(second);
        serialDisposable.dispose();
        verify(second).dispose();
    }

    @Test
    public void settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription() {
        serialDisposable.dispose();
        Disposable underlying = mock(Disposable.class);
        serialDisposable.update(underlying);
        verify(underlying).dispose();
    }

    @Test
    public void settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently() throws InterruptedException {
        final Disposable firstSet = mock(Disposable.class);
        serialDisposable.update(firstSet);
        final CountDownLatch start = new CountDownLatch(1);
        final int count = 10;
        final CountDownLatch end = new CountDownLatch(count);
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        serialDisposable.dispose();
                    } catch (InterruptedException e) {
                        fail(e.getMessage());
                    } finally {
                        end.countDown();
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        final Disposable underlying = mock(Disposable.class);
        start.countDown();
        serialDisposable.update(underlying);
        end.await();
        verify(firstSet).dispose();
        verify(underlying).dispose();
        for (final Thread t : threads) {
            t.join();
        }
    }

    @Test
    public void concurrentSetDisposableShouldNotInterleave() throws InterruptedException {
        final int count = 10;
        final List<Disposable> subscriptions = new ArrayList<>();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(count);
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Disposable subscription = mock(Disposable.class);
            subscriptions.add(subscription);
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        serialDisposable.update(subscription);
                    } catch (InterruptedException e) {
                        fail(e.getMessage());
                    } finally {
                        end.countDown();
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        end.await();
        serialDisposable.dispose();
        for (final Disposable subscription : subscriptions) {
            verify(subscription).dispose();
        }
        for (final Thread t : threads) {
            t.join();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingWithoutUnderlyingDoesNothing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribingWithoutUnderlyingDoesNothing, this.description("unsubscribingWithoutUnderlyingDoesNothing"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getDisposableShouldReturnset() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getDisposableShouldReturnset, this.description("getDisposableShouldReturnset"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notDisposedWhenReplaced() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::notDisposedWhenReplaced, this.description("notDisposedWhenReplaced"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingTwiceDoesUnsubscribeOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribingTwiceDoesUnsubscribeOnce, this.description("unsubscribingTwiceDoesUnsubscribeOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingSameDisposableTwiceDoesUnsubscribeIt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::settingSameDisposableTwiceDoesUnsubscribeIt, this.description("settingSameDisposableTwiceDoesUnsubscribeIt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingWithSingleUnderlyingUnsubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribingWithSingleUnderlyingUnsubscribes, this.description("unsubscribingWithSingleUnderlyingUnsubscribes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replacingFirstUnderlyingCausesUnsubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replacingFirstUnderlyingCausesUnsubscription, this.description("replacingFirstUnderlyingCausesUnsubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whenUnsubscribingSecondUnderlyingUnsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::whenUnsubscribingSecondUnderlyingUnsubscribed, this.description("whenUnsubscribingSecondUnderlyingUnsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription, this.description("settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently, this.description("settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concurrentSetDisposableShouldNotInterleave() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concurrentSetDisposableShouldNotInterleave, this.description("concurrentSetDisposableShouldNotInterleave"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private SequentialDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SequentialDisposableTest();
        }

        @java.lang.Override
        public SequentialDisposableTest implementation() {
            return this.implementation;
        }
    }
}
