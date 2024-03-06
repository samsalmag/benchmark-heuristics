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
package io.reactivex.rxjava3.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableInterval.IntervalSubscriber;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableIntervalTest extends RxJavaTest {

    @Test
    public void cancel() {
        Flowable.interval(1, TimeUnit.MILLISECONDS, Schedulers.trampoline()).take(10).test().assertResult(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.interval(1, TimeUnit.MILLISECONDS, Schedulers.trampoline()));
    }

    @Test
    public void cancelledOnRun() {
        TestSubscriber<Long> ts = new TestSubscriber<>();
        IntervalSubscriber is = new IntervalSubscriber(ts);
        ts.onSubscribe(is);
        is.cancel();
        is.run();
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledOnRun() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelledOnRun, this.description("cancelledOnRun"));
        }

        private FlowableIntervalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableIntervalTest();
        }

        @java.lang.Override
        public FlowableIntervalTest implementation() {
            return this.implementation;
        }
    }
}
