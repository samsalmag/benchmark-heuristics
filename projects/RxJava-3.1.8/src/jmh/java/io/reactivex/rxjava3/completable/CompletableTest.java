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
package io.reactivex.rxjava3.completable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.disposables.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

/**
 * Test Completable methods and operators.
 */
public class CompletableTest extends RxJavaTest {

    /**
     * Iterable that returns an Iterator that throws in its hasNext method.
     */
    static final class IterableIteratorNextThrows implements Iterable<Completable> {

        @Override
        public Iterator<Completable> iterator() {
            return new Iterator<Completable>() {

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Completable next() {
                    throw new TestException();
                }

                @Override
                public void remove() {
                }
            };
        }
    }

    /**
     * Iterable that returns an Iterator that throws in its next method.
     */
    static final class IterableIteratorHasNextThrows implements Iterable<Completable> {

        @Override
        public Iterator<Completable> iterator() {
            return new Iterator<Completable>() {

                @Override
                public boolean hasNext() {
                    throw new TestException();
                }

                @Override
                public Completable next() {
                    return null;
                }

                @Override
                public void remove() {
                }
            };
        }
    }

    /**
     * A class containing a completable instance and counts the number of subscribers.
     */
    static final class NormalCompletable extends AtomicInteger {

        private static final long serialVersionUID = 7192337844700923752L;

