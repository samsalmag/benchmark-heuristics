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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.functions.Consumer;

public class FlowableDoAfterNextTest extends RxJavaTest {

    @Test
    public void ifFunctionThrowsThatNoMoreEventsAreProcessed() {
        final AtomicInteger count = new AtomicInteger();
        final RuntimeException e = new RuntimeException();
        Burst.items(1, 2).create().doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) throws Exception {
                count.incrementAndGet();
                throw e;
            }
        }).test().assertError(e).assertValue(1);
        assertEquals(1, count.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ifFunctionThrowsThatNoMoreEventsAreProcessed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ifFunctionThrowsThatNoMoreEventsAreProcessed, this.description("ifFunctionThrowsThatNoMoreEventsAreProcessed"));
        }

        private FlowableDoAfterNextTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableDoAfterNextTest();
        }

        @java.lang.Override
        public FlowableDoAfterNextTest implementation() {
            return this.implementation;
        }
    }
}
