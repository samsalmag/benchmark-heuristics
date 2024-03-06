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

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableInternalHelperTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObservableInternalHelper.class);
    }

    @Test
    public void enums() {
        assertNotNull(ObservableInternalHelper.MapToInt.values()[0]);
        assertNotNull(ObservableInternalHelper.MapToInt.valueOf("INSTANCE"));
    }

    @Test
    public void mapToInt() throws Exception {
        assertEquals(0, ObservableInternalHelper.MapToInt.INSTANCE.apply(null));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enums() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::enums, this.description("enums"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapToInt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapToInt, this.description("mapToInt"));
        }

        private ObservableInternalHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableInternalHelperTest();
        }

        @java.lang.Override
        public ObservableInternalHelperTest implementation() {
            return this.implementation;
        }
    }
}
