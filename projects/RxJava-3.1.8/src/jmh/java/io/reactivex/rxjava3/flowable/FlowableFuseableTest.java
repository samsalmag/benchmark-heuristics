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
package io.reactivex.rxjava3.flowable;

import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFuseableTest extends RxJavaTest {

    @Test
    public void syncRange() {
        Flowable.range(1, 10).to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertFusionMode(QueueFuseable.SYNC).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @Test
    public void syncArray() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertFusionMode(QueueFuseable.SYNC).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @Test
    public void syncIterable() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertFusionMode(QueueFuseable.SYNC).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @Test
    public void syncRangeHidden() {
        Flowable.range(1, 10).hide().to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertNotFuseable().assertFusionMode(QueueFuseable.NONE).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @Test
    public void syncArrayHidden() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).hide().to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertNotFuseable().assertFusionMode(QueueFuseable.NONE).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @Test
    public void syncIterableHidden() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).hide().to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertNotFuseable().assertFusionMode(QueueFuseable.NONE).assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoErrors().assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncRange() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncRange, this.description("syncRange"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncArray() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncArray, this.description("syncArray"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncIterable, this.description("syncIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncRangeHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncRangeHidden, this.description("syncRangeHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncArrayHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncArrayHidden, this.description("syncArrayHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncIterableHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncIterableHidden, this.description("syncIterableHidden"));
        }

        private FlowableFuseableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFuseableTest();
        }

        @java.lang.Override
        public FlowableFuseableTest implementation() {
            return this.implementation;
        }
    }
}
