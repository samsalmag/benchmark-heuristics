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
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;

public class FutureMultiObserverTest extends RxJavaTest {

    @Test
    public void cancelBeforeOnSubscribe() {
        FutureMultiObserver<Integer> f = new FutureMultiObserver<>();
        assertTrue(f.cancel(true));
        Disposable d = Disposable.empty();
        f.onSubscribe(d);
        assertTrue(d.isDisposed());
    }

    @Test
    public void onCompleteJustAfterDispose() {
        FutureMultiObserver<Integer> f = new FutureMultiObserver<>();
        Disposable d = Disposable.empty();
        f.onSubscribe(d);
        assertTrue(f.cancel(true));
        f.onComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelBeforeOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelBeforeOnSubscribe, this.description("cancelBeforeOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteJustAfterDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteJustAfterDispose, this.description("onCompleteJustAfterDispose"));
        }

        private FutureMultiObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FutureMultiObserverTest();
        }

        @java.lang.Override
        public FutureMultiObserverTest implementation() {
            return this.implementation;
        }
    }
}
