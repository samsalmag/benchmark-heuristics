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
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class DeferredScalarObserverTest extends RxJavaTest {

    static final class TakeFirst extends DeferredScalarObserver<Integer, Integer> {

        private static final long serialVersionUID = -2793723002312330530L;

        TakeFirst(Observer<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void onNext(Integer value) {
            upstream.dispose();
            complete(value);
            complete(value);
        }
    }

    @Test
    public void normal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = new TestObserver<>();
            TakeFirst source = new TakeFirst(to);
            source.onSubscribe(Disposable.empty());
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            assertTrue(d.isDisposed());
            source.onNext(1);
            to.assertResult(1);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        source.onSubscribe(Disposable.empty());
        source.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void complete() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        source.onSubscribe(Disposable.empty());
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void dispose() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        assertFalse(d.isDisposed());
        to.dispose();
        assertTrue(d.isDisposed());
        assertTrue(source.isDisposed());
    }

    @Test
    public void fused() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeFirst source = new TakeFirst(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            to.assertFuseable();
            to.assertFusionMode(QueueFuseable.ASYNC);
            source.onNext(1);
            source.onNext(1);
            source.onError(new TestException());
            source.onComplete();
            assertTrue(d.isDisposed());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedReject() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
            TakeFirst source = new TakeFirst(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            to.assertFuseable();
            to.assertFusionMode(QueueFuseable.NONE);
            source.onNext(1);
            source.onNext(1);
            source.onError(new TestException());
            source.onComplete();
            assertTrue(d.isDisposed());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    static final class TakeLast extends DeferredScalarObserver<Integer, Integer> {

        private static final long serialVersionUID = -2793723002312330530L;

        TakeLast(Observer<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void onNext(Integer value) {
            this.value = value;
        }
    }

    @Test
    public void nonfusedTerminateMore() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onComplete();
            source.onComplete();
            source.onError(new TestException());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void nonfusedError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onError(new TestException());
            source.onError(new TestException("second"));
            source.onComplete();
            to.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedTerminateMore() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onComplete();
            source.onComplete();
            source.onError(new TestException());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onError(new TestException());
            source.onError(new TestException("second"));
            source.onComplete();
            to.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposed() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        to.dispose();
        source.onNext(1);
        source.onComplete();
        to.assertNoValues().assertNoErrors().assertNotComplete();
    }

    @Test
    public void disposedAfterOnNext() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            Disposable upstream;

            @Override
            public void onSubscribe(Disposable d) {
                this.upstream = d;
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Integer value) {
                to.onNext(value);
                upstream.dispose();
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void fusedEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void nonfusedEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void customFusion() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
                if (!d.isEmpty()) {
                    Integer v = null;
                    try {
                        to.onNext(d.poll());
                        v = d.poll();
                    } catch (Throwable ex) {
                        to.onError(ex);
                    }
                    assertNull(v);
                    assertTrue(d.isEmpty());
                }
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertResult(1);
    }

    @Test
    public void customFusionClear() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
                d.clear();
                assertTrue(d.isEmpty());
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertNoValues().assertNoErrors().assertComplete();
    }

    @Test
    public void offerThrow() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        TestHelper.assertNoOffer(source);
    }

    @Test
    public void customFusionDontConsume() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
                // not consuming
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        to.assertNoValues().assertNoErrors().assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::complete, this.description("complete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedReject, this.description("fusedReject"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedTerminateMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonfusedTerminateMore, this.description("nonfusedTerminateMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonfusedError, this.description("nonfusedError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedTerminateMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedTerminateMore, this.description("fusedTerminateMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedError, this.description("fusedError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedAfterOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedAfterOnNext, this.description("disposedAfterOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedEmpty, this.description("fusedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonfusedEmpty, this.description("nonfusedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusion() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customFusion, this.description("customFusion"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusionClear() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customFusionClear, this.description("customFusionClear"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offerThrow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::offerThrow, this.description("offerThrow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusionDontConsume() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customFusionDontConsume, this.description("customFusionDontConsume"));
        }

        private DeferredScalarObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DeferredScalarObserverTest();
        }

        @java.lang.Override
        public DeferredScalarObserverTest implementation() {
            return this.implementation;
        }
    }
}
