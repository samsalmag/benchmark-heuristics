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

public class CompletableFromObservableTest extends RxJavaTest {

    @Test
    public void fromObservable() {
        Completable.fromObservable(Observable.just(1)).test().assertResult();
    }

    @Test
    public void fromObservableEmpty() {
        Completable.fromObservable(Observable.empty()).test().assertResult();
    }

    @Test
    public void fromObservableError() {
        Completable.fromObservable(Observable.error(new UnsupportedOperationException())).test().assertFailure(UnsupportedOperationException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromObservable, this.description("fromObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromObservableEmpty, this.description("fromObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromObservableError, this.description("fromObservableError"));
        }

        private CompletableFromObservableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableFromObservableTest();
        }

        @java.lang.Override
        public CompletableFromObservableTest implementation() {
            return this.implementation;
        }
    }
}
