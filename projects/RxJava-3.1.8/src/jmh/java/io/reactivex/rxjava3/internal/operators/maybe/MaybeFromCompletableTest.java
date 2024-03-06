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

import static org.junit.Assert.assertSame;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamCompletableSource;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeFromCompletableTest extends RxJavaTest {

    @Test
    public void fromCompletable() {
        Maybe.fromCompletable(Completable.complete()).test().assertResult();
    }

    @Test
    public void fromCompletableError() {
        Maybe.fromCompletable(Completable.error(new UnsupportedOperationException())).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void source() {
        Completable c = Completable.complete();
        assertSame(c, ((HasUpstreamCompletableSource) Maybe.fromCompletable(c)).source());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.fromCompletable(PublishProcessor.create().ignoreElements()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToMaybe(new Function<Completable, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Completable v) throws Exception {
                return Maybe.fromCompletable(v);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromCompletable, this.description("fromCompletable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromCompletableError, this.description("fromCompletableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::source, this.description("source"));
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

        private MaybeFromCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeFromCompletableTest();
        }

        @java.lang.Override
        public MaybeFromCompletableTest implementation() {
            return this.implementation;
        }
    }
}
