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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableToFutureTest extends RxJavaTest {

    @Test
    public void success() throws Exception {
        @SuppressWarnings("unchecked")
        Future<Object> future = mock(Future.class);
        Object value = new Object();
        when(future.get()).thenReturn(value);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        Flowable.fromFuture(future).subscribe(ts);
        ts.cancel();
        verify(subscriber, times(1)).onNext(value);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(future, never()).cancel(anyBoolean());
    }

    @Test
    public void successOperatesOnSuppliedScheduler() throws Exception {
        @SuppressWarnings("unchecked")
        Future<Object> future = mock(Future.class);
        Object value = new Object();
        when(future.get()).thenReturn(value);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestScheduler scheduler = new TestScheduler();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        Flowable.fromFuture(future).subscribeOn(scheduler).subscribe(ts);
        verify(subscriber, never()).onNext(value);
        scheduler.triggerActions();
        verify(subscriber, times(1)).onNext(value);
    }

    @Test
    public void failure() throws Exception {
        @SuppressWarnings("unchecked")
        Future<Object> future = mock(Future.class);
        RuntimeException e = new RuntimeException();
        when(future.get()).thenThrow(e);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        Flowable.fromFuture(future).subscribe(ts);
        ts.cancel();
        verify(subscriber, never()).onNext(null);
        verify(subscriber, never()).onComplete();
        verify(subscriber, times(1)).onError(e);
        verify(future, never()).cancel(anyBoolean());
    }

    @Test
    public void cancelledBeforeSubscribe() throws Exception {
        @SuppressWarnings("unchecked")
        Future<Object> future = mock(Future.class);
        CancellationException e = new CancellationException("unit test synthetic cancellation");
        when(future.get()).thenThrow(e);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        ts.cancel();
        Flowable.fromFuture(future).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void cancellationDuringFutureGet() throws Exception {
        Future<Object> future = new Future<Object>() {

            private AtomicBoolean isCancelled = new AtomicBoolean(false);

            private AtomicBoolean isDone = new AtomicBoolean(false);

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                isCancelled.compareAndSet(false, true);
                return true;
            }

            @Override
            public boolean isCancelled() {
                return isCancelled.get();
            }

            @Override
            public boolean isDone() {
                return isCancelled() || isDone.get();
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                Thread.sleep(500);
                isDone.compareAndSet(false, true);
                return "foo";
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        Flowable<Object> futureObservable = Flowable.fromFuture(future);
        futureObservable.subscribeOn(Schedulers.computation()).subscribe(ts);
        Thread.sleep(100);
        ts.cancel();
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        FutureTask<Integer> f = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
            }
        }, 1);
        f.run();
        Flowable.fromFuture(f).subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withTimeoutNoTimeout() {
        FutureTask<Integer> task = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
            }
        }, 1);
        task.run();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.fromFuture(task, 1, TimeUnit.SECONDS).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withTimeoutTimeout() {
        FutureTask<Integer> task = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
            }
        }, 1);
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.fromFuture(task, 10, TimeUnit.MILLISECONDS).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TimeoutException.class);
        ts.assertNotComplete();
    }

    @Test
    public void withTimeoutNoTimeoutScheduler() {
        FutureTask<Integer> task = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
            }
        }, 1);
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.fromFuture(task).subscribeOn(Schedulers.computation()).subscribe(ts);
        task.run();
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::success, this.description("success"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successOperatesOnSuppliedScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successOperatesOnSuppliedScheduler, this.description("successOperatesOnSuppliedScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failure, this.description("failure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledBeforeSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelledBeforeSubscribe, this.description("cancelledBeforeSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellationDuringFutureGet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancellationDuringFutureGet, this.description("cancellationDuringFutureGet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTimeoutNoTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withTimeoutNoTimeout, this.description("withTimeoutNoTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTimeoutTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withTimeoutTimeout, this.description("withTimeoutTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTimeoutNoTimeoutScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withTimeoutNoTimeoutScheduler, this.description("withTimeoutNoTimeoutScheduler"));
        }

        private FlowableToFutureTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableToFutureTest();
        }

        @java.lang.Override
        public FlowableToFutureTest implementation() {
            return this.implementation;
        }
    }
}
