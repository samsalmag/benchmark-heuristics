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

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeFlatMapNotificationTest extends RxJavaTest {

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.just(1).flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Maybe<Integer> m) throws Exception {
                return m.flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1)));
            }
        });
    }

    @Test
    public void onSuccessNull() {
        Maybe.just(1).flatMap(Functions.justFunction((Maybe<Integer>) null), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onErrorNull() {
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException()).flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction((Maybe<Integer>) null), Functions.justSupplier(Maybe.just(1))).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> ce = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(ce, 0, TestException.class);
        TestHelper.assertError(ce, 1, NullPointerException.class);
    }

    @Test
    public void onCompleteNull() {
        Maybe.<Integer>empty().flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier((Maybe<Integer>) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onSuccessEmpty() {
        Maybe.just(1).flatMap(Functions.justFunction(Maybe.<Integer>empty()), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))).test().assertResult();
    }

    @Test
    public void onSuccessError() {
        Maybe.just(1).flatMap(Functions.justFunction(Maybe.<Integer>error(new TestException())), Functions.justFunction((Maybe<Integer>) null), Functions.justSupplier(Maybe.just(1))).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

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
        public void benchmark_onSuccessNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessNull, this.description("onSuccessNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorNull, this.description("onErrorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteNull, this.description("onCompleteNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessEmpty, this.description("onSuccessEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessError, this.description("onSuccessError"));
        }

        private MaybeFlatMapNotificationTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeFlatMapNotificationTest();
        }

        @java.lang.Override
        public MaybeFlatMapNotificationTest implementation() {
            return this.implementation;
        }
    }
}
