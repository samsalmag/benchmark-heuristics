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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSingleTest extends RxJavaTest {

    @Test
    public void singleFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).// 
        doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).// 
        singleElement().// 
        toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                request(2);
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).// 
        doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).// 
        singleElement().// 
        toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(3);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleRequestsExactlyWhatItNeedsIf1RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).// 
        doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).// 
        singleElement().// 
        toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).single(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).single(3).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().single(1).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithBackpressureFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).singleElement().toFlowable();
        Subscriber<Integer> subscriber = spy(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void single() {
        Maybe<Integer> maybe = Flowable.just(1).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElements() {
        Maybe<Integer> maybe = Flowable.just(1, 2).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmpty() {
        Maybe<Integer> maybe = Flowable.<Integer>empty().singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitItem() {
        final AtomicLong request = new AtomicLong();
        Flowable.just(1).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                request.addAndGet(n);
            }
        }).blockingSingle();
        // FIXME single now triggers fast-path
        assertEquals(Long.MAX_VALUE, request.get());
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty() {
        final AtomicLong request = new AtomicLong();
        try {
            Flowable.empty().doOnRequest(new LongConsumer() {

                @Override
                public void accept(long n) {
                    request.addAndGet(n);
                }
            }).blockingSingle();
        } catch (NoSuchElementException e) {
            // FIXME single now triggers fast-path
            assertEquals(Long.MAX_VALUE, request.get());
        }
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne() {
        final AtomicLong request = new AtomicLong();
        try {
            Flowable.just(1, 2).doOnRequest(new LongConsumer() {

                @Override
                public void accept(long n) {
                    request.addAndGet(n);
                }
            }).blockingSingle();
        } catch (IllegalArgumentException e) {
            // FIXME single now triggers fast-path
            assertEquals(Long.MAX_VALUE, request.get());
        }
    }

    @Test
    public void singleWithPredicate() {
        Maybe<Integer> maybe = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElements() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmpty() {
        Maybe<Integer> maybe = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefault() {
        Single<Integer> single = Flowable.just(1).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElements() {
        Single<Integer> single = Flowable.just(1, 2).single(3);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmpty() {
        Single<Integer> single = Flowable.<Integer>empty().single(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicate() {
        Single<Integer> single = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElements() {
        Single<Integer> single = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmpty() {
        Single<Integer> single = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleOrErrorNoElement() {
        Flowable.empty().singleOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void singleOrErrorOneElement() {
        Flowable.just(1).singleOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void singleOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).singleOrError().test().assertNoValues().assertError(IllegalArgumentException.class);
    }

    @Test
    public void singleOrErrorError() {
        Flowable.error(new RuntimeException("error")).singleOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void issue1527Flowable() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Flowable<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        }).toFlowable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleElementOperatorDoNotSwallowExceptionWhenDone() {
        final Throwable exception = new RuntimeException("some error");
        final AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    error.set(throwable);
                }
            });
            Flowable.unsafeCreate(new Publisher<Integer>() {

                @Override
                public void subscribe(final Subscriber<? super Integer> subscriber) {
                    subscriber.onComplete();
                    subscriber.onError(exception);
                }
            }).singleElement().test().assertComplete();
            assertSame(exception, error.get().getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleOrError();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleElement();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleOrError().toFlowable();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleOrError().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleElement().toFlowable();
            }
        });
    }

    @Test
    public void cancelAsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.singleOrError().toFlowable().test();
        assertTrue(pp.hasSubscribers());
        ts.assertEmpty();
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void singleOrError() {
        Flowable.empty().singleOrError().toFlowable().test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().single(1));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleFlowable, this.description("singleFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithTooManyElementsFlowable, this.description("singleWithTooManyElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithEmptyFlowable, this.description("singleWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable, this.description("singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable, this.description("singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleRequestsExactlyWhatItNeedsIf1RequestedFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleRequestsExactlyWhatItNeedsIf1RequestedFlowable, this.description("singleRequestsExactlyWhatItNeedsIf1RequestedFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateFlowable, this.description("singleWithPredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndTooManyElementsFlowable, this.description("singleWithPredicateAndTooManyElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndEmptyFlowable, this.description("singleWithPredicateAndEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultFlowable, this.description("singleOrDefaultFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithTooManyElementsFlowable, this.description("singleOrDefaultWithTooManyElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithEmptyFlowable, this.description("singleOrDefaultWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateFlowable, this.description("singleOrDefaultWithPredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElementsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndTooManyElementsFlowable, this.description("singleOrDefaultWithPredicateAndTooManyElementsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndEmptyFlowable, this.description("singleOrDefaultWithPredicateAndEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithBackpressureFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithBackpressureFlowable, this.description("singleWithBackpressureFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single, this.description("single"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithTooManyElements, this.description("singleWithTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithEmpty, this.description("singleWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoesNotRequestMoreThanItNeedsToEmitItem, this.description("singleDoesNotRequestMoreThanItNeedsToEmitItem"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty, this.description("singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne, this.description("singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicate, this.description("singleWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndTooManyElements, this.description("singleWithPredicateAndTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleWithPredicateAndEmpty, this.description("singleWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefault, this.description("singleOrDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithTooManyElements, this.description("singleOrDefaultWithTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithEmpty, this.description("singleOrDefaultWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicate, this.description("singleOrDefaultWithPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndTooManyElements, this.description("singleOrDefaultWithPredicateAndTooManyElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrDefaultWithPredicateAndEmpty, this.description("singleOrDefaultWithPredicateAndEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1527, this.description("issue1527"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorNoElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorNoElement, this.description("singleOrErrorNoElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorOneElement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorOneElement, this.description("singleOrErrorOneElement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorMultipleElements() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorMultipleElements, this.description("singleOrErrorMultipleElements"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrErrorError, this.description("singleOrErrorError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527Flowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1527Flowable, this.description("issue1527Flowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleElementOperatorDoNotSwallowExceptionWhenDone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleElementOperatorDoNotSwallowExceptionWhenDone, this.description("singleElementOperatorDoNotSwallowExceptionWhenDone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAsFlowable, this.description("cancelAsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleOrError, this.description("singleOrError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        private FlowableSingleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableSingleTest();
        }

        @java.lang.Override
        public FlowableSingleTest implementation() {
            return this.implementation;
        }
    }
}
