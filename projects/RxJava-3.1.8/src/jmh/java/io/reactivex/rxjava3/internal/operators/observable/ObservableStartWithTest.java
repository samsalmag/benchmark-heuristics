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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class ObservableStartWithTest {

    @Test
    public void justCompletableComplete() {
        Observable.just(1).startWith(Completable.complete()).test().assertResult(1);
    }

    @Test
    public void emptyCompletableComplete() {
        Observable.empty().startWith(Completable.complete()).test().assertResult();
    }

    @Test
    public void runCompletableError() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Completable.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justSingleJust() {
        Observable.just(1).startWith(Single.just(2)).test().assertResult(2, 1);
    }

    @Test
    public void emptySingleJust() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Single.just(2)).test().assertResult(2);
        verify(run).run();
    }

    @Test
    public void runSingleError() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Single.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justMaybeJust() {
        Observable.just(1).startWith(Maybe.just(2)).test().assertResult(2, 1);
    }

    @Test
    public void emptyMaybeJust() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Maybe.just(2)).test().assertResult(2);
        verify(run).run();
    }

    @Test
    public void runMaybeError() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Maybe.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justObservableJust() {
        Observable.just(1).startWith(Observable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5, 1);
    }

    @Test
    public void emptyObservableJust() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Observable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5);
        verify(run).run();
    }

    @Test
    public void emptyObservableEmpty() {
        Runnable run = mock(Runnable.class);
        Runnable run2 = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Observable.fromRunnable(run2)).test().assertResult();
        verify(run).run();
        verify(run2).run();
    }

    @Test
    public void runObservableError() {
        Runnable run = mock(Runnable.class);
        Observable.fromRunnable(run).startWith(Observable.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justCompletableComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justCompletableComplete, this.description("justCompletableComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyCompletableComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyCompletableComplete, this.description("emptyCompletableComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runCompletableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runCompletableError, this.description("runCompletableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSingleJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justSingleJust, this.description("justSingleJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySingleJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptySingleJust, this.description("emptySingleJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runSingleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runSingleError, this.description("runSingleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justMaybeJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justMaybeJust, this.description("justMaybeJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyMaybeJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyMaybeJust, this.description("emptyMaybeJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runMaybeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runMaybeError, this.description("runMaybeError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justObservableJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justObservableJust, this.description("justObservableJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservableJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyObservableJust, this.description("emptyObservableJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyObservableEmpty, this.description("emptyObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runObservableError, this.description("runObservableError"));
        }

        private ObservableStartWithTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableStartWithTest();
        }

        @java.lang.Override
        public ObservableStartWithTest implementation() {
            return this.implementation;
        }
    }
}
