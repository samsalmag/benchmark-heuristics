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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.UnicastSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDoFinallyTest extends RxJavaTest implements Action {

    int calls;

    @Override
    public void run() throws Exception {
        calls++;
    }

    @Test
    public void normalJust() {
        Observable.just(1).doFinally(this).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmpty() {
        Observable.empty().doFinally(this).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalError() {
        Observable.error(new TestException()).doFinally(this).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTake() {
        Observable.range(1, 10).doFinally(this).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.doFinally(ObservableDoFinallyTest.this);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.doFinally(ObservableDoFinallyTest.this).filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void syncFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundary() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Observable.range(1, 5).doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundary() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void normalJustConditional() {
        Observable.just(1).doFinally(this).filter(Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmptyConditional() {
        Observable.empty().doFinally(this).filter(Functions.alwaysTrue()).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalErrorConditional() {
        Observable.error(new TestException()).doFinally(this).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTakeConditional() {
        Observable.range(1, 10).doFinally(this).filter(Functions.alwaysTrue()).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).hide().doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).hide().doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundaryConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundaryConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void actionThrowsConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void clearIsEmpty() {
        Observable.range(1, 5).doFinally(this).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                @SuppressWarnings("unchecked")
                QueueDisposable<Integer> qd = (QueueDisposable<Integer>) d;
                qd.requestFusion(QueueFuseable.ANY);
                assertFalse(qd.isEmpty());
                try {
                    assertEquals(1, qd.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertEquals(1, calls);
    }

    @Test
    public void clearIsEmptyConditional() {
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                @SuppressWarnings("unchecked")
                QueueDisposable<Integer> qd = (QueueDisposable<Integer>) d;
                qd.requestFusion(QueueFuseable.ANY);
                assertFalse(qd.isEmpty());
                assertFalse(qd.isDisposed());
                try {
                    assertEquals(1, qd.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
                assertTrue(qd.isDisposed());
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertEquals(1, calls);
    }

    @Test
    public void eventOrdering() {
        final List<String> list = new ArrayList<>();
        Observable.error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                list.add("dispose");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onError", "finally"), list);
    }

    @Test
    public void eventOrdering2() {
        final List<String> list = new ArrayList<>();
        Observable.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                list.add("dispose");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onNext", "onComplete", "finally"), list);
    }

    @Test
    public void fusionRejected() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        TestHelper.rejectObservableFusion().doFinally(() -> {
        }).subscribeWith(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJust, this.description("normalJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmpty, this.description("normalEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalError, this.description("normalError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalTake, this.description("normalTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFused, this.description("syncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundary() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedBoundary, this.description("syncFusedBoundary"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFused, this.description("asyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundary() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedBoundary, this.description("asyncFusedBoundary"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJustConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalJustConditional, this.description("normalJustConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmptyConditional, this.description("normalEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalErrorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalErrorConditional, this.description("normalErrorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTakeConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalTakeConditional, this.description("normalTakeConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedConditional, this.description("syncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonFused, this.description("nonFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonFusedConditional, this.description("nonFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundaryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedBoundaryConditional, this.description("syncFusedBoundaryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedConditional, this.description("asyncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundaryConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedBoundaryConditional, this.description("asyncFusedBoundaryConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrows, this.description("actionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrowsConditional, this.description("actionThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearIsEmpty, this.description("clearIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearIsEmptyConditional, this.description("clearIsEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eventOrdering, this.description("eventOrdering"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eventOrdering2, this.description("eventOrdering2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRejected, this.description("fusionRejected"));
        }

        private ObservableDoFinallyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableDoFinallyTest();
        }

        @java.lang.Override
        public ObservableDoFinallyTest implementation() {
            return this.implementation;
        }
    }
}
