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
import java.util.concurrent.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;

public class BlockingObservableToFutureTest extends RxJavaTest {

    @Test
    public void toFuture() throws InterruptedException, ExecutionException {
        Observable<String> obs = Observable.just("one");
        Future<String> f = obs.toFuture();
        assertEquals("one", f.get());
    }

    @Test
    public void toFutureList() throws InterruptedException, ExecutionException {
        Observable<String> obs = Observable.just("one", "two", "three");
        Future<List<String>> f = obs.toList().toFuture();
        assertEquals("one", f.get().get(0));
        assertEquals("two", f.get().get(1));
        assertEquals("three", f.get().get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void exceptionWithMoreThanOneElement() throws Throwable {
        Observable<String> obs = Observable.just("one", "two");
        Future<String> f = obs.toFuture();
        try {
            // we expect an exception since there are more than 1 element
            f.get();
            fail("Should have thrown!");
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void toFutureWithException() {
        Observable<String> obs = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext("one");
                observer.onError(new TestException());
            }
        });
        Future<String> f = obs.toFuture();
        try {
            f.get();
            fail("expected exception");
        } catch (Throwable e) {
            assertEquals(TestException.class, e.getCause().getClass());
        }
    }

    @Test(expected = CancellationException.class)
    public void getAfterCancel() throws Exception {
        Observable<String> obs = Observable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get();
    }

    @Test(expected = CancellationException.class)
    public void getWithTimeoutAfterCancel() throws Exception {
        Observable<String> obs = Observable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Test(expected = NoSuchElementException.class)
    public void getWithEmptyFlowable() throws Throwable {
        Observable<String> obs = Observable.empty();
        Future<String> f = obs.toFuture();
        try {
            f.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFuture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFuture, this.description("toFuture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFutureList, this.description("toFutureList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exceptionWithMoreThanOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::exceptionWithMoreThanOneElement, this.description("exceptionWithMoreThanOneElement"), java.lang.IndexOutOfBoundsException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureWithException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFutureWithException, this.description("toFutureWithException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getAfterCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::getAfterCancel, this.description("getAfterCancel"), java.util.concurrent.CancellationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithTimeoutAfterCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::getWithTimeoutAfterCancel, this.description("getWithTimeoutAfterCancel"), java.util.concurrent.CancellationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::getWithEmptyFlowable, this.description("getWithEmptyFlowable"), java.util.NoSuchElementException.class);
        }

        private BlockingObservableToFutureTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BlockingObservableToFutureTest();
        }

        @java.lang.Override
        public BlockingObservableToFutureTest implementation() {
            return this.implementation;
        }
    }
}
