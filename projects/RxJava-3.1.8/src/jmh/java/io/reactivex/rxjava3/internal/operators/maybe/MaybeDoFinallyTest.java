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

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDoFinallyTest extends RxJavaTest implements Action {

    int calls;

    @Override
    public void run() throws Exception {
        calls++;
    }

    @Test
    public void normalJust() {
        Maybe.just(1).doFinally(this).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmpty() {
        Maybe.empty().doFinally(this).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalError() {
        Maybe.error(new TestException()).doFinally(this).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.doFinally(MaybeDoFinallyTest.this);
            }
        });
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.doFinally(MaybeDoFinallyTest.this).filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void normalJustConditional() {
        Maybe.just(1).doFinally(this).filter(Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmptyConditional() {
        Maybe.empty().doFinally(this).filter(Functions.alwaysTrue()).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalErrorConditional() {
        Maybe.error(new TestException()).doFinally(this).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void actionThrowsConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().singleElement().doFinally(this));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJust, this.description("normalJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmpty, this.description("normalEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalError, this.description("normalError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJustConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJustConditional, this.description("normalJustConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmptyConditional, this.description("normalEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalErrorConditional, this.description("normalErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrows, this.description("actionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrowsConditional, this.description("actionThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        private MaybeDoFinallyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDoFinallyTest();
        }

        @java.lang.Override
        public MaybeDoFinallyTest implementation() {
            return this.implementation;
        }
    }
}
