/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class FindingRedundantInvocationsInOrderTest extends TestBase {

    @Mock
    private IMethods mock;

    @Mock
    private IMethods mock2;

    @Test
    public void shouldWorkFineIfNoInvocations() throws Exception {
        // when
        InOrder inOrder = inOrder(mock);
        // then
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSayNoInteractionsWanted() throws Exception {
        // when
        mock.simpleMethod();
        // then
        InOrder inOrder = inOrder(mock);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch (VerificationInOrderFailure e) {
            assertThat(e).hasMessageContaining("No interactions wanted");
        }
    }

    @Test
    public void shouldVerifyNoMoreInteractionsInOrder() throws Exception {
        // when
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        // then
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        // when
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        // then
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldFailToVerifyNoMoreInteractionsInOrder() throws Exception {
        // when
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        // then
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

    @Test
    public void shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        // when
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        // then
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

    @SuppressWarnings({ "MockitoUsage", "CheckReturnValue" })
    @Test
    public void shouldValidateState() throws Exception {
        // when
        InOrder inOrder = inOrder(mock);
        // mess up state
        verify(mock);
        // then
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch (UnfinishedVerificationException e) {
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldWorkFineIfNoInvocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldWorkFineIfNoInvocations, this.description("shouldWorkFineIfNoInvocations"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSayNoInteractionsWanted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldSayNoInteractionsWanted, this.description("shouldSayNoInteractionsWanted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyNoMoreInteractionsInOrder() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyNoMoreInteractionsInOrder, this.description("shouldVerifyNoMoreInteractionsInOrder"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks, this.description("shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailToVerifyNoMoreInteractionsInOrder() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailToVerifyNoMoreInteractionsInOrder, this.description("shouldFailToVerifyNoMoreInteractionsInOrder"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks, this.description("shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldValidateState() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldValidateState, this.description("shouldValidateState"));
        }

        private FindingRedundantInvocationsInOrderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FindingRedundantInvocationsInOrderTest();
        }

        @java.lang.Override
        public FindingRedundantInvocationsInOrderTest implementation() {
            return this.implementation;
        }
    }
}
