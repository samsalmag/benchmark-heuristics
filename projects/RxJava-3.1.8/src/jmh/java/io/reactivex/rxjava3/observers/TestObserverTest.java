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
package io.reactivex.rxjava3.observers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class TestObserverTest extends RxJavaTest {

    static void assertThrowsWithMessage(String message, Class<? extends Throwable> clazz, ThrowingRunnable run) {
        assertEquals(message, assertThrows(clazz, run).getMessage());
    }

    static void assertThrowsWithMessageMatchRegex(String regex, Class<? extends Throwable> clazz, ThrowingRunnable run) {
        assertTrue(assertThrows(clazz, run).getMessage().matches(regex));
    }

    private static final String ASSERT_MESSAGE_REGEX = "\nexpected: (.*)\n\\s*got: (.*)";

    @Test
    public void assertTestObserver() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        oi.subscribe(subscriber);
        subscriber.assertValues(1, 2);
        subscriber.assertValueCount(2);
        subscriber.assertComplete().assertNoErrors();
    }

    @Test
    public void assertNotMatchCount() {
        assertThrows(AssertionError.class, () -> {
            Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
            TestSubscriber<Integer> subscriber = new TestSubscriber<>();
            oi.subscribe(subscriber);
            subscriber.assertValue(1);
            subscriber.assertValueCount(2);
            subscriber.assertComplete().assertNoErrors();
        });
    }

    @Test
    public void assertNotMatchValue() {
        assertThrows(AssertionError.class, () -> {
            Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
            TestSubscriber<Integer> subscriber = new TestSubscriber<>();
            oi.subscribe(subscriber);
            subscriber.assertValues(1, 3);
            subscriber.assertValueCount(2);
            subscriber.assertComplete().assertNoErrors();
        });
    }

    @Test
    public void assertTerminalEventNotReceived() {
        assertThrows(AssertionError.class, () -> {
            PublishProcessor<Integer> p = PublishProcessor.create();
            TestSubscriber<Integer> subscriber = new TestSubscriber<>();
            p.subscribe(subscriber);
            p.onNext(1);
            p.onNext(2);
            subscriber.assertValues(1, 2);
            subscriber.assertValueCount(2);
            subscriber.assertComplete().assertNoErrors();
        });
    }

    @Test
    public void wrappingMock() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
        Subscriber<Integer> mockSubscriber = TestHelper.mockSubscriber();
        oi.subscribe(new TestSubscriber<>(mockSubscriber));
        InOrder inOrder = inOrder(mockSubscriber);
        inOrder.verify(mockSubscriber, times(1)).onNext(1);
        inOrder.verify(mockSubscriber, times(1)).onNext(2);
        inOrder.verify(mockSubscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void wrappingMockWhenUnsubscribeInvolved() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(2);
        Subscriber<Integer> mockSubscriber = TestHelper.mockSubscriber();
        oi.subscribe(new TestSubscriber<>(mockSubscriber));
        InOrder inOrder = inOrder(mockSubscriber);
        inOrder.verify(mockSubscriber, times(1)).onNext(1);
        inOrder.verify(mockSubscriber, times(1)).onNext(2);
        inOrder.verify(mockSubscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void errorSwallowed() {
        Flowable.error(new RuntimeException()).subscribe(new TestSubscriber<>());
    }

    @Test
    public void nullExpected() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onNext(1);
        try {
            ts.assertValue((Integer) null);
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Null element check assertion didn't happen!");
    }

    @Test
    public void nullActual() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onNext(null);
        try {
            ts.assertValue(1);
        } catch (AssertionError ex) {
            // this is expected
            return;
        }
        fail("Null element check assertion didn't happen!");
    }

    @Test
    public void createDelegate() {
        TestObserver<Integer> to1 = TestObserver.create();
        TestObserver<Integer> to = TestObserver.create(to1);
        assertFalse(to.hasSubscription());
        to.onSubscribe(Disposable.empty());
        assertTrue(to.hasSubscription());
        assertFalse(to.isDisposed());
        to.onNext(1);
        to.onError(new TestException());
        to.onComplete();
        to1.assertValue(1).assertError(TestException.class).assertComplete();
        to.dispose();
        assertTrue(to.isDisposed());
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
        TestObserver<Integer> to = TestObserver.create();
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
            to.assertSubscribed();
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
        to.assertValueCount(0);
        to.assertNoValues();
    }

    @Test
    public void emptyObserverEnum() {
        assertEquals(1, TestObserver.EmptyObserver.values().length);
        assertNotNull(TestObserver.EmptyObserver.valueOf("INSTANCE"));
    }

    @Test
    public void valueAndClass() {
        assertEquals("null", TestObserver.valueAndClass(null));
        assertEquals("1 (class: Integer)", TestObserver.valueAndClass(1));
    }

    @Test
    public void assertFailure() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException("Forced failure"));
        to.assertFailure(TestException.class);
        to.onNext(1);
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void assertFuseable() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to = TestObserver.create();
        to.onSubscribe(new ScalarDisposable<>(to, 1));
    }

    @Test
    public void assertResult() {
        TestObserver<Integer> to = TestObserver.create();
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
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        assertFalse(to.await(100, TimeUnit.MILLISECONDS));
        to.awaitDone(100, TimeUnit.MILLISECONDS);
        assertTrue(to.isDisposed());
        assertFalse(to.await(100, TimeUnit.MILLISECONDS));
        to.assertNotComplete();
        to.assertNoErrors();
        to.onComplete();
        assertTrue(to.await(100, TimeUnit.MILLISECONDS));
        to.await();
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertComplete();
        to.assertNoErrors();
        assertTrue(to.await(5, TimeUnit.SECONDS));
        final TestObserver<Integer> to1 = TestObserver.create();
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
    public void onNext() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        assertEquals(0, to.values().size());
        assertEquals(Collections.emptyList(), to.values());
        to.onNext(1);
        assertEquals(Collections.singletonList(1), to.values());
        to.dispose();
        assertTrue(to.isDisposed());
        to.assertValue(1);
        to.onComplete();
    }

    @Test
    public void multipleTerminals() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.assertNotComplete();
        to.onComplete();
        try {
            to.assertNotComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        to.onComplete();
        try {
            to.assertComplete();
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
        TestObserver<Integer> to = TestObserver.create();
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
        TestObserver<Integer> to = TestObserver.create();
        to.onNext(1);
        to.assertError(IllegalStateException.class);
        to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onNext(null);
        to.assertFailure(NullPointerException.class, (Integer) null);
    }

    @Test
    public void awaitTerminalEventInterrupt() {
        final TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        Thread.currentThread().interrupt();
        try {
            to.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException allowed) {
            assertTrue(allowed.toString(), allowed.getCause() instanceof InterruptedException);
        }
        // FIXME ? catch consumes this flag
        // assertTrue(Thread.interrupted());
        Thread.currentThread().interrupt();
        try {
            to.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException allowed) {
            assertTrue(allowed.toString(), allowed.getCause() instanceof InterruptedException);
        }
        // FIXME ? catch consumes this flag
        // assertTrue(Thread.interrupted());
    }

    @Test
    public void assertTerminated2() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException());
        to.onError(new IOException());
        try {
            to.assertError(TestException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException());
        to.onComplete();
    }

    @Test
    public void onSubscribe() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(null);
        to.assertError(NullPointerException.class);
        to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        Disposable d1 = Disposable.empty();
        to.onSubscribe(d1);
        assertTrue(d1.isDisposed());
        to.assertError(IllegalStateException.class);
        to = TestObserver.create();
        to.dispose();
        d1 = Disposable.empty();
        to.onSubscribe(d1);
        assertTrue(d1.isDisposed());
    }

    @Test
    public void assertValueSequence() {
        TestObserver<Integer> to = TestObserver.create();
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
        TestObserver<Integer> to = new TestObserver<>();
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
        TestObserver<Integer> to = new TestObserver<>();
        Thread.currentThread().interrupt();
        try {
            to.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void assertErrorMultiple() {
        TestObserver<Integer> to = new TestObserver<>();
        TestException e = new TestException();
        to.onError(e);
        to.onError(new TestException());
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
    }

    @Test
    public void errorInPredicate() {
        TestObserver<Object> to = new TestObserver<>();
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
        TestObserver<Integer> to = new TestObserver<>();
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
        TestObserver<Integer> to = new TestObserver<>();
        to.onComplete();
        to.assertError(IllegalStateException.class);
    }

    @Test
    public void completeDelegateThrows() {
        TestObserver<Integer> to = new TestObserver<>(new Observer<Integer>() {

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
            to.assertComplete().assertNoErrors();
        }
    }

    @Test
    public void errorDelegateThrows() {
        TestObserver<Integer> to = new TestObserver<>(new Observer<Integer>() {

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
            to.assertNotComplete().assertError(Throwable.class);
        }
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
    public void assertValuePredicateEmpty() {
        assertThrowsWithMessage("No values (latch = 0, values = 0, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Object> to = new TestObserver<>();
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
        TestObserver<Integer> to = new TestObserver<>();
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
        assertThrowsWithMessage("Value 1 (class: Integer) at position 0 did not pass the predicate (latch = 0, values = 1, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Integer> to = new TestObserver<>();
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
        assertThrowsWithMessage("The first value passed the predicate but this consumer received more than one value (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Integer> to = new TestObserver<>();
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
        assertThrowsWithMessage("No values (latch = 0, values = 0, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Object> to = new TestObserver<>();
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
        TestObserver<Integer> to = new TestObserver<>();
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
        assertThrowsWithMessage("Value 3 (class: Integer) at position 2 did not pass the predicate (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Integer> to = new TestObserver<>();
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
        assertThrowsWithMessage("Index 2 is out of range [0, 2) (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Integer> to = new TestObserver<>();
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
    public void assertValueAtInvalidIndexNegative() {
        assertThrowsWithMessage("Index -2 is out of range [0, 2) (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Integer> to = new TestObserver<>();
            Observable.just(1, 2).subscribe(to);
            to.assertValueAt(-2, new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void assertValueAtIndexEmpty() {
        assertThrowsWithMessage("No values (latch = 0, values = 0, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<Object> to = new TestObserver<>();
            Observable.empty().subscribe(to);
            to.assertValueAt(0, "a");
        });
    }

    @Test
    public void assertValueAtIndexMatch() {
        TestObserver<String> to = new TestObserver<>();
        Observable.just("a", "b").subscribe(to);
        to.assertValueAt(1, "b");
    }

    @Test
    public void assertValueAtIndexNoMatch() {
        assertThrowsWithMessage("\nexpected: b (class: String)\ngot: c (class: String); Value at position 2 differ (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueAt(2, "b");
        });
    }

    @Test
    public void assertValueAtIndexThrowsMessageMatchRegex() {
        assertThrowsWithMessageMatchRegex(ASSERT_MESSAGE_REGEX, AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueAt(2, "b");
        });
    }

    @Test
    public void assertValuesCountNoMatch() {
        assertThrowsWithMessage("\nexpected: 2 [a, b]\ngot: 3 [a, b, c]; Value count differs (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValues("a", "b");
        });
    }

    @Test
    public void assertValuesCountThrowsMessageMatchRegex() {
        assertThrowsWithMessageMatchRegex(ASSERT_MESSAGE_REGEX, AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValues("a", "b");
        });
    }

    @Test
    public void assertValuesNoMatch() {
        assertThrowsWithMessage("\nexpected: d (class: String)\ngot: c (class: String); Value at position 2 differ (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValues("a", "b", "d");
        });
    }

    @Test
    public void assertValuesThrowsMessageMatchRegex() {
        assertThrowsWithMessageMatchRegex(ASSERT_MESSAGE_REGEX, AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValues("a", "b", "d");
        });
    }

    @Test
    public void assertValueCountNoMatch() {
        assertThrowsWithMessage("\nexpected: 2\ngot: 3; Value counts differ (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueCount(2);
        });
    }

    @Test
    public void assertValueCountThrowsMessageMatchRegex() {
        assertThrowsWithMessageMatchRegex(ASSERT_MESSAGE_REGEX, AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueCount(2);
        });
    }

    @Test
    public void assertValueSequenceNoMatch() {
        assertThrowsWithMessage("\nexpected: d (class: String)\ngot: c (class: String); Value at position 2 differ (latch = 0, values = 3, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueSequence(Arrays.asList("a", "b", "d"));
        });
    }

    @Test
    public void assertValueSequenceThrowsMessageMatchRegex() {
        assertThrowsWithMessageMatchRegex(ASSERT_MESSAGE_REGEX, AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b", "c").subscribe(to);
            to.assertValueSequence(Arrays.asList("a", "b", "d"));
        });
    }

    @Test
    public void assertValueAtIndexInvalidIndex() {
        assertThrowsWithMessage("Index 2 is out of range [0, 2) (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b").subscribe(to);
            to.assertValueAt(2, "c");
        });
    }

    @Test
    public void assertValueAtIndexInvalidIndexNegative() {
        assertThrowsWithMessage("Index -2 is out of range [0, 2) (latch = 0, values = 2, errors = 0, completions = 1)", AssertionError.class, () -> {
            TestObserver<String> to = new TestObserver<>();
            Observable.just("a", "b").subscribe(to);
            to.assertValueAt(-2, "c");
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
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.assertValuesOnly();
        to.onNext(5);
        to.assertValuesOnly(5);
        to.onNext(-1);
        to.assertValuesOnly(5, -1);
    }

    @Test
    public void assertValuesOnlyThrowsOnUnexpectedValue() {
        TestObserver<Integer> to = TestObserver.create();
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
        TestObserver<Integer> to = TestObserver.create();
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
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onError(new TestException());
        try {
            to.assertValuesOnly();
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void onErrorIsNull() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.onError(null);
        to.assertFailure(NullPointerException.class);
    }

    @Test
    public void awaitCountTimeout() {
        TestObserver<Integer> to = TestObserver.create();
        to.onSubscribe(Disposable.empty());
        to.awaitCount(1);
        assertTrue(to.timeout);
    }

    @Test(expected = RuntimeException.class)
    public void awaitCountInterrupted() {
        try {
            TestObserver<Integer> to = TestObserver.create();
            to.onSubscribe(Disposable.empty());
            Thread.currentThread().interrupt();
            to.awaitCount(1);
        } finally {
            Thread.interrupted();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTestObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTestObserver, this.description("assertTestObserver"));
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
        public void benchmark_onNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNext, this.description("onNext"));
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
        public void benchmark_awaitTerminalEventInterrupt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitTerminalEventInterrupt, this.description("awaitTerminalEventInterrupt"));
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
        public void benchmark_assertValueAtInvalidIndexNegative() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtInvalidIndexNegative, this.description("assertValueAtInvalidIndexNegative"));
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
        public void benchmark_assertValueAtIndexThrowsMessageMatchRegex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexThrowsMessageMatchRegex, this.description("assertValueAtIndexThrowsMessageMatchRegex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesCountNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesCountNoMatch, this.description("assertValuesCountNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesCountThrowsMessageMatchRegex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesCountThrowsMessageMatchRegex, this.description("assertValuesCountThrowsMessageMatchRegex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesNoMatch, this.description("assertValuesNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValuesThrowsMessageMatchRegex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValuesThrowsMessageMatchRegex, this.description("assertValuesThrowsMessageMatchRegex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueCountNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueCountNoMatch, this.description("assertValueCountNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueCountThrowsMessageMatchRegex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueCountThrowsMessageMatchRegex, this.description("assertValueCountThrowsMessageMatchRegex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueSequenceNoMatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueSequenceNoMatch, this.description("assertValueSequenceNoMatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueSequenceThrowsMessageMatchRegex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueSequenceThrowsMessageMatchRegex, this.description("assertValueSequenceThrowsMessageMatchRegex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexInvalidIndex() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexInvalidIndex, this.description("assertValueAtIndexInvalidIndex"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtIndexInvalidIndexNegative() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtIndexInvalidIndexNegative, this.description("assertValueAtIndexInvalidIndexNegative"));
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorIsNull, this.description("onErrorIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitCountTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitCountTimeout, this.description("awaitCountTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitCountInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::awaitCountInterrupted, this.description("awaitCountInterrupted"), java.lang.RuntimeException.class);
        }

        private TestObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestObserverTest();
        }

        @java.lang.Override
        public TestObserverTest implementation() {
            return this.implementation;
        }
    }
}
