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
package io.reactivex.rxjava3.internal.subscribers;

import static org.junit.Assert.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SubscriberResourceWrapperTest extends RxJavaTest {

    TestSubscriber<Integer> ts = new TestSubscriber<>();

    SubscriberResourceWrapper<Integer> s = new SubscriberResourceWrapper<>(ts);

    @Test
    public void cancel() {
        BooleanSubscription bs = new BooleanSubscription();
        Disposable d = Disposable.empty();
        s.setResource(d);
        s.onSubscribe(bs);
        assertFalse(d.isDisposed());
        assertFalse(s.isDisposed());
        ts.cancel();
        assertTrue(bs.isCancelled());
        assertTrue(d.isDisposed());
        assertTrue(s.isDisposed());
    }

    @Test
    public void error() {
        BooleanSubscription bs = new BooleanSubscription();
        Disposable d = Disposable.empty();
        s.setResource(d);
        s.onSubscribe(bs);
        s.onError(new TestException());
        assertTrue(d.isDisposed());
        assertFalse(bs.isCancelled());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void complete() {
        BooleanSubscription bs = new BooleanSubscription();
        Disposable d = Disposable.empty();
        s.setResource(d);
        s.onSubscribe(bs);
        s.onComplete();
        assertTrue(d.isDisposed());
        assertFalse(bs.isCancelled());
        ts.assertResult();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.lift(new FlowableOperator<Object, Object>() {

                    @Override
                    public Subscriber<? super Object> apply(Subscriber<? super Object> s) throws Exception {
                        return new SubscriberResourceWrapper<>(s);
                    }
                });
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().lift(new FlowableOperator<Object, Object>() {

            @Override
            public Subscriber<? super Object> apply(Subscriber<? super Object> s) throws Exception {
                return new SubscriberResourceWrapper<>(s);
            }
        }));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::complete, this.description("complete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        private SubscriberResourceWrapperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SubscriberResourceWrapperTest();
        }

        @java.lang.Override
        public SubscriberResourceWrapperTest implementation() {
            return this.implementation;
        }
    }
}
