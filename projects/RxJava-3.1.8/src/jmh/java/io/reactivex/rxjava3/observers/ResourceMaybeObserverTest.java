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
package io.reactivex.rxjava3.observers;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.util.EndConsumerHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ResourceMaybeObserverTest extends RxJavaTest {

    static final class TestResourceMaybeObserver<T> extends ResourceMaybeObserver<T> {

        T value;

        final List<Throwable> errors = new ArrayList<>();

        int complete;

        int start;

        @Override
        protected void onStart() {
            super.onStart();
            start++;
        }

        @Override
        public void onSuccess(final T value) {
            this.value = value;
            dispose();
        }

        @Override
        public void onComplete() {
            complete++;
            dispose();
        }

        @Override
        public void onError(Throwable e) {
            errors.add(e);
            dispose();
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullResource() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        rmo.add(null);
    }

    @Test
    public void addResources() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        Disposable d = Disposable.empty();
        rmo.add(d);
        assertFalse(d.isDisposed());
        rmo.dispose();
        assertTrue(rmo.isDisposed());
        assertTrue(d.isDisposed());
        rmo.dispose();
        assertTrue(rmo.isDisposed());
        assertTrue(d.isDisposed());
    }

    @Test
    public void onCompleteCleansUp() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        Disposable d = Disposable.empty();
        rmo.add(d);
        assertFalse(d.isDisposed());
        rmo.onComplete();
        assertTrue(rmo.isDisposed());
        assertTrue(d.isDisposed());
    }

    @Test
    public void onSuccessCleansUp() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        Disposable d = Disposable.empty();
        rmo.add(d);
        assertFalse(d.isDisposed());
        rmo.onSuccess(1);
        assertTrue(rmo.isDisposed());
        assertTrue(d.isDisposed());
    }

    @Test
    public void onErrorCleansUp() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        Disposable d = Disposable.empty();
        rmo.add(d);
        assertFalse(d.isDisposed());
        rmo.onError(new TestException());
        assertTrue(rmo.isDisposed());
        assertTrue(d.isDisposed());
    }

    @Test
    public void normal() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        assertEquals(0, rmo.start);
        assertNull(rmo.value);
        assertTrue(rmo.errors.isEmpty());
        Maybe.just(1).subscribe(rmo);
        assertTrue(rmo.isDisposed());
        assertEquals(1, rmo.start);
        assertEquals(Integer.valueOf(1), rmo.value);
        assertEquals(0, rmo.complete);
        assertTrue(rmo.errors.isEmpty());
    }

    @Test
    public void empty() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        assertEquals(0, rmo.start);
        assertNull(rmo.value);
        assertTrue(rmo.errors.isEmpty());
        Maybe.<Integer>empty().subscribe(rmo);
        assertTrue(rmo.isDisposed());
        assertEquals(1, rmo.start);
        assertNull(rmo.value);
        assertEquals(1, rmo.complete);
        assertTrue(rmo.errors.isEmpty());
    }

    @Test
    public void error() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        assertFalse(rmo.isDisposed());
        assertEquals(0, rmo.start);
        assertNull(rmo.value);
        assertTrue(rmo.errors.isEmpty());
        final RuntimeException error = new RuntimeException("error");
        Maybe.<Integer>error(error).subscribe(rmo);
        assertTrue(rmo.isDisposed());
        assertEquals(1, rmo.start);
        assertNull(rmo.value);
        assertEquals(0, rmo.complete);
        assertEquals(1, rmo.errors.size());
        assertTrue(rmo.errors.contains(error));
    }

    @Test
    public void startOnce() {
        List<Throwable> error = TestHelper.trackPluginErrors();
        try {
            TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
            rmo.onSubscribe(Disposable.empty());
            Disposable d = Disposable.empty();
            rmo.onSubscribe(d);
            assertTrue(d.isDisposed());
            assertEquals(1, rmo.start);
            TestHelper.assertError(error, 0, IllegalStateException.class, EndConsumerHelper.composeMessage(rmo.getClass().getName()));
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestResourceMaybeObserver<Integer> rmo = new TestResourceMaybeObserver<>();
        rmo.dispose();
        Disposable d = Disposable.empty();
        rmo.onSubscribe(d);
        assertTrue(d.isDisposed());
        assertEquals(0, rmo.start);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::nullResource, this.description("nullResource"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addResources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addResources, this.description("addResources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCleansUp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteCleansUp, this.description("onCompleteCleansUp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessCleansUp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessCleansUp, this.description("onSuccessCleansUp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCleansUp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCleansUp, this.description("onErrorCleansUp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startOnce, this.description("startOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        private ResourceMaybeObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ResourceMaybeObserverTest();
        }

        @java.lang.Override
        public ResourceMaybeObserverTest implementation() {
            return this.implementation;
        }
    }
}
