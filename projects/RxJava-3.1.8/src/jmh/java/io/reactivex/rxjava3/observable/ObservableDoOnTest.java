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
package io.reactivex.rxjava3.observable;

import static org.junit.Assert.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;

public class ObservableDoOnTest extends RxJavaTest {

    @Test
    public void doOnEach() {
        final AtomicReference<String> r = new AtomicReference<>();
        String output = Observable.just("one").doOnNext(new Consumer<String>() {

            @Override
            public void accept(String v) {
                r.set(v);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertEquals("one", r.get());
    }

    @Test
    public void doOnError() {
        final AtomicReference<Throwable> r = new AtomicReference<>();
        Throwable t = null;
        try {
            Observable.<String>error(new RuntimeException("an error")).doOnError(new Consumer<Throwable>() {

                @Override
                public void accept(Throwable v) {
                    r.set(v);
                }
            }).blockingSingle();
            fail("expected exception, not a return value");
        } catch (Throwable e) {
            t = e;
        }
        assertNotNull(t);
        assertEquals(t, r.get());
    }

    @Test
    public void doOnCompleted() {
        final AtomicBoolean r = new AtomicBoolean();
        String output = Observable.just("one").doOnComplete(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertTrue(r.get());
    }

    @Test
    public void doOnTerminateComplete() {
        final AtomicBoolean r = new AtomicBoolean();
        String output = Observable.just("one").doOnTerminate(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertTrue(r.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicBoolean r = new AtomicBoolean();
        Observable.<String>error(new TestException()).doOnTerminate(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).test().assertFailure(TestException.class);
        assertTrue(r.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEach() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnEach, this.description("doOnEach"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnError, this.description("doOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnCompleted, this.description("doOnCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateComplete, this.description("doOnTerminateComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateError, this.description("doOnTerminateError"));
        }

        private ObservableDoOnTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableDoOnTest();
        }

        @java.lang.Override
        public ObservableDoOnTest implementation() {
            return this.implementation;
        }
    }
}
