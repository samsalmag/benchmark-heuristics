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

public class ObservableCastTest extends RxJavaTest {

    @Test
    public void cast() {
        Observable<?> source = Observable.just(1, 2);
        Observable<Integer> observable = source.cast(Integer.class);
        Observer<Integer> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(1);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void castWithWrongType() {
        Observable<?> source = Observable.just(1, 2);
        Observable<Boolean> observable = source.cast(Boolean.class);
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onError(any(ClassCastException.class));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cast, this.description("cast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_castWithWrongType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::castWithWrongType, this.description("castWithWrongType"));
        }

        private ObservableCastTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableCastTest();
        }

        @java.lang.Override
        public ObservableCastTest implementation() {
            return this.implementation;
        }
    }
}
