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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableDefaultIfEmptyTest extends RxJavaTest {

    @Test
    public void defaultIfEmpty() {
        Observable<Integer> source = Observable.just(1, 2, 3);
        Observable<Integer> observable = source.defaultIfEmpty(10);
        Observer<Integer> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(10);
        verify(observer).onNext(1);
        verify(observer).onNext(2);
        verify(observer).onNext(3);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void defaultIfEmptyWithEmpty() {
        Observable<Integer> source = Observable.empty();
        Observable<Integer> observable = source.defaultIfEmpty(10);
        Observer<Integer> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer).onNext(10);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::defaultIfEmpty, this.description("defaultIfEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmptyWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::defaultIfEmptyWithEmpty, this.description("defaultIfEmptyWithEmpty"));
        }

        private ObservableDefaultIfEmptyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableDefaultIfEmptyTest();
        }

        @java.lang.Override
        public ObservableDefaultIfEmptyTest implementation() {
            return this.implementation;
        }
    }
}
