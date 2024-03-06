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
import java.util.*;
import java.util.concurrent.ExecutionException;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableMaterializeTest extends RxJavaTest {

    @Test
    public void materialize1() {
        // null will cause onError to be triggered before "three" can be
        // returned
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", null, "three");
        TestLocalObserver observer = new TestLocalObserver();
        Observable<Notification<String>> m = Observable.unsafeCreate(o1).materialize();
        m.subscribe(observer);
        try {
            o1.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(observer.onError);
        assertTrue(observer.onComplete);
        assertEquals(3, observer.notifications.size());
        assertTrue(observer.notifications.get(0).isOnNext());
        assertEquals("one", observer.notifications.get(0).getValue());
        assertTrue(observer.notifications.get(1).isOnNext());
        assertEquals("two", observer.notifications.get(1).getValue());
        assertTrue(observer.notifications.get(2).isOnError());
        assertEquals(NullPointerException.class, observer.notifications.get(2).getError().getClass());
    }

    @Test
    public void materialize2() {
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", "three");
        TestLocalObserver observer = new TestLocalObserver();
        Observable<Notification<String>> m = Observable.unsafeCreate(o1).materialize();
        m.subscribe(observer);
        try {
            o1.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(observer.onError);
        assertTrue(observer.onComplete);
        assertEquals(4, observer.notifications.size());
        assertTrue(observer.notifications.get(0).isOnNext());
        assertEquals("one", observer.notifications.get(0).getValue());
        assertTrue(observer.notifications.get(1).isOnNext());
        assertEquals("two", observer.notifications.get(1).getValue());
        assertTrue(observer.notifications.get(2).isOnNext());
        assertEquals("three", observer.notifications.get(2).getValue());
        assertTrue(observer.notifications.get(3).isOnComplete());
    }

    @Test
    public void multipleSubscribes() throws InterruptedException, ExecutionException {
        final TestAsyncErrorObservable o = new TestAsyncErrorObservable("one", "two", null, "three");
        Observable<Notification<String>> m = Observable.unsafeCreate(o).materialize();
        assertEquals(3, m.toList().toFuture().get().size());
        assertEquals(3, m.toList().toFuture().get().size());
    }

    @Test
    public void withCompletionCausingError() {
        TestObserverEx<Notification<Integer>> to = new TestObserverEx<>();
        final RuntimeException ex = new RuntimeException("boo");
        Observable.<Integer>empty().materialize().doOnNext(new Consumer<Object>() {

            @Override
            public void accept(Object t) {
                throw ex;
            }
        }).subscribe(to);
        to.assertError(ex);
        to.assertNoValues();
        to.assertTerminated();
    }

    private static class TestLocalObserver extends DefaultObserver<Notification<String>> {

        boolean onComplete;

        boolean onError;

        List<Notification<String>> notifications = new Vector<>();

        @Override
        public void onComplete() {
            this.onComplete = true;
        }

        @Override
        public void onError(Throwable e) {
            this.onError = true;
        }

        @Override
        public void onNext(Notification<String> value) {
            this.notifications.add(value);
        }
    }

    private static class TestAsyncErrorObservable implements ObservableSource<String> {

        String[] valuesToReturn;

        TestAsyncErrorObservable(String... values) {
            valuesToReturn = values;
        }

        volatile Thread t;

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

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).materialize());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Notification<Object>>>() {

            @Override
            public ObservableSource<Notification<Object>> apply(Observable<Object> o) throws Exception {
                return o.materialize();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_materialize1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::materialize1, this.description("materialize1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_materialize2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::materialize2, this.description("materialize2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleSubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleSubscribes, this.description("multipleSubscribes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withCompletionCausingError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withCompletionCausingError, this.description("withCompletionCausingError"));
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

        private ObservableMaterializeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableMaterializeTest();
        }

        @java.lang.Override
        public ObservableMaterializeTest implementation() {
            return this.implementation;
        }
    }
}
