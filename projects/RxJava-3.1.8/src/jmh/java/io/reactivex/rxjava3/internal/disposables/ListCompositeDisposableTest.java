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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ListCompositeDisposableTest extends RxJavaTest {

    @Test
    public void constructorAndAddVarargs() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        ListCompositeDisposable lcd = new ListCompositeDisposable(d1, d2);
        lcd.clear();
        assertFalse(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        lcd.addAll(d1, d2);
        lcd.dispose();
        assertTrue(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void constructorIterable() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        ListCompositeDisposable lcd = new ListCompositeDisposable(Arrays.asList(d1, d2));
        lcd.clear();
        assertFalse(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        lcd.add(d1);
        lcd.addAll(d2);
        lcd.dispose();
        assertTrue(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void empty() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        assertFalse(lcd.isDisposed());
        lcd.clear();
        assertFalse(lcd.isDisposed());
        lcd.dispose();
        lcd.dispose();
        lcd.clear();
        assertTrue(lcd.isDisposed());
    }

    @Test
    public void afterDispose() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        lcd.dispose();
        Disposable d = Disposable.empty();
        assertFalse(lcd.add(d));
        assertTrue(d.isDisposed());
        d = Disposable.empty();
        assertFalse(lcd.addAll(d));
        assertTrue(d.isDisposed());
    }

    @Test
    public void disposeThrows() {
        Disposable d = new Disposable() {

            @Override
            public void dispose() {
                throw new TestException();
            }

            @Override
            public boolean isDisposed() {
                return false;
            }
        };
        ListCompositeDisposable lcd = new ListCompositeDisposable(d, d);
        try {
            lcd.dispose();
            fail("Should have thrown!");
        } catch (CompositeException ex) {
            List<Throwable> list = ex.getExceptions();
            TestHelper.assertError(list, 0, TestException.class);
            TestHelper.assertError(list, 1, TestException.class);
        }
        lcd = new ListCompositeDisposable(d);
        try {
            lcd.dispose();
            fail("Should have thrown!");
        } catch (TestException ex) {
            // expected
        }
    }

    @Test
    public void remove() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        Disposable d = Disposable.empty();
        lcd.add(d);
        assertTrue(lcd.delete(d));
        assertFalse(d.isDisposed());
        lcd.add(d);
        assertTrue(lcd.remove(d));
        assertTrue(d.isDisposed());
        assertFalse(lcd.remove(d));
        assertFalse(lcd.delete(d));
        lcd = new ListCompositeDisposable();
        assertFalse(lcd.remove(d));
        assertFalse(lcd.delete(d));
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addAllRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void removeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void deleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void clearRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void addAllDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void removeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void deleteDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void clearDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorAndAddVarargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructorAndAddVarargs, this.description("constructorAndAddVarargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructorIterable, this.description("constructorIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_afterDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::afterDispose, this.description("afterDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeThrows, this.description("disposeThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::remove, this.description("remove"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeRace, this.description("disposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addRace, this.description("addRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAllRace, this.description("addAllRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::removeRace, this.description("removeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deleteRace, this.description("deleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearRace, this.description("clearRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addDisposeRace, this.description("addDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addAllDisposeRace, this.description("addAllDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::removeDisposeRace, this.description("removeDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::deleteDisposeRace, this.description("deleteDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearDisposeRace, this.description("clearDisposeRace"));
        }

        private ListCompositeDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ListCompositeDisposableTest();
        }

        @java.lang.Override
        public ListCompositeDisposableTest implementation() {
            return this.implementation;
        }
    }
}
