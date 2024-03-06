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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeIsEmptyTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).isEmpty().test().assertResult(false);
    }

    @Test
    public void empty() {
        Maybe.empty().isEmpty().test().assertResult(true);
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).isEmpty().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedBackToMaybe() {
        assertTrue(Maybe.just(1).isEmpty().toMaybe() instanceof MaybeIsEmpty);
    }

    @Test
    public void normalToMaybe() {
        Maybe.just(1).isEmpty().toMaybe().test().assertResult(false);
    }

    @Test
    public void emptyToMaybe() {
        Maybe.empty().isEmpty().toMaybe().test().assertResult(true);
    }

    @Test
    public void errorToMaybe() {
        Maybe.error(new TestException()).isEmpty().toMaybe().test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposedMaybeToSingle(new Function<Maybe<Object>, SingleSource<Boolean>>() {

            @Override
            public SingleSource<Boolean> apply(Maybe<Object> m) throws Exception {
                return m.isEmpty();
            }
        });
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().isEmpty());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToSingle(new Function<Maybe<Object>, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Maybe<Object> f) throws Exception {
                return f.isEmpty();
            }
        });
    }

    @Test
    public void disposeToMaybe() {
        TestHelper.checkDisposedMaybe(new Function<Maybe<Object>, Maybe<Boolean>>() {

            @Override
            public Maybe<Boolean> apply(Maybe<Object> m) throws Exception {
                return m.isEmpty().toMaybe();
            }
        });
    }

    @Test
    public void isDisposedToMaybe() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().isEmpty().toMaybe());
    }

    @Test
    public void doubleOnSubscribeToMaybe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Boolean>>() {

            @Override
            public Maybe<Boolean> apply(Maybe<Object> f) throws Exception {
                return f.isEmpty().toMaybe();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedBackToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedBackToMaybe, this.description("fusedBackToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalToMaybe, this.description("normalToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyToMaybe, this.description("emptyToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorToMaybe, this.description("errorToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDisposed, this.description("isDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeToMaybe, this.description("disposeToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposedToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDisposedToMaybe, this.description("isDisposedToMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeToMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribeToMaybe, this.description("doubleOnSubscribeToMaybe"));
        }

        private MaybeIsEmptyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeIsEmptyTest();
        }

        @java.lang.Override
        public MaybeIsEmptyTest implementation() {
            return this.implementation;
        }
    }
}
