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
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ArrayCompositeDisposableTest extends RxJavaTest {

    @Test
    public void normal() {
        ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        assertTrue(acd.setResource(0, d1));
        assertTrue(acd.setResource(1, d2));
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        acd.replaceResource(0, d3);
        acd.replaceResource(1, d4);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        acd.setResource(0, d1);
        acd.setResource(1, d2);
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertFalse(acd.isDisposed());
        acd.dispose();
        acd.dispose();
        assertTrue(acd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d5 = Disposable.empty();
        Disposable d6 = Disposable.empty();
        assertFalse(acd.setResource(0, d5));
        acd.replaceResource(1, d6);
        assertTrue(d5.isDisposed());
        assertTrue(d6.isDisposed());
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.dispose();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void replaceRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.replaceResource(0, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.setResource(0, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaceRace, this.description("replaceRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setRace, this.description("setRace"));
        }

        private ArrayCompositeDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ArrayCompositeDisposableTest();
        }

        @java.lang.Override
        public ArrayCompositeDisposableTest implementation() {
            return this.implementation;
        }
    }
}
