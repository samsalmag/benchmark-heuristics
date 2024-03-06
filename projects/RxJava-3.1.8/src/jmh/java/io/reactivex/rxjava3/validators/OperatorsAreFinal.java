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

import java.io.File;
import java.lang.reflect.Modifier;
import org.junit.Test;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class OperatorsAreFinal {

    File directoryOf(String baseClassName) throws Exception {
        File f = TestHelper.findSource("Flowable");
        if (f == null) {
            return null;
        }
        String parent = f.getParentFile().getParentFile().getAbsolutePath().replace('\\', '/');
        if (!parent.endsWith("/")) {
            parent += "/";
        }
        parent += "internal/operators/" + baseClassName.toLowerCase() + "/";
        return new File(parent);
    }

    void check(String baseClassName) throws Exception {
        File f = directoryOf(baseClassName);
        if (f == null) {
            return;
        }
        StringBuilder e = new StringBuilder();
        File[] files = f.listFiles();
        if (files != null) {
            for (File g : files) {
                if (g.getName().startsWith(baseClassName) && g.getName().endsWith(".java")) {
                    String className = "io.reactivex.rxjava3.internal.operators." + baseClassName.toLowerCase() + "." + g.getName().replace(".java", "");
                    Class<?> clazz = Class.forName(className);
                    if ((clazz.getModifiers() & Modifier.FINAL) == 0 && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
                        e.append("java.lang.RuntimeException: ").append(className).append(" is not final\r\n");
                        e.append(" at ").append(className).append(" (").append(g.getName()).append(":14)\r\n\r\n");
                    }
                }
            }
        }
        if (e.length() != 0) {
            System.out.println(e);
            throw new AssertionError(e.toString());
        }
    }

    @Test
    public void flowable() throws Exception {
        check("Flowable");
    }

    @Test
    public void observable() throws Exception {
        check("Observable");
    }

    @Test
    public void single() throws Exception {
        check("Single");
    }

    @Test
    public void completable() throws Exception {
        check("Completable");
    }

    @Test
    public void maybe() throws Exception {
        check("Maybe");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowable, this.description("flowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observable, this.description("observable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single, this.description("single"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completable, this.description("completable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybe, this.description("maybe"));
        }

        private OperatorsAreFinal implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new OperatorsAreFinal();
        }

        @java.lang.Override
        public OperatorsAreFinal implementation() {
            return this.implementation;
        }
    }
}
