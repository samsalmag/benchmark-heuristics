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
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSkipWhileTest extends RxJavaTest {

    Subscriber<Integer> w = TestHelper.mockSubscriber();

    private static final Predicate<Integer> LESS_THAN_FIVE = new Predicate<Integer>() {

        @Override
        public boolean test(Integer v) {
            if (v == 42) {
                throw new RuntimeException("that's not the answer to everything!");
            }
            return v < 5;
        }
    };

    private static final Predicate<Integer> INDEX_LESS_THAN_THREE = new Predicate<Integer>() {

        int index;

        @Override
        public boolean test(Integer value) {
            return index++ < 3;
        }
    };

    @Test
    public void skipWithIndex() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 5);
        src.skipWhile(INDEX_LESS_THAN_THREE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(4);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipEmpty() {
        Flowable<Integer> src = Flowable.empty();
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void skipEverything() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 3, 2, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void skipNothing() {
        Flowable<Integer> src = Flowable.just(5, 3, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onNext(3);
        inOrder.verify(w, times(1)).onNext(1);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipSome() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 5, 3, 1, 5);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onNext(3);
        inOrder.verify(w, times(1)).onNext(1);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipError() {
        Flowable<Integer> src = Flowable.just(1, 2, 42, 5, 3, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, never()).onNext(anyInt());
        inOrder.verify(w, never()).onComplete();
        inOrder.verify(w, times(1)).onError(any(RuntimeException.class));
    }

    @Test
    public void skipManySubscribers() {
        Flowable<Integer> src = Flowable.range(1, 10).skipWhile(LESS_THAN_FIVE);
        int n = 5;
        for (int i = 0; i < n; i++) {
            Subscriber<Object> subscriber = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber);
            src.subscribe(subscriber);
            for (int j = 5; j < 10; j++) {
                inOrder.verify(subscriber).onNext(j);
            }
            inOrder.verify(subscriber).onComplete();
            verify(subscriber, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().skipWhile(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skipWhile(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).skipWhile(Functions.alwaysFalse()).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipWithIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipWithIndex, this.description("skipWithIndex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipEmpty, this.description("skipEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEverything() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipEverything, this.description("skipEverything"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipNothing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipNothing, this.description("skipNothing"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipSome, this.description("skipSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipError, this.description("skipError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipManySubscribers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipManySubscribers, this.description("skipManySubscribers"));
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

        private FlowableSkipWhileTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableSkipWhileTest();
        }

        @java.lang.Override
        public FlowableSkipWhileTest implementation() {
            return this.implementation;
        }
    }
}
