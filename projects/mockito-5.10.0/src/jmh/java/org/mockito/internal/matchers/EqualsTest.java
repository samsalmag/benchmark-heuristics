/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockitoutil.TestBase;

public class EqualsTest extends TestBase {

    @Test
    public void shouldBeEqual() {
        assertEquals(new Equals(null), new Equals(null));
        assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
        assertFalse(new Equals(null).equals(null));
        assertFalse(new Equals(null).equals("Test"));
        assertEquals(1, new Equals(null).hashCode());
    }

    @Test
    public void shouldArraysBeEqual() {
        assertTrue(new Equals(new int[] { 1, 2 }).matches(new int[] { 1, 2 }));
        assertFalse(new Equals(new Object[] { "1" }).matches(new Object[] { "1.0" }));
    }

    @Test
    public void shouldDescribeWithExtraTypeInfo() throws Exception {
        String descStr = new Equals(100).toStringWithType(Integer.class.getSimpleName());
        assertEquals("(Integer) 100", descStr);
    }

    @Test
    public void shouldDescribeWithExtraTypeInfoOfLong() throws Exception {
        String descStr = new Equals(100L).toStringWithType(Long.class.getSimpleName());
        assertEquals("(Long) 100L", descStr);
    }

    @Test
    public void shouldDescribeWithTypeOfString() throws Exception {
        String descStr = new Equals("x").toStringWithType(String.class.getSimpleName());
        assertEquals("(String) \"x\"", descStr);
    }

    @Test
    public void shouldAppendQuotingForString() {
        String descStr = new Equals("str").toString();
        assertEquals("\"str\"", descStr);
    }

    @Test
    public void shouldAppendQuotingForChar() {
        String descStr = new Equals('s').toString();
        assertEquals("'s'", descStr);
    }

    @Test
    public void shouldDescribeUsingToString() {
        String descStr = new Equals(100).toString();
        assertEquals("100", descStr);
    }

    @Test
    public void shouldDescribeNull() {
        String descStr = new Equals(null).toString();
        assertEquals("null", descStr);
    }

    @Test
    public void shouldMatchTypes() throws Exception {
        // when
        ContainsExtraTypeInfo equals = new Equals(10);
        // then
        assertTrue(equals.typeMatches(10));
        assertFalse(equals.typeMatches(10L));
    }

    @Test
    public void shouldMatchTypesSafelyWhenActualIsNull() throws Exception {
        // when
        ContainsExtraTypeInfo equals = new Equals(null);
        // then
        assertFalse(equals.typeMatches(10));
    }

    @Test
    public void shouldMatchTypesSafelyWhenGivenIsNull() throws Exception {
        // when
        ContainsExtraTypeInfo equals = new Equals(10);
        // then
        assertFalse(equals.typeMatches(null));
    }

    @Test
    public void shouldInferType() {
        assertThat(new Equals("String").type()).isEqualTo(String.class);
    }

    @Test
    public void shouldDefaultTypeOnNull() {
        assertThat(new Equals(null).type()).isEqualTo(((ArgumentMatcher<Object>) argument -> false).type());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldBeEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldBeEqual, this.description("shouldBeEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldArraysBeEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldArraysBeEqual, this.description("shouldArraysBeEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDescribeWithExtraTypeInfo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDescribeWithExtraTypeInfo, this.description("shouldDescribeWithExtraTypeInfo"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDescribeWithExtraTypeInfoOfLong() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDescribeWithExtraTypeInfoOfLong, this.description("shouldDescribeWithExtraTypeInfoOfLong"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDescribeWithTypeOfString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDescribeWithTypeOfString, this.description("shouldDescribeWithTypeOfString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAppendQuotingForString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAppendQuotingForString, this.description("shouldAppendQuotingForString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAppendQuotingForChar() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAppendQuotingForChar, this.description("shouldAppendQuotingForChar"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDescribeUsingToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDescribeUsingToString, this.description("shouldDescribeUsingToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDescribeNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDescribeNull, this.description("shouldDescribeNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchTypes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchTypes, this.description("shouldMatchTypes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchTypesSafelyWhenActualIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchTypesSafelyWhenActualIsNull, this.description("shouldMatchTypesSafelyWhenActualIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchTypesSafelyWhenGivenIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchTypesSafelyWhenGivenIsNull, this.description("shouldMatchTypesSafelyWhenGivenIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldInferType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldInferType, this.description("shouldInferType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDefaultTypeOnNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDefaultTypeOnNull, this.description("shouldDefaultTypeOnNull"));
        }

        private EqualsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new EqualsTest();
        }

        @java.lang.Override
        public EqualsTest implementation() {
            return this.implementation;
        }
    }
}
