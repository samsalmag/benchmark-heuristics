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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SubscriptionHelperTest extends RxJavaTest {

    @Test
    public void checkEnum() {
        TestHelper.checkEnum(SubscriptionHelper.class);
    }

    @Test
    public void validateNullThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionHelper.validate(null, null);
            TestHelper.assertError(errors, 0, NullPointerException.class, "next is null");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelNoOp() {
        SubscriptionHelper.CANCELLED.cancel();
    }

    @Test
    public void set() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        BooleanSubscription bs1 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.set(atomicSubscription, bs1));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.set(atomicSubscription, bs2));
        assertTrue(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void replace() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        BooleanSubscription bs1 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.replace(atomicSubscription, bs1));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.replace(atomicSubscription, bs2));
        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.cancel(atomicSubscription);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final BooleanSubscription bs1 = new BooleanSubscription();
            final BooleanSubscription bs2 = new BooleanSubscription();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.set(atomicSubscription, bs1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.set(atomicSubscription, bs2);
                }
            };
            TestHelper.race(r1, r2);
            assertTrue(bs1.isCancelled() ^ bs2.isCancelled());
        }
    }

    @Test
    public void replaceRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final BooleanSubscription bs1 = new BooleanSubscription();
            final BooleanSubscription bs2 = new BooleanSubscription();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.replace(atomicSubscription, bs1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.replace(atomicSubscription, bs2);
                }
            };
            TestHelper.race(r1, r2);
            assertFalse(bs1.isCancelled());
            assertFalse(bs2.isCancelled());
        }
    }

    @Test
    public void cancelAndChange() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        SubscriptionHelper.cancel(atomicSubscription);
        BooleanSubscription bs1 = new BooleanSubscription();
        assertFalse(SubscriptionHelper.set(atomicSubscription, bs1));
        assertTrue(bs1.isCancelled());
        assertFalse(SubscriptionHelper.set(atomicSubscription, null));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertFalse(SubscriptionHelper.replace(atomicSubscription, bs2));
        assertTrue(bs2.isCancelled());
        assertFalse(SubscriptionHelper.replace(atomicSubscription, null));
    }

    @Test
    public void invalidDeferredRequest() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        AtomicLong r = new AtomicLong();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionHelper.deferredRequest(atomicSubscription, r, -99);
            TestHelper.assertError(errors, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void deferredRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final AtomicLong r = new AtomicLong();
            final AtomicLong q = new AtomicLong();
            final Subscription a = new Subscription() {

                @Override
                public void request(long n) {
                    q.addAndGet(n);
                }

                @Override
                public void cancel() {
                }
            };
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.deferredSetOnce(atomicSubscription, r, a);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.deferredRequest(atomicSubscription, r, 1);
                }
            };
            TestHelper.race(r1, r2);
            assertSame(a, atomicSubscription.get());
            assertEquals(1, q.get());
            assertEquals(0, r.get());
        }
    }

    @Test
    public void setOnceAndRequest() {
        AtomicReference<Subscription> ref = new AtomicReference<>();
        Subscription sub = mock(Subscription.class);
        assertTrue(SubscriptionHelper.setOnce(ref, sub, 1));
        verify(sub).request(1);
        verify(sub, never()).cancel();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            sub = mock(Subscription.class);
            assertFalse(SubscriptionHelper.setOnce(ref, sub, 1));
            verify(sub, never()).request(anyLong());
            verify(sub).cancel();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkEnum() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkEnum, this.description("checkEnum"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateNullThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::validateNullThrows, this.description("validateNullThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelNoOp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelNoOp, this.description("cancelNoOp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_set() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::set, this.description("set"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replace, this.description("replace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelRace, this.description("cancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setRace, this.description("setRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaceRace, this.description("replaceRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAndChange() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAndChange, this.description("cancelAndChange"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidDeferredRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::invalidDeferredRequest, this.description("invalidDeferredRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deferredRace, this.description("deferredRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setOnceAndRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setOnceAndRequest, this.description("setOnceAndRequest"));
        }

        private SubscriptionHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SubscriptionHelperTest();
        }

        @java.lang.Override
        public SubscriptionHelperTest implementation() {
            return this.implementation;
        }
    }
}
