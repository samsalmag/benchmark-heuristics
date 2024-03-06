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
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

/**
 * Verify static methods and final methods declaring type arguments
 * declare {@code @NonNull} for said argument.
 */
public class NonNullMethodTypeArgumentCheck {

    static void process(Class<?> clazz) {
        String className = clazz.getSimpleName();
        String parentPackage = clazz.getPackage().getName();
        StringBuilder result = new StringBuilder();
        int count = 0;
        try {
            File f = TestHelper.findSource(className, parentPackage);
            try (BufferedReader in = Files.newBufferedReader(f.toPath())) {
                int lineCount = 1;
                String line = null;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (!line.contains(" to(")) {
                        if (line.startsWith("public static <") || line.startsWith("public final <")) {
                            for (String ta : parseTypeArguments(line)) {
                                if (!ta.startsWith("@NonNull") && !ta.startsWith("@Nullable")) {
                                    if (!("Maybe".equals(clazz.getSimpleName()) && (line.contains("fromCallable(") || line.contains("fromSupplier(")))) {
                                        result.append("Missing annotation on argument ").append(ta).append("\r\nat ").append(parentPackage).append(".").append(className).append(".method(").append(className).append(".java:").append(lineCount).append(")\r\n");
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    lineCount++;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (count != 0) {
            throw new IllegalArgumentException("Found " + count + " cases\r\n" + result.toString());
        }
    }

    static List<String> parseTypeArguments(String line) {
        List<String> result = new ArrayList<>();
        int offset = line.indexOf("<");
        int c = 1;
        int i = offset + 1;
        int j = i;
        for (; i < line.length(); i++) {
            if (line.charAt(i) == '<') {
                c++;
            } else if (line.charAt(i) == '>') {
                c--;
                if (c == 0) {
                    break;
                }
            } else if (line.charAt(i) == ',' && c == 1) {
                result.add(line.substring(j, i).trim());
                j = i + 1;
            }
        }
        result.add(line.substring(j, i).trim());
        return result;
    }

    @Test
    public void parseTypeArguments() {
        assertEquals(new ArrayList<>(Arrays.asList("T")), parseTypeArguments("<T>"));
        assertEquals(new ArrayList<>(Arrays.asList("T", "U")), parseTypeArguments("<T, U>"));
        assertEquals(new ArrayList<>(Arrays.asList("T", "Flowable<U>")), parseTypeArguments("<T, Flowable<U>>"));
        assertEquals(new ArrayList<>(Arrays.asList("T", "Flowable<U, V>")), parseTypeArguments("<T, Flowable<U, V>>"));
    }

    @Test
    public void flowable() {
        process(Flowable.class);
    }

    @Test
    public void observable() {
        process(Observable.class);
    }

    @Test
    public void maybe() {
        process(Maybe.class);
    }

    @Test
    public void single() {
        process(Single.class);
    }

    @Test
    public void completable() {
        process(Completable.class);
    }

    @Test
    public void parallel() {
        process(ParallelFlowable.class);
    }

    @Test
    public void plugins() {
        process(RxJavaPlugins.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_parseTypeArguments() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::parseTypeArguments, this.description("parseTypeArguments"));
        }

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
        public void benchmark_maybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybe, this.description("maybe"));
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
        public void benchmark_parallel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::parallel, this.description("parallel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_plugins() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::plugins, this.description("plugins"));
        }

        private NonNullMethodTypeArgumentCheck implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new NonNullMethodTypeArgumentCheck();
        }

        @java.lang.Override
        public NonNullMethodTypeArgumentCheck implementation() {
            return this.implementation;
        }
    }
}
