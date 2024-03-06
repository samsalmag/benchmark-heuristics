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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableHideTest extends RxJavaTest {

    @Test
    public void hiding() {
        PublishProcessor<Integer> src = PublishProcessor.create();
        Flowable<Integer> dst = src.hide();
        assertFalse(dst instanceof PublishProcessor);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        dst.subscribe(subscriber);
        src.onNext(1);
        src.onComplete();
        verify(subscriber).onNext(1);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void hidingError() {
        PublishProcessor<Integer> src = PublishProcessor.create();
        Flowable<Integer> dst = src.hide();
        assertFalse(dst instanceof PublishProcessor);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        dst.subscribe(subscriber);
        src.onError(new TestException());
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.hide();
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.create().hide());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hiding() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hiding, this.description("hiding"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hidingError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hidingError, this.description("hidingError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        private FlowableHideTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableHideTest();
        }

        @java.lang.Override
        public FlowableHideTest implementation() {
            return this.implementation;
        }
    }
}
