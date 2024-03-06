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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFromCallableTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvokeFuncUntilSubscription() throws Exception {
        Callable<Object> func = mock(Callable.class);
        when(func.call()).thenReturn(new Object());
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(func);
        verifyNoInteractions(func);
        fromCallableFlowable.subscribe();
        verify(func).call();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnNextAndOnCompleted() throws Exception {
        Callable<String> func = mock(Callable.class);
        when(func.call()).thenReturn("test_value");
        Flowable<String> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber).onNext("test_value");
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnError() throws Exception {
        Callable<Object> func = mock(Callable.class);
        Throwable throwable = new IllegalStateException("Test exception");
        when(func.call()).thenThrow(throwable);
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(throwable);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws Exception {
        Callable<String> func = mock(Callable.class);
        final CountDownLatch funcLatch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        when(func.call()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                observerLatch.countDown();
                try {
                    funcLatch.await();
                } catch (InterruptedException e) {
                    // It's okay, unsubscription causes Thread interruption
                    // Restoring interruption status of the Thread
                    Thread.currentThread().interrupt();
                }
                return "should_not_be_delivered";
            }
        });
        Flowable<String> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> outer = new TestSubscriber<>(subscriber);
        fromCallableFlowable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.cancel();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).call();
        // Observer must not be notified at all
        verify(subscriber).onSubscribe(any(Subscription.class));
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void shouldAllowToThrowCheckedException() {
        final Exception checkedException = new Exception("test exception");
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw checkedException;
            }
        });
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber).onSubscribe(any(Subscription.class));
        verify(subscriber).onError(checkedException);
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void fusedFlatMapExecution() {
        final int[] calls = { 0 };
        Flowable.just(1).flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return ++calls[0];
                    }
                });
            }
        }).test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void fusedFlatMapExecutionHidden() {
        final int[] calls = { 0 };
        Flowable.just(1).hide().flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return ++calls[0];
                    }
                });
            }
        }).test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void fusedFlatMapNull() {
        Flowable.just(1).flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                });
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void fusedFlatMapNullHidden() {
        Flowable.just(1).hide().flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                });
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void undeliverableUponCancellation() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            Flowable.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    ts.cancel();
                    throw new TestException();
                }
            }).subscribe(ts);
            ts.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotInvokeFuncUntilSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotInvokeFuncUntilSubscription, this.description("shouldNotInvokeFuncUntilSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldCallOnNextAndOnCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldCallOnNextAndOnCompleted, this.description("shouldCallOnNextAndOnCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldCallOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldCallOnError, this.description("shouldCallOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, this.description("shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAllowToThrowCheckedException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAllowToThrowCheckedException, this.description("shouldAllowToThrowCheckedException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapExecution() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFlatMapExecution, this.description("fusedFlatMapExecution"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapExecutionHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFlatMapExecutionHidden, this.description("fusedFlatMapExecutionHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFlatMapNull, this.description("fusedFlatMapNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapNullHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFlatMapNullHidden, this.description("fusedFlatMapNullHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancellation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::undeliverableUponCancellation, this.description("undeliverableUponCancellation"));
        }

        private FlowableFromCallableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFromCallableTest();
        }

        @java.lang.Override
        public FlowableFromCallableTest implementation() {
            return this.implementation;
        }
    }
}
