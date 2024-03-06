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
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeUntilTest extends RxJavaTest {

    @Test
    public void takeUntil() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnNext("three");
        source.sendOnNext("four");
        source.sendOnCompleted();
        other.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(0)).onNext("four");
        verify(sSource, times(1)).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilSourceCompleted() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        // no longer disposing itself on terminal events
        verify(sSource, never()).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilSourceError() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        // no longer disposing itself on terminal events
        verify(sSource, never()).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilOtherError() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(result, times(0)).onComplete();
        verify(sSource, times(1)).dispose();
        // no longer disposing itself on termination
        verify(sOther, never()).dispose();
    }

    /**
     * If the 'other' onCompletes then we unsubscribe from the source and onComplete.
     */
    @Test
    public void takeUntilOtherCompleted() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnCompleted();
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onComplete();
        verify(sSource, times(1)).dispose();
        // no longer disposing itself on terminal events
        verify(sOther, never()).dispose();
    }

    private static class TestObservable implements ObservableSource<String> {

        Observer<? super String> observer;

        Disposable upstream;

        TestObservable(Disposable d) {
            this.upstream = d;
        }

        /* used to simulate subscription */
        public void sendOnCompleted() {
            observer.onComplete();
        }

        /* used to simulate subscription */
        public void sendOnNext(String value) {
            observer.onNext(value);
        }

        /* used to simulate subscription */
        public void sendOnError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public void subscribe(Observer<? super String> observer) {
            this.observer = observer;
            observer.onSubscribe(upstream);
        }
    }

    @Test
    public void untilFires() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        to.assertValue(1);
        until.onNext(1);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        // 2.0.2 - not anymore
        // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void mainCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        source.onComplete();
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        // 2.0.2 - not anymore
        // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void downstreamUnsubscribes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).take(1).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        // 2.0.2 - not anymore
        // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().takeUntil(Observable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> o) throws Exception {
                return o.takeUntil(Observable.never());
            }
        });
    }

    @Test
    public void untilPublisherMainSuccess() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onNext(1);
        main.onNext(2);
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1, 2);
    }

    @Test
    public void untilPublisherMainComplete() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onNext(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntil() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntil, this.description("takeUntil"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilSourceCompleted, this.description("takeUntilSourceCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilSourceError, this.description("takeUntilSourceError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilOtherError, this.description("takeUntilOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilOtherCompleted, this.description("takeUntilOtherCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFires, this.description("untilFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainCompletes, this.description("mainCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_downstreamUnsubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::downstreamUnsubscribes, this.description("downstreamUnsubscribes"));
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
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainSuccess, this.description("untilPublisherMainSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainComplete, this.description("untilPublisherMainComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainError, this.description("untilPublisherMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherOnNext, this.description("untilPublisherOtherOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherOnComplete, this.description("untilPublisherOtherOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherError, this.description("untilPublisherOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherDispose, this.description("untilPublisherDispose"));
        }

        private ObservableTakeUntilTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableTakeUntilTest();
        }

        @java.lang.Override
        public ObservableTakeUntilTest implementation() {
            return this.implementation;
        }
    }
}
