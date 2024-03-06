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
package io.reactivex.rxjava3.internal.operators.completable;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromPublisherTest extends RxJavaTest {

    @Test
    public void fromPublisher() {
        Completable.fromPublisher(Flowable.just(1)).test().assertResult();
    }

    @Test
    public void fromPublisherEmpty() {
        Completable.fromPublisher(Flowable.empty()).test().assertResult();
    }

    @Test
    public void fromPublisherThrows() {
        Completable.fromPublisher(Flowable.error(new UnsupportedOperationException())).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.fromPublisher(Flowable.just(1)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return Completable.fromPublisher(f);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisher, this.description("fromPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisherEmpty, this.description("fromPublisherEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisherThrows, this.description("fromPublisherThrows"));
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

        private CompletableFromPublisherTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableFromPublisherTest();
        }

        @java.lang.Override
        public CompletableFromPublisherTest implementation() {
            return this.implementation;
        }
    }
}
