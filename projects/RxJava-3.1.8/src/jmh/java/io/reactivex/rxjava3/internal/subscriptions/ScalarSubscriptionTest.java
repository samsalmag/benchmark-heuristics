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

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ScalarSubscriptionTest extends RxJavaTest {

    @Test
    public void badRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        ScalarSubscription<Integer> sc = new ScalarSubscription<>(ts, 1);
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            sc.request(-99);
            TestHelper.assertError(errors, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void noOffer() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        ScalarSubscription<Integer> sc = new ScalarSubscription<>(ts, 1);
        TestHelper.assertNoOffer(sc);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noOffer, this.description("noOffer"));
        }

        private ScalarSubscriptionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScalarSubscriptionTest();
        }

        @java.lang.Override
        public ScalarSubscriptionTest implementation() {
            return this.implementation;
        }
    }
}
