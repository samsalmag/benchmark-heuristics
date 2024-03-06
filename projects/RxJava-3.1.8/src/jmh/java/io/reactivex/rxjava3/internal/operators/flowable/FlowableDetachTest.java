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

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDetachTest extends RxJavaTest {

    Object o;

    @Test
    public void just() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.assertValue(1L);
        ts.assertComplete();
        ts.assertNoErrors();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void error() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.error(new TestException()).onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void empty() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.empty().onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void range() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.range(1, 1000).onTerminateDetach().subscribe(ts);
        ts.assertValueCount(1000);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressured() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>(0L);
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1L);
        ts.assertComplete();
        ts.assertNoErrors();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void justUnsubscribed() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>(0);
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.cancel();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void deferredUpstreamProducer() {
        final AtomicReference<Subscriber<? super Object>> subscriber = new AtomicReference<>();
        TestSubscriber<Object> ts = new TestSubscriber<>(0);
        Flowable.unsafeCreate(new Publisher<Object>() {

            @Override
            public void subscribe(Subscriber<? super Object> t) {
                subscriber.set(t);
            }
        }).onTerminateDetach().subscribe(ts);
        ts.request(2);
        new FlowableRange(1, 3).subscribe(subscriber.get());
        ts.assertValues(1, 2);
        ts.request(1);
        ts.assertValues(1, 2, 3);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().onTerminateDetach());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.onTerminateDetach();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::range, this.description("range"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressured, this.description("backpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justUnsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justUnsubscribed, this.description("justUnsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredUpstreamProducer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deferredUpstreamProducer, this.description("deferredUpstreamProducer"));
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

        private FlowableDetachTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableDetachTest();
        }

        @java.lang.Override
        public FlowableDetachTest implementation() {
            return this.implementation;
        }
    }
}
