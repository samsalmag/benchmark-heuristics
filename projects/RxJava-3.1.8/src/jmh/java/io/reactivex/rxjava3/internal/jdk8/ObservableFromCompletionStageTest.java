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

import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;

public class ObservableFromCompletionStageTest extends RxJavaTest {

    @Test
    public void syncSuccess() {
        Observable.fromCompletionStage(CompletableFuture.completedFuture(1)).test().assertResult(1);
    }

    @Test
    public void syncFailure() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        cf.completeExceptionally(new TestException());
        Observable.fromCompletionStage(cf).test().assertFailure(TestException.class);
    }

    @Test
    public void syncNull() {
        Observable.fromCompletionStage(CompletableFuture.<Integer>completedFuture(null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void cancel() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        TestObserver<Integer> to = Observable.fromCompletionStage(cf).test();
        to.assertEmpty();
        to.dispose();
        cf.complete(1);
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncSuccess, this.description("syncSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFailure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFailure, this.description("syncFailure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncNull, this.description("syncNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        private ObservableFromCompletionStageTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableFromCompletionStageTest();
        }

        @java.lang.Override
        public ObservableFromCompletionStageTest implementation() {
            return this.implementation;
        }
    }
}
