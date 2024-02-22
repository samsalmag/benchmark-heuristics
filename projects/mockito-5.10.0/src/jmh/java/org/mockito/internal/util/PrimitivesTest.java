/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PrimitivesTest {

    @Test
    public void should_not_return_null_for_primitives_wrappers() throws Exception {
        assertNotNull(Primitives.defaultValue(Boolean.class));
        assertNotNull(Primitives.defaultValue(Character.class));
        assertNotNull(Primitives.defaultValue(Byte.class));
        assertNotNull(Primitives.defaultValue(Short.class));
        assertNotNull(Primitives.defaultValue(Integer.class));
        assertNotNull(Primitives.defaultValue(Long.class));
        assertNotNull(Primitives.defaultValue(Float.class));
        assertNotNull(Primitives.defaultValue(Double.class));
    }

    @Test
    public void should_not_return_null_for_primitives() throws Exception {
        assertNotNull(Primitives.defaultValue(boolean.class));
        assertNotNull(Primitives.defaultValue(char.class));
        assertNotNull(Primitives.defaultValue(byte.class));
        assertNotNull(Primitives.defaultValue(short.class));
        assertNotNull(Primitives.defaultValue(int.class));
        assertNotNull(Primitives.defaultValue(long.class));
        assertNotNull(Primitives.defaultValue(float.class));
        assertNotNull(Primitives.defaultValue(double.class));
    }

    @Test
    public void should_default_values_for_primitive() {
        assertThat(Primitives.defaultValue(boolean.class)).isFalse();
        assertThat(Primitives.defaultValue(char.class)).isEqualTo('\u0000');
        assertThat(Primitives.defaultValue(byte.class)).isEqualTo((byte) 0);
        assertThat(Primitives.defaultValue(short.class)).isEqualTo((short) 0);
        assertThat(Primitives.defaultValue(int.class)).isEqualTo(0);
        assertThat(Primitives.defaultValue(long.class)).isEqualTo(0L);
        assertThat(Primitives.defaultValue(float.class)).isEqualTo(0.0F);
        assertThat(Primitives.defaultValue(double.class)).isEqualTo(0.0D);
    }

    @Test
    public void should_default_values_for_wrapper() {
        assertThat(Primitives.defaultValue(Boolean.class)).isFalse();
        assertThat(Primitives.defaultValue(Character.class)).isEqualTo('\u0000');
        assertThat(Primitives.defaultValue(Byte.class)).isEqualTo((byte) 0);
        assertThat(Primitives.defaultValue(Short.class)).isEqualTo((short) 0);
        assertThat(Primitives.defaultValue(Integer.class)).isEqualTo(0);
        assertThat(Primitives.defaultValue(Long.class)).isEqualTo(0L);
        assertThat(Primitives.defaultValue(Float.class)).isEqualTo(0.0F);
        assertThat(Primitives.defaultValue(Double.class)).isEqualTo(0.0D);
    }

    @Test
    public void should_return_null_for_everything_else() throws Exception {
        assertNull(Primitives.defaultValue(Object.class));
        assertNull(Primitives.defaultValue(String.class));
        assertNull(Primitives.defaultValue(null));
    }

    @Test
    public void should_check_that_value_type_is_assignable_to_wrapper_reference() {
        assertThat(Primitives.isAssignableFromWrapper(int.class, Integer.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Integer.class, Integer.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(long.class, Long.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Long.class, Long.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(double.class, Double.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Double.class, Double.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(float.class, Float.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Float.class, Float.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(char.class, Character.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Character.class, Character.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(short.class, Short.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Short.class, Short.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(byte.class, Byte.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Byte.class, Byte.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(boolean.class, Boolean.class)).isTrue();
        assertThat(Primitives.isAssignableFromWrapper(Boolean.class, Boolean.class)).isTrue();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_return_null_for_primitives_wrappers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_return_null_for_primitives_wrappers, this.description("should_not_return_null_for_primitives_wrappers"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_return_null_for_primitives() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_return_null_for_primitives, this.description("should_not_return_null_for_primitives"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_default_values_for_primitive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_default_values_for_primitive, this.description("should_default_values_for_primitive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_default_values_for_wrapper() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_default_values_for_wrapper, this.description("should_default_values_for_wrapper"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_null_for_everything_else() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_null_for_everything_else, this.description("should_return_null_for_everything_else"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_check_that_value_type_is_assignable_to_wrapper_reference() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_check_that_value_type_is_assignable_to_wrapper_reference, this.description("should_check_that_value_type_is_assignable_to_wrapper_reference"));
        }

        private PrimitivesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new PrimitivesTest();
        }

        @java.lang.Override
        public PrimitivesTest implementation() {
            return this.implementation;
        }
    }
}
