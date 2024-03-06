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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableToSortedListTest extends RxJavaTest {

    @Test
    public void sortedListFlowable() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Flowable<List<Integer>> flowable = w.toSortedList().toFlowable();
        Subscriber<List<Integer>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList(1, 2, 3, 4, 5));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void sortedListWithCustomFunctionFlowable() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Flowable<List<Integer>> flowable = w.toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer t1, Integer t2) {
                return t2 - t1;
            }
        }).toFlowable();
        Subscriber<List<Integer>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList(5, 4, 3, 2, 1));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void withFollowingFirstFlowable() {
        Flowable<Integer> f = Flowable.just(1, 3, 2, 5, 4);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), f.toSortedList().toFlowable().blockingFirst());
    }

    @Test
    public void backpressureHonoredFlowable() {
        Flowable<List<Integer>> w = Flowable.just(1, 3, 2, 5, 4).toSortedList().toFlowable();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(0L);
        w.subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void sorted() {
        Flowable.just(5, 1, 2, 4, 3).sorted().test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void sortedComparator() {
        Flowable.just(5, 1, 2, 4, 3).sorted(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }).test().assertResult(5, 4, 3, 2, 1);
    }

    @Test
    public void toSortedListCapacityFlowable() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(4).toFlowable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void toSortedListComparatorCapacityFlowable() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }, 4).toFlowable().test().assertResult(Arrays.asList(5, 4, 3, 2, 1));
    }

    @Test
    public void sortedList() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Single<List<Integer>> single = w.toSortedList();
        SingleObserver<List<Integer>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList(1, 2, 3, 4, 5));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void sortedListWithCustomFunction() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Single<List<Integer>> single = w.toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer t1, Integer t2) {
                return t2 - t1;
            }
        });
        SingleObserver<List<Integer>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList(5, 4, 3, 2, 1));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void withFollowingFirst() {
        Flowable<Integer> f = Flowable.just(1, 3, 2, 5, 4);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), f.toSortedList().blockingGet());
    }

    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void toSortedListCapacity() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(4).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void toSortedListComparatorCapacity() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }, 4).test().assertResult(Arrays.asList(5, 4, 3, 2, 1));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sortedListFlowable, this.description("sortedListFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListWithCustomFunctionFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sortedListWithCustomFunctionFlowable, this.description("sortedListWithCustomFunctionFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirstFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFollowingFirstFlowable, this.description("withFollowingFirstFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureHonoredFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureHonoredFlowable, this.description("backpressureHonoredFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sorted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sorted, this.description("sorted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedComparator() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sortedComparator, this.description("sortedComparator"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListCapacityFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSortedListCapacityFlowable, this.description("toSortedListCapacityFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListComparatorCapacityFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSortedListComparatorCapacityFlowable, this.description("toSortedListComparatorCapacityFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sortedList, this.description("sortedList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListWithCustomFunction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sortedListWithCustomFunction, this.description("sortedListWithCustomFunction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFollowingFirst, this.description("withFollowingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSortedListCapacity, this.description("toSortedListCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListComparatorCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSortedListComparatorCapacity, this.description("toSortedListComparatorCapacity"));
        }

        private FlowableToSortedListTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableToSortedListTest();
        }

        @java.lang.Override
        public FlowableToSortedListTest implementation() {
            return this.implementation;
        }
    }
}
