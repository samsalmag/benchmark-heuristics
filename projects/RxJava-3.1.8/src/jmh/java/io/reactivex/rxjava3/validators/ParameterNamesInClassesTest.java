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
package io.reactivex.rxjava3.validators;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParameterNamesInClassesTest {

    void method(int paramName) {
        // deliberately empty
    }

    @Test
    public void javacParametersEnabled() throws Exception {
        assertEquals("Please enable saving parameter names via the -parameters javac argument", "paramName", getClass().getDeclaredMethod("method", Integer.TYPE).getParameters()[0].getName());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_javacParametersEnabled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::javacParametersEnabled, this.description("javacParametersEnabled"));
        }

        private ParameterNamesInClassesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParameterNamesInClassesTest();
        }

        @java.lang.Override
        public ParameterNamesInClassesTest implementation() {
            return this.implementation;
        }
    }
}
