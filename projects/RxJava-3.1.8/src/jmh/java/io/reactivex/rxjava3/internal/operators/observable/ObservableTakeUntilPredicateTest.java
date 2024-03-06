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
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeUntilPredicateTest extends RxJavaTest {

    @Test
    public void takeEmpty() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.empty().takeUntil(new Predicate<Object>() {

            @Override
            public boolean test(Object v) {
                return true;
            }
        }).subscribe(o);
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeAll() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o).onNext(2);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeFirst() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeSome() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2, 3).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 == 2;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o).onNext(2);
        verify(o, never()).onNext(3);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void functionThrows() {
        Observer<Object> o = TestHelper.mockObserver();
        Predicate<Integer> predicate = (new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                throw new TestException("Forced failure");
            }
        });
        Observable.just(1, 2, 3).takeUntil(predicate).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o, never()).onNext(3);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void sourceThrows() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).concatWith(Observable.just(2)).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void errorIncludesLastValueAsCause() {
        TestObserverEx<String> to = new TestObserverEx<>();
        final TestException e = new TestException("Forced failure");
        Predicate<String> predicate = (new Predicate<String>() {

            @Override
            public boolean test(String t) {
                throw e;
            }
        });
        Observable.just("abc").takeUntil(predicate).subscribe(to);
        to.assertTerminated();
        to.assertNotComplete();
        to.assertError(TestException.class);
        // FIXME last cause value is not saved
        // assertTrue(ts.errors().get(0).getCause().getMessage().contains("abc"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().takeUntil(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.takeUntil(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.takeUntil(Functions.alwaysFalse()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeEmpty, this.description("takeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeAll, this.description("takeAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirst, this.description("takeFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeSome, this.description("takeSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::functionThrows, this.description("functionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sourceThrows, this.description("sourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorIncludesLastValueAsCause() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorIncludesLastValueAsCause, this.description("errorIncludesLastValueAsCause"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        private ObservableTakeUntilPredicateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableTakeUntilPredicateTest();
        }

        @java.lang.Override
        public ObservableTakeUntilPredicateTest implementation() {
            return this.implementation;
        }
    }
}
