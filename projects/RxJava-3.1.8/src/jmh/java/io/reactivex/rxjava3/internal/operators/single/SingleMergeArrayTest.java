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

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class SingleMergeArrayTest extends RxJavaTest {

    @Test
    public void normal() {
        Single.mergeArray(Single.just(1), Single.just(2), Single.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void error() {
        Single.mergeArray(Single.just(1), Single.error(new TestException()), Single.just(3)).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void normalDelayError() {
        Single.mergeArrayDelayError(Single.just(1), Single.just(2), Single.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void errorDelayError() {
        Single.mergeArrayDelayError(Single.just(1), Single.error(new TestException()), Single.just(3)).test().assertFailure(TestException.class, 1, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalDelayError, this.description("normalDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayError, this.description("errorDelayError"));
        }

        private SingleMergeArrayTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleMergeArrayTest();
        }

        @java.lang.Override
        public SingleMergeArrayTest implementation() {
            return this.implementation;
        }
    }
}
