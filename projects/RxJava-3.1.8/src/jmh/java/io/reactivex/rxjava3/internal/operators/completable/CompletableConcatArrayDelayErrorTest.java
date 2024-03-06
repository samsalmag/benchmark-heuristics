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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;

public class CompletableConcatArrayDelayErrorTest {

    @Test
    public void normal() throws Throwable {
        Action action1 = mock(Action.class);
        Action action2 = mock(Action.class);
        Completable.concatArrayDelayError(Completable.fromAction(action1), Completable.error(new TestException()), Completable.fromAction(action2)).test().assertFailure(TestException.class);
        verify(action1).run();
        verify(action2).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        private CompletableConcatArrayDelayErrorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableConcatArrayDelayErrorTest();
        }

        @java.lang.Override
        public CompletableConcatArrayDelayErrorTest implementation() {
            return this.implementation;
        }
    }
}
