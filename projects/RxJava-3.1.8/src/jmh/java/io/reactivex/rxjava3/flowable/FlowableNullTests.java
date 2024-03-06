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
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

/**
 * Verifies the operators handle null values properly by emitting/throwing NullPointerExceptions.
 */
public class FlowableNullTests extends RxJavaTest {

    Flowable<Integer> just1 = Flowable.just(1);

    // ***********************************************************
    // Static methods
    // ***********************************************************
    @Test(expected = NullPointerException.class)
    public void ambVarargsOneIsNull() {
        Flowable.ambArray(Flowable.never(), null).blockingLast();
    }

    @Test
    public void ambIterableIteratorNull() {
        Flowable.amb(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Flowable.amb(Arrays.asList(Flowable.never(), null)).test().assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableIteratorNull() {
        Flowable.combineLatestDelayError(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableOneIsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(Flowable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableFunctionReturnsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Flowable.concat(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableOneIsNull() {
        Flowable.concat(Arrays.asList(just1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatArrayOneIsNull() {
        Flowable.concatArray(just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void deferFunctionReturnsNull() {
        Flowable.defer(new Supplier<Publisher<Object>>() {

            @Override
            public Publisher<Object> get() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void errorFunctionReturnsNull() {
        Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void fromArrayOneIsNull() {
        Flowable.fromArray(1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromCallableReturnsNull() {
        Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return null;
            }
        }).blockingLast();
    }

    @Test
    public void fromFutureReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.fromFuture(f).subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureTimedReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Flowable.fromFuture(f, 1, TimeUnit.SECONDS).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableIteratorNull() {
        Flowable.fromIterable(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableValueNull() {
        Flowable.fromIterable(Arrays.asList(1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateConsumerEmitsNull() {
        Flowable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> s) {
                s.onNext(null);
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateStateConsumerInitialStateNull() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onNext(1);
            }
        };
        Flowable.generate(null, generator);
    }

    @Test(expected = NullPointerException.class)
    public void generateStateFunctionInitialStateNull() {
        Flowable.generate(null, new BiFunction<Object, Emitter<Object>, Object>() {

            @Override
            public Object apply(Object s, Emitter<Object> o) {
                o.onNext(1);
                return s;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void generateStateConsumerNull() {
        Flowable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, (BiConsumer<Integer, Emitter<Object>>) null);
    }

    @Test
    public void generateConsumerStateNullAllowed() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onComplete();
            }
        };
        Flowable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return null;
            }
        }, generator).blockingSubscribe();
    }

    @Test
    public void generateFunctionStateNullAllowed() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Emitter<Object>, Object>() {

            @Override
            public Object apply(Object s, Emitter<Object> o) {
                o.onComplete();
                return s;
            }
        }).blockingSubscribe();
    }

    @Test
    public void justNull() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<Flowable> clazz = Flowable.class;
        for (int argCount = 1; argCount < 10; argCount++) {
            for (int argNull = 1; argNull <= argCount; argNull++) {
                Class<?>[] params = new Class[argCount];
                Arrays.fill(params, Object.class);
                Object[] values = new Object[argCount];
                Arrays.fill(values, 1);
                values[argNull - 1] = null;
                Method m = clazz.getMethod("just", params);
                try {
                    m.invoke(null, values);
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableIteratorNull() {
        Flowable.merge(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableOneIsNull() {
        Flowable.merge(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeArrayOneIsNull() {
        Flowable.mergeArray(128, 128, just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Flowable.mergeDelayError(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableOneIsNull() {
        Flowable.mergeDelayError(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorArrayOneIsNull() {
        Flowable.mergeArrayDelayError(128, 128, just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void usingFlowableSupplierReturnsNull() {
        Flowable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Object d) {
                return null;
            }
        }, Functions.emptyConsumer()).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableIteratorNull() {
        Flowable.zip(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableFunctionReturnsNull() {
        Flowable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2Null() {
        Flowable.zip((Iterable<Publisher<Object>>) null, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return 1;
            }
        }, true, 128);
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2IteratorNull() {
        Flowable.zip(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return 1;
            }
        }, true, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2FunctionReturnsNull() {
        Flowable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }, true, 128).blockingLast();
    }

    // *************************************************************
    // Instance methods
    // *************************************************************
    @Test(expected = NullPointerException.class)
    public void bufferSupplierReturnsNull() {
        just1.buffer(1, 1, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferTimedSupplierReturnsNull() {
        just1.buffer(1L, 1L, TimeUnit.SECONDS, Schedulers.single(), new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferOpenCloseCloseReturnsNull() {
        just1.buffer(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferBoundarySupplierReturnsNull() {
        just1.buffer(just1, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void collectInitialSupplierReturnsNull() {
        just1.collect(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiConsumer<Object, Integer>() {

            @Override
            public void accept(Object a, Integer b) {
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapReturnsNull() {
        just1.concatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapIterableReturnNull() {
        just1.concatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapIterableIteratorNull() {
        just1.concatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return new Iterable<Object>() {

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }
                };
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void debounceFunctionReturnsNull() {
        just1.debounce(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayWithFunctionReturnsNull() {
        just1.delay(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayBothItemSupplierReturnsNull() {
        just1.delay(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void distinctSupplierReturnsNull() {
        just1.distinct(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Collection<Object>>() {

            @Override
            public Collection<Object> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void distinctFunctionReturnsNull() {
        just1.distinct(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test
    public void distinctUntilChangedFunctionReturnsNull() {
        Flowable.range(1, 2).distinctUntilChanged(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).test().assertResult(1);
    }

    @Test(expected = NullPointerException.class)
    public void flatMapFunctionReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnNextReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return null;
            }
        }, new Function<Throwable, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return just1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnCompleteReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return just1;
            }
        }, new Function<Throwable, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerMapperReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerCombinerReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return just1;
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperReturnsNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperIteratorNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return new Iterable<Object>() {

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }
                };
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperIterableOneNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(1, null);
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableCombinerReturnsNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(1);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    public void groupByKeyNull() {
        just1.groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void groupByValueReturnsNull() {
        just1.groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void liftReturnsNull() {
        just1.lift(new FlowableOperator<Object, Integer>() {

            @Override
            public Subscriber<? super Integer> apply(Subscriber<? super Object> s) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void mapReturnsNull() {
        just1.map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test
    public void onErrorResumeNextFunctionReturnsNull() {
        try {
            Flowable.error(new TestException()).onErrorResumeNext(new Function<Throwable, Publisher<Object>>() {

                @Override
                public Publisher<Object> apply(Throwable e) {
                    return null;
                }
            }).blockingSubscribe();
            fail("Should have thrown");
        } catch (CompositeException ex) {
            List<Throwable> errors = ex.getExceptions();
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class);
            assertEquals(2, errors.size());
        }
    }

    @Test
    public void onErrorReturnFunctionReturnsNull() {
        try {
            Flowable.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

                @Override
                public Object apply(Throwable e) {
                    return null;
                }
            }).blockingSubscribe();
            fail("Should have thrown");
        } catch (CompositeException ex) {
            List<Throwable> errors = TestHelper.compositeList(ex);
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class, "The valueSupplier returned a null value");
        }
    }

    @Test(expected = NullPointerException.class)
    public void publishFunctionReturnsNull() {
        just1.publish(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void reduceFunctionReturnsNull() {
        Flowable.just(1, 1).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).toFlowable().blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void reduceSeedFunctionReturnsNull() {
        just1.reduce(1, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void reduceWithSeedNull() {
        just1.reduceWith(null, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void reduceWithSeedReturnsNull() {
        just1.reduceWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void repeatWhenFunctionReturnsNull() {
        just1.repeatWhen(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorNull() {
        just1.replay((Function<Flowable<Integer>, Flowable<Integer>>) null);
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayBoundedSelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayTimeBoundedSelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }, 1, TimeUnit.SECONDS, Schedulers.single()).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void retryWhenFunctionReturnsNull() {
        Flowable.error(new TestException()).retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanFunctionReturnsNull() {
        Flowable.just(1, 1).scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedNull() {
        just1.scan(null, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedFunctionReturnsNull() {
        just1.scan(1, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedSupplierReturnsNull() {
        just1.scanWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedSupplierFunctionReturnsNull() {
        just1.scanWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void startWithIterableIteratorNull() {
        just1.startWithIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void startWithIterableOneNull() {
        just1.startWithIterable(Arrays.asList(1, null)).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void startWithArrayOneNull() {
        just1.startWithArray(1, null).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void switchMapFunctionReturnsNull() {
        just1.switchMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorReturnsNull() {
        just1.timeout(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorOtherNull() {
        just1.timeout(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return just1;
            }
        }, null);
    }

    @Test(expected = NullPointerException.class)
    public void timeoutFirstItemReturnsNull() {
        just1.timeout(Flowable.never(), new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timestampUnitNull() {
        just1.timestamp(null, Schedulers.single());
    }

    @Test(expected = NullPointerException.class)
    public void timestampSchedulerNull() {
        just1.timestamp(TimeUnit.SECONDS, null);
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNull() {
        just1.toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).toFlowable().blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNullSingle() {
        just1.toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test
    public void toMapValueSelectorReturnsNull() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMapMapSupplierReturnsNull() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Object, Object>>() {

            @Override
            public Map<Object, Object> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test
    public void toMultiMapValueSelectorReturnsNullAllowed() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMultimapMapSupplierReturnsNull() {
        just1.toMultimap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Object, Collection<Object>>>() {

            @Override
            public Map<Object, Collection<Object>> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMultimapMapCollectionSupplierReturnsNull() {
        just1.toMultimap(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Integer, Collection<Integer>>>() {

            @Override
            public Map<Integer, Collection<Integer>> get() {
                return new HashMap<>();
            }
        }, new Function<Integer, Collection<Integer>>() {

            @Override
            public Collection<Integer> apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void windowOpenCloseOpenNull() {
        just1.window(null, new Function<Object, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Object v) {
                return just1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void windowOpenCloseCloseReturnsNull() {
        Flowable.never().window(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void withLatestFromOtherNull() {
        just1.withLatestFrom(null, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void withLatestFromCombinerReturnsNull() {
        just1.withLatestFrom(just1, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableNull() {
        just1.zipWith((Iterable<Integer>) null, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableCombinerReturnsNull() {
        just1.zipWith(Arrays.asList(1), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableIteratorNull() {
        just1.zipWith(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableOneIsNull() {
        Flowable.just(1, 2).zipWith(Arrays.asList(1, null), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithPublisherNull() {
        just1.zipWith((Publisher<Integer>) null, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void zipWithCombinerReturnsNull() {
        just1.zipWith(just1, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    // *********************************************
    // Subject null tests
    // *********************************************
    @Test(expected = NullPointerException.class)
    public void asyncSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = AsyncProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void asyncSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = AsyncProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void behaviorSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = BehaviorProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void behaviorSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = BehaviorProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaycSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = ReplayProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = ReplayProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void serializedcSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.<Integer>create().toSerialized();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void serializedSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.<Integer>create().toSerialized();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableFunctionReturnsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableIteratorNull() {
        Flowable.combineLatestDelayError(new Iterable<Flowable<Object>>() {

            @Override
            public Iterator<Flowable<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableOneIsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(Flowable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambVarargsOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::ambVarargsOneIsNull, this.description("ambVarargsOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIteratorNull, this.description("ambIterableIteratorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableOneIsNull, this.description("ambIterableOneIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestIterableIteratorNull, this.description("combineLatestIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestIterableOneIsNull, this.description("combineLatestIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestIterableFunctionReturnsNull, this.description("combineLatestIterableFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIteratorNull, this.description("concatIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableOneIsNull, this.description("concatIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatArrayOneIsNull, this.description("concatArrayOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::deferFunctionReturnsNull, this.description("deferFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorFunctionReturnsNull, this.description("errorFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromArrayOneIsNull, this.description("fromArrayOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromCallableReturnsNull, this.description("fromCallableReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromFutureReturnsNull, this.description("fromFutureReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimedReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromFutureTimedReturnsNull, this.description("fromFutureTimedReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromIterableIteratorNull, this.description("fromIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterableValueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromIterableValueNull, this.description("fromIterableValueNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateConsumerEmitsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::generateConsumerEmitsNull, this.description("generateConsumerEmitsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateStateConsumerInitialStateNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::generateStateConsumerInitialStateNull, this.description("generateStateConsumerInitialStateNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateStateFunctionInitialStateNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::generateStateFunctionInitialStateNull, this.description("generateStateFunctionInitialStateNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateStateConsumerNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::generateStateConsumerNull, this.description("generateStateConsumerNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateConsumerStateNullAllowed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::generateConsumerStateNullAllowed, this.description("generateConsumerStateNullAllowed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateFunctionStateNullAllowed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::generateFunctionStateNullAllowed, this.description("generateFunctionStateNullAllowed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justNull, this.description("justNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIteratorNull, this.description("mergeIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableOneIsNull, this.description("mergeIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeArrayOneIsNull, this.description("mergeArrayOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableIteratorNull, this.description("mergeDelayErrorIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorIterableOneIsNull, this.description("mergeDelayErrorIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeDelayErrorArrayOneIsNull, this.description("mergeDelayErrorArrayOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFlowableSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingFlowableSupplierReturnsNull, this.description("usingFlowableSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableIteratorNull, this.description("zipIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableFunctionReturnsNull, this.description("zipIterableFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable2Null() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterable2Null, this.description("zipIterable2Null"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable2IteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterable2IteratorNull, this.description("zipIterable2IteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable2FunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterable2FunctionReturnsNull, this.description("zipIterable2FunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::bufferSupplierReturnsNull, this.description("bufferSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::bufferTimedSupplierReturnsNull, this.description("bufferTimedSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::bufferOpenCloseCloseReturnsNull, this.description("bufferOpenCloseCloseReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferBoundarySupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::bufferBoundarySupplierReturnsNull, this.description("bufferBoundarySupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectInitialSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::collectInitialSupplierReturnsNull, this.description("collectInitialSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatMapReturnsNull, this.description("concatMapReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapIterableReturnNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatMapIterableReturnNull, this.description("concatMapIterableReturnNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatMapIterableIteratorNull, this.description("concatMapIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::debounceFunctionReturnsNull, this.description("debounceFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::delayWithFunctionReturnsNull, this.description("delayWithFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayBothItemSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::delayBothItemSupplierReturnsNull, this.description("delayBothItemSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::distinctSupplierReturnsNull, this.description("distinctSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::distinctFunctionReturnsNull, this.description("distinctFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::distinctUntilChangedFunctionReturnsNull, this.description("distinctUntilChangedFunctionReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapFunctionReturnsNull, this.description("flatMapFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotificationOnNextReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapNotificationOnNextReturnsNull, this.description("flatMapNotificationOnNextReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotificationOnCompleteReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapNotificationOnCompleteReturnsNull, this.description("flatMapNotificationOnCompleteReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapCombinerMapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapCombinerMapperReturnsNull, this.description("flatMapCombinerMapperReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapCombinerCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapCombinerCombinerReturnsNull, this.description("flatMapCombinerCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapIterableMapperReturnsNull, this.description("flatMapIterableMapperReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapIterableMapperIteratorNull, this.description("flatMapIterableMapperIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperIterableOneNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapIterableMapperIterableOneNull, this.description("flatMapIterableMapperIterableOneNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapIterableCombinerReturnsNull, this.description("flatMapIterableCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByValueReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::groupByValueReturnsNull, this.description("groupByValueReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::liftReturnsNull, this.description("liftReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mapReturnsNull, this.description("mapReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextFunctionReturnsNull, this.description("onErrorResumeNextFunctionReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorReturnFunctionReturnsNull, this.description("onErrorReturnFunctionReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::publishFunctionReturnsNull, this.description("publishFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::reduceFunctionReturnsNull, this.description("reduceFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceSeedFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::reduceSeedFunctionReturnsNull, this.description("reduceSeedFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithSeedNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::reduceWithSeedNull, this.description("reduceWithSeedNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithSeedReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::reduceWithSeedReturnsNull, this.description("reduceWithSeedReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::repeatWhenFunctionReturnsNull, this.description("repeatWhenFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replaySelectorNull, this.description("replaySelectorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replaySelectorReturnsNull, this.description("replaySelectorReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayBoundedSelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replayBoundedSelectorReturnsNull, this.description("replayBoundedSelectorReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayTimeBoundedSelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replayTimeBoundedSelectorReturnsNull, this.description("replayTimeBoundedSelectorReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryWhenFunctionReturnsNull, this.description("retryWhenFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::scanFunctionReturnsNull, this.description("scanFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::scanSeedNull, this.description("scanSeedNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::scanSeedFunctionReturnsNull, this.description("scanSeedFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::scanSeedSupplierReturnsNull, this.description("scanSeedSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedSupplierFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::scanSeedSupplierFunctionReturnsNull, this.description("scanSeedSupplierFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::startWithIterableIteratorNull, this.description("startWithIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithIterableOneNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::startWithIterableOneNull, this.description("startWithIterableOneNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithArrayOneNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::startWithArrayOneNull, this.description("startWithArrayOneNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::switchMapFunctionReturnsNull, this.description("switchMapFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timeoutSelectorReturnsNull, this.description("timeoutSelectorReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorOtherNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timeoutSelectorOtherNull, this.description("timeoutSelectorOtherNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutFirstItemReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timeoutFirstItemReturnsNull, this.description("timeoutFirstItemReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampUnitNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timestampUnitNull, this.description("timestampUnitNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampSchedulerNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timestampSchedulerNull, this.description("timestampSchedulerNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toListSupplierReturnsNull, this.description("toListSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNullSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toListSupplierReturnsNullSingle, this.description("toListSupplierReturnsNullSingle"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapValueSelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapValueSelectorReturnsNull, this.description("toMapValueSelectorReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapMapSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toMapMapSupplierReturnsNull, this.description("toMapMapSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultiMapValueSelectorReturnsNullAllowed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMultiMapValueSelectorReturnsNullAllowed, this.description("toMultiMapValueSelectorReturnsNullAllowed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapMapSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toMultimapMapSupplierReturnsNull, this.description("toMultimapMapSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapMapCollectionSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toMultimapMapCollectionSupplierReturnsNull, this.description("toMultimapMapCollectionSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenCloseOpenNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::windowOpenCloseOpenNull, this.description("windowOpenCloseOpenNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::windowOpenCloseCloseReturnsNull, this.description("windowOpenCloseCloseReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromOtherNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::withLatestFromOtherNull, this.description("withLatestFromOtherNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::withLatestFromCombinerReturnsNull, this.description("withLatestFromCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithIterableNull, this.description("zipWithIterableNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithIterableCombinerReturnsNull, this.description("zipWithIterableCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithIterableIteratorNull, this.description("zipWithIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithIterableOneIsNull, this.description("zipWithIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithPublisherNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithPublisherNull, this.description("zipWithPublisherNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithCombinerReturnsNull, this.description("zipWithCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectOnNextNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::asyncSubjectOnNextNull, this.description("asyncSubjectOnNextNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectOnErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::asyncSubjectOnErrorNull, this.description("asyncSubjectOnErrorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectOnNextNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::behaviorSubjectOnNextNull, this.description("behaviorSubjectOnNextNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectOnErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::behaviorSubjectOnErrorNull, this.description("behaviorSubjectOnErrorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectOnNextNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::publishSubjectOnNextNull, this.description("publishSubjectOnNextNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectOnErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::publishSubjectOnErrorNull, this.description("publishSubjectOnErrorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaycSubjectOnNextNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replaycSubjectOnNextNull, this.description("replaycSubjectOnNextNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectOnErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::replaySubjectOnErrorNull, this.description("replaySubjectOnErrorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedcSubjectOnNextNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::serializedcSubjectOnNextNull, this.description("serializedcSubjectOnNextNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedSubjectOnErrorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::serializedSubjectOnErrorNull, this.description("serializedSubjectOnErrorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableFunctionReturnsNull, this.description("combineLatestDelayErrorIterableFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableIteratorNull, this.description("combineLatestDelayErrorIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableOneIsNull, this.description("combineLatestDelayErrorIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        private FlowableNullTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableNullTests();
        }

        @java.lang.Override
        public FlowableNullTests implementation() {
            return this.implementation;
        }
    }
}
