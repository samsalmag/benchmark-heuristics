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
package io.reactivex.rxjava3.internal.jdk8;

import java.util.Optional;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;

public class MaybeFromOptionalTest extends RxJavaTest {

    @Test
    public void hasValue() {
        Maybe.fromOptional(Optional.of(1)).test().assertResult(1);
    }

    @Test
    public void empty() {
        Maybe.fromOptional(Optional.empty()).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasValue, this.description("hasValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        private MaybeFromOptionalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeFromOptionalTest();
        }

        @java.lang.Override
        public MaybeFromOptionalTest implementation() {
            return this.implementation;
        }
    }
}
