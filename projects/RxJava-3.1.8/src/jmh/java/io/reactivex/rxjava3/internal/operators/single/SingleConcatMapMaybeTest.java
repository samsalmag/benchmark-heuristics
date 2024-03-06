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

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleConcatMapMaybeTest extends RxJavaTest {

    @Test
    public void concatMapMaybeValue() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Maybe.just(2);
                }
                return Maybe.just(1);
            }
        }).test().assertResult(2);
    }

    @Test
    public void concatMapMaybeValueDifferentType() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<String>>() {

            @Override
            public MaybeSource<String> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Maybe.just("2");
                }
                return Maybe.just("1");
            }
        }).test().assertResult("2");
    }

    @Test
    public void concatMapMaybeValueNull() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The mapper returned a null MaybeSource");
    }

    @Test
    public void concatMapMaybeValueErrorThrown() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void concatMapMaybeError() {
        RuntimeException exception = new RuntimeException("test");
        Single.error(exception).concatMapMaybe(new Function<Object, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(final Object integer) throws Exception {
                return Maybe.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1);
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToMaybe(new Function<Single<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Single<Integer> v) throws Exception {
                return v.concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

                    @Override
                    public MaybeSource<Integer> apply(Integer v) throws Exception {
                        return Maybe.just(1);
                    }
                });
            }
        });
    }

    @Test
    public void mapsToError() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapsToEmpty() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMapMaybeValue, this.description("concatMapMaybeValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueDifferentType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMapMaybeValueDifferentType, this.description("concatMapMaybeValueDifferentType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMapMaybeValueNull, this.description("concatMapMaybeValueNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueErrorThrown() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMapMaybeValueErrorThrown, this.description("concatMapMaybeValueErrorThrown"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMapMaybeError, this.description("concatMapMaybeError"));
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
        public void benchmark_mapsToError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapsToError, this.description("mapsToError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapsToEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapsToEmpty, this.description("mapsToEmpty"));
        }

        private SingleConcatMapMaybeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleConcatMapMaybeTest();
        }

        @java.lang.Override
        public SingleConcatMapMaybeTest implementation() {
            return this.implementation;
        }
    }
}
