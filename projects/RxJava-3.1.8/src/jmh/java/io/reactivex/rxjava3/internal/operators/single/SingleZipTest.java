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

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;

public class SingleZipTest extends RxJavaTest {

    @Test
    public void zip2() {
        Single.zip(Single.just(1), Single.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + "" + b;
            }
        }).test().assertResult("12");
    }

    @Test
    public void zip3() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                return a + "" + b + c;
            }
        }).test().assertResult("123");
    }

    @Test
    public void zip4() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), new Function4<Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d) throws Exception {
                return a + "" + b + c + d;
            }
        }).test().assertResult("1234");
    }

    @Test
    public void zip5() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), new Function5<Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e) throws Exception {
                return a + "" + b + c + d + e;
            }
        }).test().assertResult("12345");
    }

    @Test
    public void zip6() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), new Function6<Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f) throws Exception {
                return a + "" + b + c + d + e + f;
            }
        }).test().assertResult("123456");
    }

    @Test
    public void zip7() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), new Function7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g) throws Exception {
                return a + "" + b + c + d + e + f + g;
            }
        }).test().assertResult("1234567");
    }

    @Test
    public void zip8() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), Single.just(8), new Function8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h) throws Exception {
                return a + "" + b + c + d + e + f + g + h;
            }
        }).test().assertResult("12345678");
    }

    @Test
    public void zip9() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), Single.just(8), Single.just(9), new Function9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h, Integer i) throws Exception {
                return a + "" + b + c + d + e + f + g + h + i;
            }
        }).test().assertResult("123456789");
    }

    @Test
    public void noDisposeOnAllSuccess() {
        final AtomicInteger counter = new AtomicInteger();
        Single<Integer> source = Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Single.zip(source, source, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertResult(2);
        assertEquals(0, counter.get());
    }

    @Test
    public void noDisposeOnAllSuccess2() {
        final AtomicInteger counter = new AtomicInteger();
        Single<Integer> source = Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Single.zip(Arrays.asList(source, source), new Function<Object[], Object>() {

            @Override
            public Integer apply(Object[] o) throws Exception {
                return (Integer) o[0] + (Integer) o[1];
            }
        }).test().assertResult(2);
        assertEquals(0, counter.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip2, this.description("zip2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip3, this.description("zip3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip4, this.description("zip4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip5() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip5, this.description("zip5"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip6() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip6, this.description("zip6"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip7() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip7, this.description("zip7"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip8() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip8, this.description("zip8"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip9() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zip9, this.description("zip9"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDisposeOnAllSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noDisposeOnAllSuccess, this.description("noDisposeOnAllSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDisposeOnAllSuccess2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noDisposeOnAllSuccess2, this.description("noDisposeOnAllSuccess2"));
        }

        private SingleZipTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleZipTest();
        }

        @java.lang.Override
        public SingleZipTest implementation() {
            return this.implementation;
        }
    }
}
