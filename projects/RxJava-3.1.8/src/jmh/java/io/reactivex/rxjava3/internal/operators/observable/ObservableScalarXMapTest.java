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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableScalarXMapTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObservableScalarXMap.class);
    }

    static final class CallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            EmptyDisposable.error(new TestException(), observer);
        }

        @Override
        public Integer get() throws Exception {
            throw new TestException();
        }
    }

    static final class EmptyCallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            EmptyDisposable.complete(observer);
        }

        @Override
        public Integer get() throws Exception {
            return null;
        }
    }

    static final class OneCallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            ScalarDisposable<Integer> sd = new ScalarDisposable<>(observer, 1);
            observer.onSubscribe(sd);
            sd.run();
        }

        @Override
        public Integer get() throws Exception {
            return 1;
        }
    }

    @Test
    public void tryScalarXMap() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new CallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void emptyXMap() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new EmptyCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertResult();
    }

    @Test
    public void mapperCrashes() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                throw new TestException();
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void mapperToJust() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertResult(1);
    }

    @Test
    public void mapperToEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.empty();
            }
        }));
        to.assertResult();
    }

    @Test
    public void mapperToCrashingCallable() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return new CallablePublisher();
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void scalarMapToEmpty() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void scalarMapToCrashingCallable() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return new CallablePublisher();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void scalarDisposableStateCheck() {
        TestObserver<Integer> to = new TestObserver<>();
        ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
        to.onSubscribe(sd);
        assertFalse(sd.isDisposed());
        assertTrue(sd.isEmpty());
        sd.run();
        assertTrue(sd.isDisposed());
        assertTrue(sd.isEmpty());
        to.assertResult(1);
        try {
            sd.offer(1);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
        try {
            sd.offer(1, 2);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void scalarDisposableRunDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            final ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
            to.onSubscribe(sd);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    sd.run();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sd.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void scalarDisposbleWrongFusion() {
        TestObserver<Integer> to = new TestObserver<>();
        final ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
        to.onSubscribe(sd);
        assertEquals(QueueFuseable.NONE, sd.requestFusion(QueueFuseable.ASYNC));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryScalarXMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryScalarXMap, this.description("tryScalarXMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyXMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyXMap, this.description("emptyXMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperCrashes, this.description("mapperCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperToJust, this.description("mapperToJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperToEmpty, this.description("mapperToEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToCrashingCallable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperToCrashingCallable, this.description("mapperToCrashingCallable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMapToEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarMapToEmpty, this.description("scalarMapToEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMapToCrashingCallable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarMapToCrashingCallable, this.description("scalarMapToCrashingCallable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposableStateCheck() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarDisposableStateCheck, this.description("scalarDisposableStateCheck"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposableRunDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarDisposableRunDisposeRace, this.description("scalarDisposableRunDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposbleWrongFusion() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarDisposbleWrongFusion, this.description("scalarDisposbleWrongFusion"));
        }

        private ObservableScalarXMapTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableScalarXMapTest();
        }

        @java.lang.Override
        public ObservableScalarXMapTest implementation() {
            return this.implementation;
        }
    }
}
