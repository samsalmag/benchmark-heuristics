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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;

public class ComputationSchedulerInternalTest extends RxJavaTest {

    @Test
    public void capPoolSize() {
        assertEquals(8, ComputationScheduler.cap(8, -1));
        assertEquals(8, ComputationScheduler.cap(8, 0));
        assertEquals(4, ComputationScheduler.cap(8, 4));
        assertEquals(8, ComputationScheduler.cap(8, 8));
        assertEquals(8, ComputationScheduler.cap(8, 9));
        assertEquals(8, ComputationScheduler.cap(8, 16));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capPoolSize() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::capPoolSize, this.description("capPoolSize"));
        }

        private ComputationSchedulerInternalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ComputationSchedulerInternalTest();
        }

        @java.lang.Override
        public ComputationSchedulerInternalTest implementation() {
            return this.implementation;
        }
    }
}
