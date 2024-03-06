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

import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableResumeNextTest extends RxJavaTest {

    @Test
    public void resumeNextError() {
        Completable.error(new TestException()).onErrorResumeNext(Functions.justFunction(Completable.error(new TestException("second")))).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "second");
    }

    @Test
    public void disposeInMain() {
        TestHelper.checkDisposedCompletable(new Function<Completable, CompletableSource>() {

            @Override
            public CompletableSource apply(Completable c) throws Exception {
                return c.onErrorResumeNext(Functions.justFunction(Completable.complete()));
            }
        });
    }

    @Test
    public void disposeInResume() {
        TestHelper.checkDisposedCompletable(new Function<Completable, CompletableSource>() {

            @Override
            public CompletableSource apply(Completable c) throws Exception {
                return Completable.error(new TestException()).onErrorResumeNext(Functions.justFunction(c));
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Completable.error(new TestException()).onErrorResumeNext(Functions.justFunction(Completable.never())));
    }

    @Test
    public void resumeWithNoError() throws Throwable {
        Action action = mock(Action.class);
        Completable.complete().onErrorResumeWith(Completable.fromAction(action)).test().assertResult();
        verify(action, never()).run();
    }

    @Test
    public void resumeWithError() throws Throwable {
        Action action = mock(Action.class);
        Completable.error(new TestException()).onErrorResumeWith(Completable.fromAction(action)).test().assertResult();
        verify(action).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resumeNextError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resumeNextError, this.description("resumeNextError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeInMain, this.description("disposeInMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInResume() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeInResume, this.description("disposeInResume"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resumeWithNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resumeWithNoError, this.description("resumeWithNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resumeWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resumeWithError, this.description("resumeWithError"));
        }

        private CompletableResumeNextTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableResumeNextTest();
        }

        @java.lang.Override
        public CompletableResumeNextTest implementation() {
            return this.implementation;
        }
    }
}
