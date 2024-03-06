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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDetachTest extends RxJavaTest {

    @Test
    public void doubleSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.onTerminateDetach();
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onTerminateDetach());
    }

    @Test
    public void onError() {
        Maybe.error(new TestException()).onTerminateDetach().test().assertFailure(TestException.class);
    }

    @Test
    public void onComplete() {
        Maybe.empty().onTerminateDetach().test().assertResult();
    }

    @Test
    public void cancelDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Object> to = new Maybe<Object>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Object> observer) {
                observer.onSubscribe(wr.get());
            }
        }.onTerminateDetach().test();
        d = null;
        to.dispose();
        System.gc();
        Thread.sleep(200);
        to.assertEmpty();
        assertNull(wr.get());
    }

    @Test
    public void completeDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Integer> to = new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(wr.get());
                observer.onComplete();
                observer.onComplete();
            }
        }.onTerminateDetach().test();
        d = null;
        System.gc();
        Thread.sleep(200);
        to.assertResult();
        assertNull(wr.get());
    }

    @Test
    public void errorDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Integer> to = new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(wr.get());
                observer.onError(new TestException());
                observer.onError(new IOException());
            }
        }.onTerminateDetach().test();
        d = null;
        System.gc();
        Thread.sleep(200);
        to.assertFailure(TestException.class);
        assertNull(wr.get());
    }

    @Test
    public void successDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Integer> to = new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(wr.get());
                observer.onSuccess(1);
                observer.onSuccess(2);
            }
        }.onTerminateDetach().test();
        d = null;
        System.gc();
        Thread.sleep(200);
        to.assertResult(1);
        assertNull(wr.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleSubscribe, this.description("doubleSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onError, this.description("onError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onComplete, this.description("onComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelDetaches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelDetaches, this.description("cancelDetaches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeDetaches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeDetaches, this.description("completeDetaches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDetaches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDetaches, this.description("errorDetaches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successDetaches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successDetaches, this.description("successDetaches"));
        }

        private MaybeDetachTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDetachTest();
        }

        @java.lang.Override
        public MaybeDetachTest implementation() {
            return this.implementation;
        }
    }
}
