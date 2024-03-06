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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TestSubscriberExTest extends RxJavaTest {

    @Test
    public void assertTestSubscriberEx() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        oi.subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertValueCount(2);
        ts.assertTerminated();
    }

    @Test
    public void assertNotMatchCount() {
        assertThrows(AssertionError.class, () -> {
            Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            oi.subscribe(ts);
            ts.assertValues(1);
            ts.assertValueCount(2);
            ts.assertTerminated();
        });
    }

    @Test
    public void assertNotMatchValue() {
        assertThrows(AssertionError.class, () -> {
            Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            oi.subscribe(ts);
            ts.assertValues(1, 3);
            ts.assertValueCount(2);
            ts.assertTerminated();
        });
    }

    @Test
    public void assertNeverAtNotMatchingValue() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        oi.subscribe(ts);
        ts.assertNever(3);
        ts.assertValueCount(2);
        ts.assertTerminated();
    }

    @Test
    public void assertNeverAtMatchingValue() {
        assertThrows(AssertionError.class, () -> {
            Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            oi.subscribe(ts);
            ts.assertValues(1, 2);
            ts.assertNever(2);
            ts.assertValueCount(2);
            ts.assertTerminated();
        });
    }

    @Test
    public void assertNeverAtMatchingPredicate() {
        assertThrows(AssertionError.class, () -> {
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.just(1, 2).subscribe(ts);
            ts.assertValues(1, 2);
            ts.assertNever(new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void assertNeverAtNotMatchingPredicate() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.just(2, 3).subscribe(ts);
        ts.assertNever(new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 1;
            }
        });
    }

    @Test
    public void assertTerminalEventNotReceived() {
        assertThrows(AssertionError.class, () -> {
            PublishProcessor<Integer> p = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            p.subscribe(ts);
            p.onNext(1);
            p.onNext(2);
            ts.assertValues(1, 2);
            ts.assertValueCount(2);
            ts.assertTerminated();
        });
    }

    @Test
    public void wrappingMock() {
        Flowable<Integer> oi = Flowable.fromIterable(Arrays.asList(1, 2));
        Subscriber<Integer> mockSubscriber = TestHelper.mockSubscriber();
        oi.subscribe(new TestSubscriberEx<>(mockSubscriber));
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
        oi.subscribe(new TestSubscriberEx<>(mockSubscriber));
        InOrder inOrder = inOrder(mockSubscriber);
        inOrder.verify(mockSubscriber, times(1)).onNext(1);
        inOrder.verify(mockSubscriber, times(1)).onNext(2);
        inOrder.verify(mockSubscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void assertError() {
        RuntimeException e = new RuntimeException("Oops");
        TestSubscriberEx<Object> subscriber = new TestSubscriberEx<>();
        Flowable.error(e).subscribe(subscriber);
        subscriber.assertError(e);
    }

    @Test
    public void awaitTerminalEventWithDurationAndUnsubscribeOnTimeout() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        final AtomicBoolean unsub = new AtomicBoolean(false);
        Flowable.just(1).// 
        doOnCancel(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).// 
        delay(1000, TimeUnit.MILLISECONDS).subscribe(ts);
        ts.awaitDone(100, TimeUnit.MILLISECONDS);
        ts.dispose();
        assertTrue(unsub.get());
    }

    @Test(expected = NullPointerException.class)
    public void nullDelegate1() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(null);
        ts.onComplete();
    }

    @Test(expected = NullPointerException.class)
    public void nullDelegate2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(null);
        ts.onComplete();
    }

    @Test(expected = NullPointerException.class)
    public void nullDelegate3() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(null, 0L);
        ts.onComplete();
    }

    @Test
    public void delegate1() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<>();
        ts0.onSubscribe(EmptySubscription.INSTANCE);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(ts0);
        ts.onComplete();
        ts0.assertTerminated();
    }

    @Test
    public void delegate2() {
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>(ts1);
        ts2.onComplete();
        ts1.assertComplete();
    }

    @Test
    public void delegate3() {
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>(ts1, 0L);
        ts2.onComplete();
        ts1.assertComplete();
    }

    @Test
    public void unsubscribed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        assertFalse(ts.isCancelled());
    }

    @Test
    public void noErrors() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new TestException());
        try {
            ts.assertNoErrors();
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Error present but no assertion error!");
    }

    @Test
    public void notCompleted() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertComplete();
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Not completed and no assertion error!");
    }

    @Test
    public void multipleCompletions() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        ts.onComplete();
        try {
            ts.assertComplete();
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Multiple completions and no assertion error!");
    }

    @Test
    public void completed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        try {
            ts.assertNotComplete();
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Completed and no assertion error!");
    }

    @Test
    public void multipleCompletions2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        ts.onComplete();
        try {
            ts.assertNotComplete();
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Multiple completions and no assertion error!");
    }

    @Test
    public void multipleErrors() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(EmptySubscription.INSTANCE);
        ts.onError(new TestException());
        ts.onError(new TestException());
        try {
            ts.assertNoErrors();
        } catch (AssertionError ex) {
            Throwable e = ex.getCause();
            if (!(e instanceof CompositeException)) {
                fail("Multiple Error present but the reported error doesn't have a composite cause!");
            }
            CompositeException ce = (CompositeException) e;
            if (ce.size() != 2) {
                ce.printStackTrace();
            }
            assertEquals(2, ce.size());
            // expected
            return;
        }
        fail("Multiple Error present but no assertion error!");
    }

    @Test
    public void multipleErrors2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(EmptySubscription.INSTANCE);
        ts.onError(new TestException());
        ts.onError(new TestException());
        try {
            ts.assertError(TestException.class);
        } catch (AssertionError ex) {
            Throwable e = ex.getCause();
            if (!(e instanceof CompositeException)) {
                fail("Multiple Error present but the reported error doesn't have a composite cause!");
            }
            CompositeException ce = (CompositeException) e;
            assertEquals(2, ce.size());
            // expected
            return;
        }
        fail("Multiple Error present but no assertion error!");
    }

    @Test
    public void multipleErrors3() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(EmptySubscription.INSTANCE);
        ts.onError(new TestException());
        ts.onError(new TestException());
        try {
            ts.assertError(new TestException());
        } catch (AssertionError ex) {
            Throwable e = ex.getCause();
            if (!(e instanceof CompositeException)) {
                fail("Multiple Error present but the reported error doesn't have a composite cause!");
            }
            CompositeException ce = (CompositeException) e;
            assertEquals(2, ce.size());
            // expected
            return;
        }
        fail("Multiple Error present but no assertion error!");
    }

    @Test
    public void multipleErrors4() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(EmptySubscription.INSTANCE);
        ts.onError(new TestException());
        ts.onError(new TestException());
        try {
            ts.assertError(Functions.<Throwable>alwaysTrue());
        } catch (AssertionError ex) {
            Throwable e = ex.getCause();
            if (!(e instanceof CompositeException)) {
                fail("Multiple Error present but the reported error doesn't have a composite cause!");
            }
            CompositeException ce = (CompositeException) e;
            assertEquals(2, ce.size());
            // expected
            return;
        }
        fail("Multiple Error present but no assertion error!");
    }

    @Test
    public void differentError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new TestException());
        try {
            ts.assertError(new TestException());
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Different Error present but no assertion error!");
    }

    @Test
    public void differentError2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new RuntimeException());
        try {
            ts.assertError(new TestException());
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Different Error present but no assertion error!");
    }

    @Test
    public void differentError3() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new RuntimeException());
        try {
            ts.assertError(TestException.class);
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Different Error present but no assertion error!");
    }

    @Test
    public void differentError4() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new RuntimeException());
        try {
            ts.assertError(Functions.<Throwable>alwaysFalse());
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("Different Error present but no assertion error!");
    }

    @Test
    public void errorInPredicate() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new RuntimeException());
        try {
            ts.assertError(new Predicate<Throwable>() {

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
    public void noError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertError(TestException.class);
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("No present but no assertion error!");
    }

    @Test
    public void noError2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertError(new TestException());
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("No present but no assertion error!");
    }

    @Test
    public void noError3() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertError(Functions.<Throwable>alwaysTrue());
        } catch (AssertionError ex) {
            // expected
            return;
        }
        fail("No present but no assertion error!");
    }

    @Test
    public void interruptTerminalEventAwait() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final Thread t0 = Thread.currentThread();
        Worker w = Schedulers.computation().createWorker();
        try {
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    t0.interrupt();
                }
            }, 200, TimeUnit.MILLISECONDS);
            try {
                if (ts.await(5, TimeUnit.SECONDS)) {
                    fail("Did not interrupt wait!");
                }
            } catch (InterruptedException expected) {
                // expected
            }
        } finally {
            w.dispose();
        }
    }

    @Test
    public void interruptTerminalEventAwaitTimed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final Thread t0 = Thread.currentThread();
        Worker w = Schedulers.computation().createWorker();
        try {
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    t0.interrupt();
                }
            }, 200, TimeUnit.MILLISECONDS);
            try {
                if (ts.await(5, TimeUnit.SECONDS)) {
                    fail("Did not interrupt wait!");
                }
            } catch (InterruptedException expected) {
                // expected
            }
        } finally {
            // clear interrupted flag
            Thread.interrupted();
            w.dispose();
        }
    }

    @Test
    public void interruptTerminalEventAwaitAndUnsubscribe() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final Thread t0 = Thread.currentThread();
        Worker w = Schedulers.computation().createWorker();
        try {
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    t0.interrupt();
                }
            }, 200, TimeUnit.MILLISECONDS);
            try {
                ts.awaitDone(5, TimeUnit.SECONDS);
            } catch (RuntimeException allowed) {
                assertTrue(allowed.toString(), allowed.getCause() instanceof InterruptedException);
            }
            ts.dispose();
            if (!ts.isCancelled()) {
                fail("Did not unsubscribe!");
            }
        } finally {
            w.dispose();
        }
    }

    @Test
    public void noTerminalEventBut1Completed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        try {
            ts.assertNotTerminated();
            throw new RuntimeException("Failed to report there were terminal event(s)!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void noTerminalEventBut1Error() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onError(new TestException());
        try {
            ts.assertNotTerminated();
            throw new RuntimeException("Failed to report there were terminal event(s)!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void noTerminalEventBut1Error1Completed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        ts.onError(new TestException());
        try {
            ts.assertNotTerminated();
            throw new RuntimeException("Failed to report there were terminal event(s)!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void noTerminalEventBut2Errors() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(EmptySubscription.INSTANCE);
        ts.onError(new TestException());
        ts.onError(new TestException());
        try {
            ts.assertNotTerminated();
            throw new RuntimeException("Failed to report there were terminal event(s)!");
        } catch (AssertionError ex) {
            // expected
            Throwable e = ex.getCause();
            if (!(e instanceof CompositeException)) {
                fail("Multiple Error present but the reported error doesn't have a composite cause!");
            }
            CompositeException ce = (CompositeException) e;
            assertEquals(2, ce.size());
        }
    }

    @Test
    public void noValues() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onNext(1);
        try {
            ts.assertNoValues();
            throw new RuntimeException("Failed to report there were values!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void valueCount() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onNext(1);
        ts.onNext(2);
        try {
            ts.assertValueCount(3);
            throw new RuntimeException("Failed to report there were values!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void onCompletedCrashCountsDownLatch() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>() {

            @Override
            public void onComplete() {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(ts0);
        try {
            ts.onComplete();
        } catch (TestException ex) {
            // expected
        }
        ts.awaitDone(5, TimeUnit.SECONDS);
    }

    @Test
    public void onErrorCrashCountsDownLatch() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>() {

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(ts0);
        try {
            ts.onError(new RuntimeException());
        } catch (TestException ex) {
            // expected
        }
        ts.awaitDone(5, TimeUnit.SECONDS);
    }

    @Test
    public void createDelegate() {
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(ts1);
        ts.assertNotSubscribed();
        assertFalse(ts.hasSubscription());
        ts.onSubscribe(new BooleanSubscription());
        try {
            ts.assertNotSubscribed();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        assertTrue(ts.hasSubscription());
        assertFalse(ts.isDisposed());
        ts.onNext(1);
        ts.onError(new TestException());
        ts.onComplete();
        ts1.assertValue(1).assertError(TestException.class).assertComplete();
        ts.dispose();
        assertTrue(ts.isDisposed());
        assertSame(Thread.currentThread(), ts.lastThread());
        try {
            ts.assertNoValues();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertValueCount(0);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        ts.assertValueSequence(Collections.singletonList(1));
        try {
            ts.assertValueSequence(Collections.singletonList(2));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
    }

    @Test
    public void assertError2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertError(TestException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertError(new TestException());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertErrorMessage("");
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertError(Functions.<Throwable>alwaysTrue());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertSubscribed();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        ts.onSubscribe(new BooleanSubscription());
        ts.assertSubscribed();
        ts.assertNoErrors();
        TestException ex = new TestException("Forced failure");
        ts.onError(ex);
        ts.assertError(ex);
        ts.assertError(TestException.class);
        ts.assertErrorMessage("Forced failure");
        ts.assertError(Functions.<Throwable>alwaysTrue());
        ts.assertError(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable t) {
                return t.getMessage() != null && t.getMessage().contains("Forced");
            }
        });
        try {
            ts.assertErrorMessage("");
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertError(new RuntimeException());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertError(IOException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertNoErrors();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        try {
            ts.assertError(Functions.<Throwable>alwaysFalse());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError exc) {
            // expected
        }
        ts.assertTerminated();
        ts.assertValueCount(0);
        ts.assertNoValues();
    }

    @Test
    public void emptyObserverEnum() {
        assertEquals(1, TestSubscriberEx.EmptySubscriber.values().length);
        assertNotNull(TestSubscriberEx.EmptySubscriber.valueOf("INSTANCE"));
    }

    @Test
    public void valueAndClass() {
        assertEquals("null", TestSubscriberEx.valueAndClass(null));
        assertEquals("1 (class: Integer)", TestSubscriberEx.valueAndClass(1));
    }

    @Test
    public void assertFailure() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onError(new TestException("Forced failure"));
        ts.assertFailure(TestException.class);
        ts.assertFailure(Functions.<Throwable>alwaysTrue());
        ts.assertFailureAndMessage(TestException.class, "Forced failure");
        ts.onNext(1);
        ts.assertFailure(TestException.class, 1);
        ts.assertFailure(Functions.<Throwable>alwaysTrue(), 1);
        ts.assertFailureAndMessage(TestException.class, "Forced failure", 1);
    }

    @Test
    public void assertFuseable() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertNotFuseable();
        try {
            ts.assertFuseable();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertFusionMode(QueueFuseable.SYNC);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        ts.onSubscribe(new ScalarSubscription<>(ts, 1));
        ts.assertFuseable();
        ts.assertFusionMode(QueueFuseable.SYNC);
        try {
            ts.assertFusionMode(QueueFuseable.NONE);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertNotFuseable();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertTerminated() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.assertNotTerminated();
        ts.onError(null);
        try {
            ts.assertNotTerminated();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertResult() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onComplete();
        ts.assertResult();
        try {
            ts.assertResult(1);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        ts.onNext(1);
        ts.assertResult(1);
        try {
            ts.assertResult(2);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertResult();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void await() throws Exception {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        assertFalse(ts.await(100, TimeUnit.MILLISECONDS));
        ts.awaitDone(100, TimeUnit.MILLISECONDS);
        assertTrue(ts.isDisposed());
        assertFalse(ts.await(100, TimeUnit.MILLISECONDS));
        ts.assertNotComplete().assertNoErrors();
        ts.onComplete();
        assertTrue(ts.await(100, TimeUnit.MILLISECONDS));
        ts.await();
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertComplete().assertNoErrors();
        assertTrue(ts.await(5, TimeUnit.SECONDS));
        final TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        ts1.onSubscribe(new BooleanSubscription());
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                ts1.onComplete();
            }
        }, 200, TimeUnit.MILLISECONDS);
        ts1.await();
    }

    @Test
    public void errors() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        assertEquals(0, ts.errors().size());
        ts.onError(new TestException());
        assertEquals(1, ts.errors().size());
        TestHelper.assertError(ts.errors(), 0, TestException.class);
    }

    @Test
    public void onNext() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertNoValues();
        assertEquals(Collections.emptyList(), ts.values());
        ts.onNext(1);
        assertEquals(Collections.singletonList(1), ts.values());
        ts.cancel();
        assertTrue(ts.isCancelled());
        assertTrue(ts.isDisposed());
        ts.assertValue(1);
        ts.onComplete();
    }

    @Test
    public void fusionModeToString() {
        assertEquals("NONE", TestSubscriberEx.fusionModeToString(QueueFuseable.NONE));
        assertEquals("SYNC", TestSubscriberEx.fusionModeToString(QueueFuseable.SYNC));
        assertEquals("ASYNC", TestSubscriberEx.fusionModeToString(QueueFuseable.ASYNC));
        assertEquals("Unknown(100)", TestSubscriberEx.fusionModeToString(100));
    }

    @Test
    public void multipleTerminals() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertNotComplete();
        ts.onComplete();
        try {
            ts.assertNotComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        ts.assertTerminated();
        ts.onComplete();
        try {
            ts.assertComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        try {
            ts.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        try {
            ts.assertNotComplete();
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
    }

    @Test
    public void assertValue() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        try {
            ts.assertValue(1);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        ts.onNext(1);
        ts.assertValue(1);
        try {
            ts.assertValue(2);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
        ts.onNext(2);
        try {
            ts.assertValue(1);
            throw new RuntimeException("Should have thrown");
        } catch (Throwable ex) {
            // expected
        }
    }

    @Test
    public void onNextMisbehave() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onNext(1);
        ts.assertError(IllegalStateException.class);
        ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onNext(null);
        ts.assertFailure(NullPointerException.class, (Integer) null);
    }

    @Test
    public void awaitTerminalEventInterrupt() {
        final TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        Thread.currentThread().interrupt();
        try {
            ts.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException allowed) {
            assertTrue(allowed.toString(), allowed.getCause() instanceof InterruptedException);
        }
        // FIXME ? catch consumes this flag
        // assertTrue(Thread.interrupted());
        Thread.currentThread().interrupt();
        try {
            ts.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException allowed) {
            assertTrue(allowed.toString(), allowed.getCause() instanceof InterruptedException);
        }
        // FIXME ? catch consumes this flag
        // assertTrue(Thread.interrupted());
    }

    @Test
    public void assertTerminated2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertNotTerminated();
        ts.onError(new TestException());
        ts.assertTerminated();
        ts.onError(new IOException());
        try {
            ts.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertError(TestException.class);
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onError(new TestException());
        ts.onComplete();
        try {
            ts.assertTerminated();
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void onSubscribe() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(null);
        ts.assertError(NullPointerException.class);
        ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        BooleanSubscription bs1 = new BooleanSubscription();
        ts.onSubscribe(bs1);
        assertTrue(bs1.isCancelled());
        ts.assertError(IllegalStateException.class);
        ts = new TestSubscriberEx<>();
        ts.dispose();
        bs1 = new BooleanSubscription();
        ts.onSubscribe(bs1);
        assertTrue(bs1.isCancelled());
    }

    @Test
    public void assertValueSequence() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onNext(1);
        ts.onNext(2);
        try {
            ts.assertValueSequence(Collections.<Integer>emptyList());
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertValueSequence(Collections.singletonList(1));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
        ts.assertValueSequence(Arrays.asList(1, 2));
        try {
            ts.assertValueSequence(Arrays.asList(1, 2, 3));
            throw new RuntimeException("Should have thrown");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertEmpty() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        try {
            ts.assertEmpty();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        ts.onSubscribe(new BooleanSubscription());
        ts.assertEmpty();
        ts.onNext(1);
        try {
            ts.assertEmpty();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void awaitDoneTimed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Thread.currentThread().interrupt();
        try {
            ts.awaitDone(5, TimeUnit.SECONDS);
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void assertNotSubscribed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.assertNotSubscribed();
        ts.errors().add(new TestException());
        try {
            ts.assertNotSubscribed();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertErrorMultiple() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        TestException e = new TestException();
        ts.errors().add(e);
        ts.errors().add(new TestException());
        try {
            ts.assertError(TestException.class);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertError(e);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        try {
            ts.assertErrorMessage("");
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertComplete() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        try {
            ts.assertComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
        ts.onComplete();
        ts.assertComplete();
        ts.onComplete();
        try {
            ts.assertComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void completeWithoutOnSubscribe() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onComplete();
        ts.assertError(IllegalStateException.class);
    }

    @Test
    public void completeDelegateThrows() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
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
        ts.onSubscribe(new BooleanSubscription());
        try {
            ts.onComplete();
            throw new RuntimeException("Should have thrown!");
        } catch (TestException ex) {
            ts.assertTerminated();
        }
    }

    @Test
    public void errorDelegateThrows() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
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
        ts.onSubscribe(new BooleanSubscription());
        try {
            ts.onError(new IOException());
            throw new RuntimeException("Should have thrown!");
        } catch (TestException ex) {
            ts.assertTerminated();
        }
    }

    @Test
    public void syncQueueThrows() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).subscribe(ts);
        ts.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertFailure(TestException.class);
    }

    @Test
    public void asyncQueueThrows() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).subscribe(ts);
        up.onNext(1);
        ts.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertFailure(TestException.class);
    }

    @Test
    public void assertValuePredicateEmpty() {
        assertThrows("No values", AssertionError.class, () -> {
            TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
            Flowable.empty().subscribe(ts);
            ts.assertValue(new Predicate<Object>() {

                @Override
                public boolean test(final Object o) throws Exception {
                    return false;
                }
            });
        });
    }

    @Test
    public void assertValuePredicateMatch() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.just(1).subscribe(ts);
        ts.assertValue(new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 1;
            }
        });
    }

    @Test
    public void assertValuePredicateNoMatch() {
        assertThrows("Value not present", AssertionError.class, () -> {
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.just(1).subscribe(ts);
            ts.assertValue(new Predicate<Integer>() {

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
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.just(1, 2).subscribe(ts);
            ts.assertValue(new Predicate<Integer>() {

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
            TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
            Flowable.empty().subscribe(ts);
            ts.assertValueAt(0, new Predicate<Object>() {

                @Override
                public boolean test(final Object o) throws Exception {
                    return false;
                }
            });
        });
    }

    @Test
    public void assertValueAtPredicateMatch() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.just(1, 2).subscribe(ts);
        ts.assertValueAt(1, new Predicate<Integer>() {

            @Override
            public boolean test(final Integer o) throws Exception {
                return o == 2;
            }
        });
    }

    @Test
    public void assertValueAtPredicateNoMatch() {
        assertThrows("Value not present", AssertionError.class, () -> {
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.just(1, 2, 3).subscribe(ts);
            ts.assertValueAt(2, new Predicate<Integer>() {

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
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.just(1, 2).subscribe(ts);
            ts.assertValueAt(2, new Predicate<Integer>() {

                @Override
                public boolean test(final Integer o) throws Exception {
                    return o == 1;
                }
            });
        });
    }

    @Test
    public void requestMore() {
        Flowable.range(1, 5).test(0).requestMore(1).assertValue(1).requestMore(2).assertValues(1, 2, 3).requestMore(3).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void withTag() {
        try {
            for (int i = 1; i < 3; i++) {
                Flowable.just(i).test().withTag("testing with item=" + i).assertResult(1);
            }
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            assertTrue(ex.toString(), ex.toString().contains("testing with item=2"));
        }
    }

    @Test
    public void timeoutIndicated() throws InterruptedException {
        // clear flag
        Thread.interrupted();
        TestSubscriberEx<Object> ts = Flowable.never().to(TestHelper.<Object>testConsumer());
        assertFalse(ts.await(1, TimeUnit.MILLISECONDS));
        try {
            ts.assertResult(1);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            assertTrue(ex.toString(), ex.toString().contains("timeout!"));
        }
    }

    @Test
    public void timeoutIndicated2() throws InterruptedException {
        try {
            Flowable.never().test().awaitDone(1, TimeUnit.MILLISECONDS).assertResult(1);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            assertTrue(ex.toString(), ex.toString().contains("timeout!"));
        }
    }

    @Test
    public void timeoutIndicated3() throws InterruptedException {
        TestSubscriberEx<Object> ts = Flowable.never().to(TestHelper.<Object>testConsumer());
        assertFalse(ts.await(1, TimeUnit.MILLISECONDS));
        try {
            ts.assertResult(1);
            throw new RuntimeException("Should have thrown!");
        } catch (AssertionError ex) {
            assertTrue(ex.toString(), ex.toString().contains("timeout!"));
        }
    }

    @Test
    public void disposeIndicated() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.cancel();
        try {
            ts.assertResult(1);
            throw new RuntimeException("Should have thrown!");
        } catch (Throwable ex) {
            assertTrue(ex.toString(), ex.toString().contains("disposed!"));
        }
    }

    @Test
    public void awaitCount() {
        Flowable.range(1, 10).delay(100, TimeUnit.MILLISECONDS).test(5).awaitCount(5).assertValues(1, 2, 3, 4, 5).requestMore(5).awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void awaitCountLess() {
        Flowable.range(1, 4).test().awaitCount(5).assertResult(1, 2, 3, 4);
    }

    @Test
    public void assertValueAtPredicateThrows() {
        try {
            Flowable.just(1).test().assertValueAt(0, new Predicate<Integer>() {

                @Override
                public boolean test(Integer t) throws Exception {
                    throw new IllegalArgumentException();
                }
            });
            throw new RuntimeException("Should have thrown!");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void assertValuesOnly() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertValuesOnly();
        ts.onNext(5);
        ts.assertValuesOnly(5);
        ts.onNext(-1);
        ts.assertValuesOnly(5, -1);
    }

    @Test
    public void assertValuesOnlyThrowsOnUnexpectedValue() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.assertValuesOnly();
        ts.onNext(5);
        ts.assertValuesOnly(5);
        ts.onNext(-1);
        try {
            ts.assertValuesOnly(5);
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertValuesOnlyThrowsWhenCompleted() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onComplete();
        try {
            ts.assertValuesOnly();
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @Test
    public void assertValuesOnlyThrowsWhenErrored() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.onSubscribe(new BooleanSubscription());
        ts.onError(new TestException());
        try {
            ts.assertValuesOnly();
            throw new RuntimeException();
        } catch (AssertionError ex) {
            // expected
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertTestSubscriberEx() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertTestSubscriberEx, this.description("assertTestSubscriberEx"));
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
        public void benchmark_assertError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertError, this.description("assertError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitTerminalEventWithDurationAndUnsubscribeOnTimeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitTerminalEventWithDurationAndUnsubscribeOnTimeout, this.description("awaitTerminalEventWithDurationAndUnsubscribeOnTimeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullDelegate1() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::nullDelegate1, this.description("nullDelegate1"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullDelegate2() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::nullDelegate2, this.description("nullDelegate2"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullDelegate3() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::nullDelegate3, this.description("nullDelegate3"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegate1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegate1, this.description("delegate1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegate2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegate2, this.description("delegate2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegate3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegate3, this.description("delegate3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribed, this.description("unsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noErrors, this.description("noErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::notCompleted, this.description("notCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleCompletions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleCompletions, this.description("multipleCompletions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completed, this.description("completed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleCompletions2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleCompletions2, this.description("multipleCompletions2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleErrors, this.description("multipleErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleErrors2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleErrors2, this.description("multipleErrors2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleErrors3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleErrors3, this.description("multipleErrors3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleErrors4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multipleErrors4, this.description("multipleErrors4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_differentError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::differentError, this.description("differentError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_differentError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::differentError2, this.description("differentError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_differentError3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::differentError3, this.description("differentError3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_differentError4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::differentError4, this.description("differentError4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorInPredicate, this.description("errorInPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noError, this.description("noError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noError2, this.description("noError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noError3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noError3, this.description("noError3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptTerminalEventAwait() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptTerminalEventAwait, this.description("interruptTerminalEventAwait"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptTerminalEventAwaitTimed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptTerminalEventAwaitTimed, this.description("interruptTerminalEventAwaitTimed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptTerminalEventAwaitAndUnsubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interruptTerminalEventAwaitAndUnsubscribe, this.description("interruptTerminalEventAwaitAndUnsubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noTerminalEventBut1Completed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noTerminalEventBut1Completed, this.description("noTerminalEventBut1Completed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noTerminalEventBut1Error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noTerminalEventBut1Error, this.description("noTerminalEventBut1Error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noTerminalEventBut1Error1Completed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noTerminalEventBut1Error1Completed, this.description("noTerminalEventBut1Error1Completed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noTerminalEventBut2Errors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noTerminalEventBut2Errors, this.description("noTerminalEventBut2Errors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noValues() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noValues, this.description("noValues"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::valueCount, this.description("valueCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompletedCrashCountsDownLatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompletedCrashCountsDownLatch, this.description("onCompletedCrashCountsDownLatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrashCountsDownLatch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCrashCountsDownLatch, this.description("onErrorCrashCountsDownLatch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createDelegate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createDelegate, this.description("createDelegate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertError2, this.description("assertError2"));
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
        public void benchmark_requestMore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestMore, this.description("requestMore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTag() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withTag, this.description("withTag"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutIndicated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutIndicated, this.description("timeoutIndicated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutIndicated2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutIndicated2, this.description("timeoutIndicated2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutIndicated3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutIndicated3, this.description("timeoutIndicated3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeIndicated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeIndicated, this.description("disposeIndicated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitCount, this.description("awaitCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitCountLess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::awaitCountLess, this.description("awaitCountLess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_assertValueAtPredicateThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::assertValueAtPredicateThrows, this.description("assertValueAtPredicateThrows"));
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

        private TestSubscriberExTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestSubscriberExTest();
        }

        @java.lang.Override
        public TestSubscriberExTest implementation() {
            return this.implementation;
        }
    }
}
