/*
 * Copyright (c) 2020 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.exceptions.base.MockitoException;

public class MockAnnotationProcessorTest {

    @SuppressWarnings("unused")
    private MockedStatic<Void> nonGeneric;

    @SuppressWarnings("unused")
    private MockedStatic<List<?>> generic;

    @SuppressWarnings({ "raw", "unused" })
    private MockedStatic raw;

    @Test
    public void testNonGeneric() throws Exception {
        Class<?> type = MockAnnotationProcessor.inferParameterizedType(MockAnnotationProcessorTest.class.getDeclaredField("nonGeneric").getGenericType(), "nonGeneric", "Sample");
        assertThat(type).isEqualTo(Void.class);
    }

    @Test
    public void testGeneric() {
        assertThatThrownBy(() -> {
            MockAnnotationProcessor.inferParameterizedType(MockAnnotationProcessorTest.class.getDeclaredField("generic").getGenericType(), "generic", "Sample");
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Mockito cannot infer a static mock from a raw type for generic");
    }

    @Test
    public void testRaw() {
        assertThatThrownBy(() -> {
            MockAnnotationProcessor.inferParameterizedType(MockAnnotationProcessorTest.class.getDeclaredField("raw").getGenericType(), "raw", "Sample");
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Mockito cannot infer a static mock from a raw type for raw");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testNonGeneric() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testNonGeneric, this.description("testNonGeneric"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testGeneric() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testGeneric, this.description("testGeneric"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testRaw() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testRaw, this.description("testRaw"));
        }

        private MockAnnotationProcessorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MockAnnotationProcessorTest();
        }

        @java.lang.Override
        public MockAnnotationProcessorTest implementation() {
            return this.implementation;
        }
    }
}
