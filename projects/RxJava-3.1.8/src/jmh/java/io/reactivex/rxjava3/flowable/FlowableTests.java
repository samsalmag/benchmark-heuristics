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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTests extends RxJavaTest {

    Subscriber<Number> w;

    SingleObserver<Number> wo;

    MaybeObserver<Number> wm;

    private static final Predicate<Integer> IS_EVEN = new Predicate<Integer>() {

        @Override
        public boolean test(Integer v) {
            return v % 2 == 0;
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockSubscriber();
        wo = TestHelper.mockSingleObserver();
        wm = TestHelper.mockMaybeObserver();
    }

    @Test
    public void fromArray() {
        String[] items = new String[] { "one", "two", "three" };
        assertEquals((Long) 3L, Flowable.fromArray(items).count().blockingGet());
        assertEquals("two", Flowable.fromArray(items).skip(1).take(1).blockingSingle());
        assertEquals("three", Flowable.fromArray(items).takeLast(1).blockingSingle());
    }

    @Test
    public void fromIterable() {
        ArrayList<String> items = new ArrayList<>();
        items.add("one");
        items.add("two");
        items.add("three");
        assertEquals((Long) 3L, Flowable.fromIterable(items).count().blockingGet());
        assertEquals("two", Flowable.fromIterable(items).skip(1).take(1).blockingSingle());
        assertEquals("three", Flowable.fromIterable(items).takeLast(1).blockingSingle());
    }

    @Test
    public void fromArityArgs3() {
        Flowable<String> items = Flowable.just("one", "two", "three");
        assertEquals((Long) 3L, items.count().blockingGet());
        assertEquals("two", items.skip(1).take(1).blockingSingle());
        assertEquals("three", items.takeLast(1).blockingSingle());
    }

    @Test
    public void fromArityArgs1() {
        Flowable<String> items = Flowable.just("one");
        assertEquals((Long) 1L, items.count().blockingGet());
        assertEquals("one", items.takeLast(1).blockingSingle());
    }

    @Test
    public void create() {
        Flowable<String> flowable = Flowable.just("one", "two", "three");
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void countAFewItemsFlowable() {
        Flowable<String> flowable = Flowable.just("a", "b", "c", "d");
        flowable.count().toFlowable().subscribe(w);
        // we should be called only once
        verify(w).onNext(4L);
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void countZeroItemsFlowable() {
        Flowable<String> flowable = Flowable.empty();
        flowable.count().toFlowable().subscribe(w);
        // we should be called only once
        verify(w).onNext(0L);
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void countErrorFlowable() {
        Flowable<String> f = Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new RuntimeException();
            }
        });
        f.count().toFlowable().subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, never()).onComplete();
        verify(w, times(1)).onError(any(RuntimeException.class));
    }

    @Test
    public void countAFewItems() {
        Flowable<String> flowable = Flowable.just("a", "b", "c", "d");
        flowable.count().subscribe(wo);
        // we should be called only once
        verify(wo).onSuccess(4L);
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void countZeroItems() {
        Flowable<String> flowable = Flowable.empty();
        flowable.count().subscribe(wo);
        // we should be called only once
        verify(wo).onSuccess(0L);
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void countError() {
        Flowable<String> f = Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new RuntimeException();
            }
        });
        f.count().subscribe(wo);
        verify(wo, never()).onSuccess(anyInt());
        verify(wo, times(1)).onError(any(RuntimeException.class));
    }

    @Test
    public void takeFirstWithPredicateOfSome() {
        Flowable<Integer> flowable = Flowable.just(1, 3, 5, 4, 6, 3);
        flowable.filter(IS_EVEN).take(1).subscribe(w);
        verify(w, times(1)).onNext(anyInt());
        verify(w).onNext(4);
        verify(w, times(1)).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeFirstWithPredicateOfNoneMatchingThePredicate() {
        Flowable<Integer> flowable = Flowable.just(1, 3, 5, 7, 9, 7, 5, 3, 1);
        flowable.filter(IS_EVEN).take(1).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, times(1)).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeFirstOfSome() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3);
        flowable.take(1).subscribe(w);
        verify(w, times(1)).onNext(anyInt());
        verify(w).onNext(1);
        verify(w, times(1)).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeFirstOfNone() {
        Flowable<Integer> flowable = Flowable.empty();
        flowable.take(1).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, times(1)).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOfNoneFlowable() {
        Flowable<Integer> flowable = Flowable.empty();
        flowable.firstElement().toFlowable().subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstWithPredicateOfNoneMatchingThePredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 3, 5, 7, 9, 7, 5, 3, 1);
        flowable.filter(IS_EVEN).firstElement().toFlowable().subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w).onComplete();
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOfNone() {
        Flowable<Integer> flowable = Flowable.empty();
        flowable.firstElement().subscribe(wm);
        verify(wm, never()).onSuccess(anyInt());
        verify(wm).onComplete();
        verify(wm, never()).onError(isA(NoSuchElementException.class));
    }

    @Test
    public void firstWithPredicateOfNoneMatchingThePredicate() {
        Flowable<Integer> flowable = Flowable.just(1, 3, 5, 7, 9, 7, 5, 3, 1);
        flowable.filter(IS_EVEN).firstElement().subscribe(wm);
        verify(wm, never()).onSuccess(anyInt());
        verify(wm, times(1)).onComplete();
        verify(wm, never()).onError(isA(NoSuchElementException.class));
    }

    @Test
    public void reduce() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4);
        flowable.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().subscribe(w);
        // we should be called only once
        verify(w, times(1)).onNext(anyInt());
        verify(w).onNext(10);
    }

    @Test
    public void reduceWithEmptyObservable() {
        Flowable<Integer> flowable = Flowable.range(1, 0);
        flowable.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().test().assertResult();
    }

    /**
     * A reduce on an empty Observable and a seed should just pass the seed through.
     *
     * This is confirmed at https://github.com/ReactiveX/RxJava/issues/423#issuecomment-27642456
     */
    @Test
    public void reduceWithEmptyObservableAndSeed() {
        Flowable<Integer> flowable = Flowable.range(1, 0);
        int value = flowable.reduce(1, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).blockingGet();
        assertEquals(1, value);
    }

    @Test
    public void reduceWithInitialValue() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4);
        flowable.reduce(50, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).subscribe(wo);
        // we should be called only once
        verify(wo, times(1)).onSuccess(anyInt());
        verify(wo).onSuccess(60);
    }

    @Test
    public void materializeDematerializeChaining() {
        Flowable<Integer> obs = Flowable.just(1);
        Flowable<Integer> chained = obs.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        chained.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onError(any(Throwable.class));
    }

    /**
     * The error from the user provided Observer is not handled by the subscribe method try/catch.
     *
     * It is handled by the AtomicObserver that wraps the provided Observer.
     *
     * Result: Passes (if AtomicObserver functionality exists)
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void customObservableWithErrorInObserverAsynchronous() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        // FIXME custom built???
        Flowable.just("1", "2", "three", "4").subscribeOn(Schedulers.newThread()).safeSubscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
                System.out.println("error");
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(String v) {
                int num = Integer.parseInt(v);
                System.out.println(num);
                // doSomething(num);
                count.incrementAndGet();
            }
        });
        // wait for async sequence to complete
        latch.await();
        assertEquals(2, count.get());
        assertNotNull(error.get());
        if (!(error.get() instanceof NumberFormatException)) {
            fail("It should be a NumberFormatException");
        }
    }

    /**
     * The error from the user provided Observer is handled by the subscribe try/catch because this is synchronous.
     *
     * Result: Passes
     */
    @Test
    public void customObservableWithErrorInObserverSynchronous() {
        final AtomicInteger count = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        // FIXME custom built???
        Flowable.just("1", "2", "three", "4").safeSubscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
                System.out.println("error");
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                int num = Integer.parseInt(v);
                System.out.println(num);
                // doSomething(num);
                count.incrementAndGet();
            }
        });
        assertEquals(2, count.get());
        assertNotNull(error.get());
        if (!(error.get() instanceof NumberFormatException)) {
            fail("It should be a NumberFormatException");
        }
    }

    /**
     * The error from the user provided Observable is handled by the subscribe try/catch because this is synchronous.
     *
     * Result: Passes
     */
    @Test
    public void customObservableWithErrorInObservableSynchronous() {
        final AtomicInteger count = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        // FIXME custom built???
        Flowable.just("1", "2").concatWith(Flowable.<String>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new NumberFormatException();
            }
        })).subscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
                System.out.println("error");
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                System.out.println(v);
                count.incrementAndGet();
            }
        });
        assertEquals(2, count.get());
        assertNotNull(error.get());
        if (!(error.get() instanceof NumberFormatException)) {
            fail("It should be a NumberFormatException");
        }
    }

    @Test
    public void publishLast() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger();
        ConnectableFlowable<String> connectable = Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                count.incrementAndGet();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        subscriber.onNext("first");
                        subscriber.onNext("last");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).takeLast(1).publish();
        // subscribe once
        final CountDownLatch latch = new CountDownLatch(1);
        connectable.subscribe(new Consumer<String>() {

            @Override
            public void accept(String value) {
                assertEquals("last", value);
                latch.countDown();
            }
        });
        // subscribe twice
        connectable.subscribe();
        Disposable subscription = connectable.connect();
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        assertEquals(1, count.get());
        subscription.dispose();
    }

    @Test
    public void replay() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        ConnectableFlowable<String> f = Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        subscriber.onNext("one");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).replay();
        // we connect immediately and it will emit the value
        Disposable connection = f.connect();
        try {
            // we then expect the following 2 subscriptions to get that same value
            final CountDownLatch latch = new CountDownLatch(2);
            // subscribe once
            f.subscribe(new Consumer<String>() {

                @Override
                public void accept(String v) {
                    assertEquals("one", v);
                    latch.countDown();
                }
            });
            // subscribe again
            f.subscribe(new Consumer<String>() {

                @Override
                public void accept(String v) {
                    assertEquals("one", v);
                    latch.countDown();
                }
            });
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                fail("subscriptions did not receive values");
            }
            assertEquals(1, counter.get());
        } finally {
            connection.dispose();
        }
    }

    @Test
    public void cache() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Flowable<String> f = Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        subscriber.onNext("one");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).cache();
        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);
        // subscribe once
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        // subscribe again
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("subscriptions did not receive values");
        }
        assertEquals(1, counter.get());
    }

    @Test
    public void cacheWithCapacity() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Flowable<String> f = Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        subscriber.onNext("one");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).cacheWithInitialCapacity(1);
        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);
        // subscribe once
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        // subscribe again
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("subscriptions did not receive values");
        }
        assertEquals(1, counter.get());
    }

    @Test
    public void takeWithErrorInObserver() {
        final AtomicInteger count = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        Flowable.just("1", "2", "three", "4").take(3).safeSubscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
                System.out.println("error");
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                int num = Integer.parseInt(v);
                System.out.println(num);
                // doSomething(num);
                count.incrementAndGet();
            }
        });
        assertEquals(2, count.get());
        assertNotNull(error.get());
        if (!(error.get() instanceof NumberFormatException)) {
            fail("It should be a NumberFormatException");
        }
    }

    @Test
    public void ofType() {
        Flowable<String> flowable = Flowable.just(1, "abc", false, 2L).ofType(String.class);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(1);
        verify(subscriber, times(1)).onNext("abc");
        verify(subscriber, never()).onNext(false);
        verify(subscriber, never()).onNext(2L);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void ofTypeWithPolymorphism() {
        ArrayList<Integer> l1 = new ArrayList<>();
        l1.add(1);
        LinkedList<Integer> l2 = new LinkedList<>();
        l2.add(2);
        @SuppressWarnings("rawtypes")
        Flowable<List> flowable = Flowable.<Object>just(l1, l2, "123").ofType(List.class);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(l1);
        verify(subscriber, times(1)).onNext(l2);
        verify(subscriber, never()).onNext("123");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void containsFlowable() {
        Flowable<Boolean> flowable = Flowable.just("a", "b", "c").contains("b").toFlowable();
        FlowableSubscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void containsWithInexistenceFlowable() {
        Flowable<Boolean> flowable = Flowable.just("a", "b").contains("c").toFlowable();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void containsWithEmptyObservableFlowable() {
        Flowable<Boolean> flowable = Flowable.<String>empty().contains("a").toFlowable();
        FlowableSubscriber<Object> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void contains() {
        // FIXME nulls not allowed, changed to "c"
        Single<Boolean> single = Flowable.just("a", "b", "c").contains("b");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void containsWithInexistence() {
        // FIXME null values are not allowed, removed
        Single<Boolean> single = Flowable.just("a", "b").contains("c");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void containsWithEmptyObservable() {
        Single<Boolean> single = Flowable.<String>empty().contains("a");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void ignoreElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3).ignoreElements().toFlowable();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(Integer.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void ignoreElements() {
        Completable completable = Flowable.just(1, 2, 3).ignoreElements();
        CompletableObserver observer = TestHelper.mockCompletableObserver();
        completable.subscribe(observer);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void justWithScheduler() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Integer> flowable = Flowable.fromArray(1, 2).subscribeOn(scheduler);
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void startWithWithScheduler() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Integer> flowable = Flowable.just(3, 4).startWithIterable(Arrays.asList(1, 2)).subscribeOn(scheduler);
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onNext(3);
        inOrder.verify(subscriber, times(1)).onNext(4);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rangeWithScheduler() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Integer> flowable = Flowable.range(3, 4).subscribeOn(scheduler);
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(3);
        inOrder.verify(subscriber, times(1)).onNext(4);
        inOrder.verify(subscriber, times(1)).onNext(5);
        inOrder.verify(subscriber, times(1)).onNext(6);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void mergeWith() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).mergeWith(Flowable.just(2)).subscribe(ts);
        ts.assertValues(1, 2);
    }

    @Test
    public void concatWith() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).concatWith(Flowable.just(2)).subscribe(ts);
        ts.assertValues(1, 2);
    }

    @Test
    public void ambWith() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).ambWith(Flowable.just(2)).subscribe(ts);
        ts.assertValue(1);
    }

    @Test
    public void takeWhileToList() {
        final int expectedCount = 3;
        final AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < expectedCount; i++) {
            Flowable.just(Boolean.TRUE, Boolean.FALSE).takeWhile(new Predicate<Boolean>() {

                @Override
                public boolean test(Boolean v) {
                    return v;
                }
            }).toList().doOnSuccess(new Consumer<List<Boolean>>() {

                @Override
                public void accept(List<Boolean> booleans) {
                    count.incrementAndGet();
                }
            }).subscribe();
        }
        assertEquals(expectedCount, count.get());
    }

    @Test
    public void compose() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        Flowable.just(1, 2, 3).compose(new FlowableTransformer<Integer, String>() {

            @Override
            public Publisher<String> apply(Flowable<Integer> t1) {
                return t1.map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer v) {
                        return String.valueOf(v);
                    }
                });
            }
        }).subscribe(ts);
        ts.assertTerminated();
        ts.assertNoErrors();
        ts.assertValues("1", "2", "3");
    }

    @Test
    public void errorThrownIssue1685() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            FlowableProcessor<Object> processor = ReplayProcessor.create();
            Flowable.error(new RuntimeException("oops")).materialize().delay(1, TimeUnit.SECONDS).dematerialize(Functions.<Notification<Object>>identity()).subscribe(processor);
            processor.subscribe();
            processor.materialize().blockingFirst();
            System.out.println("Done");
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emptyIdentity() {
        assertEquals(Flowable.empty(), Flowable.empty());
    }

    @Test
    public void emptyIsEmpty() {
        Flowable.<Integer>empty().subscribe(w);
        verify(w).onComplete();
        verify(w, never()).onNext(any(Integer.class));
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void extend() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final Object value = new Object();
        Object returned = Flowable.just(value).to(new FlowableConverter<Object, Object>() {

            @Override
            public Object apply(Flowable<Object> onSubscribe) {
                onSubscribe.subscribe(subscriber);
                subscriber.assertNoErrors();
                subscriber.assertComplete();
                subscriber.assertValue(value);
                return subscriber.values().get(0);
            }
        });
        assertSame(returned, value);
    }

    @Test
    public void asExtend() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final Object value = new Object();
        Object returned = Flowable.just(value).to(new FlowableConverter<Object, Object>() {

            @Override
            public Object apply(Flowable<Object> onSubscribe) {
                onSubscribe.subscribe(subscriber);
                subscriber.assertNoErrors();
                subscriber.assertComplete();
                subscriber.assertValue(value);
                return subscriber.values().get(0);
            }
        });
        assertSame(returned, value);
    }

    @Test
    public void as() {
        Flowable.just(1).to(new FlowableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Flowable<Integer> v) {
                return v.toObservable();
            }
        }).test().assertResult(1);
    }

    @Test
    public void toObservableEmpty() {
        Flowable.empty().toObservable().test().assertResult();
    }

    @Test
    public void toObservableJust() {
        Flowable.just(1).toObservable().test().assertResult(1);
    }

    @Test
    public void toObservableRange() {
        Flowable.range(1, 5).toObservable().test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void toObservableError() {
        Flowable.error(new TestException()).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void zipIterableObject() {
        final List<Flowable<Integer>> flowables = Arrays.asList(Flowable.just(1, 2, 3), Flowable.just(1, 2, 3));
        Flowable.zip(flowables, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] o) throws Exception {
                int sum = 0;
                for (Object i : o) {
                    sum += (Integer) i;
                }
                return sum;
            }
        }).test().assertResult(2, 4, 6);
    }

    @Test
    public void combineLatestObject() {
        final List<Flowable<Integer>> flowables = Arrays.asList(Flowable.just(1, 2, 3), Flowable.just(1, 2, 3));
        Flowable.combineLatest(flowables, new Function<Object[], Object>() {

            @Override
            public Object apply(final Object[] o) throws Exception {
                int sum = 1;
                for (Object i : o) {
                    sum *= (Integer) i;
                }
                return sum;
            }
        }).test().assertResult(3, 6, 9);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArray() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromArray, this.description("fromArray"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromIterable, this.description("fromIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArityArgs3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromArityArgs3, this.description("fromArityArgs3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArityArgs1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromArityArgs1, this.description("fromArityArgs1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_create() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::create, this.description("create"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countAFewItemsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countAFewItemsFlowable, this.description("countAFewItemsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countZeroItemsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countZeroItemsFlowable, this.description("countZeroItemsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countErrorFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countErrorFlowable, this.description("countErrorFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countAFewItems() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countAFewItems, this.description("countAFewItems"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countZeroItems() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countZeroItems, this.description("countZeroItems"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::countError, this.description("countError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirstWithPredicateOfSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirstWithPredicateOfSome, this.description("takeFirstWithPredicateOfSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirstWithPredicateOfNoneMatchingThePredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirstWithPredicateOfNoneMatchingThePredicate, this.description("takeFirstWithPredicateOfNoneMatchingThePredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirstOfSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirstOfSome, this.description("takeFirstOfSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirstOfNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeFirstOfNone, this.description("takeFirstOfNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOfNoneFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOfNoneFlowable, this.description("firstOfNoneFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateOfNoneMatchingThePredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateOfNoneMatchingThePredicateFlowable, this.description("firstWithPredicateOfNoneMatchingThePredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOfNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstOfNone, this.description("firstOfNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateOfNoneMatchingThePredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstWithPredicateOfNoneMatchingThePredicate, this.description("firstWithPredicateOfNoneMatchingThePredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduce, this.description("reduce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithEmptyObservable, this.description("reduceWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithEmptyObservableAndSeed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithEmptyObservableAndSeed, this.description("reduceWithEmptyObservableAndSeed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithInitialValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithInitialValue, this.description("reduceWithInitialValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_materializeDematerializeChaining() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::materializeDematerializeChaining, this.description("materializeDematerializeChaining"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customObservableWithErrorInObserverAsynchronous() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customObservableWithErrorInObserverAsynchronous, this.description("customObservableWithErrorInObserverAsynchronous"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customObservableWithErrorInObserverSynchronous() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customObservableWithErrorInObserverSynchronous, this.description("customObservableWithErrorInObserverSynchronous"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customObservableWithErrorInObservableSynchronous() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::customObservableWithErrorInObservableSynchronous, this.description("customObservableWithErrorInObservableSynchronous"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishLast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishLast, this.description("publishLast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replay, this.description("replay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cache() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cache, this.description("cache"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cacheWithCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cacheWithCapacity, this.description("cacheWithCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWithErrorInObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeWithErrorInObserver, this.description("takeWithErrorInObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ofType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ofType, this.description("ofType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ofTypeWithPolymorphism() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ofTypeWithPolymorphism, this.description("ofTypeWithPolymorphism"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_containsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::containsFlowable, this.description("containsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_containsWithInexistenceFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::containsWithInexistenceFlowable, this.description("containsWithInexistenceFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_containsWithEmptyObservableFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::containsWithEmptyObservableFlowable, this.description("containsWithEmptyObservableFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_contains() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::contains, this.description("contains"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_containsWithInexistence() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::containsWithInexistence, this.description("containsWithInexistence"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_containsWithEmptyObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::containsWithEmptyObservable, this.description("containsWithEmptyObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreElementsFlowable, this.description("ignoreElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreElements, this.description("ignoreElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justWithScheduler, this.description("justWithScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithWithScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithWithScheduler, this.description("startWithWithScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeWithScheduler, this.description("rangeWithScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeWith() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeWith, this.description("mergeWith"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWith() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatWith, this.description("concatWith"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWith() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambWith, this.description("ambWith"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhileToList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeWhileToList, this.description("takeWhileToList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::compose, this.description("compose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorThrownIssue1685() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorThrownIssue1685, this.description("errorThrownIssue1685"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIdentity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyIdentity, this.description("emptyIdentity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyIsEmpty, this.description("emptyIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_extend() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::extend, this.description("extend"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asExtend() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asExtend, this.description("asExtend"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_as() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::as, this.description("as"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableEmpty, this.description("toObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableJust, this.description("toObservableJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableRange() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableRange, this.description("toObservableRange"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableError, this.description("toObservableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableObject() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zipIterableObject, this.description("zipIterableObject"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestObject() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::combineLatestObject, this.description("combineLatestObject"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FlowableTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableTests();
        }

        @java.lang.Override
        public FlowableTests implementation() {
            return this.implementation;
        }
    }
}
