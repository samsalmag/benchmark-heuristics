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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDematerializeTest extends RxJavaTest {

    @Test
    public void simpleSelector() {
        Flowable<Notification<Integer>> notifications = Flowable.just(1, 2).materialize();
        Flowable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void selectorCrash() {
        Flowable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorNull() {
        Flowable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void dematerialize1() {
        Flowable<Notification<Integer>> notifications = Flowable.just(1, 2).materialize();
        Flowable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void dematerialize2() {
        Throwable exception = new Throwable("test");
        Flowable<Integer> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void dematerialize3() {
        Exception exception = new Exception("test");
        Flowable<Integer> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void errorPassThru() {
        Exception exception = new Exception("test");
        Flowable<Notification<Integer>> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void completePassThru() {
        Flowable<Notification<Integer>> flowable = Flowable.empty();
        Flowable<Integer> dematerialize = flowable.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(subscriber);
        dematerialize.subscribe(ts);
        System.out.println(ts.errors());
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void honorsContractWhenCompleted() {
        Flowable<Integer> source = Flowable.just(1);
        Flowable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void honorsContractWhenThrows() {
        Flowable<Integer> source = Flowable.error(new TestException());
        Flowable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(Integer.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(Notification.createOnComplete()).dematerialize(Functions.<Notification<Object>>identity()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Notification<Object>>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Notification<Object>> f) throws Exception {
                return f.dematerialize(Functions.<Notification<Object>>identity());
            }
        });
    }

    @Test
    public void eventsAfterDematerializedTerminal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Notification<Object>>() {

                @Override
                protected void subscribeActual(Subscriber<? super Notification<Object>> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(Notification.createOnComplete());
                    subscriber.onNext(Notification.<Object>createOnNext(1));
                    subscriber.onNext(Notification.createOnError(new TestException("First")));
                    subscriber.onError(new TestException("Second"));
                }
            }.dematerialize(Functions.<Notification<Object>>identity()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void notificationInstanceAfterDispose() {
        new Flowable<Notification<Object>>() {

            @Override
            protected void subscribeActual(Subscriber<? super Notification<Object>> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(Notification.createOnComplete());
                subscriber.onNext(Notification.<Object>createOnNext(1));
            }
        }.dematerialize(Functions.<Notification<Object>>identity()).test().assertResult();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nonNotificationInstanceAfterDispose() {
        new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(Notification.createOnComplete());
                subscriber.onNext(1);
            }
        }.dematerialize(v -> (Notification<Object>) v).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleSelector() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleSelector, this.description("simpleSelector"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorCrash, this.description("selectorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorNull, this.description("selectorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dematerialize1, this.description("dematerialize1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dematerialize2, this.description("dematerialize2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dematerialize3, this.description("dematerialize3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorPassThru() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorPassThru, this.description("errorPassThru"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completePassThru() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completePassThru, this.description("completePassThru"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::honorsContractWhenCompleted, this.description("honorsContractWhenCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::honorsContractWhenThrows, this.description("honorsContractWhenThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventsAfterDematerializedTerminal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eventsAfterDematerializedTerminal, this.description("eventsAfterDematerializedTerminal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notificationInstanceAfterDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::notificationInstanceAfterDispose, this.description("notificationInstanceAfterDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonNotificationInstanceAfterDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonNotificationInstanceAfterDispose, this.description("nonNotificationInstanceAfterDispose"));
        }

        private FlowableDematerializeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableDematerializeTest();
        }

        @java.lang.Override
        public FlowableDematerializeTest implementation() {
            return this.implementation;
        }
    }
}
