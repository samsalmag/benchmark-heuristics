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
package io.reactivex.rxjava3.observable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Verifies the operators handle null values properly by emitting/throwing NullPointerExceptions.
 */
public class ObservableNullTests extends RxJavaTest {

    Observable<Integer> just1 = Observable.just(1);

    // ***********************************************************
    // Static methods
    // ***********************************************************
    @Test
    public void ambIterableIteratorNull() {
        Observable.amb(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Observable.amb(Arrays.asList(Observable.never(), null)).test().assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableIteratorNull() {
        Observable.combineLatest(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
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
    public void combineLatestIterableOneIsNull() {
        Observable.combineLatest(Arrays.asList(Observable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableFunctionReturnsNull() {
        Observable.combineLatest(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableIteratorNull() {
        Observable.combineLatestDelayError(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
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
        Observable.combineLatestDelayError(Arrays.asList(Observable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableFunctionReturnsNull() {
        Observable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Observable.concat(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableOneIsNull() {
        Observable.concat(Arrays.asList(just1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatArrayOneIsNull() {
        Observable.concatArray(just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void deferFunctionReturnsNull() {
        Observable.defer(new Supplier<Observable<Object>>() {

            @Override
            public Observable<Object> get() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void errorFunctionReturnsNull() {
        Observable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void fromArrayOneIsNull() {
        Observable.fromArray(1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromCallableReturnsNull() {
        Observable.fromCallable(new Callable<Object>() {

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
        TestObserver<Object> to = new TestObserver<>();
        Observable.fromFuture(f).subscribe(to);
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureTimedReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Observable.fromFuture(f, 1, TimeUnit.SECONDS).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableIteratorNull() {
        Observable.fromIterable(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableValueNull() {
        Observable.fromIterable(Arrays.asList(1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateConsumerEmitsNull() {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> s) {
                s.onNext(null);
            }
        }).blockingLast();
    }

    @Test
    public void generateConsumerStateNullAllowed() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onComplete();
            }
        };
        Observable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return null;
            }
        }, generator).blockingSubscribe();
    }

    @Test
    public void generateFunctionStateNullAllowed() {
        Observable.generate(new Supplier<Object>() {

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

    public void intervalSchedulerNull() {
        Observable.interval(1, TimeUnit.SECONDS, null);
    }

    @Test
    public void justNull() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<Observable> clazz = Observable.class;
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
        Observable.merge(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableOneIsNull() {
        Observable.merge(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Observable.mergeDelayError(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableOneIsNull() {
        Observable.mergeDelayError(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void usingObservableSupplierReturnsNull() {
        Observable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Object d) {
                return null;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) {
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableIteratorNull() {
        Observable.zip(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
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
        Observable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2IteratorNull() {
        Observable.zip(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
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
        Observable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

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
        just1.buffer(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
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
    public void collectInitialCollectorNull() {
        just1.collect(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, null);
    }

    @Test(expected = NullPointerException.class)
    public void concatMapReturnsNull() {
        just1.concatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
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
        just1.debounce(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayWithFunctionReturnsNull() {
        just1.delay(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayBothItemSupplierReturnsNull() {
        just1.delay(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
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
        Observable.range(1, 2).distinctUntilChanged(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).test().assertResult(1);
    }

    @Test(expected = NullPointerException.class)
    public void flatMapFunctionReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnNextReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
                return null;
            }
        }, new Function<Throwable, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return just1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnCompleteReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
                return just1;
            }
        }, new Function<Throwable, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerMapperReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
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
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
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
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
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
        just1.lift(new ObservableOperator<Object, Integer>() {

            @Override
            public Observer<? super Integer> apply(Observer<? super Object> observer) {
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

    @Test(expected = NullPointerException.class)
    public void onErrorResumeNextFunctionReturnsNull() {
        Observable.error(new TestException()).onErrorResumeNext(new Function<Throwable, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Throwable e) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void onErrorReturnFunctionReturnsNull() {
        Observable.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

            @Override
            public Object apply(Throwable e) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishFunctionReturnsNull() {
        just1.publish(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void reduceFunctionReturnsNull() {
        Observable.just(1, 1).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingGet();
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
        just1.repeatWhen(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> o) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayBoundedSelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayTimeBoundedSelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }, 1, TimeUnit.SECONDS, Schedulers.single()).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void retryWhenFunctionReturnsNull() {
        Observable.error(new TestException()).retryWhen(new Function<Observable<? extends Throwable>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<? extends Throwable> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanFunctionReturnsNull() {
        Observable.just(1, 1).scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
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
    public void switchMapFunctionReturnsNull() {
        just1.switchMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorReturnsNull() {
        just1.timeout(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutFirstItemReturnsNull() {
        Observable.just(1, 1).timeout(Observable.never(), new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNull() {
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
    public void windowOpenCloseCloseReturnsNull() {
        Observable.never().window(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
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
        Observable.just(1, 2).zipWith(Arrays.asList(1, null), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
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

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

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
        public void benchmark_combineLatestDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableIteratorNull, this.description("combineLatestDelayErrorIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableOneIsNull, this.description("combineLatestDelayErrorIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::combineLatestDelayErrorIterableFunctionReturnsNull, this.description("combineLatestDelayErrorIterableFunctionReturnsNull"), java.lang.NullPointerException.class);
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
        public void benchmark_usingObservableSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingObservableSupplierReturnsNull, this.description("usingObservableSupplierReturnsNull"), java.lang.NullPointerException.class);
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
        public void benchmark_collectInitialCollectorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::collectInitialCollectorNull, this.description("collectInitialCollectorNull"), java.lang.NullPointerException.class);
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
            this.runExceptionBenchmark(this.implementation()::onErrorResumeNextFunctionReturnsNull, this.description("onErrorResumeNextFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onErrorReturnFunctionReturnsNull, this.description("onErrorReturnFunctionReturnsNull"), java.lang.NullPointerException.class);
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
        public void benchmark_timeoutFirstItemReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::timeoutFirstItemReturnsNull, this.description("timeoutFirstItemReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::toListSupplierReturnsNull, this.description("toListSupplierReturnsNull"), java.lang.NullPointerException.class);
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
        public void benchmark_windowOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::windowOpenCloseCloseReturnsNull, this.description("windowOpenCloseCloseReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::withLatestFromCombinerReturnsNull, this.description("withLatestFromCombinerReturnsNull"), java.lang.NullPointerException.class);
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
        public void benchmark_zipWithCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithCombinerReturnsNull, this.description("zipWithCombinerReturnsNull"), java.lang.NullPointerException.class);
        }

        private ObservableNullTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableNullTests();
        }

        @java.lang.Override
        public ObservableNullTests implementation() {
            return this.implementation;
        }
    }
}
