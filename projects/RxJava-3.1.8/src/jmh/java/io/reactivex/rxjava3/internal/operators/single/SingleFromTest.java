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

import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SingleFromTest extends RxJavaTest {

    @Test
    public void fromFuture() throws Exception {
        Single.fromFuture(Flowable.just(1).toFuture()).subscribeOn(Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void fromFutureTimeout() throws Exception {
        Single.fromFuture(Flowable.never().toFuture(), 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void fromPublisher() {
        Single.fromPublisher(Flowable.just(1)).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFuture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFuture, this.description("fromFuture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFutureTimeout, this.description("fromFutureTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromPublisher, this.description("fromPublisher"));
        }

        private SingleFromTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleFromTest();
        }

        @java.lang.Override
        public SingleFromTest implementation() {
            return this.implementation;
        }
    }
}
