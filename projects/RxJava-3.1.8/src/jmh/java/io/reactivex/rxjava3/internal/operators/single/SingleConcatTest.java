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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleConcatTest extends RxJavaTest {

    @Test
    public void concatWith() {
        Single.just(1).concatWith(Single.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void concat2() {
        Single.concat(Single.just(1), Single.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void concat3() {
        Single.concat(Single.just(1), Single.just(2), Single.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void concat4() {
        Single.concat(Single.just(1), Single.just(2), Single.just(3), Single.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatArray() {
        for (int i = 1; i < 100; i++) {
            Single<Integer>[] array = new Single[i];
            Arrays.fill(array, Single.just(1));
            Single.concatArray(array).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(i).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void concatArrayEagerTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatArrayEager(pp1.single("1"), pp2.single("2")).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("1", "2");
        ts.assertComplete();
    }

    @Test
    public void concatEagerIterableTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatEager(Arrays.asList(pp1.single("2"), pp2.single("1"))).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("2", "1");
        ts.assertComplete();
    }

    @Test
    public void concatEagerPublisherTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatEager(Flowable.just(pp1.single("1"), pp2.single("2"))).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("1", "2");
        ts.assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatObservable() {
        for (int i = 1; i < 100; i++) {
            Single<Integer>[] array = new Single[i];
            Arrays.fill(array, Single.just(1));
            Single.concat(Observable.fromArray(array)).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(i).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };
        Single<Integer> source = Single.create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Single.concatArray(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionIterable() {
        final int[] calls = { 0 };
        Single<Integer> source = Single.create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Single.concat(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWith() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatWith, this.description("concatWith"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concat2, this.description("concat2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concat3, this.description("concat3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concat4, this.description("concat4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArray() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatArray, this.description("concatArray"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayEagerTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatArrayEagerTest, this.description("concatArrayEagerTest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerIterableTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatEagerIterableTest, this.description("concatEagerIterableTest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerPublisherTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatEagerPublisherTest, this.description("concatEagerPublisherTest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatObservable, this.description("concatObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noSubsequentSubscription, this.description("noSubsequentSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noSubsequentSubscriptionIterable, this.description("noSubsequentSubscriptionIterable"));
        }

        private SingleConcatTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleConcatTest();
        }

        @java.lang.Override
        public SingleConcatTest implementation() {
            return this.implementation;
        }
    }
}
