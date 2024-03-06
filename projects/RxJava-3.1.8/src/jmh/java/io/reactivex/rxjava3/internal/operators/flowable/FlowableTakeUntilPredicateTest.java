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
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeUntilPredicateTest extends RxJavaTest {

    @Test
    public void takeEmpty() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.empty().takeUntil(new Predicate<Object>() {

            @Override
            public boolean test(Object v) {
                return true;
            }
        }).subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeAll() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onNext(2);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeFirst() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeSome() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2, 3).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 == 2;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onNext(2);
        verify(subscriber, never()).onNext(3);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void functionThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Predicate<Integer> predicate = new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                throw new TestException("Forced failure");
            }
        };
        Flowable.just(1, 2, 3).takeUntil(predicate).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber, never()).onNext(3);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sourceThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).concatWith(Flowable.just(2)).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(5L);
        Flowable.range(1, 1000).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(ts);
        ts.assertNoErrors();
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNotComplete();
    }

    @Test
    public void errorIncludesLastValueAsCause() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        final TestException e = new TestException("Forced failure");
        Predicate<String> predicate = new Predicate<String>() {

            @Override
            public boolean test(String t) {
                throw e;
            }
        };
        Flowable.just("abc").takeUntil(predicate).subscribe(ts);
        ts.assertTerminated();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
        // FIXME last cause value is not saved
        // assertTrue(ts.errors().get(0).getCause().getMessage().contains("abc"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().takeUntil(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeUntil(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onComplete();
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.takeUntil(Functions.alwaysFalse()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeEmpty, this.description("takeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeAll, this.description("takeAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirst, this.description("takeFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeSome, this.description("takeSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::functionThrows, this.description("functionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sourceThrows, this.description("sourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorIncludesLastValueAsCause() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorIncludesLastValueAsCause, this.description("errorIncludesLastValueAsCause"));
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
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        private FlowableTakeUntilPredicateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableTakeUntilPredicateTest();
        }

        @java.lang.Override
        public FlowableTakeUntilPredicateTest implementation() {
            return this.implementation;
        }
    }
}