        public final Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                getAndIncrement();
                EmptyDisposable.complete(observer);
            }
        });

        /**
         * Asserts the given number of subscriptions happened.
         * @param n the expected number of subscriptions
         */
        public void assertSubscriptions(int n) {
            Assert.assertEquals(n, get());
        }
    }

    /**
     * A class containing a completable instance that emits a TestException and counts
     * the number of subscribers.
     */
    static final class ErrorCompletable extends AtomicInteger {

        private static final long serialVersionUID = 7192337844700923752L;

        public final Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                getAndIncrement();
                EmptyDisposable.error(new TestException(), observer);
            }
        });

        /**
         * Asserts the given number of subscriptions happened.
         * @param n the expected number of subscriptions
         */
        public void assertSubscriptions(int n) {
            Assert.assertEquals(n, get());
        }
    }

    /**
     * A normal Completable object.
     */
    final NormalCompletable normal = new NormalCompletable();

    /**
     * An error Completable object.
     */
    final ErrorCompletable error = new ErrorCompletable();

    @Test
    public void complete() {
        Completable c = Completable.complete();
        c.blockingAwait();
    }

    @Test
    public void concatEmpty() {
        Completable c = Completable.concatArray();
        c.blockingAwait();
    }

    @Test
    public void concatSingleSource() {
        Completable c = Completable.concatArray(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void concatSingleSourceThrows() {
        Completable c = Completable.concatArray(error.completable);
        c.blockingAwait();
    }

    @Test
    public void concatMultipleSources() {
        Completable c = Completable.concatArray(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatMultipleOneThrows() {
        Completable c = Completable.concatArray(normal.completable, error.completable, normal.completable);
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void concatMultipleOneIsNull() {
        Completable c = Completable.concatArray(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void concatIterableEmpty() {
        Completable c = Completable.concat(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Completable c = Completable.concat(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void concatIterableSingle() {
        Completable c = Completable.concat(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void concatIterableMany() {
        Completable c = Completable.concat(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatIterableOneThrows() {
        Completable c = Completable.concat(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableManyOneThrows() {
        Completable c = Completable.concat(Arrays.asList(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIterableThrows() {
        Completable c = Completable.concat(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIteratorHasNextThrows() {
        Completable c = Completable.concat(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIteratorNextThrows() {
        Completable c = Completable.concat(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void concatObservableEmpty() {
        Completable c = Completable.concat(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatObservableError() {
        Completable c = Completable.concat(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void concatObservableSingle() {
        Completable c = Completable.concat(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void concatObservableSingleThrows() {
        Completable c = Completable.concat(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void concatObservableMany() {
        Completable c = Completable.concat(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatObservableManyOneThrows() {
        Completable c = Completable.concat(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void concatObservablePrefetch() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.concat(cs, 5);
        c.blockingAwait();
        Assert.assertEquals(Arrays.asList(5L, 4L, 4L), requested);
    }

    @Test(expected = NullPointerException.class)
    public void createOnSubscribeThrowsNPE() {
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                throw new NullPointerException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void createOnSubscribeThrowsRuntimeException() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable c = Completable.unsafeCreate(new CompletableSource() {

                @Override
                public void subscribe(CompletableObserver observer) {
                    throw new TestException();
                }
            });
            c.blockingAwait();
            Assert.fail("Did not throw exception");
        } catch (NullPointerException ex) {
            if (!(ex.getCause() instanceof TestException)) {
                ex.printStackTrace();
                Assert.fail("Did not wrap the TestException but it returned: " + ex);
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void defer() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return normal.completable;
            }
        });
        normal.assertSubscriptions(0);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = NullPointerException.class)
    public void deferReturnsNull() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void deferFunctionThrows() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void deferErrorSource() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return error.completable;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorSupplierNormal() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void errorSupplierReturnsNull() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorSupplierThrows() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorNormal() {
        Completable c = Completable.error(new TestException());
        c.blockingAwait();
    }

    @Test
    public void fromCallableNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromCallableThrows() {
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void fromFlowableEmpty() {
        Completable c = Completable.fromPublisher(Flowable.empty());
        c.blockingAwait();
    }

    @Test
    public void fromFlowableSome() {
        for (int n = 1; n < 10000; n *= 10) {
            Completable c = Completable.fromPublisher(Flowable.range(1, n));
            c.blockingAwait();
        }
    }

    @Test(expected = TestException.class)
    public void fromFlowableError() {
        Completable c = Completable.fromPublisher(Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void fromObservableEmpty() {
        Completable c = Completable.fromObservable(Observable.empty());
        c.blockingAwait();
    }

    @Test
    public void fromObservableSome() {
        for (int n = 1; n < 10000; n *= 10) {
            Completable c = Completable.fromObservable(Observable.range(1, n));
            c.blockingAwait();
        }
    }

    @Test(expected = TestException.class)
    public void fromObservableError() {
        Completable c = Completable.fromObservable(Observable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void fromActionNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromActionThrows() {
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void fromSingleNormal() {
        Completable c = Completable.fromSingle(Single.just(1));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void fromSingleThrows() {
        Completable c = Completable.fromSingle(Single.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeEmpty() {
        Completable c = Completable.mergeArray();
        c.blockingAwait();
    }

    @Test
    public void mergeSingleSource() {
        Completable c = Completable.mergeArray(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeSingleSourceThrows() {
        Completable c = Completable.mergeArray(error.completable);
        c.blockingAwait();
    }

    @Test
    public void mergeMultipleSources() {
        Completable c = Completable.mergeArray(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeMultipleOneThrows() {
        Completable c = Completable.mergeArray(normal.completable, error.completable, normal.completable);
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeMultipleOneIsNull() {
        Completable c = Completable.mergeArray(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void mergeIterableEmpty() {
        Completable c = Completable.merge(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableIteratorNull() {
        Completable c = Completable.merge(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void mergeIterableSingle() {
        Completable c = Completable.merge(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void mergeIterableMany() {
        Completable c = Completable.merge(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeIterableOneThrows() {
        Completable c = Completable.merge(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableManyOneThrows() {
        Completable c = Completable.merge(Arrays.asList(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIterableThrows() {
        Completable c = Completable.merge(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIteratorHasNextThrows() {
        Completable c = Completable.merge(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIteratorNextThrows() {
        Completable c = Completable.merge(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void mergeObservableEmpty() {
        Completable c = Completable.merge(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeObservableError() {
        Completable c = Completable.merge(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableSingle() {
        Completable c = Completable.merge(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeObservableSingleThrows() {
        Completable c = Completable.merge(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableMany() {
        Completable c = Completable.merge(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeObservableManyOneThrows() {
        Completable c = Completable.merge(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableMaxConcurrent() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.merge(cs, 5);
        c.blockingAwait();
        // FIXME this request pattern looks odd because all 10 completions trigger 1 requests
        Assert.assertEquals(Arrays.asList(5L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), requested);
    }

    @Test
    public void mergeDelayErrorEmpty() {
        Completable c = Completable.mergeArrayDelayError();
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorSingleSource() {
        Completable c = Completable.mergeArrayDelayError(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorSingleSourceThrows() {
        Completable c = Completable.mergeArrayDelayError(error.completable);
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorMultipleSources() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test
    public void mergeDelayErrorMultipleOneThrows() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, error.completable, normal.completable);
        try {
            c.blockingAwait();
        } catch (TestException ex) {
            normal.assertSubscriptions(2);
        }
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorMultipleOneIsNull() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableEmpty() {
        Completable c = Completable.mergeDelayError(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Completable c = Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableSingle() {
        Completable c = Completable.mergeDelayError(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void mergeDelayErrorIterableMany() {
        Completable c = Completable.mergeDelayError(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableOneThrows() {
        Completable c = Completable.mergeDelayError(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableManyOneThrows() {
        Completable c = Completable.mergeDelayError(Arrays.asList(normal.completable, error.completable, normal.completable));
        try {
            c.blockingAwait();
        } catch (TestException ex) {
            normal.assertSubscriptions(2);
        }
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIterableThrows() {
        Completable c = Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIteratorHasNextThrows() {
        Completable c = Completable.mergeDelayError(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIteratorNextThrows() {
        Completable c = Completable.mergeDelayError(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableEmpty() {
        Completable c = Completable.mergeDelayError(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableError() {
        Completable c = Completable.mergeDelayError(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableSingle() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableSingleThrows() {
        Completable c = Completable.mergeDelayError(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableMany() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableManyOneThrows() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableMaxConcurrent() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.mergeDelayError(cs, 5);
        c.blockingAwait();
        // FIXME this request pattern looks odd because all 10 completions trigger 1 requests
        Assert.assertEquals(Arrays.asList(5L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), requested);
    }

    @Test
    public void never() {
        final AtomicBoolean onSubscribeCalled = new AtomicBoolean();
        final AtomicInteger calls = new AtomicInteger();
        Completable.never().subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                onSubscribeCalled.set(true);
            }

            @Override
            public void onError(Throwable e) {
                calls.getAndIncrement();
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }
        });
        Assert.assertTrue("onSubscribe not called", onSubscribeCalled.get());
        Assert.assertEquals("There were calls to onXXX methods", 0, calls.get());
    }

    @Test
    public void timer() {
        Completable c = Completable.timer(500, TimeUnit.MILLISECONDS);
        c.blockingAwait();
    }

    @Test
    public void timerNewThread() {
        Completable c = Completable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread());
        c.blockingAwait();
    }

    @Test
    public void timerTestScheduler() {
        TestScheduler scheduler = new TestScheduler();
        Completable c = Completable.timer(250, TimeUnit.MILLISECONDS, scheduler);
        final AtomicInteger calls = new AtomicInteger();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }

            @Override
            public void onError(Throwable e) {
                RxJavaPlugins.onError(e);
            }
        });
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, calls.get());
        scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void timerCancel() throws InterruptedException {
        Completable c = Completable.timer(250, TimeUnit.MILLISECONDS);
        final SequentialDisposable sd = new SequentialDisposable();
        final AtomicInteger calls = new AtomicInteger();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                sd.replace(d);
            }

            @Override
            public void onError(Throwable e) {
                calls.getAndIncrement();
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }
        });
        Thread.sleep(100);
        sd.dispose();
        Thread.sleep(200);
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void usingNormalEager() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        });
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.lazySet(e);
            }

            @Override
            public void onComplete() {
                disposedFirst.set(dispose.get() != 0);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertTrue("Not disposed first", disposedFirst.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void usingNormalLazy() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return normal.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        }, false);
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.lazySet(e);
            }

            @Override
            public void onComplete() {
                disposedFirst.set(dispose.get() != 0);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertFalse("Disposed first", disposedFirst.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void usingErrorEager() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return error.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        });
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                disposedFirst.set(dispose.get() != 0);
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertTrue("Not disposed first", disposedFirst.get());
        Assert.assertFalse(complete.get());
    }

    @Test
    public void usingErrorLazy() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return error.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        }, false);
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                disposedFirst.set(dispose.get() != 0);
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertFalse("Disposed first", disposedFirst.get());
        Assert.assertFalse(complete.get());
    }

    @Test(expected = NullPointerException.class)
    public void usingMapperReturnsNull() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return null;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingResourceThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                throw new TestException();
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingMapperThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingDisposerThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void composeNormal() {
        Completable c = error.completable.compose(new CompletableTransformer() {

            @Override
            public Completable apply(Completable n) {
                return n.onErrorComplete();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void concatWithNormal() {
        Completable c = normal.completable.concatWith(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(2);
    }

    @Test(expected = TestException.class)
    public void concatWithError() {
        Completable c = normal.completable.concatWith(error.completable);
        c.blockingAwait();
    }

    @Test
    public void delayNormal() throws InterruptedException {
        Completable c = normal.completable.delay(250, TimeUnit.MILLISECONDS);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        Thread.sleep(100);
        Assert.assertFalse("Already done", done.get());
        int timeout = 10;
        while (timeout-- > 0 && !done.get()) {
            Thread.sleep(100);
        }
        Assert.assertTrue("Not done", done.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void delayErrorImmediately() throws InterruptedException {
        final TestScheduler scheduler = new TestScheduler();
        final Completable c = error.completable.delay(250, TimeUnit.MILLISECONDS, scheduler);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(error.get().toString(), error.get() instanceof TestException);
        Assert.assertFalse("Already done", done.get());
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertFalse("Already done", done.get());
    }

    @Test
    public void delayErrorToo() throws InterruptedException {
        Completable c = error.completable.delay(250, TimeUnit.MILLISECONDS, Schedulers.computation(), true);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        Thread.sleep(100);
        Assert.assertFalse("Already done", done.get());
        Assert.assertNull(error.get());
        Thread.sleep(200);
        Assert.assertFalse("Already done", done.get());
        Assert.assertTrue(error.get() instanceof TestException);
    }

    @Test
    public void doOnCompleteNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnCompleteError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw TestException");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnCompleteThrows() {
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnDisposeNormalDoesntCall() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void doOnDisposeErrorDoesntCall() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("No exception thrown");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void doOnDisposeChildCancels() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                d.dispose();
            }

            @Override
            public void onError(Throwable e) {
                // ignored
            }

            @Override
            public void onComplete() {
                // ignored
            }
        });
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnDisposeThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable c = normal.completable.doOnDispose(new Action() {

                @Override
                public void run() {
                    throw new TestException();
                }
            });
            c.subscribe(new CompletableObserver() {

                @Override
                public void onSubscribe(Disposable d) {
                    d.dispose();
                }

                @Override
                public void onError(Throwable e) {
                    // ignored
                }

                @Override
                public void onComplete() {
                    // ignored
                }
            });
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnErrorNoError() {
        final AtomicReference<Throwable> error = new AtomicReference<>();
        Completable c = normal.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                error.set(e);
            }
        });
        c.blockingAwait();
        Assert.assertNull(error.get());
    }

    @Test
    public void doOnErrorHasError() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        Completable c = error.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw exception");
        } catch (Throwable e) {
            // expected
        }
        Assert.assertTrue(err.get() instanceof TestException);
    }

    @Test
    public void doOnErrorThrows() {
        Completable c = error.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                throw new IllegalStateException();
            }
        });
        try {
            c.blockingAwait();
        } catch (CompositeException ex) {
            List<Throwable> a = ex.getExceptions();
            Assert.assertEquals(2, a.size());
            Assert.assertTrue(a.get(0) instanceof TestException);
            Assert.assertTrue(a.get(1) instanceof IllegalStateException);
        }
    }

    @Test
    public void doOnSubscribeNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                calls.getAndIncrement();
            }
        });
        for (int i = 0; i < 10; i++) {
            c.blockingAwait();
        }
        Assert.assertEquals(10, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnSubscribeThrows() {
        Completable c = normal.completable.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnTerminateNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnTerminate(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnTerminate(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did dot throw exception");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = NullPointerException.class)
    public void liftReturnsNull() {
        Completable c = normal.completable.lift(new CompletableOperator() {

            @Override
            public CompletableObserver apply(CompletableObserver v) {
                return null;
            }
        });
        c.blockingAwait();
    }

    static final class CompletableOperatorSwap implements CompletableOperator {

        @Override
        public CompletableObserver apply(final CompletableObserver v) {
            return new CompletableObserver() {

                @Override
                public void onComplete() {
                    v.onError(new TestException());
                }

                @Override
                public void onError(Throwable e) {
                    v.onComplete();
                }

                @Override
                public void onSubscribe(Disposable d) {
                    v.onSubscribe(d);
                }
            };
        }
    }

    @Test(expected = TestException.class)
    public void liftOnCompleteError() {
        Completable c = normal.completable.lift(new CompletableOperatorSwap());
        c.blockingAwait();
    }

    @Test
    public void liftOnErrorComplete() {
        Completable c = error.completable.lift(new CompletableOperatorSwap());
        c.blockingAwait();
    }

    @Test
    public void mergeWithNormal() {
        Completable c = normal.completable.mergeWith(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(2);
    }

    @Test
    public void observeOnNormal() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        Completable c = normal.completable.observeOn(Schedulers.computation());
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }

            @Override
            public void onError(Throwable e) {
                err.set(e);
                cdl.countDown();
            }
        });
        cdl.await();
        Assert.assertNull(err.get());
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void observeOnError() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        Completable c = error.completable.observeOn(Schedulers.computation());
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }

            @Override
            public void onError(Throwable e) {
                name.set(Thread.currentThread().getName());
                err.set(e);
                cdl.countDown();
            }
        });
        cdl.await();
        Assert.assertTrue(err.get() instanceof TestException);
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void onErrorComplete() {
        Completable c = error.completable.onErrorComplete();
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void onErrorCompleteFalse() {
        Completable c = error.completable.onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return e instanceof IllegalStateException;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void onErrorResumeNextFunctionReturnsNull() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable e) {
                return null;
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw an exception");
        } catch (CompositeException ex) {
            List<Throwable> errors = ex.getExceptions();
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class);
            assertEquals(2, errors.size());
        }
    }

    @Test
    public void onErrorResumeNextFunctionThrows() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable e) {
                throw new TestException();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw an exception");
        } catch (CompositeException ex) {
            List<Throwable> a = ex.getExceptions();
            Assert.assertEquals(2, a.size());
            Assert.assertTrue(a.get(0) instanceof TestException);
            Assert.assertTrue(a.get(1) instanceof TestException);
        }
    }

    @Test
    public void onErrorResumeNextNormal() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable v) {
                return normal.completable;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void onErrorResumeNextError() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable v) {
                return error.completable;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void repeatNormal() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                Thread.sleep(100);
                return null;
            }
        }).repeat();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(final Disposable d) {
                Schedulers.single().scheduleDirect(new Runnable() {

                    @Override
                    public void run() {
                        d.dispose();
                    }
                }, 550, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onError(Throwable e) {
                err.set(e);
            }

            @Override
            public void onComplete() {
            }
        });
        Assert.assertEquals(6, calls.get());
        Assert.assertNull(err.get());
    }

    @Test(expected = TestException.class)
    public void repeatError() {
        Completable c = error.completable.repeat();
        c.blockingAwait();
    }

    @Test
    public void repeat5Times() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(5);
        c.blockingAwait();
        Assert.assertEquals(5, calls.get());
    }

    @Test
    public void repeat1Time() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(1);
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void repeat0Time() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(0);
        c.blockingAwait();
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void repeatUntilNormal() {
        final AtomicInteger calls = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger(5);
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() {
                return times.decrementAndGet() == 0;
            }
        });
        c.blockingAwait();
        Assert.assertEquals(5, calls.get());
    }

    @Test
    public void retryNormal() {
        Completable c = normal.completable.retry();
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void retry5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retry();
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void retryBiPredicate5Times() {
        Completable c = error.completable.retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer n, Throwable e) {
                return n < 5;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void retryTimes5Error() {
        Completable c = error.completable.retry(5);
        c.blockingAwait();
    }

    @Test
    public void retryTimes5Normal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.incrementAndGet() != 6) {
                    throw new TestException();
                }
            }
        }).retry(5);
        c.blockingAwait();
        assertEquals(6, calls.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void retryNegativeTimes() {
        normal.completable.retry(-1);
    }

    @Test(expected = TestException.class)
    public void retryPredicateError() {
        Completable c = error.completable.retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return false;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void retryPredicate5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return true;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void retryWhen5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> f) {
                return (Publisher) f;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void subscribe() throws InterruptedException {
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.delay(100, TimeUnit.MILLISECONDS).doOnComplete(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Disposable d = c.subscribe();
        assertFalse(d.isDisposed());
        Thread.sleep(150);
        Assert.assertTrue("Not completed", complete.get());
        assertTrue(d.isDisposed());
    }

    @Test
    public void subscribeDispose() throws InterruptedException {
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.delay(200, TimeUnit.MILLISECONDS).doOnComplete(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Disposable d = c.subscribe();
        Thread.sleep(100);
        d.dispose();
        Thread.sleep(150);
        Assert.assertFalse("Completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksNormal() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicBoolean complete = new AtomicBoolean();
        normal.completable.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        Assert.assertNull(err.get());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksError() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicBoolean complete = new AtomicBoolean();
        error.completable.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        Assert.assertTrue(err.get() instanceof TestException);
        Assert.assertFalse("Not completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksCompleteThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Throwable> err = new AtomicReference<>();
            normal.completable.subscribe(new Action() {

                @Override
                public void run() {
                    throw new TestException();
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) {
                    err.set(e);
                }
            });
            Assert.assertNull(err.get());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeTwoCallbacksOnErrorThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            error.completable.subscribe(new Action() {

                @Override
                public void run() {
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) {
                    throw new TestException();
                }
            });
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeObserverNormal() {
        TestObserver<Object> to = new TestObserver<>();
        normal.completable.toObservable().subscribe(to);
        to.assertComplete();
        to.assertNoValues();
        to.assertNoErrors();
    }

    @Test
    public void subscribeObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        error.completable.toObservable().subscribe(to);
        to.assertNotComplete();
        to.assertNoValues();
        to.assertError(TestException.class);
    }

    @Test
    public void subscribeActionNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        normal.completable.subscribe(new Action() {

            @Override
            public void run() {
                run.set(true);
            }
        });
        Assert.assertTrue("Not completed", run.get());
    }

    @Test
    public void subscribeActionError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicBoolean run = new AtomicBoolean();
            error.completable.subscribe(new Action() {

                @Override
                public void run() {
                    run.set(true);
                }
            });
            Assert.assertFalse("Completed", run.get());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeSubscriberNormal() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        normal.completable.toFlowable().subscribe(ts);
        ts.assertComplete();
        ts.assertNoValues();
        ts.assertNoErrors();
    }

    @Test
    public void subscribeSubscriberError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        error.completable.toFlowable().subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoValues();
        ts.assertError(TestException.class);
    }

    @Test
    public void subscribeOnNormal() {
        final AtomicReference<String> name = new AtomicReference<>();
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                name.set(Thread.currentThread().getName());
                EmptyDisposable.complete(observer);
            }
        }).subscribeOn(Schedulers.computation());
        c.blockingAwait();
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void subscribeOnError() {
        final AtomicReference<String> name = new AtomicReference<>();
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                name.set(Thread.currentThread().getName());
                EmptyDisposable.error(new TestException(), observer);
            }
        }).subscribeOn(Schedulers.computation());
        try {
            c.blockingAwait();
            Assert.fail("No exception thrown");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void timeoutSwitchNormal() {
        Completable c = Completable.never().timeout(100, TimeUnit.MILLISECONDS, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void timeoutTimerCancelled() throws InterruptedException {
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Thread.sleep(50);
                return null;
            }
        }).timeout(100, TimeUnit.MILLISECONDS, normal.completable);
        c.blockingAwait();
        Thread.sleep(100);
        normal.assertSubscriptions(0);
    }

    @Test
    public void toNormal() {
        normal.completable.to(new CompletableConverter<Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Completable c) {
                return c.toFlowable();
            }
        }).test().assertComplete().assertNoValues();
    }

    @Test
    public void asNormal() {
        normal.completable.to(new CompletableConverter<Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Completable c) {
                return c.toFlowable();
            }
        }).test().assertComplete().assertNoValues();
    }

    @Test
    public void as() {
        Completable.complete().to(new CompletableConverter<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Completable v) {
                return v.toFlowable();
            }
        }).test().assertComplete();
    }

    @Test
    public void toFlowableNormal() {
        normal.completable.toFlowable().blockingForEach(Functions.emptyConsumer());
    }

    @Test(expected = TestException.class)
    public void toFlowableError() {
        error.completable.toFlowable().blockingForEach(Functions.emptyConsumer());
    }

    @Test
    public void toObservableNormal() {
        normal.completable.toObservable().blockingForEach(Functions.emptyConsumer());
    }

    @Test(expected = TestException.class)
    public void toObservableError() {
        error.completable.toObservable().blockingForEach(Functions.emptyConsumer());
    }

    @Test
    public void toSingleSupplierNormal() {
        Assert.assertEquals(1, normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }).blockingGet());
    }

    @Test(expected = TestException.class)
    public void toSingleSupplierError() {
        error.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toSingleSupplierReturnsNull() {
        normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = TestException.class)
    public void toSingleSupplierThrows() {
        normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                throw new TestException();
            }
        }).blockingGet();
    }

    @Test(expected = TestException.class)
    public void toSingleDefaultError() {
        error.completable.toSingleDefault(1).blockingGet();
    }

    @Test
    public void toSingleDefaultNormal() {
        Assert.assertEquals((Integer) 1, normal.completable.toSingleDefault(1).blockingGet());
    }

    @Test
    public void unsubscribeOnNormal() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        normal.completable.delay(1, TimeUnit.SECONDS).doOnDispose(new Action() {

            @Override
            public void run() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }
        }).unsubscribeOn(Schedulers.computation()).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(final Disposable d) {
                Schedulers.single().scheduleDirect(new Runnable() {

                    @Override
                    public void run() {
                        d.dispose();
                    }
                }, 100, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        cdl.await();
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void ambArrayEmpty() {
        Completable c = Completable.ambArray();
        c.blockingAwait();
    }

    @Test
    public void ambArraySingleNormal() {
        Completable c = Completable.ambArray(normal.completable);
        c.blockingAwait();
    }

    @Test
    public void ambArraySingleError() {
        Completable.ambArray(error.completable).test().assertError(TestException.class);
    }

    @Test
    public void ambArrayOneFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambArrayOneFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambArraySecondFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambArraySecondFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambMultipleOneIsNull() {
        Completable.ambArray(null, normal.completable).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableEmpty() {
        Completable c = Completable.amb(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test
    public void ambIterableIteratorNull() {
        Completable.amb(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableWithNull() {
        Completable.amb(Arrays.asList(null, normal.completable)).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableSingle() {
        Completable c = Completable.amb(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void ambIterableMany() {
        Completable c = Completable.amb(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void ambIterableOneThrows() {
        Completable.amb(Collections.singleton(error.completable)).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableManyOneThrows() {
        Completable.amb(Arrays.asList(error.completable, normal.completable)).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIterableThrows() {
        Completable.amb(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        }).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIteratorHasNextThrows() {
        Completable.amb(new IterableIteratorHasNextThrows()).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIteratorNextThrows() {
        Completable.amb(new IterableIteratorNextThrows()).test().assertError(TestException.class);
    }

    @Test
    public void ambWithArrayOneFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambWithArrayOneFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambWithArraySecondFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambWithArraySecondFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void startWithCompletableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Completable c = normal.completable.startWith(Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return null;
            }
        }));
        c.blockingAwait();
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
    }

    @Test
    public void startWithCompletableError() {
        Completable c = normal.completable.startWith(error.completable);
        try {
            c.blockingAwait();
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
            normal.assertSubscriptions(0);
            error.assertSubscriptions(1);
        }
    }

    @Test
    public void startWithFlowableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Flowable<Object> c = normal.completable.startWith(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void startWithFlowableError() {
        Flowable<Object> c = normal.completable.startWith(Flowable.error(new TestException()));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        normal.assertSubscriptions(0);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void startWithObservableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Observable<Object> o = normal.completable.startWith(Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestObserver<Object> to = new TestObserver<>();
        o.subscribe(to);
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
        to.assertValue(1);
        to.assertComplete();
        to.assertNoErrors();
    }

    @Test
    public void startWithObservableError() {
        Observable<Object> o = normal.completable.startWith(Observable.error(new TestException()));
        TestObserver<Object> to = new TestObserver<>();
        o.subscribe(to);
        normal.assertSubscriptions(0);
        to.assertNoValues();
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void andThen() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Completable.complete().andThen(Flowable.just("foo")).subscribe(ts);
        ts.request(1);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
        TestObserver<String> to = new TestObserver<>();
        Completable.complete().andThen(Observable.just("foo")).subscribe(to);
        to.assertValue("foo");
        to.assertComplete();
        to.assertNoErrors();
    }

    private static void expectUncaughtTestException(Action action) {
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        CapturingUncaughtExceptionHandler handler = new CapturingUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable error) throws Exception {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), error);
            }
        });
        try {
            action.run();
            assertEquals("Should have received exactly 1 exception", 1, handler.count);
            Throwable caught = handler.caught;
            while (caught != null) {
                if (caught instanceof TestException) {
                    break;
                }
                if (caught == caught.getCause()) {
                    break;
                }
                caught = caught.getCause();
            }
            assertTrue("A TestException should have been delivered to the handler", caught instanceof TestException);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler);
            RxJavaPlugins.setErrorHandler(null);
        }
    }

    @Test
    public void subscribeOneActionThrowFromOnCompleted() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                normal.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void subscribeTwoActionsThrowFromOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void propagateExceptionSubscribeOneAction() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.toSingleDefault(1).subscribe(new Consumer<Integer>() {

                    @Override
                    public void accept(Integer integer) {
                    }
                });
            }
        });
    }

    @Test
    public void usingFactoryReturnsNullAndDisposerThrows() {
        Consumer<Integer> onDispose = new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                return null;
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException ex = (CompositeException) ts.errors().get(0);
        List<Throwable> listEx = ex.getExceptions();
        assertEquals(2, listEx.size());
        assertTrue(listEx.get(0).toString(), listEx.get(0) instanceof NullPointerException);
        assertTrue(listEx.get(1).toString(), listEx.get(1) instanceof TestException);
    }

    @Test
    public void subscribeReportsUnsubscribedOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<String> stringSubject = PublishSubject.create();
            Completable completable = stringSubject.ignoreElements();
            Disposable completableSubscription = completable.subscribe();
            stringSubject.onError(new TestException());
            assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeActionReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        });
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void subscribeActionReportsUnsubscribedAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        });
        disposableRef.set(completableSubscription);
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onComplete", disposableRef.get());
    }

    @Test
    public void subscribeActionReportsUnsubscribedOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<String> stringSubject = PublishSubject.create();
            Completable completable = stringSubject.ignoreElements();
            Disposable completableSubscription = completable.subscribe(new Action() {

                @Override
                public void run() {
                }
            });
            stringSubject.onError(new TestException());
            assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeAction2ReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) {
            }
        });
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedOnError() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
            }
        });
        stringSubject.onError(new TestException());
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void andThenSubscribeOn() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        TestScheduler scheduler = new TestScheduler();
        Completable.complete().andThen(Flowable.just("foo").delay(1, TimeUnit.SECONDS, scheduler)).subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void andThenSingleNever() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        Completable.never().andThen(Single.just("foo")).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
    }

    @Test
    public void andThenSingleError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        final Exception e = new Exception();
        Completable.error(e).andThen(new Single<String>() {

            @Override
            public void subscribeActual(SingleObserver<? super String> observer) {
                hasRun.set(true);
                observer.onSuccess("foo");
            }
        }).toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertError(e);
        Assert.assertFalse("Should not have subscribed to single when completable errors", hasRun.get());
    }

    @Test
    public void andThenSingleSubscribeOn() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        TestScheduler scheduler = new TestScheduler();
        Completable.complete().andThen(Single.just("foo").delay(1, TimeUnit.SECONDS, scheduler)).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    private Function<Completable, Completable> onCreate;

    private BiFunction<Completable, CompletableObserver, CompletableObserver> onStart;

    @Before
    public void setUp() throws Exception {
        onCreate = spy(new Function<Completable, Completable>() {

            @Override
            public Completable apply(Completable t) {
                return t;
            }
        });
        RxJavaPlugins.setOnCompletableAssembly(onCreate);
        onStart = spy(new BiFunction<Completable, CompletableObserver, CompletableObserver>() {

            @Override
            public CompletableObserver apply(Completable t1, CompletableObserver t2) {
                return t2;
            }
        });
        RxJavaPlugins.setOnCompletableSubscribe(onStart);
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void hookCreate() throws Throwable {
        CompletableSource subscriber = mock(CompletableSource.class);
        Completable create = Completable.unsafeCreate(subscriber);
        verify(onCreate, times(1)).apply(create);
    }

    @Test
    public void doOnCompletedNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnCompletedError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw TestException");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnCompletedThrows() {
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doAfterTerminateNormal() {
        final AtomicBoolean doneAfter = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.doAfterTerminate(new Action() {

            @Override
            public void run() {
                doneAfter.set(complete.get());
            }
        });
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        c.blockingAwait();
        Assert.assertTrue("Not completed", complete.get());
        Assert.assertTrue("Closure called before onComplete", doneAfter.get());
    }

    @Test
    public void doAfterTerminateWithError() {
        final AtomicBoolean doneAfter = new AtomicBoolean();
        Completable c = error.completable.doAfterTerminate(new Action() {

            @Override
            public void run() {
                doneAfter.set(true);
            }
        });
        try {
            c.blockingAwait(5, TimeUnit.SECONDS);
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
            // expected
        }
        Assert.assertTrue("Closure not called", doneAfter.get());
    }

    @Test
    public void subscribeEmptyOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe();
            }
        });
    }

    @Test
    public void subscribeOneActionOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    @Test
    public void propagateExceptionSubscribeEmpty() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.toSingleDefault(0).subscribe();
            }
        });
    }

    @Test
    public void andThenCompletableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Completable c = normal.completable.andThen(Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return null;
            }
        }));
        c.blockingAwait();
        Assert.assertFalse("Start with other", run.get());
        normal.assertSubscriptions(1);
    }

    @Test
    public void andThenCompletableError() {
        Completable c = normal.completable.andThen(error.completable);
        try {
            c.blockingAwait();
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
            normal.assertSubscriptions(1);
            error.assertSubscriptions(1);
        }
    }

    @Test
    public void andThenFlowableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Flowable<Object> c = normal.completable.andThen(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        Assert.assertFalse("Start with other", run.get());
        normal.assertSubscriptions(1);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void andThenFlowableError() {
        Flowable<Object> c = normal.completable.andThen(Flowable.error(new TestException()));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        normal.assertSubscriptions(1);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void usingFactoryThrows() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> onDispose = mock(Consumer.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                throw new TestException();
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        verify(onDispose).accept(1);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void usingFactoryAndDisposerThrow() {
        Consumer<Integer> onDispose = new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                throw new TestException();
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException ex = (CompositeException) ts.errors().get(0);
        List<Throwable> listEx = ex.getExceptions();
        assertEquals(2, listEx.size());
        assertTrue(listEx.get(0).toString(), listEx.get(0) instanceof TestException);
        assertTrue(listEx.get(1).toString(), listEx.get(1) instanceof TestException);
    }

    @Test
    public void usingFactoryReturnsNull() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> onDispose = mock(Consumer.class);
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                return null;
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        verify(onDispose).accept(1);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(NullPointerException.class);
    }

    @Test
    public void subscribeReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe();
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void hookSubscribeStart() throws Throwable {
        TestSubscriber<String> ts = new TestSubscriber<>();
        Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                observer.onComplete();
            }
        });
        completable.<String>toFlowable().subscribe(ts);
        verify(onStart, times(1)).apply(eq(completable), any(CompletableObserver.class));
    }

    @Test
    public void onStartCalledSafe() {
        TestSubscriber<Object> ts = new TestSubscriber<Object>() {

            @Override
            public void onStart() {
                onNext(1);
            }
        };
        normal.completable.<Object>toFlowable().subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void onErrorCompleteFunctionThrows() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        error.completable.onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable t) {
                throw new TestException("Forced inner failure");
            }
        }).<String>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException composite = (CompositeException) ts.errors().get(0);
        List<Throwable> errors = composite.getExceptions();
        Assert.assertEquals(2, errors.size());
        Assert.assertTrue(errors.get(0).toString(), errors.get(0) instanceof TestException);
        Assert.assertNull(errors.get(0).toString(), errors.get(0).getMessage());
        Assert.assertTrue(errors.get(1).toString(), errors.get(1) instanceof TestException);
        Assert.assertEquals(errors.get(1).toString(), "Forced inner failure", errors.get(1).getMessage());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        }, Functions.emptyConsumer());
        disposableRef.set(completableSubscription);
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onComplete", disposableRef.get());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedOnErrorAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        });
        disposableRef.set(completableSubscription);
        stringSubject.onError(new TestException());
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onError", disposableRef.get());
    }

    @Test
    public void propagateExceptionSubscribeOneActionThrowFromOnSuccess() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                normal.completable.toSingleDefault(1).subscribe(new Consumer<Integer>() {

                    @Override
                    public void accept(Integer integer) {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void andThenNever() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        Completable.never().andThen(Flowable.just("foo")).subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
    }

    @Test
    public void andThenError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        final Exception e = new Exception();
        Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver co) {
                co.onSubscribe(Disposable.empty());
                co.onError(e);
            }
        }).andThen(Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> s) {
                hasRun.set(true);
                s.onSubscribe(new BooleanSubscription());
                s.onNext("foo");
                s.onComplete();
            }
        })).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(e);
        Assert.assertFalse("Should not have subscribed to observable when completable errors", hasRun.get());
    }

    @Test
    public void andThenSingle() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Completable.complete().andThen(Single.just("foo")).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void fromFutureNormal() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Completable c = Completable.fromFuture(exec.submit(new Runnable() {

                @Override
                public void run() {
                    // no action
                }
            }));
            c.blockingAwait();
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void fromFutureThrows() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Completable c = Completable.fromFuture(exec.submit(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        }));
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw Exception");
        } catch (RuntimeException ex) {
            if (!((ex.getCause() instanceof ExecutionException) && (ex.getCause().getCause() instanceof TestException))) {
                ex.printStackTrace();
                Assert.fail("Wrong exception received");
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void fromRunnableNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromRunnableThrows() {
        Completable c = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnEventComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Completable.complete().doOnEvent(new Consumer<Throwable>() {

            @Override
            public void accept(final Throwable throwable) throws Exception {
                if (throwable == null) {
                    atomicInteger.incrementAndGet();
                }
            }
        }).subscribe();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void doOnEventError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicInteger atomicInteger = new AtomicInteger(0);
            Completable.error(new RuntimeException()).doOnEvent(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    if (throwable != null) {
                        atomicInteger.incrementAndGet();
                    }
                }
            }).subscribe();
            assertEquals(1, atomicInteger.get());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeTwoCallbacksDispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Disposable d = pp.ignoreElements().subscribe(Functions.EMPTY_ACTION, Functions.emptyConsumer());
        assertFalse(d.isDisposed());
        assertTrue(pp.hasSubscribers());
        d.dispose();
        assertTrue(d.isDisposed());
        assertFalse(pp.hasSubscribers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::complete, this.description("complete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatEmpty, this.description("concatEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSingleSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatSingleSource, this.description("concatSingleSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSingleSourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatSingleSourceThrows, this.description("concatSingleSourceThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleSources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatMultipleSources, this.description("concatMultipleSources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatMultipleOneThrows, this.description("concatMultipleOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatMultipleOneIsNull, this.description("concatMultipleOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatIterableEmpty, this.description("concatIterableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIteratorNull, this.description("concatIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatIterableSingle, this.description("concatIterableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatIterableMany, this.description("concatIterableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableOneThrows, this.description("concatIterableOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableManyOneThrows, this.description("concatIterableManyOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIterableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIterableThrows, this.description("concatIterableIterableThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIteratorHasNextThrows, this.description("concatIterableIteratorHasNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIteratorNextThrows, this.description("concatIterableIteratorNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatObservableEmpty, this.description("concatObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatObservableError, this.description("concatObservableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatObservableSingle, this.description("concatObservableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableSingleThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatObservableSingleThrows, this.description("concatObservableSingleThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatObservableMany, this.description("concatObservableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatObservableManyOneThrows, this.description("concatObservableManyOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservablePrefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatObservablePrefetch, this.description("concatObservablePrefetch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createOnSubscribeThrowsNPE() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::createOnSubscribeThrowsNPE, this.description("createOnSubscribeThrowsNPE"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createOnSubscribeThrowsRuntimeException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::createOnSubscribeThrowsRuntimeException, this.description("createOnSubscribeThrowsRuntimeException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::defer, this.description("defer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::deferReturnsNull, this.description("deferReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferFunctionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::deferFunctionThrows, this.description("deferFunctionThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferErrorSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::deferErrorSource, this.description("deferErrorSource"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorSupplierNormal, this.description("errorSupplierNormal"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorSupplierReturnsNull, this.description("errorSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorSupplierThrows, this.description("errorSupplierThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorNormal, this.description("errorNormal"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromCallableNormal, this.description("fromCallableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromCallableThrows, this.description("fromCallableThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFlowableEmpty, this.description("fromFlowableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFlowableSome, this.description("fromFlowableSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromFlowableError, this.description("fromFlowableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromObservableEmpty, this.description("fromObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableSome() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromObservableSome, this.description("fromObservableSome"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromObservableError, this.description("fromObservableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromActionNormal, this.description("fromActionNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromActionThrows, this.description("fromActionThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSingleNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromSingleNormal, this.description("fromSingleNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSingleThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromSingleThrows, this.description("fromSingleThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeEmpty, this.description("mergeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSingleSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeSingleSource, this.description("mergeSingleSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSingleSourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeSingleSourceThrows, this.description("mergeSingleSourceThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleSources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeMultipleSources, this.description("mergeMultipleSources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeMultipleOneThrows, this.description("mergeMultipleOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeMultipleOneIsNull, this.description("mergeMultipleOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableEmpty, this.description("mergeIterableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIteratorNull, this.description("mergeIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableSingle, this.description("mergeIterableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeIterableMany, this.description("mergeIterableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableOneThrows, this.description("mergeIterableOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableManyOneThrows, this.description("mergeIterableManyOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIterableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIterableThrows, this.description("mergeIterableIterableThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIteratorHasNextThrows, this.description("mergeIterableIteratorHasNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIteratorNextThrows, this.description("mergeIterableIteratorNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeObservableEmpty, this.description("mergeObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeObservableError, this.description("mergeObservableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeObservableSingle, this.description("mergeObservableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableSingleThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeObservableSingleThrows, this.description("mergeObservableSingleThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeObservableMany, this.description("mergeObservableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeObservableManyOneThrows, this.description("mergeObservableManyOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableMaxConcurrent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeObservableMaxConcurrent, this.description("mergeObservableMaxConcurrent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorEmpty, this.description("mergeDelayErrorEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorSingleSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorSingleSource, this.description("mergeDelayErrorSingleSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorSingleSourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorSingleSourceThrows, this.description("mergeDelayErrorSingleSourceThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleSources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorMultipleSources, this.description("mergeDelayErrorMultipleSources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorMultipleOneThrows, this.description("mergeDelayErrorMultipleOneThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorMultipleOneIsNull, this.description("mergeDelayErrorMultipleOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorIterableEmpty, this.description("mergeDelayErrorIterableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableIteratorNull, this.description("mergeDelayErrorIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorIterableSingle, this.description("mergeDelayErrorIterableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorIterableMany, this.description("mergeDelayErrorIterableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableOneThrows, this.description("mergeDelayErrorIterableOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorIterableManyOneThrows, this.description("mergeDelayErrorIterableManyOneThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIterableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableIterableThrows, this.description("mergeDelayErrorIterableIterableThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableIteratorHasNextThrows, this.description("mergeDelayErrorIterableIteratorHasNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableIteratorNextThrows, this.description("mergeDelayErrorIterableIteratorNextThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorObservableEmpty, this.description("mergeDelayErrorObservableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorObservableError, this.description("mergeDelayErrorObservableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorObservableSingle, this.description("mergeDelayErrorObservableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableSingleThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorObservableSingleThrows, this.description("mergeDelayErrorObservableSingleThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorObservableMany, this.description("mergeDelayErrorObservableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorObservableManyOneThrows, this.description("mergeDelayErrorObservableManyOneThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableMaxConcurrent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeDelayErrorObservableMaxConcurrent, this.description("mergeDelayErrorObservableMaxConcurrent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::never, this.description("never"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timer, this.description("timer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerNewThread() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timerNewThread, this.description("timerNewThread"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerTestScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timerTestScheduler, this.description("timerTestScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timerCancel, this.description("timerCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingNormalEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingNormalEager, this.description("usingNormalEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingNormalLazy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingNormalLazy, this.description("usingNormalLazy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingErrorEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingErrorEager, this.description("usingErrorEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingErrorLazy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingErrorLazy, this.description("usingErrorLazy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingMapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingMapperReturnsNull, this.description("usingMapperReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingResourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingResourceThrows, this.description("usingResourceThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingMapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingMapperThrows, this.description("usingMapperThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingDisposerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingDisposerThrows, this.description("usingDisposerThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::composeNormal, this.description("composeNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatWithNormal, this.description("concatWithNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatWithError, this.description("concatWithError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayNormal, this.description("delayNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorImmediately() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayErrorImmediately, this.description("delayErrorImmediately"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorToo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayErrorToo, this.description("delayErrorToo"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnCompleteNormal, this.description("doOnCompleteNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnCompleteError, this.description("doOnCompleteError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::doOnCompleteThrows, this.description("doOnCompleteThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeNormalDoesntCall() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeNormalDoesntCall, this.description("doOnDisposeNormalDoesntCall"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeErrorDoesntCall() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeErrorDoesntCall, this.description("doOnDisposeErrorDoesntCall"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeChildCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeChildCancels, this.description("doOnDisposeChildCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeThrows, this.description("doOnDisposeThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorNoError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnErrorNoError, this.description("doOnErrorNoError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorHasError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnErrorHasError, this.description("doOnErrorHasError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnErrorThrows, this.description("doOnErrorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribeNormal, this.description("doOnSubscribeNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::doOnSubscribeThrows, this.description("doOnSubscribeThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateNormal, this.description("doOnTerminateNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateError, this.description("doOnTerminateError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::liftReturnsNull, this.description("liftReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftOnCompleteError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::liftOnCompleteError, this.description("liftOnCompleteError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftOnErrorComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::liftOnErrorComplete, this.description("liftOnErrorComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeWithNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeWithNormal, this.description("mergeWithNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observeOnNormal, this.description("observeOnNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observeOnError, this.description("observeOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorComplete, this.description("onErrorComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onErrorCompleteFalse, this.description("onErrorCompleteFalse"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextFunctionReturnsNull, this.description("onErrorResumeNextFunctionReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextFunctionThrows, this.description("onErrorResumeNextFunctionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextNormal, this.description("onErrorResumeNextNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onErrorResumeNextError, this.description("onErrorResumeNextError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::repeatNormal, this.description("repeatNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::repeatError, this.description("repeatError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat5Times() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::repeat5Times, this.description("repeat5Times"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat1Time() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::repeat1Time, this.description("repeat1Time"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat0Time() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::repeat0Time, this.description("repeat0Time"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntilNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::repeatUntilNormal, this.description("repeatUntilNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryNormal, this.description("retryNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retry5Times() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retry5Times, this.description("retry5Times"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryBiPredicate5Times() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryBiPredicate5Times, this.description("retryBiPredicate5Times"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimes5Error() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryTimes5Error, this.description("retryTimes5Error"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimes5Normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryTimes5Normal, this.description("retryTimes5Normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryNegativeTimes() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryNegativeTimes, this.description("retryNegativeTimes"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicateError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryPredicateError, this.description("retryPredicateError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicate5Times() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryPredicate5Times, this.description("retryPredicate5Times"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWhen5Times() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retryWhen5Times, this.description("retryWhen5Times"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribe, this.description("subscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeDispose, this.description("subscribeDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoCallbacksNormal, this.description("subscribeTwoCallbacksNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoCallbacksError, this.description("subscribeTwoCallbacksError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksCompleteThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoCallbacksCompleteThrows, this.description("subscribeTwoCallbacksCompleteThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksOnErrorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoCallbacksOnErrorThrows, this.description("subscribeTwoCallbacksOnErrorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeObserverNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeObserverNormal, this.description("subscribeObserverNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeObserverError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeObserverError, this.description("subscribeObserverError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeActionNormal, this.description("subscribeActionNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeActionError, this.description("subscribeActionError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeSubscriberNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeSubscriberNormal, this.description("subscribeSubscriberNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeSubscriberError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeSubscriberError, this.description("subscribeSubscriberError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeOnNormal, this.description("subscribeOnNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeOnError, this.description("subscribeOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSwitchNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutSwitchNormal, this.description("timeoutSwitchNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutTimerCancelled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutTimerCancelled, this.description("timeoutTimerCancelled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toNormal, this.description("toNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asNormal, this.description("asNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_as() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::as, this.description("as"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableNormal, this.description("toFlowableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toFlowableError, this.description("toFlowableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableNormal, this.description("toObservableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toObservableError, this.description("toObservableError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSingleSupplierNormal, this.description("toSingleSupplierNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toSingleSupplierError, this.description("toSingleSupplierError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toSingleSupplierReturnsNull, this.description("toSingleSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toSingleSupplierThrows, this.description("toSingleSupplierThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleDefaultError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toSingleDefaultError, this.description("toSingleDefaultError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleDefaultNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toSingleDefaultNormal, this.description("toSingleDefaultNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribeOnNormal, this.description("unsubscribeOnNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArrayEmpty, this.description("ambArrayEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySingleNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArraySingleNormal, this.description("ambArraySingleNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySingleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArraySingleError, this.description("ambArraySingleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArrayOneFires, this.description("ambArrayOneFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneFiresError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArrayOneFiresError, this.description("ambArrayOneFiresError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySecondFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArraySecondFires, this.description("ambArraySecondFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySecondFiresError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArraySecondFiresError, this.description("ambArraySecondFiresError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambMultipleOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambMultipleOneIsNull, this.description("ambMultipleOneIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableEmpty, this.description("ambIterableEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIteratorNull, this.description("ambIterableIteratorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableWithNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableWithNull, this.description("ambIterableWithNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableSingle, this.description("ambIterableSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableMany, this.description("ambIterableMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableOneThrows, this.description("ambIterableOneThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableManyOneThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableManyOneThrows, this.description("ambIterableManyOneThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIterableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIterableThrows, this.description("ambIterableIterableThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIteratorHasNextThrows, this.description("ambIterableIteratorHasNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIteratorNextThrows, this.description("ambIterableIteratorNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArrayOneFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambWithArrayOneFires, this.description("ambWithArrayOneFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArrayOneFiresError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambWithArrayOneFiresError, this.description("ambWithArrayOneFiresError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArraySecondFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambWithArraySecondFires, this.description("ambWithArraySecondFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArraySecondFiresError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambWithArraySecondFiresError, this.description("ambWithArraySecondFiresError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithCompletableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithCompletableNormal, this.description("startWithCompletableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithCompletableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithCompletableError, this.description("startWithCompletableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithFlowableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithFlowableNormal, this.description("startWithFlowableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithFlowableError, this.description("startWithFlowableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithObservableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithObservableNormal, this.description("startWithObservableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithObservableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::startWithObservableError, this.description("startWithObservableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThen() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThen, this.description("andThen"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOneActionThrowFromOnCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeOneActionThrowFromOnCompleted, this.description("subscribeOneActionThrowFromOnCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoActionsThrowFromOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoActionsThrowFromOnError, this.description("subscribeTwoActionsThrowFromOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeOneAction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::propagateExceptionSubscribeOneAction, this.description("propagateExceptionSubscribeOneAction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryReturnsNullAndDisposerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingFactoryReturnsNullAndDisposerThrows, this.description("usingFactoryReturnsNullAndDisposerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeReportsUnsubscribedOnError, this.description("subscribeReportsUnsubscribedOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeActionReportsUnsubscribed, this.description("subscribeActionReportsUnsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribedAfter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeActionReportsUnsubscribedAfter, this.description("subscribeActionReportsUnsubscribedAfter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeActionReportsUnsubscribedOnError, this.description("subscribeActionReportsUnsubscribedOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeAction2ReportsUnsubscribed, this.description("subscribeAction2ReportsUnsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeAction2ReportsUnsubscribedOnError, this.description("subscribeAction2ReportsUnsubscribedOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSubscribeOn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenSubscribeOn, this.description("andThenSubscribeOn"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleNever() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenSingleNever, this.description("andThenSingleNever"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenSingleError, this.description("andThenSingleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleSubscribeOn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenSingleSubscribeOn, this.description("andThenSingleSubscribeOn"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hookCreate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hookCreate, this.description("hookCreate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnCompletedNormal, this.description("doOnCompletedNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnCompletedError, this.description("doOnCompletedError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::doOnCompletedThrows, this.description("doOnCompletedThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doAfterTerminateNormal, this.description("doAfterTerminateNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doAfterTerminateWithError, this.description("doAfterTerminateWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeEmptyOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeEmptyOnError, this.description("subscribeEmptyOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOneActionOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeOneActionOnError, this.description("subscribeOneActionOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::propagateExceptionSubscribeEmpty, this.description("propagateExceptionSubscribeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenCompletableNormal, this.description("andThenCompletableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenCompletableError, this.description("andThenCompletableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenFlowableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenFlowableNormal, this.description("andThenFlowableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenFlowableError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenFlowableError, this.description("andThenFlowableError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingFactoryThrows, this.description("usingFactoryThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryAndDisposerThrow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingFactoryAndDisposerThrow, this.description("usingFactoryAndDisposerThrow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::usingFactoryReturnsNull, this.description("usingFactoryReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeReportsUnsubscribed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeReportsUnsubscribed, this.description("subscribeReportsUnsubscribed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hookSubscribeStart() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hookSubscribeStart, this.description("hookSubscribeStart"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onStartCalledSafe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onStartCalledSafe, this.description("onStartCalledSafe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFunctionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCompleteFunctionThrows, this.description("onErrorCompleteFunctionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedAfter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeAction2ReportsUnsubscribedAfter, this.description("subscribeAction2ReportsUnsubscribedAfter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedOnErrorAfter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeAction2ReportsUnsubscribedOnErrorAfter, this.description("subscribeAction2ReportsUnsubscribedOnErrorAfter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeOneActionThrowFromOnSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::propagateExceptionSubscribeOneActionThrowFromOnSuccess, this.description("propagateExceptionSubscribeOneActionThrowFromOnSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenNever() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenNever, this.description("andThenNever"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenError, this.description("andThenError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::andThenSingle, this.description("andThenSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFutureNormal, this.description("fromFutureNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFutureThrows, this.description("fromFutureThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromRunnableNormal, this.description("fromRunnableNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromRunnableThrows, this.description("fromRunnableThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnEventComplete, this.description("doOnEventComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnEventError, this.description("doOnEventError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscribeTwoCallbacksDispose, this.description("subscribeTwoCallbacksDispose"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().after();
            super.after();
        }

        private CompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableTest();
        }

        @java.lang.Override
        public CompletableTest implementation() {
            return this.implementation;
        }
    }
}
