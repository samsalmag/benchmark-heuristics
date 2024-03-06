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

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeLastOneTest extends RxJavaTest {

    @Test
    public void lastOfManyReturnsLast() {
        TestSubscriberEx<Integer> s = new TestSubscriberEx<>();
        Flowable.range(1, 10).takeLast(1).subscribe(s);
        s.assertValue(10);
        s.assertNoErrors();
        s.assertTerminated();
        // NO longer assertable
        // s.assertUnsubscribed();
    }

    @Test
    public void lastOfEmptyReturnsEmpty() {
        TestSubscriberEx<Object> s = new TestSubscriberEx<>();
        Flowable.empty().takeLast(1).subscribe(s);
        s.assertNoValues();
        s.assertNoErrors();
        s.assertTerminated();
        // NO longer assertable
        // s.assertUnsubscribed();
    }

    @Test
    public void lastOfOneReturnsLast() {
        TestSubscriberEx<Integer> s = new TestSubscriberEx<>();
        Flowable.just(1).takeLast(1).subscribe(s);
        s.assertValue(1);
        s.assertNoErrors();
        s.assertTerminated();
        // NO longer assertable
        // s.assertUnsubscribed();
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Action unsubscribeAction = new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }
        };
        Flowable.just(1).concatWith(Flowable.<Integer>never()).doOnCancel(unsubscribeAction).takeLast(1).subscribe().dispose();
        assertTrue(unsubscribed.get());
    }

    @Test
    public void lastWithBackpressure() {
        MySubscriber<Integer> s = new MySubscriber<>(0);
        Flowable.just(1).takeLast(1).subscribe(s);
        assertEquals(0, s.list.size());
        s.requestMore(1);
        assertEquals(1, s.list.size());
    }

    @Test
    public void takeLastZeroProcessesAllItemsButIgnoresThem() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final int num = 10;
        long count = Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).takeLast(0).count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0L, count);
    }

    private static class MySubscriber<T> extends DefaultSubscriber<T> {

        private long initialRequest;

        MySubscriber(long initialRequest) {
            this.initialRequest = initialRequest;
        }

        final List<T> list = new ArrayList<>();

        public void requestMore(long n) {
            request(n);
        }

        @Override
        public void onStart() {
            if (initialRequest > 0) {
                request(initialRequest);
            }
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(T t) {
            list.add(t);
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).takeLast(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeLast(1);
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).takeLast(1).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfManyReturnsLast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOfManyReturnsLast, this.description("lastOfManyReturnsLast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfEmptyReturnsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOfEmptyReturnsEmpty, this.description("lastOfEmptyReturnsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfOneReturnsLast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastOfOneReturnsLast, this.description("lastOfOneReturnsLast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribesFromUpstream, this.description("unsubscribesFromUpstream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastWithBackpressure, this.description("lastWithBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastZeroProcessesAllItemsButIgnoresThem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastZeroProcessesAllItemsButIgnoresThem, this.description("takeLastZeroProcessesAllItemsButIgnoresThem"));
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
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        private FlowableTakeLastOneTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableTakeLastOneTest();
        }

        @java.lang.Override
        public FlowableTakeLastOneTest implementation() {
            return this.implementation;
        }
    }
}
