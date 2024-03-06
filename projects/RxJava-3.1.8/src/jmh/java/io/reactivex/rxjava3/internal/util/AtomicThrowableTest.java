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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class AtomicThrowableTest extends RxJavaTest {

    @Test
    public void isTerminated() {
        AtomicThrowable ex = new AtomicThrowable();
        assertFalse(ex.isTerminated());
        assertNull(ex.terminate());
        assertTrue(ex.isTerminated());
    }

    @Test
    public void tryTerminateAndReportNull() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.tryTerminateAndReport();
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateAndReportAlreadyTerminated() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.terminate();
            ex.tryTerminateAndReport();
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateAndReportHasError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.set(new TestException());
            ex.tryTerminateAndReport();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            assertEquals(1, errors.size());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateConsumerSubscriberNoError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer(ts);
        ts.assertResult();
    }

    @Test
    public void tryTerminateConsumerSubscriberError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer(ts);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerSubscriberTerminated() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer(ts);
        ts.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerMaybeObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerMaybeObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerMaybeObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerSingleNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerSingleError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerSingleTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerCompletableObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerCompletableObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerCompletableObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertEmpty();
    }

    static <T> Emitter<T> wrapToEmitter(final Observer<T> observer) {
        return new Emitter<T>() {

            @Override
            public void onNext(T value) {
                observer.onNext(value);
            }

            @Override
            public void onError(Throwable error) {
                observer.onError(error);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        };
    }

    @Test
    public void tryTerminateConsumerEmitterNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerEmitterError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerEmitterTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertEmpty();
    }

    @Test
    public void tryAddThrowableOrReportNull() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.tryAddThrowableOrReport(new TestException());
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryAddThrowableOrReportTerminated() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.terminate();
            assertFalse(ex.tryAddThrowableOrReport(new TestException()));
            assertFalse("" + errors, errors.isEmpty());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isTerminated, this.description("isTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateAndReportNull, this.description("tryTerminateAndReportNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportAlreadyTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateAndReportAlreadyTerminated, this.description("tryTerminateAndReportAlreadyTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportHasError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateAndReportHasError, this.description("tryTerminateAndReportHasError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSubscriberNoError, this.description("tryTerminateConsumerSubscriberNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSubscriberError, this.description("tryTerminateConsumerSubscriberError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSubscriberTerminated, this.description("tryTerminateConsumerSubscriberTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerObserverNoError, this.description("tryTerminateConsumerObserverNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerObserverError, this.description("tryTerminateConsumerObserverError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerObserverTerminated, this.description("tryTerminateConsumerObserverTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerMaybeObserverNoError, this.description("tryTerminateConsumerMaybeObserverNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerMaybeObserverError, this.description("tryTerminateConsumerMaybeObserverError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerMaybeObserverTerminated, this.description("tryTerminateConsumerMaybeObserverTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSingleNoError, this.description("tryTerminateConsumerSingleNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSingleError, this.description("tryTerminateConsumerSingleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerSingleTerminated, this.description("tryTerminateConsumerSingleTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerCompletableObserverNoError, this.description("tryTerminateConsumerCompletableObserverNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerCompletableObserverError, this.description("tryTerminateConsumerCompletableObserverError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerCompletableObserverTerminated, this.description("tryTerminateConsumerCompletableObserverTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerEmitterNoError, this.description("tryTerminateConsumerEmitterNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerEmitterError, this.description("tryTerminateConsumerEmitterError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryTerminateConsumerEmitterTerminated, this.description("tryTerminateConsumerEmitterTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryAddThrowableOrReportNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryAddThrowableOrReportNull, this.description("tryAddThrowableOrReportNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryAddThrowableOrReportTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryAddThrowableOrReportTerminated, this.description("tryAddThrowableOrReportTerminated"));
        }

        private AtomicThrowableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AtomicThrowableTest();
        }

        @java.lang.Override
        public AtomicThrowableTest implementation() {
            return this.implementation;
        }
    }
}
