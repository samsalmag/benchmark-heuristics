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
package io.reactivex.rxjava3.internal.observers;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.observers.TestObserver;

public class DeferredScalarDisposableTest extends RxJavaTest {

    @Test
    public void tryDispose() {
        TestObserver<Integer> to = new TestObserver<>();
        DeferredScalarDisposable<Integer> d = new DeferredScalarDisposable<>(to);
        to.onSubscribe(d);
        assertTrue(d.tryDispose());
        assertFalse(d.tryDispose());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryDispose, this.description("tryDispose"));
        }

        private DeferredScalarDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DeferredScalarDisposableTest();
        }

        @java.lang.Override
        public DeferredScalarDisposableTest implementation() {
            return this.implementation;
        }
    }
}
