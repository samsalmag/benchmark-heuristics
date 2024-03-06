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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableHideTest extends RxJavaTest {

    @Test
    public void hiding() {
        PublishSubject<Integer> src = PublishSubject.create();
        Observable<Integer> dst = src.hide();
        assertFalse(dst instanceof PublishSubject);
        Observer<Object> o = TestHelper.mockObserver();
        dst.subscribe(o);
        src.onNext(1);
        src.onComplete();
        verify(o).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void hidingError() {
        PublishSubject<Integer> src = PublishSubject.create();
        Observable<Integer> dst = src.hide();
        assertFalse(dst instanceof PublishSubject);
        Observer<Object> o = TestHelper.mockObserver();
        dst.subscribe(o);
        src.onError(new TestException());
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.hide();
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().hide());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hiding() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hiding, this.description("hiding"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hidingError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hidingError, this.description("hidingError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        private ObservableHideTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableHideTest();
        }

        @java.lang.Override
        public ObservableHideTest implementation() {
            return this.implementation;
        }
    }
}
