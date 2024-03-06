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
package io.reactivex.rxjava3.core;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.BackpressureKind;

public class BackpressureEnumTest extends RxJavaTest {

    @Test
    public void backpressureOverflowStrategy() {
        assertEquals(3, BackpressureOverflowStrategy.values().length);
        assertNotNull(BackpressureOverflowStrategy.valueOf("ERROR"));
    }

    @Test
    public void backpressureStrategy() {
        assertEquals(5, BackpressureStrategy.values().length);
        assertNotNull(BackpressureStrategy.valueOf("BUFFER"));
    }

    @Test
    public void backpressureKind() {
        assertEquals(6, BackpressureKind.values().length);
        assertNotNull(BackpressureKind.valueOf("FULL"));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflowStrategy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureOverflowStrategy, this.description("backpressureOverflowStrategy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureStrategy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureStrategy, this.description("backpressureStrategy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureKind() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureKind, this.description("backpressureKind"));
        }

        private BackpressureEnumTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BackpressureEnumTest();
        }

        @java.lang.Override
        public BackpressureEnumTest implementation() {
            return this.implementation;
        }
    }
}
