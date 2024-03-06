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
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.functions.Action;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableThrottleFirstTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Observer<String> observer;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        observer = TestHelper.mockObserver();
    }

    @Test
    public void throttlingWithDropCallbackCrashes() throws Throwable {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                // publish as it's first
                publishNext(innerObserver, 100, "one");
                // skip as it's last within the first 400
                publishNext(innerObserver, 300, "two");
                // publish
                publishNext(innerObserver, 900, "three");
                // skip
                publishNext(innerObserver, 905, "four");
                // Should be published as soon as the timeout expires.
                publishCompleted(innerObserver, 1000);
            }
        });
        Action whenDisposed = mock(Action.class);
        Observable<String> sampled = source.doOnDispose(whenDisposed).throttleFirst(400, TimeUnit.MILLISECONDS, scheduler, e -> {
            if ("two".equals(e)) {
                throw new TestException("forced");
            }
        });
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        inOrder.verify(observer, times(1)).onError(any(TestException.class));
        inOrder.verify(observer, times(0)).onNext("two");
        inOrder.verify(observer, times(0)).onNext("three");
        inOrder.verify(observer, times(0)).onNext("four");
        inOrder.verify(observer, times(0)).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(whenDisposed).run();
    }

    @Test
    public void throttlingWithDropCallback() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                // publish as it's first
                publishNext(innerObserver, 100, "one");
                // skip as it's last within the first 400
                publishNext(innerObserver, 300, "two");
                // publish
                publishNext(innerObserver, 900, "three");
                // skip
                publishNext(innerObserver, 905, "four");
                // Should be published as soon as the timeout expires.
                publishCompleted(innerObserver, 1000);
            }
        });
        Observer<Object> dropCallbackObserver = TestHelper.mockObserver();
        Observable<String> sampled = source.throttleFirst(400, TimeUnit.MILLISECONDS, scheduler, dropCallbackObserver::onNext);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        InOrder dropCallbackOrder = inOrder(dropCallbackObserver);
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        inOrder.verify(observer, times(0)).onNext("two");
        dropCallbackOrder.verify(dropCallbackObserver, times(1)).onNext("two");
        inOrder.verify(observer, times(1)).onNext("three");
        inOrder.verify(observer, times(0)).onNext("four");
        dropCallbackOrder.verify(dropCallbackObserver, times(1)).onNext("four");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        dropCallbackOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttlingWithCompleted() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                // publish as it's first
                publishNext(innerObserver, 100, "one");
                // skip as it's last within the first 400
                publishNext(innerObserver, 300, "two");
                // publish
                publishNext(innerObserver, 900, "three");
                // skip
                publishNext(innerObserver, 905, "four");
                // Should be published as soon as the timeout expires.
                publishCompleted(innerObserver, 1000);
            }
        });
        Observable<String> sampled = source.throttleFirst(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        inOrder.verify(observer, times(0)).onNext("two");
        inOrder.verify(observer, times(1)).onNext("three");
        inOrder.verify(observer, times(0)).onNext("four");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttlingWithError() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                Exception error = new TestException();
                // Should be published since it is first
                publishNext(innerObserver, 100, "one");
                // Should be skipped since onError will arrive before the timeout expires
                publishNext(innerObserver, 200, "two");
                // Should be published as soon as the timeout expires.
                publishError(innerObserver, 300, error);
            }
        });
        Observable<String> sampled = source.throttleFirst(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        inOrder.verify(observer).onNext("one");
        inOrder.verify(observer).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private <T> void publishCompleted(final Observer<T> innerObserver, long delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                innerObserver.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishError(final Observer<T> innerObserver, long delay, final Exception error) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                innerObserver.onError(error);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishNext(final Observer<T> innerObserver, long delay, final T value) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                innerObserver.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Test
    public void throttle() {
        Observer<Integer> observer = TestHelper.mockObserver();
        TestScheduler s = new TestScheduler();
        PublishSubject<Integer> o = PublishSubject.create();
        o.throttleFirst(500, TimeUnit.MILLISECONDS, s).subscribe(observer);
        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(1);
        // skip
        o.onNext(2);
        s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(3);
        s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(4);
        s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(5);
        // skip
        o.onNext(6);
        s.advanceTimeTo(1001, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(7);
        s.advanceTimeTo(1501, TimeUnit.MILLISECONDS);
        o.onComplete();
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext(1);
        inOrder.verify(observer).onNext(3);
        inOrder.verify(observer).onNext(7);
        inOrder.verify(observer).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttleFirstDefaultScheduler() {
        Observable.just(1).throttleFirst(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).throttleFirst(1, TimeUnit.DAYS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.throttleFirst(1, TimeUnit.SECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithDropCallbackCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithDropCallbackCrashes, this.description("throttlingWithDropCallbackCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithDropCallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithDropCallback, this.description("throttlingWithDropCallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithCompleted, this.description("throttlingWithCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithError, this.description("throttlingWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttle, this.description("throttle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttleFirstDefaultScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttleFirstDefaultScheduler, this.description("throttleFirstDefaultScheduler"));
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

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableThrottleFirstTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableThrottleFirstTest();
        }

        @java.lang.Override
        public ObservableThrottleFirstTest implementation() {
            return this.implementation;
        }
    }
}
