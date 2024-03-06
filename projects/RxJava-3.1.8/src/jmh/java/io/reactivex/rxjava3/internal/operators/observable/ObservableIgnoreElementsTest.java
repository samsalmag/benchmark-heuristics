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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableIgnoreElementsTest extends RxJavaTest {

    @Test
    public void withEmptyObservable() {
        assertTrue(Observable.empty().ignoreElements().toObservable().isEmpty().blockingGet());
    }

    @Test
    public void withNonEmptyObservable() {
        assertTrue(Observable.just(1, 2, 3).ignoreElements().toObservable().isEmpty().blockingGet());
    }

    @Test
    public void upstreamIsProcessedButIgnoredObservable() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        long count = Observable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().toObservable().count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count);
    }

    @Test
    public void completedOkObservable() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Observable.range(1, 10).ignoreElements().toObservable().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceivedObservable() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Observable.error(ex).ignoreElements().toObservable().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstreamObservable() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Observable.range(1, 10).concatWith(Observable.<Integer>never()).doOnDispose(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().toObservable().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void withEmpty() {
        Observable.empty().ignoreElements().blockingAwait();
    }

    @Test
    public void withNonEmpty() {
        Observable.just(1, 2, 3).ignoreElements().blockingAwait();
    }

    @Test
    public void upstreamIsProcessedButIgnored() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        Observable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().blockingAwait();
        assertEquals(num, upstreamCount.get());
    }

    @Test
    public void completedOk() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Observable.range(1, 10).ignoreElements().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceived() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Observable.error(ex).ignoreElements().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Observable.range(1, 10).concatWith(Observable.<Integer>never()).doOnDispose(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void dispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.ignoreElements().<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse(ps.hasObservers());
        TestHelper.checkDisposed(ps.ignoreElements().<Integer>toObservable());
    }

    @Test
    public void checkDispose() {
        TestHelper.checkDisposed(Observable.just(1).ignoreElements());
        TestHelper.checkDisposed(Observable.just(1).ignoreElements().toObservable());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmptyObservable, this.description("withEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withNonEmptyObservable, this.description("withNonEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnoredObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamIsProcessedButIgnoredObservable, this.description("upstreamIsProcessedButIgnoredObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOkObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completedOkObservable, this.description("completedOkObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceivedObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorReceivedObservable, this.description("errorReceivedObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstreamObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribesFromUpstreamObservable, this.description("unsubscribesFromUpstreamObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty, this.description("withEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withNonEmpty, this.description("withNonEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnored() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamIsProcessedButIgnored, this.description("upstreamIsProcessedButIgnored"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOk() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completedOk, this.description("completedOk"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceived() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorReceived, this.description("errorReceived"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribesFromUpstream, this.description("unsubscribesFromUpstream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDispose, this.description("checkDispose"));
        }

        private ObservableIgnoreElementsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableIgnoreElementsTest();
        }

        @java.lang.Override
        public ObservableIgnoreElementsTest implementation() {
            return this.implementation;
        }
    }
}
