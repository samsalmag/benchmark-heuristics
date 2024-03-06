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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTimeoutWithSelectorTest extends RxJavaTest {

    @Test
    public void timeoutSelectorNormal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.timeout(timeout, timeoutFunc, other).subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        timeout.onNext(1);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onNext(100);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void timeoutSelectorTimeoutFirst() throws InterruptedException {
        Observable<Integer> source = Observable.<Integer>never();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.timeout(timeout, timeoutFunc, other).subscribe(o);
        timeout.onNext(1);
        inOrder.verify(o).onNext(100);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void timeoutSelectorFirstThrows() {
        Observable<Integer> source = Observable.<Integer>never();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Supplier<Observable<Integer>> firstTimeoutFunc = new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                throw new TestException();
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        source.timeout(Observable.defer(firstTimeoutFunc), timeoutFunc, other).subscribe(o);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void timeoutSelectorSubsequentThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.timeout(timeout, timeoutFunc, other).subscribe(o);
        source.onNext(1);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void timeoutSelectorFirstObservableThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        source.timeout(Observable.<Integer>error(new TestException()), timeoutFunc, other).subscribe(o);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void timeoutSelectorSubsequentObservableThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return Observable.<Integer>error(new TestException());
            }
        };
        Observable<Integer> other = Observable.fromIterable(Arrays.asList(100));
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.timeout(timeout, timeoutFunc, other).subscribe(o);
        source.onNext(1);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void timeoutSelectorWithFirstTimeoutFirstAndNoOtherObservable() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return PublishSubject.create();
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        source.timeout(timeout, timeoutFunc).subscribe(o);
        timeout.onNext(1);
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onError(isA(TimeoutException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void timeoutSelectorWithTimeoutFirstAndNoOtherObservable() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> timeout = PublishSubject.create();
        Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        source.timeout(PublishSubject.create(), timeoutFunc).subscribe(o);
        source.onNext(1);
        timeout.onNext(1);
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onError(isA(TimeoutException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void timeoutSelectorWithTimeoutAndOnNextRaceCondition() throws InterruptedException {
        // Thread 1                                    Thread 2
        // 
        // observer.onNext(1)
        // start timeout
        // unsubscribe timeout in thread 2          start to do some long-time work in "unsubscribe"
        // observer.onNext(2)
        // timeout.onNext(1)
        // "unsubscribe" done
        // 
        // 
        // In the above case, the timeout operator should ignore "timeout.onNext(1)"
        // since "observer" has already seen 2.
        final CountDownLatch observerReceivedTwo = new CountDownLatch(1);
        final CountDownLatch timeoutEmittedOne = new CountDownLatch(1);
        final CountDownLatch observerCompleted = new CountDownLatch(1);
        final CountDownLatch enteredTimeoutOne = new CountDownLatch(1);
        final AtomicBoolean latchTimeout = new AtomicBoolean(false);
        final Function<Integer, Observable<Integer>> timeoutFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                if (t1 == 1) {
                    // Force "unsubscribe" run on another thread
                    return Observable.unsafeCreate(new ObservableSource<Integer>() {

                        @Override
                        public void subscribe(Observer<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            enteredTimeoutOne.countDown();
                            // force the timeout message be sent after observer.onNext(2)
                            while (true) {
                                try {
                                    if (!observerReceivedTwo.await(30, TimeUnit.SECONDS)) {
                                        // CountDownLatch timeout
                                        // There should be something wrong
                                        latchTimeout.set(true);
                                    }
                                    break;
                                } catch (InterruptedException e) {
                                    // Since we just want to emulate a busy method,
                                    // we ignore the interrupt signal from Scheduler.
                                }
                            }
                            observer.onNext(1);
                            timeoutEmittedOne.countDown();
                        }
                    }).subscribeOn(Schedulers.newThread());
                } else {
                    return PublishSubject.create();
                }
            }
        };
        final Observer<Integer> o = TestHelper.mockObserver();
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                observerReceivedTwo.countDown();
                return null;
            }
        }).when(o).onNext(2);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                observerCompleted.countDown();
                return null;
            }
        }).when(o).onComplete();
        final TestObserver<Integer> to = new TestObserver<>(o);
        new Thread(new Runnable() {

            @Override
            public void run() {
                PublishSubject<Integer> source = PublishSubject.create();
                source.timeout(timeoutFunc, Observable.just(3)).subscribe(to);
                // start timeout
                source.onNext(1);
                try {
                    if (!enteredTimeoutOne.await(30, TimeUnit.SECONDS)) {
                        latchTimeout.set(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // disable timeout
                source.onNext(2);
                try {
                    if (!timeoutEmittedOne.await(30, TimeUnit.SECONDS)) {
                        latchTimeout.set(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                source.onComplete();
            }
        }).start();
        if (!observerCompleted.await(30, TimeUnit.SECONDS)) {
            latchTimeout.set(true);
        }
        assertFalse("CoundDownLatch timeout", latchTimeout.get());
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onSubscribe((Disposable) notNull());
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o, never()).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().timeout(Functions.justFunction(Observable.never())));
        TestHelper.checkDisposed(PublishSubject.create().timeout(Functions.justFunction(Observable.never()), Observable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.timeout(Functions.justFunction(Observable.never()));
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.timeout(Functions.justFunction(Observable.never()), Observable.never());
            }
        });
    }

    @Test
    public void empty() {
        Observable.empty().timeout(Functions.justFunction(Observable.never())).test().assertResult();
    }

    @Test
    public void error() {
        Observable.error(new TestException()).timeout(Functions.justFunction(Observable.never())).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyInner() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.timeout(Functions.justFunction(Observable.empty())).test();
        ps.onNext(1);
        to.assertFailure(TimeoutException.class, 1);
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<Integer> ps = PublishSubject.create();
            TestObserverEx<Integer> to = ps.timeout(Functions.justFunction(new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onNext(2);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            })).to(TestHelper.<Integer>testConsumer());
            ps.onNext(1);
            to.assertFailureAndMessage(TestException.class, "First", 1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSourceOther() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<Integer> ps = PublishSubject.create();
            TestObserverEx<Integer> to = ps.timeout(Functions.justFunction(new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onNext(2);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }), Observable.just(2)).to(TestHelper.<Integer>testConsumer());
            ps.onNext(1);
            to.assertFailureAndMessage(TestException.class, "First", 1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withOtherMainError() {
        Observable.error(new TestException()).timeout(Functions.justFunction(Observable.never()), Observable.never()).test().assertFailure(TestException.class);
    }

    @Test
    @SuppressUndeliverable
    public void badSourceTimeout() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
                observer.onError(new TestException("First"));
                observer.onNext(3);
                observer.onComplete();
                observer.onError(new TestException("Second"));
            }
        }.timeout(Functions.justFunction(Observable.never()), Observable.<Integer>never()).take(1).test().assertResult(1);
    }

    @Test
    public void selectorTake() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.timeout(Functions.justFunction(Observable.never())).take(1).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        assertFalse(ps.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void selectorFallbackTake() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.timeout(Functions.justFunction(Observable.never()), Observable.just(2)).take(1).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        assertFalse(ps.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void lateOnTimeoutError() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Observer<?>[] sub = { null, null };
                final Observable<Integer> pp2 = new Observable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        observer.onSubscribe(Disposable.empty());
                        sub[count++] = observer;
                    }
                };
                TestObserver<Integer> to = ps.timeout(Functions.justFunction(pp2)).test();
                ps.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onNext(1);
                    }
                };
                final Throwable ex = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void lateOnTimeoutFallbackRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Observer<?>[] sub = { null, null };
                final Observable<Integer> pp2 = new Observable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        assertFalse(((Disposable) observer).isDisposed());
                        observer.onSubscribe(Disposable.empty());
                        sub[count++] = observer;
                    }
                };
                TestObserver<Integer> to = ps.timeout(Functions.justFunction(pp2), Observable.<Integer>never()).test();
                ps.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onNext(1);
                    }
                };
                final Throwable ex = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onErrorOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Observer<?>[] sub = { null, null };
                final Observable<Integer> pp2 = new Observable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        assertFalse(((Disposable) observer).isDisposed());
                        observer.onSubscribe(Disposable.empty());
                        sub[count++] = observer;
                    }
                };
                TestObserver<Integer> to = ps.timeout(Functions.justFunction(pp2)).test();
                ps.onNext(0);
                final Throwable ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Observer<?>[] sub = { null, null };
                final Observable<Integer> pp2 = new Observable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        assertFalse(((Disposable) observer).isDisposed());
                        observer.onSubscribe(Disposable.empty());
                        sub[count++] = observer;
                    }
                };
                TestObserver<Integer> to = ps.timeout(Functions.justFunction(pp2)).test();
                ps.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteOnTimeoutRaceFallback() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Observer<?>[] sub = { null, null };
                final Observable<Integer> pp2 = new Observable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        assertFalse(((Disposable) observer).isDisposed());
                        observer.onSubscribe(Disposable.empty());
                        sub[count++] = observer;
                    }
                };
                TestObserver<Integer> to = ps.timeout(Functions.justFunction(pp2), Observable.<Integer>never()).test();
                ps.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void disposedUpfront() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final AtomicInteger counter = new AtomicInteger();
        Observable<Object> timeoutAndFallback = Observable.never().doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                counter.incrementAndGet();
            }
        });
        ps.timeout(timeoutAndFallback, Functions.justFunction(timeoutAndFallback)).test(true).assertEmpty();
        assertEquals(0, counter.get());
    }

    @Test
    public void disposedUpfrontFallback() {
        PublishSubject<Object> ps = PublishSubject.create();
        final AtomicInteger counter = new AtomicInteger();
        Observable<Object> timeoutAndFallback = Observable.never().doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                counter.incrementAndGet();
            }
        });
        ps.timeout(timeoutAndFallback, Functions.justFunction(timeoutAndFallback), timeoutAndFallback).test(true).assertEmpty();
        assertEquals(0, counter.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorNormal1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorNormal1, this.description("timeoutSelectorNormal1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorTimeoutFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorTimeoutFirst, this.description("timeoutSelectorTimeoutFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorFirstThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorFirstThrows, this.description("timeoutSelectorFirstThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorSubsequentThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorSubsequentThrows, this.description("timeoutSelectorSubsequentThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorFirstObservableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorFirstObservableThrows, this.description("timeoutSelectorFirstObservableThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorSubsequentObservableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorSubsequentObservableThrows, this.description("timeoutSelectorSubsequentObservableThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithFirstTimeoutFirstAndNoOtherObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorWithFirstTimeoutFirstAndNoOtherObservable, this.description("timeoutSelectorWithFirstTimeoutFirstAndNoOtherObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithTimeoutFirstAndNoOtherObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorWithTimeoutFirstAndNoOtherObservable, this.description("timeoutSelectorWithTimeoutFirstAndNoOtherObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithTimeoutAndOnNextRaceCondition() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSelectorWithTimeoutAndOnNextRaceCondition, this.description("timeoutSelectorWithTimeoutAndOnNextRaceCondition"));
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
        public void benchmark_emptyInner() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyInner, this.description("emptyInner"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badInnerSource, this.description("badInnerSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSourceOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badInnerSourceOther, this.description("badInnerSourceOther"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withOtherMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withOtherMainError, this.description("withOtherMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceTimeout, this.description("badSourceTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorTake, this.description("selectorTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorFallbackTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorFallbackTake, this.description("selectorFallbackTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lateOnTimeoutError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lateOnTimeoutError, this.description("lateOnTimeoutError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lateOnTimeoutFallbackRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lateOnTimeoutFallbackRace, this.description("lateOnTimeoutFallbackRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnTimeoutRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorOnTimeoutRace, this.description("onErrorOnTimeoutRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnTimeoutRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnTimeoutRace, this.description("onCompleteOnTimeoutRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnTimeoutRaceFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnTimeoutRaceFallback, this.description("onCompleteOnTimeoutRaceFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedUpfront, this.description("disposedUpfront"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfrontFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedUpfrontFallback, this.description("disposedUpfrontFallback"));
        }

        private ObservableTimeoutWithSelectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableTimeoutWithSelectorTest();
        }

        @java.lang.Override
        public ObservableTimeoutWithSelectorTest implementation() {
            return this.implementation;
        }
    }
}
