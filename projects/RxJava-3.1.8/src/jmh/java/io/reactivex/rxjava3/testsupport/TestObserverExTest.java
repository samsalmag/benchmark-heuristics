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
package io.reactivex.rxjava3.testsupport;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;

public class TestObserverExTest extends RxJavaTest {

    @Test
    public void assertTestObserverEx() {
        Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
        TestObserverEx<Integer> subscriber = new TestObserverEx<>();
        oi.subscribe(subscriber);
        subscriber.assertValues(1, 2);
        subscriber.assertValueCount(2);
        subscriber.assertTerminated();
    }

    @Test
    public void assertNotMatchCount() {
        assertThrows(AssertionError.class, () -> {
            Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
            TestObserverEx<Integer> subscriber = new TestObserverEx<>();
            oi.subscribe(subscriber);
            subscriber.assertValue(1);
            subscriber.assertValueCount(2);
            subscriber.assertTerminated();
        });
    }

    @Test
    public void assertNotMatchValue() {
        assertThrows(AssertionError.class, () -> {
            Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
            TestObserverEx<Integer> subscriber = new TestObserverEx<>();
            oi.subscribe(subscriber);
            subscriber.assertValues(1, 3);
            subscriber.assertValueCount(2);
            subscriber.assertTerminated();
        });
    }

    @Test
    public void assertNeverAtNotMatchingValue() {
        Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
        TestObserverEx<Integer> subscriber = new TestObserverEx<>();
        oi.subscribe(subscriber);
        subscriber.assertNever(3);
        subscriber.assertValueCount(2);
        subscriber.assertTerminated();
    }

    @Test
    public void assertNeverAtMatchingValue() {
        assertThrows(AssertionError.class, () -> {
            Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
            TestObserverEx<Integer> subscriber = new TestObserverEx<>();
            oi.subscribe(subscriber);
            subscriber.assertValues(1, 2);
            subscriber.assertNever(2);
            subscriber.assertValueCount(2);
            subscriber.assertTerminated();
        });
    }

    @Test
    public void assertNeverAtMatchingPredicate() {
        assertThrows(AssertionError.class, () -> {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1, 2).subscribe(to);
            to.assertValues(1, 2);
            to.assertNever(new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void assertNeverAtNotMatchingPredicate() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.just(2, 3).subscribe(to);
        to.assertNever(new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 1;
            }
        });
    }

    @Test
    public void assertTerminalEventNotReceived() {
        assertThrows(AssertionError.class, () -> {
            PublishSubject<Integer> p = PublishSubject.create();
            TestObserverEx<Integer> subscriber = new TestObserverEx<>();
            p.subscribe(subscriber);
            p.onNext(1);
            p.onNext(2);
            subscriber.assertValues(1, 2);
            subscriber.assertValueCount(2);
            subscriber.assertTerminated();
        });
    }

