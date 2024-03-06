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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDelaySubscriptionTest extends RxJavaTest {

    @Test
    public void normal() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.just(1).delaySubscription(pp).test();
        assertTrue(pp.hasSubscribers());
        to.assertEmpty();
        pp.onNext("one");
        assertFalse(pp.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void timed() {
        Maybe.just(1).delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timedEmpty() {
        Maybe.<Integer>empty().delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void timedTestScheduler() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Integer> to = Maybe.just(1).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler).test();
        to.assertEmpty();
        scheduler.advanceTimeBy(99, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        to.assertResult(1);
    }

    @Test
    public void otherError() {
        Maybe.just(1).delaySubscription(Flowable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void mainError() {
        Maybe.error(new TestException()).delaySubscription(Flowable.empty()).test().assertFailure(TestException.class);
    }

    @Test
    public void withPublisherDispose() {
        TestHelper.checkDisposed(Maybe.just(1).delaySubscription(Flowable.never()));
    }

    @Test
    public void withPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.delaySubscription(Flowable.just(1));
            }
        });
    }

    @Test
    public void withPublisherCallAfterTerminalEvent() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable<Integer> f = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                    subscriber.onNext(2);
                }
            };
            Maybe.just(1).delaySubscription(f).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribePublisher() {
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(f -> Maybe.just(1).delaySubscription(f));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timed, this.description("timed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timedEmpty, this.description("timedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedTestScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timedTestScheduler, this.description("timedTestScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherError, this.description("otherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withPublisherDispose, this.description("withPublisherDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withPublisherDoubleOnSubscribe, this.description("withPublisherDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherCallAfterTerminalEvent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withPublisherCallAfterTerminalEvent, this.description("withPublisherCallAfterTerminalEvent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribePublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribePublisher, this.description("doubleOnSubscribePublisher"));
        }

        private MaybeDelaySubscriptionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDelaySubscriptionTest();
        }

        @java.lang.Override
        public MaybeDelaySubscriptionTest implementation() {
            return this.implementation;
        }
    }
}
