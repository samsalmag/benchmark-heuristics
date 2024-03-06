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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;
import static org.mockito.Mockito.inOrder;

public class FlowableOnBackpressureLatestTest extends RxJavaTest {

    @Test
    public void simple() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.range(1, 5).onBackpressureLatest().subscribe(ts);
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(new TestException())).onBackpressureLatest().subscribe(ts);
        ts.assertTerminated();
        ts.assertError(TestException.class);
        ts.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleBackpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(2L);
        Flowable.range(1, 5).onBackpressureLatest().subscribe(ts);
        ts.assertNoErrors();
        ts.assertValues(1, 2);
        ts.assertNotComplete();
    }

    @Test
    public void simpleBackpressureWithOnDroppedCallback() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        Observer<Object> dropCallbackObserver = TestHelper.mockObserver();
        source.onBackpressureLatest(dropCallbackObserver::onNext).subscribe(ts);
        ts.assertNoValues();
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        ts.request(1);
        ts.assertValues(3);
        source.onNext(4);
        source.onNext(5);
        ts.request(2);
        ts.assertValues(3, 5);
        InOrder dropCallbackOrder = inOrder(dropCallbackObserver);
        dropCallbackOrder.verify(dropCallbackObserver).onNext(1);
        dropCallbackOrder.verify(dropCallbackObserver).onNext(2);
        dropCallbackOrder.verify(dropCallbackObserver).onNext(4);
        dropCallbackOrder.verifyNoMoreInteractions();
    }

    @Test
    public void simpleBackpressureWithOnDroppedCallbackEx() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        source.onBackpressureLatest(e -> {
            if (e == 3) {
                throw new TestException("forced");
            }
        }).subscribe(ts);
        ts.assertNoValues();
        source.onNext(1);
        source.onNext(2);
        ts.request(1);
        ts.assertValues(2);
        source.onNext(3);
        source.onNext(4);
        ts.assertError(TestException.class);
        ts.assertValues(2);
    }

    @Test
    public void synchronousDrop() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        source.onBackpressureLatest().subscribe(ts);
        ts.assertNoValues();
        source.onNext(1);
        ts.request(2);
        ts.assertValue(1);
        source.onNext(2);
        ts.assertValues(1, 2);
        source.onNext(3);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        ts.request(2);
        ts.assertValues(1, 2, 6);
        source.onNext(7);
        ts.assertValues(1, 2, 6, 7);
        source.onNext(8);
        source.onNext(9);
        source.onComplete();
        ts.request(1);
        ts.assertValues(1, 2, 6, 7, 9);
        ts.assertNoErrors();
        ts.assertTerminated();
    }

    @Test
    public void asynchronousDrop() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>(1L) {

            final Random rnd = new Random();

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (rnd.nextDouble() < 0.001) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                request(1);
            }
        };
        int m = 100000;
        Flowable.range(1, m).subscribeOn(Schedulers.computation()).onBackpressureLatest().observeOn(Schedulers.io()).subscribe(ts);
        ts.awaitDone(2, TimeUnit.SECONDS);
        ts.assertTerminated();
        int n = ts.values().size();
        System.out.println("testAsynchronousDrop -> " + n);
        Assert.assertTrue("All events received?", n < m);
        int previous = 0;
        for (Integer current : ts.values()) {
            Assert.assertTrue("The sequence must be increasing [current value=" + previous + ", previous value=" + current + "]", previous <= current);
            previous = current;
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureLatest();
            }
        });
    }

    @Test
    public void take() {
        Flowable.just(1, 2).onBackpressureLatest().take(1).test().assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().onBackpressureLatest());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().onBackpressureLatest());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple, this.description("simple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleError, this.description("simpleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleBackpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleBackpressure, this.description("simpleBackpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleBackpressureWithOnDroppedCallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleBackpressureWithOnDroppedCallback, this.description("simpleBackpressureWithOnDroppedCallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleBackpressureWithOnDroppedCallbackEx() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simpleBackpressureWithOnDroppedCallbackEx, this.description("simpleBackpressureWithOnDroppedCallbackEx"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousDrop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::synchronousDrop, this.description("synchronousDrop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asynchronousDrop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asynchronousDrop, this.description("asynchronousDrop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        private FlowableOnBackpressureLatestTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableOnBackpressureLatestTest();
        }

        @java.lang.Override
        public FlowableOnBackpressureLatestTest implementation() {
            return this.implementation;
        }
    }
}
