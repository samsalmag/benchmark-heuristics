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

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeContainsTest extends RxJavaTest {

    @Test
    public void doesContain() {
        Maybe.just(1).contains(1).test().assertResult(true);
    }

    @Test
    public void doesntContain() {
        Maybe.just(1).contains(2).test().assertResult(false);
    }

    @Test
    public void empty() {
        Maybe.empty().contains(2).test().assertResult(false);
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).contains(2).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Boolean> to = pp.singleElement().contains(1).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().contains(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToSingle(new Function<Maybe<Object>, SingleSource<Boolean>>() {

            @Override
            public SingleSource<Boolean> apply(Maybe<Object> f) throws Exception {
                return f.contains(1);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasSource() {
        assertSame(Maybe.empty(), ((HasUpstreamMaybeSource<Object>) (Maybe.empty().contains(0))).source());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesContain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesContain, this.description("doesContain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntContain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesntContain, this.description("doesntContain"));
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
        public void benchmark_hasSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasSource, this.description("hasSource"));
        }

        private MaybeContainsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeContainsTest();
        }

        @java.lang.Override
        public MaybeContainsTest implementation() {
            return this.implementation;
        }
    }
}
