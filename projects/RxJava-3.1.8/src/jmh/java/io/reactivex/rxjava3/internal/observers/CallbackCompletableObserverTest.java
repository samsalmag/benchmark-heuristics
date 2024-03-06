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
import io.reactivex.rxjava3.internal.functions.Functions;

public final class CallbackCompletableObserverTest extends RxJavaTest {

    @Test
    public void emptyActionShouldReportNoCustomOnError() {
        CallbackCompletableObserver o = new CallbackCompletableObserver(Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION);
        assertFalse(o.hasCustomOnError());
    }

    @Test
    public void customOnErrorShouldReportCustomOnError() {
        CallbackCompletableObserver o = new CallbackCompletableObserver(Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION);
        assertTrue(o.hasCustomOnError());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyActionShouldReportNoCustomOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyActionShouldReportNoCustomOnError, this.description("emptyActionShouldReportNoCustomOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customOnErrorShouldReportCustomOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customOnErrorShouldReportCustomOnError, this.description("customOnErrorShouldReportCustomOnError"));
        }

        private CallbackCompletableObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CallbackCompletableObserverTest();
        }

        @java.lang.Override
        public CallbackCompletableObserverTest implementation() {
            return this.implementation;
        }
    }
}