    @Test
    public void wrappingMock() {
        Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2));
        Observer<Integer> mockSubscriber = TestHelper.mockObserver();
        oi.subscribe(new TestObserverEx<>(mockSubscriber));
        InOrder inOrder = inOrder(mockSubscriber);
        inOrder.verify(mockSubscriber, times(1)).onNext(1);
        inOrder.verify(mockSubscriber, times(1)).onNext(2);
        inOrder.verify(mockSubscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void wrappingMockWhenUnsubscribeInvolved() {
        Observable<Integer> oi = Observable.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(2);
        Observer<Integer> mockSubscriber = TestHelper.mockObserver();
        oi.subscribe(new TestObserverEx<>(mockSubscriber));
        InOrder inOrder = inOrder(mockSubscriber);
        inOrder.verify(mockSubscriber, times(1)).onNext(1);
        inOrder.verify(mockSubscriber, times(1)).onNext(2);
        inOrder.verify(mockSubscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void errorSwallowed() {
        Observable.error(new RuntimeException()).subscribe(new TestObserverEx<>());
    }

    @Test
    public void nullExpected() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onNext(1);
        try {
            to.assertValue((Integer) null);
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Null element check assertion didn't happen!");
    }

    @Test
    public void nullActual() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onNext(null);
        try {
            to.assertValue(1);
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Null element check assertion didn't happen!");
    }

    @Test
    public void terminalErrorOnce() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onError(new TestException());
        to.onError(new TestException());
        try {
            to.assertTerminated();
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Failed to report multiple onError terminal events!");
    }

    @Test
    public void terminalCompletedOnce() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onComplete();
        to.onComplete();
        try {
            to.assertTerminated();
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Failed to report multiple onComplete terminal events!");
    }

    @Test
    public void terminalOneKind() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onError(new TestException());
        to.onComplete();
        try {
            to.assertTerminated();
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Failed to report multiple kinds of events!");
    }

    @Test
    public void createDelegate() {
        TestObserverEx<Integer> to1 = new TestObserverEx<>();
        TestObserverEx<Integer> to = new TestObserverEx<>(to1);
        to.assertNotSubscribed();
        assertFalse(to.hasSubscription());
        to.onSubscribe(Disposable.empty());
        try {
            to.assertNotSubscribed();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        assertTrue(to.hasSubscription());
        assertFalse(to.isDisposed());
        to.onNext(1);
        to.onError(new TestException());
        to.onComplete();
        to1.assertValue(1).assertError(TestException.class).assertComplete();
        to.dispose();
        assertTrue(to.isDisposed());
        assertSame(Thread.currentThread(), to.lastThread());
        try {
            to.assertNoValues();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertValueCount(0);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        to.assertValueSequence(Collections.singletonList(1));
        try {
            to.assertValueSequence(Collections.singletonList(2));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
    }

    @Test
    public void assertError() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        try {
            to.assertError(TestException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertError(new TestException());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertError(Functions.<Throwable>alwaysTrue());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertErrorMessage("");
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertSubscribed();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        to.onSubscribe(Disposable.empty());
        to.assertSubscribed();
        to.assertNoErrors();
        TestException ex = new TestException("Forced failure");
        to.onError(ex);
        to.assertError(ex);
        to.assertError(TestException.class);
        to.assertError(Functions.<Throwable>alwaysTrue());
        to.assertError(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable t) throws Exception {
                return t.getMessage() != null && t.getMessage().contains("Forced");
            }
        });
        to.assertErrorMessage("Forced failure");
        try {
            to.assertErrorMessage("");
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertError(new RuntimeException());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertError(IOException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertError(Functions.<Throwable>alwaysFalse());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            to.assertNoErrors();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        to.assertTerminated();
        to.assertValueCount(0);
        to.assertNoValues();
    }

    @Test
    public void emptyObserverEnum() {
        assertEquals(1, TestObserverEx.EmptyObserver.values().length);
        assertNotNull(TestObserverEx.EmptyObserver.valueOf("INSTANCE"));
    }

    @Test
    public void valueAndClass() {
        assertEquals("null", TestObserver.valueAndClass(null));
        assertEquals("1 (class: Integer)", TestObserver.valueAndClass(1));
    }

    @Test
    public void assertFailure() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException("Forced failure"));
        to.assertFailure(TestException.class);
        to.assertFailure(Functions.<Throwable>alwaysTrue());
        to.assertFailureAndMessage(TestException.class, "Forced failure");
        to.onNext(1);
        to.assertFailure(TestException.class, 1);
        to.assertFailure(Functions.<Throwable>alwaysTrue(), 1);
        to.assertFailureAndMessage(TestException.class, "Forced failure", 1);
    }

    @Test
    public void assertFuseable() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertNotFuseable();
        try {
            to.assertFuseable();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertFusionMode(QueueFuseable.SYNC);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        to.onSubscribe(new ScalarDisposable<>(to, 1));
        to.assertFuseable();
        to.assertFusionMode(QueueFuseable.SYNC);
        try {
            to.assertFusionMode(QueueFuseable.NONE);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertNotFuseable();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertTerminated() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.assertNotTerminated();
        to.onError(null);
        try {
            to.assertNotTerminated();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertResult() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onComplete();
        to.assertResult();
        try {
            to.assertResult(1);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        to.onNext(1);
        to.assertResult(1);
        try {
            to.assertResult(2);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertResult();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void await() throws Exception {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        assertFalse(to.await(100, TimeUnit.MILLISECONDS));
        to.awaitDone(100, TimeUnit.MILLISECONDS);
        assertTrue(to.isDisposed());
        to.assertNotTerminated();
        to.onComplete();
        assertTrue(to.await(100, TimeUnit.MILLISECONDS));
        to.await();
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors().assertComplete();
        final TestObserverEx<Integer> to1 = new TestObserverEx<>();
        to1.onSubscribe(Disposable.empty());
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                to1.onComplete();
            }
        }, 200, TimeUnit.MILLISECONDS);
        to1.await();
    }

    @Test
    public void errors() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        assertEquals(0, to.errors().size());
        to.onError(new TestException());
        assertEquals(1, to.errors().size());
        TestHelper.assertError(to.errors(), 0, TestException.class);
    }

    @Test
    public void onNext() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertValueCount(0);
        assertEquals(Collections.emptyList(), to.values());
        to.onNext(1);
        assertEquals(Collections.singletonList(1), to.values());
        to.dispose();
        assertTrue(to.isDisposed());
        to.assertValue(1);
        to.onComplete();
    }

    @Test
    public void fusionModeToString() {
        assertEquals("NONE", TestObserverEx.fusionModeToString(QueueFuseable.NONE));
        assertEquals("SYNC", TestObserverEx.fusionModeToString(QueueFuseable.SYNC));
        assertEquals("ASYNC", TestObserverEx.fusionModeToString(QueueFuseable.ASYNC));
        assertEquals("Unknown(100)", TestObserverEx.fusionModeToString(100));
    }

    @Test
    public void multipleTerminals() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertNotComplete();
        to.onComplete();
        try {
            to.assertNotComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        to.assertTerminated();
        to.onComplete();
        try {
            to.assertComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        try {
            to.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        try {
            to.assertNotComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
    }

    @Test
    public void assertValue() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        try {
            to.assertValue(1);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        to.onNext(1);
        to.assertValue(1);
        try {
            to.assertValue(2);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        to.onNext(2);
        try {
            to.assertValue(1);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
    }

    @Test
    public void onNextMisbehave() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onNext(1);
        to.assertError(IllegalStateException.class);
        to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onNext(null);
        to.assertFailure(NullPointerException.class, (Integer) null);
    }

    @Test
    public void assertTerminated2() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertNotTerminated();
        to.onError(new TestException());
        to.assertTerminated();
        to.onError(new IOException());
        try {
            to.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertError(TestException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException());
        to.onComplete();
        try {
            to.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void onSubscribe() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(null);
        to.assertError(NullPointerException.class);
        to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        Disposable d1 = Disposable.empty();
        to.onSubscribe(d1);
        assertTrue(d1.isDisposed());
        to.assertError(IllegalStateException.class);
        to = new TestObserverEx<>();
        to.dispose();
        d1 = Disposable.empty();
        to.onSubscribe(d1);
        assertTrue(d1.isDisposed());
    }

    @Test
    public void assertValueSequence() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onNext(1);
        to.onNext(2);
        try {
            to.assertValueSequence(Collections.<Integer>emptyList());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("More values received than expected (0)"));
        }
        try {
            to.assertValueSequence(Collections.singletonList(1));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("More values received than expected (1)"));
        }
        to.assertValueSequence(Arrays.asList(1, 2));
        try {
            to.assertValueSequence(Arrays.asList(1, 2, 3));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("Fewer values received than expected (2)"));
        }
    }

    @Test
    public void assertEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        try {
            to.assertEmpty();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        to.onSubscribe(Disposable.empty());
        to.assertEmpty();
        to.onNext(1);
        try {
            to.assertEmpty();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void awaitDoneTimed() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Thread.currentThread().interrupt();
        try {
            to.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void assertNotSubscribed() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.assertNotSubscribed();
        to.errors().add(new TestException());
        try {
            to.assertNotSubscribed();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertErrorMultiple() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        TestException e = new TestException();
        to.errors().add(e);
        to.errors().add(new TestException());
        try {
            to.assertError(TestException.class);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertError(e);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertError(Functions.<Throwable>alwaysTrue());
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            to.assertErrorMessage("");
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void errorInPredicate() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.onError(new RuntimeException());
        try {
            to.assertError(new Predicate<Throwable>() {

                @Override
                public boolean test(Throwable throwable) throws Exception {
                    throw new TestException();
                }
            });
        } catch (TestException ex) {
            // expected
            return;
        }
        fail("Error in predicate but not thrown!");
    }

    @Test
    public void assertComplete() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        try {
            to.assertComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        to.onComplete();
        to.assertComplete();
        to.onComplete();
        try {
            to.assertComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void completeWithoutOnSubscribe() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onComplete();
        to.assertError(IllegalStateException.class);
    }

    @Test
    public void completeDelegateThrows() {
        TestObserverEx<Integer> to = new TestObserverEx<>(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
        to.onSubscribe(Disposable.empty());
        try {
            to.onComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (TestException ex) {
            to.assertTerminated();
        }
    }

    @Test
    public void errorDelegateThrows() {
        TestObserverEx<Integer> to = new TestObserverEx<>(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
        to.onSubscribe(Disposable.empty());
        try {
            to.onError(new IOException());
            throw new RuntimeException("Should have thrown!");
        } catch (TestException ex) {
            to.assertTerminated();
        }
    }

    @Test
    public void syncQueueThrows() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.SYNC);
        Observable.range(1, 5).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).subscribe(to);
        to.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertFailure(TestException.class);
    }

    @Test
    public void asyncQueueThrows() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).subscribe(to);
        us.onNext(1);
        to.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertFailure(TestException.class);
    }

    @Test
    public void completedMeansDisposed() {
        // 2.0.2 - a terminated TestObserver no longer reports isDisposed
        assertFalse(Observable.just(1).test().assertResult(1).isDisposed());
    }

    @Test
    public void errorMeansDisposed() {
        // 2.0.2 - a terminated TestObserver no longer reports isDisposed
        assertFalse(Observable.error(new TestException()).test().assertFailure(TestException.class).isDisposed());
    }

    @Test
    public void asyncFusion() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.subscribe(to);
        us.onNext(1);
        us.onComplete();
        to.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1);
    }

    @Test
    public void assertValuePredicateEmpty() {
        assertThrows("No values", AssertionError.class, () -> {
            TestObserverEx<Object> to = new TestObserverEx<>();
            Observable.empty().subscribe(to);
            to.assertValue(new Predicate<Object>() {

                @Override
                public boolean test(final Object o) throws Exception {
                    return false;
                }
            });
        });
    }

    @Test
    public void assertValuePredicateMatch() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.just(1).subscribe(to);
        to.assertValue(new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 1;
            }
        });
    }

    @Test
    public void assertValuePredicateNoMatch() {
        assertThrows("Value not present", AssertionError.class, () -> {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1).subscribe(to);
            to.assertValue(new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o != 1;
                }
            });
        });
    }

    @Test
    public void assertValuePredicateMatchButMore() {
        assertThrows("Value present but other values as well", AssertionError.class, () -> {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1, 2).subscribe(to);
            to.assertValue(new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void assertValueAtPredicateEmpty() {
        assertThrows("No values", AssertionError.class, () -> {
            TestObserverEx<Object> to = new TestObserverEx<>();
            Observable.empty().subscribe(to);
            to.assertValueAt(0, new Predicate<Object>() {

                @Override
                public boolean test(final Object o) throws Exception {
                    return false;
                }
            });
        });
    }

    @Test
    public void assertValueAtPredicateMatch() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.just(1, 2).subscribe(to);
        to.assertValueAt(1, new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 2;
            }
        });
    }

    @Test
    public void assertValueAtPredicateNoMatch() {
        assertThrows("Value not present", AssertionError.class, () -> {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1, 2, 3).subscribe(to);
            to.assertValueAt(2, new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o != 3;
                }
            });
        });
    }

    @Test
    public void assertValueAtInvalidIndex() {
        assertThrows("Invalid index: 2 (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1, 2).subscribe(to);
            to.assertValueAt(2, new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void assertValueAtIndexEmpty() {
        assertThrows("No values", AssertionError.class, () -> {
            TestObserverEx<Object> to = new TestObserverEx<>();
            Observable.empty().subscribe(to);
            to.assertValueAt(0, "a");
        });
    }

    @Test
    public void assertValueAtIndexMatch() {
        TestObserverEx<String> to = new TestObserverEx<>();
        Observable.just("a", "b").subscribe(to);
        to.assertValueAt(1, "b");
    }

    @Test
    public void assertValueAtIndexNoMatch() {
        assertThrows("\nexpected: b (class: String)\ngot: c (class: String) (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserverEx<String> to = new TestObserverEx<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueAt(2, "b");
        });
    }

    @Test
    public void assertValueAtIndexInvalidIndex() {
        assertThrows("Invalid index: 2 (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserverEx<String> to = new TestObserverEx<>();
            Observable.just("a", "b").subscribe(to);
            to.assertValueAt(2, "c");
        });
    }

    @Test
    public void withTag() {
        try {
            for (int i = 1; i < 3; i++) {
                Observable.just(i).test().withTag("testing with item=" + i).assertResult(1);
            }
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            assertTrue(ex.toString(), ex.toString().contains("testing with item=2"));
        }
    }

    @Test
    public void assertValuesOnly() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertValuesOnly();
        to.onNext(5);
        to.assertValuesOnly(5);
        to.onNext(-1);
        to.assertValuesOnly(5, -1);
    }

    @Test
    public void assertValuesOnlyThrowsOnUnexpectedValue() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.assertValuesOnly();
        to.onNext(5);
        to.assertValuesOnly(5);
        to.onNext(-1);
        try {
            to.assertValuesOnly(5);
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertValuesOnlyThrowsWhenCompleted() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onComplete();
        try {
            to.assertValuesOnly();
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertValuesOnlyThrowsWhenErrored() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException());
        try {
            to.assertValuesOnly();
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTestObserverEx() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTestObserverEx, this.description("assertTestObserverEx"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNotMatchCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNotMatchCount, this.description("assertNotMatchCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNotMatchValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNotMatchValue, this.description("assertNotMatchValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNeverAtNotMatchingValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNeverAtNotMatchingValue, this.description("assertNeverAtNotMatchingValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNeverAtMatchingValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNeverAtMatchingValue, this.description("assertNeverAtMatchingValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNeverAtMatchingPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNeverAtMatchingPredicate, this.description("assertNeverAtMatchingPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNeverAtNotMatchingPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNeverAtNotMatchingPredicate, this.description("assertNeverAtNotMatchingPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTerminalEventNotReceived() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTerminalEventNotReceived, this.description("assertTerminalEventNotReceived"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrappingMock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::wrappingMock, this.description("wrappingMock"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrappingMockWhenUnsubscribeInvolved() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::wrappingMockWhenUnsubscribeInvolved, this.description("wrappingMockWhenUnsubscribeInvolved"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSwallowed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorSwallowed, this.description("errorSwallowed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullExpected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullExpected, this.description("nullExpected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nullActual, this.description("nullActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_terminalErrorOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::terminalErrorOnce, this.description("terminalErrorOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_terminalCompletedOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::terminalCompletedOnce, this.description("terminalCompletedOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_terminalOneKind() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::terminalOneKind, this.description("terminalOneKind"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createDelegate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createDelegate, this.description("createDelegate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertError, this.description("assertError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObserverEnum() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyObserverEnum, this.description("emptyObserverEnum"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueAndClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::valueAndClass, this.description("valueAndClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertFailure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertFailure, this.description("assertFailure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertFuseable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertFuseable, this.description("assertFuseable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTerminated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTerminated, this.description("assertTerminated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertResult() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertResult, this.description("assertResult"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_await() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::await, this.description("await"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errors, this.description("errors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNext, this.description("onNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionModeToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionModeToString, this.description("fusionModeToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleTerminals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleTerminals, this.description("multipleTerminals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValue, this.description("assertValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextMisbehave() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextMisbehave, this.description("onNextMisbehave"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTerminated2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTerminated2, this.description("assertTerminated2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribe, this.description("onSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueSequence() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueSequence, this.description("assertValueSequence"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertEmpty, this.description("assertEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitDoneTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitDoneTimed, this.description("awaitDoneTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertNotSubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertNotSubscribed, this.description("assertNotSubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertErrorMultiple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertErrorMultiple, this.description("assertErrorMultiple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorInPredicate, this.description("errorInPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertComplete, this.description("assertComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeWithoutOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeWithoutOnSubscribe, this.description("completeWithoutOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeDelegateThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeDelegateThrows, this.description("completeDelegateThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelegateThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDelegateThrows, this.description("errorDelegateThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncQueueThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncQueueThrows, this.description("syncQueueThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncQueueThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncQueueThrows, this.description("asyncQueueThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedMeansDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completedMeansDisposed, this.description("completedMeansDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorMeansDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorMeansDisposed, this.description("errorMeansDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusion() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusion, this.description("asyncFusion"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuePredicateEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuePredicateEmpty, this.description("assertValuePredicateEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuePredicateMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuePredicateMatch, this.description("assertValuePredicateMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuePredicateNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuePredicateNoMatch, this.description("assertValuePredicateNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuePredicateMatchButMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuePredicateMatchButMore, this.description("assertValuePredicateMatchButMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtPredicateEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtPredicateEmpty, this.description("assertValueAtPredicateEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtPredicateMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtPredicateMatch, this.description("assertValueAtPredicateMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtPredicateNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtPredicateNoMatch, this.description("assertValueAtPredicateNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtInvalidIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtInvalidIndex, this.description("assertValueAtInvalidIndex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexEmpty, this.description("assertValueAtIndexEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexMatch, this.description("assertValueAtIndexMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexNoMatch, this.description("assertValueAtIndexNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexInvalidIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexInvalidIndex, this.description("assertValueAtIndexInvalidIndex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTag() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withTag, this.description("withTag"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesOnly() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesOnly, this.description("assertValuesOnly"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesOnlyThrowsOnUnexpectedValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesOnlyThrowsOnUnexpectedValue, this.description("assertValuesOnlyThrowsOnUnexpectedValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesOnlyThrowsWhenCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesOnlyThrowsWhenCompleted, this.description("assertValuesOnlyThrowsWhenCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesOnlyThrowsWhenErrored() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesOnlyThrowsWhenErrored, this.description("assertValuesOnlyThrowsWhenErrored"));
        }

        private TestObserverExTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestObserverExTest();
        }

        @java.lang.Override
        public TestObserverExTest implementation() {
            return this.implementation;
        }
    }
}
