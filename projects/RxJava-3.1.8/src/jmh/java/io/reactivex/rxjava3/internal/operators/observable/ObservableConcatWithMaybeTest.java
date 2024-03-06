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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.MaybeSubject;

public class ObservableConcatWithMaybeTest extends RxJavaTest {

    @Test
    public void normalEmpty() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).subscribe(to);
        to.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void normalNonEmpty() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.just(100)).subscribe(to);
        to.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void mainError() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.<Integer>error(new TestException()).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).subscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>error(new TestException())).subscribe(to);
        to.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void takeMain() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).take(3).subscribe(to);
        to.assertResult(1, 2, 3);
    }

    @Test
    public void cancelOther() {
        MaybeSubject<Object> other = MaybeSubject.create();
        TestObserver<Object> to = Observable.empty().concatWith(other).test();
        assertTrue(other.hasObservers());
        to.dispose();
        assertFalse(other.hasObservers());
    }

    @Test
    public void consumerDisposed() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                assertFalse(((Disposable) observer).isDisposed());
                observer.onNext(1);
                assertTrue(((Disposable) observer).isDisposed());
                assertTrue(bs1.isDisposed());
            }
        }.concatWith(Maybe.just(100)).take(1).test().assertResult(1);
    }

    @Test
    public void badSource() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                Disposable bs2 = Disposable.empty();
                observer.onSubscribe(bs2);
                assertFalse(bs1.isDisposed());
                assertTrue(bs2.isDisposed());
                observer.onComplete();
            }
        }.concatWith(Maybe.<Integer>empty()).test().assertResult();
    }

    @Test
    public void badSource2() {
        Flowable.empty().concatWith(new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                Disposable bs2 = Disposable.empty();
                observer.onSubscribe(bs2);
                assertFalse(bs1.isDisposed());
                assertTrue(bs2.isDisposed());
                observer.onComplete();
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalEmpty, this.description("normalEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalNonEmpty, this.description("normalNonEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherError, this.description("otherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeMain, this.description("takeMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOther, this.description("cancelOther"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::consumerDisposed, this.description("consumerDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource2, this.description("badSource2"));
        }

        private ObservableConcatWithMaybeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableConcatWithMaybeTest();
        }

        @java.lang.Override
        public ObservableConcatWithMaybeTest implementation() {
            return this.implementation;
        }
    }
}
