/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.junit;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.Mock;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class ArgMismatchFinderTest extends TestBase {

    ArgMismatchFinder finder = new ArgMismatchFinder();

    @Mock
    IMethods mock1;

    @Mock
    IMethods mock2;

    @Test
    public void no_interactions() throws Exception {
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(0, mismatches.size());
    }

    @Test
    public void no_mismatch_when_mock_different() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        // arg mismatch on different mock
        mock2.simpleMethod(2);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(0, mismatches.size());
    }

    @Test
    public void no_mismatch_when_method_different() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        mock1.otherMethod();
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(0, mismatches.size());
    }

    @Test
    public void no_mismatch_when_stubbing_used() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        // stub used
        mock1.simpleMethod(1);
        // no stubbing, but we don't want it to be reported, either
        mock1.simpleMethod(2);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(0, mismatches.size());
    }

    @Test
    public void stubbing_mismatch() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        mock1.simpleMethod(2);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(1, mismatches.size());
    }

    @Test
    public void single_mismatch_with_multiple_invocations() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        mock1.simpleMethod(2);
        mock1.simpleMethod(3);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(1, mismatches.size());
        assertEquals("{mock1.simpleMethod(1);=[mock1.simpleMethod(2);, mock1.simpleMethod(3);]}", mismatches.toString());
    }

    @Test
    public void single_invocation_with_multiple_stubs() throws Exception {
        // given
        when(mock1.simpleMethod(1)).thenReturn("1");
        when(mock1.simpleMethod(2)).thenReturn("2");
        mock1.simpleMethod(3);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals(2, mismatches.size());
        assertEquals("{mock1.simpleMethod(1);=[mock1.simpleMethod(3);], mock1.simpleMethod(2);=[mock1.simpleMethod(3);]}", mismatches.toString());
    }

    @Test
    public void mismatch_reports_only_unstubbed_invocations() throws Exception {
        // given
        // unused
        when(mock1.simpleMethod(1)).thenReturn("1");
        // used
        when(mock1.simpleMethod(2)).thenReturn("2");
        // stubbed
        mock1.simpleMethod(2);
        // unstubbed
        mock1.simpleMethod(3);
        // when
        StubbingArgMismatches mismatches = finder.getStubbingArgMismatches(asList(mock1, mock2));
        // then
        assertEquals("{mock1.simpleMethod(1);=[mock1.simpleMethod(3);]}", mismatches.toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_interactions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_interactions, this.description("no_interactions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_mismatch_when_mock_different() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_mismatch_when_mock_different, this.description("no_mismatch_when_mock_different"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_mismatch_when_method_different() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_mismatch_when_method_different, this.description("no_mismatch_when_method_different"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_mismatch_when_stubbing_used() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_mismatch_when_stubbing_used, this.description("no_mismatch_when_stubbing_used"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbing_mismatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbing_mismatch, this.description("stubbing_mismatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single_mismatch_with_multiple_invocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single_mismatch_with_multiple_invocations, this.description("single_mismatch_with_multiple_invocations"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single_invocation_with_multiple_stubs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single_invocation_with_multiple_stubs, this.description("single_invocation_with_multiple_stubs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mismatch_reports_only_unstubbed_invocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mismatch_reports_only_unstubbed_invocations, this.description("mismatch_reports_only_unstubbed_invocations"));
        }

        private ArgMismatchFinderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ArgMismatchFinderTest();
        }

        @java.lang.Override
        public ArgMismatchFinderTest implementation() {
            return this.implementation;
        }
    }
}
