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
package io.reactivex.rxjava3.internal.subscriptions;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;

public class AsyncSubscriptionTest extends RxJavaTest {

    @Test
    public void noResource() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        as.request(1);
        as.cancel();
        verify(s).request(1);
        verify(s).cancel();
    }

    @Test
    public void requestBeforeSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.request(1);
        as.setSubscription(s);
        as.cancel();
        verify(s).request(1);
        verify(s).cancel();
    }

    @Test
    public void cancelBeforeSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.request(1);
        as.cancel();
        as.setSubscription(s);
        verify(s, never()).request(1);
        verify(s).cancel();
    }

    @Test
    public void singleSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        Subscription s1 = mock(Subscription.class);
        as.setSubscription(s1);
        assertSame(as.actual.get(), s);
        verify(s1).cancel();
    }

    @Test
    public void initialResource() {
        Disposable r = mock(Disposable.class);
        AsyncSubscription as = new AsyncSubscription(r);
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void setResource() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.setResource(r));
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void replaceResource() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.replaceResource(r));
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void setResource2() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.setResource(r));
        Disposable r2 = mock(Disposable.class);
        assertTrue(as.setResource(r2));
        as.cancel();
        verify(r).dispose();
        verify(r2).dispose();
    }

    @Test
    public void replaceResource2() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.replaceResource(r));
        Disposable r2 = mock(Disposable.class);
        as.replaceResource(r2);
        as.cancel();
        verify(r, never()).dispose();
        verify(r2).dispose();
    }

    @Test
    public void setResourceAfterCancel() {
        AsyncSubscription as = new AsyncSubscription();
        as.cancel();
        Disposable r = mock(Disposable.class);
        as.setResource(r);
        verify(r).dispose();
    }

    @Test
    public void replaceResourceAfterCancel() {
        AsyncSubscription as = new AsyncSubscription();
        as.cancel();
        Disposable r = mock(Disposable.class);
        as.replaceResource(r);
        verify(r).dispose();
    }

    @Test
    public void cancelOnce() {
        Disposable r = mock(Disposable.class);
        AsyncSubscription as = new AsyncSubscription(r);
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        as.cancel();
        as.cancel();
        as.cancel();
        verify(s, never()).request(anyLong());
        verify(s).cancel();
        verify(r).dispose();
    }

    @Test
    public void disposed() {
        AsyncSubscription as = new AsyncSubscription();
        assertFalse(as.isDisposed());
        as.dispose();
        assertTrue(as.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noResource, this.description("noResource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBeforeSet, this.description("requestBeforeSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelBeforeSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelBeforeSet, this.description("cancelBeforeSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSet, this.description("singleSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initialResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::initialResource, this.description("initialResource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setResource, this.description("setResource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaceResource, this.description("replaceResource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setResource2, this.description("setResource2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaceResource2, this.description("replaceResource2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResourceAfterCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::setResourceAfterCancel, this.description("setResourceAfterCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResourceAfterCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaceResourceAfterCancel, this.description("replaceResourceAfterCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnce, this.description("cancelOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        private AsyncSubscriptionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AsyncSubscriptionTest();
        }

        @java.lang.Override
        public AsyncSubscriptionTest implementation() {
            return this.implementation;
        }
    }
}
