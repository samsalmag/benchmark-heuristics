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
package io.reactivex.rxjava3.internal.functions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObjectHelperTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObjectHelper.class);
    }

    @Test
    public void verifyPositiveInt() throws Exception {
        assertEquals(1, ObjectHelper.verifyPositive(1, "param"));
    }

    @Test
    public void verifyPositiveLong() throws Exception {
        assertEquals(1L, ObjectHelper.verifyPositive(1L, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveIntFail() throws Exception {
        assertEquals(-1, ObjectHelper.verifyPositive(-1, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveLongFail() throws Exception {
        assertEquals(-1L, ObjectHelper.verifyPositive(-1L, "param"));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveInt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::verifyPositiveInt, this.description("verifyPositiveInt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveLong() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::verifyPositiveLong, this.description("verifyPositiveLong"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveIntFail() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::verifyPositiveIntFail, this.description("verifyPositiveIntFail"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveLongFail() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::verifyPositiveLongFail, this.description("verifyPositiveLongFail"), java.lang.IllegalArgumentException.class);
        }

        private ObjectHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObjectHelperTest();
        }

        @java.lang.Override
        public ObjectHelperTest implementation() {
            return this.implementation;
        }
    }
}
