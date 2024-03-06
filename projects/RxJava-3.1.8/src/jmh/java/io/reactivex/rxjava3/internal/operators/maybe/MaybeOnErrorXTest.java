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

import java.io.IOException;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeOnErrorXTest extends RxJavaTest {

    @Test
    public void onErrorReturnConst() {
        Maybe.error(new TestException()).onErrorReturnItem(1).test().assertResult(1);
    }

    @Test
    public void onErrorReturn() {
        Maybe.error(new TestException()).onErrorReturn(Functions.justFunction(1)).test().assertResult(1);
    }

    @Test
    public void onErrorComplete() {
        Maybe.error(new TestException()).onErrorComplete().test().assertResult();
    }

    @Test
    public void onErrorCompleteTrue() {
        Maybe.error(new TestException()).onErrorComplete(Functions.alwaysTrue()).test().assertResult();
    }

    @Test
    public void onErrorCompleteFalse() {
        Maybe.error(new TestException()).onErrorComplete(Functions.alwaysFalse()).test().assertFailure(TestException.class);
    }

    @Test
    public void onErrorReturnFunctionThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

            @Override
            public Object apply(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorCompletePredicateThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorResumeNext() {
        Maybe.error(new TestException()).onErrorResumeNext(Functions.justFunction(Maybe.just(1))).test().assertResult(1);
    }

    @Test
    public void onErrorResumeNextFunctionThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorResumeNext(new Function<Throwable, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorReturnSuccess() {
        Maybe.just(1).onErrorReturnItem(2).test().assertResult(1);
    }

    @Test
    public void onErrorReturnEmpty() {
        Maybe.<Integer>empty().onErrorReturnItem(2).test().assertResult();
    }

    @Test
    public void onErrorReturnDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorReturnItem(1));
    }

    @Test
    public void onErrorReturnDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorReturnItem(1);
            }
        });
    }

    @Test
    public void onErrorCompleteSuccess() {
        Maybe.just(1).onErrorComplete().test().assertResult(1);
    }

    @Test
    public void onErrorCompleteEmpty() {
        Maybe.<Integer>empty().onErrorComplete().test().assertResult();
    }

    @Test
    public void onErrorCompleteDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorComplete());
    }

    @Test
    public void onErrorCompleteDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorComplete();
            }
        });
    }

    @Test
    public void onErrorNextDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorResumeWith(Maybe.just(1)));
    }

    @Test
    public void onErrorNextDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorResumeWith(Maybe.just(1));
            }
        });
    }

    @Test
    public void onErrorNextIsAlsoError() {
        Maybe.error(new TestException("Main")).onErrorResumeWith(Maybe.error(new TestException("Secondary"))).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "Secondary");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnConst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnConst, this.description("onErrorReturnConst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturn, this.description("onErrorReturn"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorComplete, this.description("onErrorComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteTrue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteTrue, this.description("onErrorCompleteTrue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteFalse, this.description("onErrorCompleteFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnFunctionThrows, this.description("onErrorReturnFunctionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompletePredicateThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompletePredicateThrows, this.description("onErrorCompletePredicateThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNext, this.description("onErrorResumeNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextFunctionThrows, this.description("onErrorResumeNextFunctionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnSuccess, this.description("onErrorReturnSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnEmpty, this.description("onErrorReturnEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnDispose, this.description("onErrorReturnDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnDoubleOnSubscribe, this.description("onErrorReturnDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteSuccess, this.description("onErrorCompleteSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteEmpty, this.description("onErrorCompleteEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteDispose, this.description("onErrorCompleteDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteDoubleOnSubscribe, this.description("onErrorCompleteDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorNextDispose, this.description("onErrorNextDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorNextDoubleOnSubscribe, this.description("onErrorNextDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextIsAlsoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorNextIsAlsoError, this.description("onErrorNextIsAlsoError"));
        }

        private MaybeOnErrorXTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeOnErrorXTest();
        }

        @java.lang.Override
        public MaybeOnErrorXTest implementation() {
            return this.implementation;
        }
    }
}
