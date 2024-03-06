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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ExecutorSchedulerInternalTest {

    @Test
    public void helperHolder() {
        assertNotNull(new ExecutorScheduler.SingleHolder());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_helperHolder() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::helperHolder, this.description("helperHolder"));
        }

        private ExecutorSchedulerInternalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ExecutorSchedulerInternalTest();
        }

        @java.lang.Override
        public ExecutorSchedulerInternalTest implementation() {
            return this.implementation;
        }
    }
}
