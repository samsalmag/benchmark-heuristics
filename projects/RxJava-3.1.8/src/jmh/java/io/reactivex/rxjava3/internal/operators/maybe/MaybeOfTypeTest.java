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
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeOfTypeTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).ofType(Integer.class).test().assertResult(1);
    }

    @Test
    public void normalDowncast() {
        TestObserver<Number> to = Maybe.just(1).ofType(Number.class).test();
        // don't make this fluent, target type required!
        to.assertResult((Number) 1);
    }

    @Test
    public void notInstance() {
        TestObserver<String> to = Maybe.just(1).ofType(String.class).test();
        // don't make this fluent, target type required!
        to.assertResult();
    }

    @Test
    public void error() {
        TestObserver<Number> to = Maybe.<Integer>error(new TestException()).ofType(Number.class).test();
        // don't make this fluent, target type required!
        to.assertFailure(TestException.class);
    }

    @Test
    public void errorNotInstance() {
        TestObserver<String> to = Maybe.<Integer>error(new TestException()).ofType(String.class).test();
        // don't make this fluent, target type required!
        to.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposedMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> m) throws Exception {
                return m.ofType(Object.class);
            }
        });
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().ofType(Object.class));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.ofType(Object.class);
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
        public void benchmark_normalDowncast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDowncast, this.description("normalDowncast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notInstance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::notInstance, this.description("notInstance"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotInstance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorNotInstance, this.description("errorNotInstance"));
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

        private MaybeOfTypeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeOfTypeTest();
        }

        @java.lang.Override
        public MaybeOfTypeTest implementation() {
            return this.implementation;
        }
    }
}
