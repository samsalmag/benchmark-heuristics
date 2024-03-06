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
package io.reactivex.rxjava3.internal.disposables;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class DisposableHelperTest extends RxJavaTest {

    @Test
    public void enumMethods() {
        assertEquals(1, DisposableHelper.values().length);
        assertNotNull(DisposableHelper.valueOf("DISPOSED"));
    }

    @Test
    public void innerDisposed() {
        assertTrue(DisposableHelper.DISPOSED.isDisposed());
        DisposableHelper.DISPOSED.dispose();
        assertTrue(DisposableHelper.DISPOSED.isDisposed());
    }

    @Test
    public void validationNull() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            assertFalse(DisposableHelper.validate(null, null));
            TestHelper.assertError(list, 0, NullPointerException.class, "next is null");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.dispose(d);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setReplace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.replace(d, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.set(d, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setReplaceNull() {
        final AtomicReference<Disposable> d = new AtomicReference<>();
        DisposableHelper.dispose(d);
        assertFalse(DisposableHelper.set(d, null));
        assertFalse(DisposableHelper.replace(d, null));
    }

    @Test
    public void dispose() {
        Disposable u = Disposable.empty();
        final AtomicReference<Disposable> d = new AtomicReference<>(u);
        DisposableHelper.dispose(d);
        assertTrue(u.isDisposed());
    }

    @Test
    public void trySet() {
        AtomicReference<Disposable> ref = new AtomicReference<>();
        Disposable d1 = Disposable.empty();
        assertTrue(DisposableHelper.trySet(ref, d1));
        Disposable d2 = Disposable.empty();
        assertFalse(DisposableHelper.trySet(ref, d2));
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        DisposableHelper.dispose(ref);
        Disposable d3 = Disposable.empty();
        assertFalse(DisposableHelper.trySet(ref, d3));
        assertTrue(d3.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enumMethods() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::enumMethods, this.description("enumMethods"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerDisposed, this.description("innerDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validationNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::validationNull, this.description("validationNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setReplace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setReplace, this.description("setReplace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setRace, this.description("setRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setReplaceNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setReplaceNull, this.description("setReplaceNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_trySet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::trySet, this.description("trySet"));
        }

        private DisposableHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DisposableHelperTest();
        }

        @java.lang.Override
        public DisposableHelperTest implementation() {
            return this.implementation;
        }
    }
}
