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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.assertEquals;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class BackpressureHelperTest extends RxJavaTest {

    @Test
    public void constructorShouldBePrivate() {
        TestHelper.checkUtilityClass(BackpressureHelper.class);
    }

    @Test
    public void addCap() {
        assertEquals(2L, BackpressureHelper.addCap(1, 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(1, Long.MAX_VALUE - 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(1, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    public void multiplyCap() {
        assertEquals(6, BackpressureHelper.multiplyCap(2, 3));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(2, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(1L << 32, 1L << 32));
    }

    @Test
    public void producedMore() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            AtomicLong requested = new AtomicLong(1);
            assertEquals(0, BackpressureHelper.produced(requested, 2));
            TestHelper.assertError(list, 0, IllegalStateException.class, "More produced than requested: -1");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void producedMoreCancel() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            AtomicLong requested = new AtomicLong(1);
            assertEquals(0, BackpressureHelper.producedCancel(requested, 2));
            TestHelper.assertError(list, 0, IllegalStateException.class, "More produced than requested: -1");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void requestProduceRace() {
        final AtomicLong requested = new AtomicLong(1);
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    BackpressureHelper.produced(requested, 1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    BackpressureHelper.add(requested, 1);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void requestCancelProduceRace() {
        final AtomicLong requested = new AtomicLong(1);
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    BackpressureHelper.produced(requested, 1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    BackpressureHelper.addCancel(requested, 1);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(BackpressureHelper.class);
    }

    @Test
    public void capped() {
        final AtomicLong requested = new AtomicLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, BackpressureHelper.addCancel(requested, 1));
        assertEquals(Long.MIN_VALUE, BackpressureHelper.addCancel(requested, Long.MAX_VALUE));
        requested.set(0);
        assertEquals(0, BackpressureHelper.addCancel(requested, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCancel(requested, 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.addCancel(requested, Long.MAX_VALUE));
        requested.set(0);
        assertEquals(0, BackpressureHelper.add(requested, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.add(requested, 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.add(requested, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.produced(requested, 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.produced(requested, Long.MAX_VALUE));
    }

    @Test
    public void multiplyCap2() {
        assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(3, Long.MAX_VALUE >> 1));
        assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(1, Long.MAX_VALUE));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorShouldBePrivate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructorShouldBePrivate, this.description("constructorShouldBePrivate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addCap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addCap, this.description("addCap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiplyCap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiplyCap, this.description("multiplyCap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producedMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::producedMore, this.description("producedMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producedMoreCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::producedMoreCancel, this.description("producedMoreCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestProduceRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestProduceRace, this.description("requestProduceRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelProduceRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCancelProduceRace, this.description("requestCancelProduceRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capped() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::capped, this.description("capped"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiplyCap2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiplyCap2, this.description("multiplyCap2"));
        }

        private BackpressureHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BackpressureHelperTest();
        }

        @java.lang.Override
        public BackpressureHelperTest implementation() {
            return this.implementation;
        }
    }
}
