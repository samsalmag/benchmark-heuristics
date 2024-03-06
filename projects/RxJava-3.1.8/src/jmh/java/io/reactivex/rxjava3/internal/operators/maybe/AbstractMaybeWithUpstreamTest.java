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
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamMaybeSource;

public class AbstractMaybeWithUpstreamTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void upstream() {
        Maybe<Integer> source = Maybe.just(1);
        assertSame(source, ((HasUpstreamMaybeSource<Integer>) source.map(Functions.<Integer>identity())).source());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstream, this.description("upstream"));
        }

        private AbstractMaybeWithUpstreamTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AbstractMaybeWithUpstreamTest();
        }

        @java.lang.Override
        public AbstractMaybeWithUpstreamTest implementation() {
            return this.implementation;
        }
    }
}
