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

public class SingleStartWithTest {

    @Test
    public void justCompletableComplete() {
        Single.just(1).startWith(Completable.complete()).test().assertResult(1);
    }

    @Test
    public void justCompletableError() {
        Single.just(1).startWith(Completable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void justSingleJust() {
        Single.just(1).startWith(Single.just(0)).test().assertResult(0, 1);
    }

    @Test
    public void justSingleError() {
        Single.just(1).startWith(Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void justMaybeJust() {
        Single.just(1).startWith(Maybe.just(0)).test().assertResult(0, 1);
    }

    @Test
    public void justMaybeEmpty() {
        Single.just(1).startWith(Maybe.empty()).test().assertResult(1);
    }

    @Test
    public void justMaybeError() {
        Single.just(1).startWith(Maybe.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void justObservableJust() {
        Single.just(1).startWith(Observable.just(-1, 0)).test().assertResult(-1, 0, 1);
    }

    @Test
    public void justObservableEmpty() {
        Single.just(1).startWith(Observable.empty()).test().assertResult(1);
    }

    @Test
    public void justObservableError() {
        Single.just(1).startWith(Observable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void justFlowableJust() {
        Single.just(1).startWith(Flowable.just(-1, 0)).test().assertResult(-1, 0, 1);
    }

    @Test
    public void justFlowableEmpty() {
        Single.just(1).startWith(Observable.empty()).test().assertResult(1);
    }

    @Test
    public void justFlowableError() {
        Single.just(1).startWith(Flowable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justCompletableComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justCompletableComplete, this.description("justCompletableComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justCompletableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justCompletableError, this.description("justCompletableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSingleJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justSingleJust, this.description("justSingleJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSingleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justSingleError, this.description("justSingleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justMaybeJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justMaybeJust, this.description("justMaybeJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justMaybeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justMaybeEmpty, this.description("justMaybeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justMaybeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justMaybeError, this.description("justMaybeError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justObservableJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justObservableJust, this.description("justObservableJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justObservableEmpty, this.description("justObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justObservableError, this.description("justObservableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justFlowableJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justFlowableJust, this.description("justFlowableJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justFlowableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justFlowableEmpty, this.description("justFlowableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justFlowableError, this.description("justFlowableError"));
        }

        private SingleStartWithTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleStartWithTest();
        }

        @java.lang.Override
        public SingleStartWithTest implementation() {
            return this.implementation;
        }
    }
}
