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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableMergeDelayErrorTest extends RxJavaTest {

    Observer<String> stringObserver;

    @Before
    public void before() {
        stringObserver = TestHelper.mockObserver();
    }

    @Test
    public void errorDelayed1() {
        // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("four", null, "six"));
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("one", "two", "three"));
        Observable<String> m = Observable.mergeDelayError(o1, o2);
        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        // inner Observable errors are considered terminal for that source
        // verify(stringObserver, times(1)).onNext("six");
        // inner Observable errors are considered terminal for that source
    }

    @Test
    public void errorDelayed2() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("one", "two", "three"));
        // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("four", null, "six"));
        final Observable<String> o3 = Observable.unsafeCreate(new TestErrorObservable("seven", "eight", null));
        final Observable<String> o4 = Observable.unsafeCreate(new TestErrorObservable("nine"));
        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);
        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onError(any(CompositeException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        // inner Observable errors are considered terminal for that source
        // verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void errorDelayed3() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("one", "two", "three"));
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("four", "five", "six"));
        final Observable<String> o3 = Observable.unsafeCreate(new TestErrorObservable("seven", "eight", null));
        final Observable<String> o4 = Observable.unsafeCreate(new TestErrorObservable("nine"));
        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);
        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void errorDelayed4() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("one", "two", "three"));
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("four", "five", "six"));
        final Observable<String> o3 = Observable.unsafeCreate(new TestErrorObservable("seven", "eight"));
        final Observable<String> o4 = Observable.unsafeCreate(new TestErrorObservable("nine", null));
        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);
        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void errorDelayed4WithThreading() {
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", "three");
        final TestAsyncErrorObservable o2 = new TestAsyncErrorObservable("four", "five", "six");
        final TestAsyncErrorObservable o3 = new TestAsyncErrorObservable("seven", "eight");
        // throw the error at the very end so no onComplete will be called after it
        final TestAsyncErrorObservable o4 = new TestAsyncErrorObservable("nine", null);
        Observable<String> m = Observable.mergeDelayError(Observable.unsafeCreate(o1), Observable.unsafeCreate(o2), Observable.unsafeCreate(o3), Observable.unsafeCreate(o4));
        m.subscribe(stringObserver);
        try {
            o1.t.join();
            o2.t.join();
            o3.t.join();
            o4.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
    }

    @Test
    public void compositeErrorDelayed1() {
        // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("four", null, "six"));
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("one", "two", null));
        Observable<String> m = Observable.mergeDelayError(o1, o2);
        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onError(any(Throwable.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(0)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        // inner Observable errors are considered terminal for that source
        // verify(stringObserver, times(1)).onNext("six");
    }

    @Test
    public void compositeErrorDelayed2() {
        // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o1 = Observable.unsafeCreate(new TestErrorObservable("four", null, "six"));
        final Observable<String> o2 = Observable.unsafeCreate(new TestErrorObservable("one", "two", null));
        Observable<String> m = Observable.mergeDelayError(o1, o2);
        CaptureObserver w = new CaptureObserver();
        m.subscribe(w);
        assertNotNull(w.e);
        assertEquals(2, ((CompositeException) w.e).size());
        // if (w.e instanceof CompositeException) {
        // assertEquals(2, ((CompositeException) w.e).getExceptions().size());
        // w.e.printStackTrace();
        // } else {
        // fail("Expecting CompositeException");
        // }
    }

    /**
     * The unit tests below are from OperationMerge and should ensure the normal merge functionality is correct.
     */
    @Test
    public void mergeObservableOfObservables() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.unsafeCreate(new TestSynchronousObservable());
        Observable<Observable<String>> observableOfObservables = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> observer) {
                observer.onSubscribe(Disposable.empty());
                // simulate what would happen in an Observable
                observer.onNext(o1);
                observer.onNext(o2);
                observer.onComplete();
            }
        });
        Observable<String> m = Observable.mergeDelayError(observableOfObservables);
        m.subscribe(stringObserver);
        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onComplete();
        verify(stringObserver, times(2)).onNext("hello");
    }

    @Test
    public void mergeArray() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.unsafeCreate(new TestSynchronousObservable());
        Observable<String> m = Observable.mergeDelayError(o1, o2);
        m.subscribe(stringObserver);
        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(2)).onNext("hello");
        verify(stringObserver, times(1)).onComplete();
    }

    @Test
    public void mergeList() {
        final Observable<String> o1 = Observable.unsafeCreate(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.unsafeCreate(new TestSynchronousObservable());
        List<Observable<String>> listOfObservables = new ArrayList<>();
        listOfObservables.add(o1);
        listOfObservables.add(o2);
        Observable<String> m = Observable.mergeDelayError(Observable.fromIterable(listOfObservables));
        m.subscribe(stringObserver);
        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onComplete();
        verify(stringObserver, times(2)).onNext("hello");
    }

    @Test
    public void mergeArrayWithThreading() {
        final TestASynchronousObservable o1 = new TestASynchronousObservable();
        final TestASynchronousObservable o2 = new TestASynchronousObservable();
        Observable<String> m = Observable.mergeDelayError(Observable.unsafeCreate(o1), Observable.unsafeCreate(o2));
        m.subscribe(stringObserver);
        try {
            o1.t.join();
            o2.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(2)).onNext("hello");
        verify(stringObserver, times(1)).onComplete();
    }

    @Test
    public void synchronousError() {
        final Observable<Observable<String>> o1 = Observable.error(new RuntimeException("unit test"));
        final CountDownLatch latch = new CountDownLatch(1);
        Observable.mergeDelayError(o1).subscribe(new DefaultObserver<String>() {

            @Override
            public void onComplete() {
                fail("Expected onError path");
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }

            @Override
            public void onNext(String s) {
                fail("Expected onError path");
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            fail("interrupted");
        }
    }

    private static class TestSynchronousObservable implements ObservableSource<String> {

        @Override
        public void subscribe(Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            observer.onNext("hello");
            observer.onComplete();
        }
    }

    private static class TestASynchronousObservable implements ObservableSource<String> {

        Thread t;

        @Override
        public void subscribe(final Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    observer.onNext("hello");
                    observer.onComplete();
                }
            });
            t.start();
        }
    }

    private static class TestErrorObservable implements ObservableSource<String> {

        String[] valuesToReturn;

        TestErrorObservable(String... values) {
            valuesToReturn = values;
        }

        @Override
        public void subscribe(Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            boolean errorThrown = false;
            for (String s : valuesToReturn) {
                if (s == null) {
                    System.out.println("throwing exception");
                    observer.onError(new NullPointerException());
                    errorThrown = true;
                    // purposefully not returning here so it will continue calling onNext
                    // so that we also test that we handle bad sequences like this
                } else {
                    observer.onNext(s);
                }
            }
            if (!errorThrown) {
                observer.onComplete();
            }
        }
    }

    private static class TestAsyncErrorObservable implements ObservableSource<String> {

        String[] valuesToReturn;

        TestAsyncErrorObservable(String... values) {
            valuesToReturn = values;
        }

        Thread t;

        @Override
        public void subscribe(final Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    for (String s : valuesToReturn) {
                        if (s == null) {
                            System.out.println("throwing exception");
                            try {
                                Thread.sleep(100);
                            } catch (Throwable e) {
                            }
                            observer.onError(new NullPointerException());
                            return;
                        } else {
                            observer.onNext(s);
                        }
                    }
                    System.out.println("subscription complete");
                    observer.onComplete();
                }
            });
            t.start();
        }
    }

    private static class CaptureObserver extends DefaultObserver<String> {

        volatile Throwable e;

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            this.e = e;
        }

        @Override
        public void onNext(String args) {
        }
    }

    @Test
    public void errorInParentObservable() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.mergeDelayError(Observable.just(Observable.just(1), Observable.just(2)).startWithItem(Observable.<Integer>error(new RuntimeException()))).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertTerminated();
        to.assertValues(1, 2);
        assertEquals(1, to.errors().size());
    }

    @Test
    public void errorInParentObservableDelayed() throws Exception {
        for (int i = 0; i < 50; i++) {
            final TestASynchronous1sDelayedObservable o1 = new TestASynchronous1sDelayedObservable();
            final TestASynchronous1sDelayedObservable o2 = new TestASynchronous1sDelayedObservable();
            Observable<Observable<String>> parentObservable = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

                @Override
                public void subscribe(Observer<? super Observable<String>> op) {
                    op.onSubscribe(Disposable.empty());
                    op.onNext(Observable.unsafeCreate(o1));
                    op.onNext(Observable.unsafeCreate(o2));
                    op.onError(new NullPointerException("throwing exception in parent"));
                }
            });
            Observer<String> stringObserver = TestHelper.mockObserver();
            TestObserverEx<String> to = new TestObserverEx<>(stringObserver);
            Observable<String> m = Observable.mergeDelayError(parentObservable);
            m.subscribe(to);
            System.out.println("testErrorInParentObservableDelayed | " + i);
            to.awaitDone(2000, TimeUnit.MILLISECONDS);
            to.assertTerminated();
            verify(stringObserver, times(2)).onNext("hello");
            verify(stringObserver, times(1)).onError(any(NullPointerException.class));
            verify(stringObserver, never()).onComplete();
        }
    }

    private static class TestASynchronous1sDelayedObservable implements ObservableSource<String> {

        Thread t;

        @Override
        public void subscribe(final Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        observer.onError(e);
                    }
                    observer.onNext("hello");
                    observer.onComplete();
                }
            });
            t.start();
        }
    }

    @Test
    public void mergeIterableDelayError() {
        Observable.mergeDelayError(Arrays.asList(Observable.just(1), Observable.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void mergeArrayDelayError() {
        Observable.mergeArrayDelayError(Observable.just(1), Observable.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void mergeIterableDelayErrorWithError() {
        Observable.mergeDelayError(Arrays.asList(Observable.just(1).concatWith(Observable.<Integer>error(new TestException())), Observable.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayError() {
        Observable.mergeDelayError(Observable.just(Observable.just(1), Observable.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void mergeDelayErrorWithError() {
        Observable.mergeDelayError(Observable.just(Observable.just(1).concatWith(Observable.<Integer>error(new TestException())), Observable.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayErrorMaxConcurrency() {
        Observable.mergeDelayError(Observable.just(Observable.just(1), Observable.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void mergeDelayErrorWithErrorMaxConcurrency() {
        Observable.mergeDelayError(Observable.just(Observable.just(1).concatWith(Observable.<Integer>error(new TestException())), Observable.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeIterableDelayErrorMaxConcurrency() {
        Observable.mergeDelayError(Arrays.asList(Observable.just(1), Observable.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void mergeIterableDelayErrorWithErrorMaxConcurrency() {
        Observable.mergeDelayError(Arrays.asList(Observable.just(1).concatWith(Observable.<Integer>error(new TestException())), Observable.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayError3() {
        Observable.mergeDelayError(Observable.just(1), Observable.just(2), Observable.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void mergeDelayError3WithError() {
        Observable.mergeDelayError(Observable.just(1), Observable.just(2).concatWith(Observable.<Integer>error(new TestException())), Observable.just(3)).test().assertFailure(TestException.class, 1, 2, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed1, this.description("errorDelayed1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed2, this.description("errorDelayed2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed3, this.description("errorDelayed3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed4, this.description("errorDelayed4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed4WithThreading() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelayed4WithThreading, this.description("errorDelayed4WithThreading"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeErrorDelayed1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::compositeErrorDelayed1, this.description("compositeErrorDelayed1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeErrorDelayed2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::compositeErrorDelayed2, this.description("compositeErrorDelayed2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableOfObservables() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeObservableOfObservables, this.description("mergeObservableOfObservables"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArray() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeArray, this.description("mergeArray"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeList, this.description("mergeList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayWithThreading() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeArrayWithThreading, this.description("mergeArrayWithThreading"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::synchronousError, this.description("synchronousError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInParentObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorInParentObservable, this.description("errorInParentObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInParentObservableDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorInParentObservableDelayed, this.description("errorInParentObservableDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableDelayError, this.description("mergeIterableDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeArrayDelayError, this.description("mergeArrayDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableDelayErrorWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableDelayErrorWithError, this.description("mergeIterableDelayErrorWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayError, this.description("mergeDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorWithError, this.description("mergeDelayErrorWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorMaxConcurrency, this.description("mergeDelayErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorWithErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorWithErrorMaxConcurrency, this.description("mergeDelayErrorWithErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableDelayErrorMaxConcurrency, this.description("mergeIterableDelayErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableDelayErrorWithErrorMaxConcurrency() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableDelayErrorWithErrorMaxConcurrency, this.description("mergeIterableDelayErrorWithErrorMaxConcurrency"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayError3, this.description("mergeDelayError3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError3WithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayError3WithError, this.description("mergeDelayError3WithError"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableMergeDelayErrorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableMergeDelayErrorTest();
        }

        @java.lang.Override
        public ObservableMergeDelayErrorTest implementation() {
            return this.implementation;
        }
    }
}
