/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.basicapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.opentest4j.ArgumentsAreDifferent;
import org.mockitoutil.TestBase;

public class UsingVarargsTest extends TestBase {

    private interface IVarArgs {

        void withStringVarargs(int value, String... s);

        String withStringVarargsReturningString(int value, String... s);

        void withObjectVarargs(int value, Object... o);

        boolean withBooleanVarargs(int value, boolean... b);

        int foo(Object... objects);
    }

    @Mock
    IVarArgs mock;

    @Test
    public void shouldStubStringVarargs() {
        when(mock.withStringVarargsReturningString(1)).thenReturn("1");
        when(mock.withStringVarargsReturningString(2, "1", "2", "3")).thenReturn("2");
        RuntimeException expected = new RuntimeException();
        doThrow(expected).when(mock).withStringVarargs(3, "1", "2", "3", "4");
        assertEquals("1", mock.withStringVarargsReturningString(1));
        assertEquals(null, mock.withStringVarargsReturningString(2));
        assertEquals("2", mock.withStringVarargsReturningString(2, "1", "2", "3"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2", "3", "4"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2", "9999"));
        mock.withStringVarargs(3, "1", "2", "3", "9999");
        mock.withStringVarargs(9999, "1", "2", "3", "4");
        try {
            mock.withStringVarargs(3, "1", "2", "3", "4");
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

    @Test
    public void shouldStubBooleanVarargs() {
        when(mock.withBooleanVarargs(1)).thenReturn(true);
        when(mock.withBooleanVarargs(1, true, false)).thenReturn(true);
        assertEquals(true, mock.withBooleanVarargs(1));
        assertEquals(false, mock.withBooleanVarargs(9999));
        assertEquals(true, mock.withBooleanVarargs(1, true, false));
        assertEquals(false, mock.withBooleanVarargs(1, true, false, true));
        assertEquals(false, mock.withBooleanVarargs(2, true, false));
        assertEquals(false, mock.withBooleanVarargs(1, true));
        assertEquals(false, mock.withBooleanVarargs(1, false, false));
    }

    @Test
    public void shouldVerifyStringVarargs() {
        mock.withStringVarargs(1);
        mock.withStringVarargs(2, "1", "2", "3");
        mock.withStringVarargs(3, "1", "2", "3", "4");
        verify(mock).withStringVarargs(1);
        verify(mock).withStringVarargs(2, "1", "2", "3");
        try {
            verify(mock).withStringVarargs(2, "1", "2", "79", "4");
            fail();
        } catch (ArgumentsAreDifferent e) {
        }
    }

    @Test
    public void shouldVerifyObjectVarargs() {
        mock.withObjectVarargs(1);
        mock.withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
        mock.withObjectVarargs(3, new Integer(1));
        verify(mock).withObjectVarargs(1);
        verify(mock).withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
        }
    }

    @Test
    public void shouldVerifyBooleanVarargs() {
        mock.withBooleanVarargs(1);
        mock.withBooleanVarargs(2, true, false, true);
        mock.withBooleanVarargs(3, true, true, true);
        verify(mock).withBooleanVarargs(1);
        verify(mock).withBooleanVarargs(2, true, false, true);
        try {
            verify(mock).withBooleanVarargs(3, true, true, true, true);
            fail();
        } catch (ArgumentsAreDifferent e) {
        }
    }

    @Test
    public void shouldVerifyWithAny() {
        Foo foo = Mockito.mock(Foo.class);
        foo.varArgs("");
        Mockito.verify(foo).varArgs((String[]) Mockito.any());
        Mockito.verify(foo).varArgs((String) Mockito.any());
    }

    @Test
    public void shouldVerifyWithNullVarArgArray() {
        Foo foo = Mockito.mock(Foo.class);
        foo.varArgs((String[]) null);
        Mockito.verify(foo).varArgs((String[]) Mockito.any());
        Mockito.verify(foo).varArgs((String[]) null);
    }

    public class Foo {

        public void varArgs(String... args) {
        }
    }

    interface MixedVarargs {

        String doSomething(String one, String... varargs);

        String doSomething(String one, String two, String... varargs);
    }

    @SuppressWarnings("all")
    @Test
    public // See bug #31
    void shouldStubCorrectlyWhenMixedVarargsUsed() {
        MixedVarargs mixedVarargs = mock(MixedVarargs.class);
        when(mixedVarargs.doSomething("hello", (String[]) null)).thenReturn("hello");
        when(mixedVarargs.doSomething("goodbye", (String[]) null)).thenReturn("goodbye");
        String result = mixedVarargs.doSomething("hello", (String[]) null);
        assertEquals("hello", result);
        verify(mixedVarargs).doSomething("hello", (String[]) null);
    }

    @SuppressWarnings("all")
    @Test
    public void shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed() {
        MixedVarargs mixedVarargs = mock(MixedVarargs.class);
        when(mixedVarargs.doSomething("one", "two", (String[]) null)).thenReturn("hello");
        when(mixedVarargs.doSomething("1", "2", (String[]) null)).thenReturn("goodbye");
        String result = mixedVarargs.doSomething("one", "two", (String[]) null);
        assertEquals("hello", result);
    }

    @Test
    public // See bug #157
    void shouldMatchEasilyEmptyVararg() {
        // when
        when(mock.foo(any(Object[].class))).thenReturn(-1);
        // then
        assertEquals(-1, mock.foo());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldStubStringVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldStubStringVarargs, this.description("shouldStubStringVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldStubBooleanVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldStubBooleanVarargs, this.description("shouldStubBooleanVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyStringVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyStringVarargs, this.description("shouldVerifyStringVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyObjectVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyObjectVarargs, this.description("shouldVerifyObjectVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBooleanVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBooleanVarargs, this.description("shouldVerifyBooleanVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyWithAny() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyWithAny, this.description("shouldVerifyWithAny"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyWithNullVarArgArray() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyWithNullVarArgArray, this.description("shouldVerifyWithNullVarArgArray"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldStubCorrectlyWhenMixedVarargsUsed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldStubCorrectlyWhenMixedVarargsUsed, this.description("shouldStubCorrectlyWhenMixedVarargsUsed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed, this.description("shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchEasilyEmptyVararg() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchEasilyEmptyVararg, this.description("shouldMatchEasilyEmptyVararg"));
        }

        private UsingVarargsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new UsingVarargsTest();
        }

        @java.lang.Override
        public UsingVarargsTest implementation() {
            return this.implementation;
        }
    }
}
