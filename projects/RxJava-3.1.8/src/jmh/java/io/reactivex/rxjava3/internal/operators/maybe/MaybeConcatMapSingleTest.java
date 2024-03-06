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
package io.reactivex.rxjava3.internal.operators.maybe;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeConcatMapSingleTest extends RxJavaTest {

    @Test
    public void flatMapSingleElementValue() {
        Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Single.just(2);
                }
                return Single.just(1);
            }
        }).test().assertResult(2);
    }

    @Test
    public void flatMapSingleElementValueDifferentType() {
        Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<String>>() {

            @Override
            public SingleSource<String> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Single.just("2");
                }
                return Single.just("1");
            }
        }).test().assertResult("2");
    }

    @Test
    public void flatMapSingleElementValueNull() {
        Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The mapper returned a null SingleSource");
    }

    @Test
    public void flatMapSingleElementValueErrorThrown() {
        Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void flatMapSingleElementError() {
        RuntimeException exception = new RuntimeException("test");
        Maybe.error(exception).concatMapSingle(new Function<Object, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(final Object integer) throws Exception {
                return Single.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void flatMapSingleElementEmpty() {
        Maybe.<Integer>empty().concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.just(2);
            }
        }).test().assertNoValues().assertResult();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.just(2);
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Maybe<Integer> m) throws Exception {
                return m.concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

                    @Override
                    public SingleSource<Integer> apply(final Integer integer) throws Exception {
                        return Single.just(2);
                    }
                });
            }
        });
    }

    @Test
    public void singleErrors() {
        Maybe.just(1).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementValue, this.description("flatMapSingleElementValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementValueDifferentType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementValueDifferentType, this.description("flatMapSingleElementValueDifferentType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementValueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementValueNull, this.description("flatMapSingleElementValueNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementValueErrorThrown() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementValueErrorThrown, this.description("flatMapSingleElementValueErrorThrown"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementError, this.description("flatMapSingleElementError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleElementEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapSingleElementEmpty, this.description("flatMapSingleElementEmpty"));
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
        public void benchmark_singleErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleErrors, this.description("singleErrors"));
        }

        private MaybeConcatMapSingleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeConcatMapSingleTest();
        }

        @java.lang.Override
        public MaybeConcatMapSingleTest implementation() {
            return this.implementation;
        }
    }
}
