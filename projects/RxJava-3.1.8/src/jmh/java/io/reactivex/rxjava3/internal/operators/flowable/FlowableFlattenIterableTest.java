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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterable.FlattenIterableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlattenIterableTest extends RxJavaTest {

    @Test
    public void normal0() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 2).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return Math.max(a, b);
            }
        }).toFlowable().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(ts);
        ts.assertValues(2, 3).assertNoErrors().assertComplete();
    }

    final Function<Integer, Iterable<Integer>> mapper = new Function<Integer, Iterable<Integer>>() {

        @Override
        public Iterable<Integer> apply(Integer v) {
            return Arrays.asList(v, v + 1);
        }
    };

    @Test
    public void normal() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalViaFlatMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).flatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        Flowable.range(1, 5).concatMapIterable(mapper).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 2);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(7);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void longRunning() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        int n = 1000 * 1000;
        Flowable.range(1, n).concatMapIterable(mapper).subscribe(ts);
        ts.assertValueCount(n * 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void asIntermediate() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        int n = 1000 * 1000;
        Flowable.range(1, n).concatMapIterable(mapper).concatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.just(v);
            }
        }).subscribe(ts);
        ts.assertValueCount(n * 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void just() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void justHidden() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).hide().concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void empty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>empty().concatMapIterable(mapper).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void error() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>just(1).concatWith(Flowable.<Integer>error(new TestException())).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsImmediately() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        throw new TestException();
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsImmediatelyJust() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        throw new TestException();
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.just(1).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsSecondCall() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count >= 2) {
                            throw new TestException();
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorNextThrows() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorNextThrowsAndUnsubscribes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        pp.onNext(1);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
        Assert.assertFalse("PublishProcessor has Subscribers?!", pp.hasSubscribers());
    }

    @Test
    public void mixture() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, 1000).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return (v % 2) == 0 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValueCount(500);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void emptyInnerThenSingleBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return v == 2 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void manyEmptyInnerThenSingleBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Flowable.range(1, 1000).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return v == 1000 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void hasNextIsNotCalledAfterChildUnsubscribedOnNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger counter = new AtomicInteger();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        counter.getAndIncrement();
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).take(1).subscribe(ts);
        pp.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
        Assert.assertFalse("PublishProcessor has Subscribers?!", pp.hasSubscribers());
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void normalPrefetchViaFlatMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).flatMapIterable(mapper, 2).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withResultSelectorMaxConcurrent() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 5).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Collections.singletonList(1);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return a * 10 + b;
            }
        }, 2).subscribe(ts);
        ts.assertValues(11, 21, 31, 41, 51);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void flatMapIterablePrefetch() {
        Flowable.just(1, 2).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer t) throws Exception {
                return Arrays.asList(t * 10);
            }
        }, 1).test().assertResult(10, 20);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().flatMapIterable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Arrays.asList(10, 20);
            }
        }));
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapIterable(new Function<Object, Iterable<Integer>>() {

                    @Override
                    public Iterable<Integer> apply(Object v) throws Exception {
                        return Arrays.asList(10, 20);
                    }
                });
            }
        }, false, 1, 1, 10, 20);
    }

    @Test
    public void callableThrows() {
        Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3))).test().assertFailure(TestException.class);
    }

    @Test
    public void fusionMethods() {
        Flowable.just(1, 2).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3))).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.SYNC, qs.requestFusion(QueueFuseable.ANY));
                try {
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(1, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(2, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    qs.clear();
                    assertTrue("Source reports not empty!", qs.isEmpty());
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw ExceptionHelper.wrapOrThrow(ex);
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void smallPrefetch() {
        Flowable.just(1, 2, 3).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3)), 1).test().assertResult(1, 2, 3, 1, 2, 3, 1, 2, 3);
    }

    @Test
    public void smallPrefetch2() {
        Flowable.just(1, 2, 3).hide().flatMapIterable(Functions.justFunction(Collections.emptyList()), 1).test().assertResult();
    }

    @Test
    public void mixedInnerSource() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 0) {
                    return Collections.emptyList();
                }
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 1, 2);
    }

    @Test
    public void mixedInnerSource2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 1) {
                    return Collections.emptyList();
                }
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2);
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).hide().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 1, 2, 1, 2);
    }

    @Test
    public void fusedIsEmptyWithEmptySource() {
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 0) {
                    return Collections.emptyList();
                }
                return Arrays.asList(v);
            }
        }).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.SYNC, qs.requestFusion(QueueFuseable.ANY));
                try {
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(1, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(3, qs.poll().intValue());
                    assertTrue("Source reports being non-empty!", qs.isEmpty());
                } catch (Throwable ex) {
                    throw ExceptionHelper.wrapOrThrow(ex);
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void fusedSourceCrash() {
        Flowable.range(1, 3).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).flatMapIterable(Functions.justFunction(Collections.emptyList()), 1).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Flowable.range(1, 3).flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).take(1).test().assertResult(1);
    }

    @Test
    public void overflowSource() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
                s.onNext(3);
            }
        }.flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).test(0L).assertFailure(QueueOverflowException.class);
    }

    @Test
    public void oneByOne() {
        Flowable.range(1, 3).hide().flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).rebatchRequests(1).test().assertResult(1, 1, 1);
    }

    @Test
    public void cancelAfterHasNext() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 3).hide().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    int count;

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            @Override
                            public boolean hasNext() {
                                if (++count == 2) {
                                    ts.cancel();
                                    ts.onComplete();
                                }
                                return true;
                            }

                            @Override
                            public Integer next() {
                                return 1;
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        }).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void doubleShare() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity()).share().share().count().test().assertResult(600L);
    }

    @Test
    public void multiShare() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        for (int i = 0; i < 5; i++) {
            Flowable<Integer> f = Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity());
            for (int j = 0; j < i; j++) {
                f = f.share();
            }
            f.count().test().withTag("Share: " + i).assertResult(600L);
        }
    }

    @Test
    public void multiShareHidden() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        for (int i = 0; i < 5; i++) {
            Flowable<Integer> f = Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity()).hide();
            for (int j = 0; j < i; j++) {
                f = f.share();
            }
            f.count().test().withTag("Share: " + i).assertResult(600L);
        }
    }

    @Test
    public void failingInnerCancelsSource() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                counter.getAndIncrement();
            }
        }).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            @Override
                            public boolean hasNext() {
                                return true;
                            }

                            @Override
                            public Integer next() {
                                throw new TestException();
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        }).test().assertFailure(TestException.class);
        assertEquals(1, counter.get());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.flatMapIterable(Functions.justFunction(Collections.emptyList()));
            }
        });
    }

    @Test
    public void upstreamFusionRejected() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Collections.<Integer>emptyList()), 128);
        final AtomicLong requested = new AtomicLong();
        f.onSubscribe(new QueueSubscription<Integer>() {

            @Override
            public int requestFusion(int mode) {
                return 0;
            }

            @Override
            public boolean offer(Integer value) {
                return false;
            }

            @Override
            public boolean offer(Integer v1, Integer v2) {
                return false;
            }

            @Override
            public Integer poll() throws Exception {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public void clear() {
            }

            @Override
            public void request(long n) {
                requested.set(n);
            }

            @Override
            public void cancel() {
            }
        });
        assertEquals(128, requested.get());
        assertNotNull(f.queue);
        ts.assertEmpty();
    }

    @Test
    public void onErrorLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Collections.<Integer>emptyList()), 128);
            f.onSubscribe(new BooleanSubscription());
            f.onError(new TestException("first"));
            ts.assertFailureAndMessage(TestException.class, "first");
            assertTrue(errors.isEmpty());
            f.done = false;
            f.onError(new TestException("second"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMapIterable(Functions.justFunction(Collections.emptyList())));
    }

    @Test
    public void fusedCurrentIteratorEmpty() throws Throwable {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Arrays.<Integer>asList(1, 2)), 128);
        f.onSubscribe(new BooleanSubscription());
        f.onNext(1);
        assertFalse(f.isEmpty());
        assertEquals(1, f.poll().intValue());
        assertFalse(f.isEmpty());
        assertEquals(2, f.poll().intValue());
        assertTrue(f.isEmpty());
    }

    @Test
    public void fusionRequestedState() throws Exception {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Arrays.<Integer>asList(1, 2)), 128);
        f.onSubscribe(new BooleanSubscription());
        f.fusionMode = QueueFuseable.NONE;
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.SYNC));
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.ASYNC));
        f.fusionMode = QueueFuseable.SYNC;
        assertEquals(QueueFuseable.SYNC, f.requestFusion(QueueFuseable.SYNC));
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.ASYNC));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal0, this.description("normal0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalViaFlatMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalViaFlatMap, this.description("normalViaFlatMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalBackpressured, this.description("normalBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longRunning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::longRunning, this.description("longRunning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asIntermediate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asIntermediate, this.description("asIntermediate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justHidden, this.description("justHidden"));
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
        public void benchmark_iteratorHasNextThrowsImmediately() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorHasNextThrowsImmediately, this.description("iteratorHasNextThrowsImmediately"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorHasNextThrowsImmediatelyJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorHasNextThrowsImmediatelyJust, this.description("iteratorHasNextThrowsImmediatelyJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorHasNextThrowsSecondCall() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorHasNextThrowsSecondCall, this.description("iteratorHasNextThrowsSecondCall"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorNextThrows, this.description("iteratorNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorNextThrowsAndUnsubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorNextThrowsAndUnsubscribes, this.description("iteratorNextThrowsAndUnsubscribes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixture, this.description("mixture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyInnerThenSingleBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyInnerThenSingleBackpressured, this.description("emptyInnerThenSingleBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyEmptyInnerThenSingleBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyEmptyInnerThenSingleBackpressured, this.description("manyEmptyInnerThenSingleBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextIsNotCalledAfterChildUnsubscribedOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextIsNotCalledAfterChildUnsubscribedOnNext, this.description("hasNextIsNotCalledAfterChildUnsubscribedOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPrefetchViaFlatMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalPrefetchViaFlatMap, this.description("normalPrefetchViaFlatMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withResultSelectorMaxConcurrent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withResultSelectorMaxConcurrent, this.description("withResultSelectorMaxConcurrent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterablePrefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flatMapIterablePrefetch, this.description("flatMapIterablePrefetch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::callableThrows, this.description("callableThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionMethods() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionMethods, this.description("fusionMethods"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_smallPrefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::smallPrefetch, this.description("smallPrefetch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_smallPrefetch2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::smallPrefetch2, this.description("smallPrefetch2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedInnerSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixedInnerSource, this.description("mixedInnerSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedInnerSource2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixedInnerSource2, this.description("mixedInnerSource2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRejected, this.description("fusionRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedIsEmptyWithEmptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedIsEmptyWithEmptySource, this.description("fusedIsEmptyWithEmptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedSourceCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedSourceCrash, this.description("fusedSourceCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::overflowSource, this.description("overflowSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneByOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneByOne, this.description("oneByOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterHasNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAfterHasNext, this.description("cancelAfterHasNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleShare() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleShare, this.description("doubleShare"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiShare() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiShare, this.description("multiShare"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiShareHidden() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiShareHidden, this.description("multiShareHidden"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failingInnerCancelsSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failingInnerCancelsSource, this.description("failingInnerCancelsSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamFusionRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::upstreamFusionRejected, this.description("upstreamFusionRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorLate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorLate, this.description("onErrorLate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCurrentIteratorEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedCurrentIteratorEmpty, this.description("fusedCurrentIteratorEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRequestedState() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusionRequestedState, this.description("fusionRequestedState"));
        }

        private FlowableFlattenIterableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFlattenIterableTest();
        }

        @java.lang.Override
        public FlowableFlattenIterableTest implementation() {
            return this.implementation;
        }
    }
}
