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
package io.reactivex.rxjava3.internal.subscriptions;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SubscriptionArbiterTest extends RxJavaTest {

    @Test
    public void setSubscriptionMissed() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.getAndIncrement();
        BooleanSubscription bs1 = new BooleanSubscription();
        sa.setSubscription(bs1);
        BooleanSubscription bs2 = new BooleanSubscription();
        sa.setSubscription(bs2);
        assertTrue(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void invalidDeferredRequest() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            sa.request(-99);
            TestHelper.assertError(errors, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unbounded() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.request(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, sa.requested);
        assertTrue(sa.isUnbounded());
        sa.unbounded = false;
        sa.request(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, sa.requested);
        sa.produced(1);
        assertEquals(Long.MAX_VALUE, sa.requested);
        sa.unbounded = false;
        sa.produced(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, sa.requested);
    }

    @Test
    public void cancelled() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.cancelled = true;
        BooleanSubscription bs1 = new BooleanSubscription();
        sa.missedSubscription.set(bs1);
        sa.getAndIncrement();
        sa.drainLoop();
        assertTrue(bs1.isCancelled());
    }

    @Test
    public void drainUnbounded() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.getAndIncrement();
        sa.requested = Long.MAX_VALUE;
        sa.drainLoop();
    }

    @Test
    public void drainMissedRequested() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.getAndIncrement();
        sa.requested = 0;
        sa.missedRequested.set(1);
        sa.drainLoop();
        assertEquals(1, sa.requested);
    }

    @Test
    public void drainMissedRequestedProduced() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.getAndIncrement();
        sa.requested = 0;
        sa.missedRequested.set(Long.MAX_VALUE);
        sa.missedProduced.set(1);
        sa.drainLoop();
        assertEquals(Long.MAX_VALUE, sa.requested);
    }

    @Test
    public void drainMissedRequestedMoreProduced() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionArbiter sa = new SubscriptionArbiter(true);
            sa.getAndIncrement();
            sa.requested = 0;
            sa.missedRequested.set(1);
            sa.missedProduced.set(2);
            sa.drainLoop();
            assertEquals(0, sa.requested);
            TestHelper.assertError(errors, 0, IllegalStateException.class, "More produced than requested: -1");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void missedSubscriptionNoPrior() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        sa.getAndIncrement();
        BooleanSubscription bs1 = new BooleanSubscription();
        sa.missedSubscription.set(bs1);
        sa.drainLoop();
        assertSame(bs1, sa.actual);
    }

    @Test
    public void noCancelFastPath() {
        SubscriptionArbiter sa = new SubscriptionArbiter(false);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.setSubscription(bs2);
        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void cancelFastPath() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.setSubscription(bs2);
        assertTrue(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void noCancelSlowPathReplace() {
        SubscriptionArbiter sa = new SubscriptionArbiter(false);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        BooleanSubscription bs3 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.getAndIncrement();
        sa.setSubscription(bs2);
        sa.setSubscription(bs3);
        sa.drainLoop();
        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
        assertFalse(bs3.isCancelled());
    }

    @Test
    public void cancelSlowPathReplace() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        BooleanSubscription bs3 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.getAndIncrement();
        sa.setSubscription(bs2);
        sa.setSubscription(bs3);
        sa.drainLoop();
        assertTrue(bs1.isCancelled());
        assertTrue(bs2.isCancelled());
        assertFalse(bs3.isCancelled());
    }

    @Test
    public void noCancelSlowPath() {
        SubscriptionArbiter sa = new SubscriptionArbiter(false);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.getAndIncrement();
        sa.setSubscription(bs2);
        sa.drainLoop();
        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void cancelSlowPath() {
        SubscriptionArbiter sa = new SubscriptionArbiter(true);
        BooleanSubscription bs1 = new BooleanSubscription();
        BooleanSubscription bs2 = new BooleanSubscription();
        sa.setSubscription(bs1);
        sa.getAndIncrement();
        sa.setSubscription(bs2);
        sa.drainLoop();
        assertTrue(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void moreProducedViolationFastPath() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionArbiter sa = new SubscriptionArbiter(true);
            sa.produced(2);
            assertEquals(0, sa.requested);
            TestHelper.assertError(errors, 0, IllegalStateException.class, "More produced than requested: -2");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setSubscriptionMissed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setSubscriptionMissed, this.description("setSubscriptionMissed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidDeferredRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::invalidDeferredRequest, this.description("invalidDeferredRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unbounded() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unbounded, this.description("unbounded"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelled, this.description("cancelled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainUnbounded() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainUnbounded, this.description("drainUnbounded"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMissedRequested() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainMissedRequested, this.description("drainMissedRequested"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMissedRequestedProduced() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainMissedRequestedProduced, this.description("drainMissedRequestedProduced"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMissedRequestedMoreProduced() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::drainMissedRequestedMoreProduced, this.description("drainMissedRequestedMoreProduced"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missedSubscriptionNoPrior() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::missedSubscriptionNoPrior, this.description("missedSubscriptionNoPrior"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelFastPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noCancelFastPath, this.description("noCancelFastPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelFastPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelFastPath, this.description("cancelFastPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelSlowPathReplace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noCancelSlowPathReplace, this.description("noCancelSlowPathReplace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelSlowPathReplace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelSlowPathReplace, this.description("cancelSlowPathReplace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelSlowPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noCancelSlowPath, this.description("noCancelSlowPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelSlowPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelSlowPath, this.description("cancelSlowPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_moreProducedViolationFastPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::moreProducedViolationFastPath, this.description("moreProducedViolationFastPath"));
        }

        private SubscriptionArbiterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SubscriptionArbiterTest();
        }

        @java.lang.Override
        public SubscriptionArbiterTest implementation() {
            return this.implementation;
        }
    }
}
