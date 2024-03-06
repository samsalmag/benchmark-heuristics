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
package io.reactivex.rxjava3.internal.observers;

import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BlockingMultiObserverTest extends RxJavaTest {

    @Test
    public void dispose() {
        BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        bmo.dispose();
        Disposable d = Disposable.empty();
        bmo.onSubscribe(d);
    }

    @Test
    public void blockingGetDefault() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                bmo.onSuccess(1);
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, bmo.blockingGet(0).intValue());
    }

    @Test
    public void blockingAwait() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                bmo.onSuccess(1);
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertTrue(bmo.blockingAwait(1, TimeUnit.MINUTES));
    }

    @Test
    public void blockingGetDefaultInterrupt() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Thread.currentThread().interrupt();
        try {
            bmo.blockingGet(0);
            fail("Should have thrown");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof InterruptedException);
        } finally {
            Thread.interrupted();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingGetDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingGetDefault, this.description("blockingGetDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingAwait() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingAwait, this.description("blockingAwait"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingGetDefaultInterrupt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingGetDefaultInterrupt, this.description("blockingGetDefaultInterrupt"));
        }

        private BlockingMultiObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BlockingMultiObserverTest();
        }

        @java.lang.Override
        public BlockingMultiObserverTest implementation() {
            return this.implementation;
        }
    }
}
