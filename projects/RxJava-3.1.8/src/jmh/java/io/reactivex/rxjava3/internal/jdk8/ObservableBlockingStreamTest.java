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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.List;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ObservableBlockingStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        try (Stream<Integer> stream = Observable.<Integer>empty().blockingStream()) {
            assertEquals(0, stream.toArray().length);
        }
    }

    @Test
    public void just() {
        try (Stream<Integer> stream = Observable.just(1).blockingStream()) {
            assertArrayEquals(new Integer[] { 1 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void range() {
        try (Stream<Integer> stream = Observable.range(1, 5).blockingStream()) {
            assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void rangeBackpressured() {
        try (Stream<Integer> stream = Observable.range(1, 5).blockingStream(1)) {
            assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void rangeAsyncBackpressured() {
        try (Stream<Integer> stream = Observable.range(1, 1000).subscribeOn(Schedulers.computation()).blockingStream()) {
            List<Integer> list = stream.collect(Collectors.toList());
            assertEquals(1000, list.size());
            for (int i = 1; i <= 1000; i++) {
                assertEquals(i, list.get(i - 1).intValue());
            }
        }
    }

    @Test
    public void rangeAsyncBackpressured1() {
        try (Stream<Integer> stream = Observable.range(1, 1000).subscribeOn(Schedulers.computation()).blockingStream(1)) {
            List<Integer> list = stream.collect(Collectors.toList());
            assertEquals(1000, list.size());
            for (int i = 1; i <= 1000; i++) {
                assertEquals(i, list.get(i - 1).intValue());
            }
        }
    }

    @Test
    public void error() {
        try (Stream<Integer> stream = Observable.<Integer>error(new TestException()).blockingStream()) {
            stream.toArray(Integer[]::new);
            fail("Should have thrown!");
        } catch (TestException expected) {
            // expected
        }
    }

    @Test
    public void close() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        try (Stream<Integer> stream = up.blockingStream()) {
            assertArrayEquals(new Integer[] { 1, 2, 3 }, stream.limit(3).toArray(Integer[]::new));
        }
        assertFalse(up.hasSubscribers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::range, this.description("range"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeBackpressured, this.description("rangeBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeAsyncBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeAsyncBackpressured, this.description("rangeAsyncBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeAsyncBackpressured1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeAsyncBackpressured1, this.description("rangeAsyncBackpressured1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_close() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::close, this.description("close"));
        }

        private ObservableBlockingStreamTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableBlockingStreamTest();
        }

        @java.lang.Override
        public ObservableBlockingStreamTest implementation() {
            return this.implementation;
        }
    }
}
