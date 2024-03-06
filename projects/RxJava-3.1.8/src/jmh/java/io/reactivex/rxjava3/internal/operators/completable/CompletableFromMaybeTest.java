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

public class CompletableFromMaybeTest extends RxJavaTest {

    @Test
    public void fromMaybe() {
        Completable.fromMaybe(Maybe.just(1)).test().assertResult();
    }

    @Test
    public void fromMaybeEmpty() {
        Completable.fromMaybe(Maybe.<Integer>empty()).test().assertResult();
    }

    @Test
    public void fromMaybeError() {
        Completable.fromMaybe(Maybe.error(new UnsupportedOperationException())).test().assertFailure(UnsupportedOperationException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromMaybe, this.description("fromMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromMaybeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromMaybeEmpty, this.description("fromMaybeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromMaybeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromMaybeError, this.description("fromMaybeError"));
        }

        private CompletableFromMaybeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableFromMaybeTest();
        }

        @java.lang.Override
        public CompletableFromMaybeTest implementation() {
            return this.implementation;
        }
    }
}
