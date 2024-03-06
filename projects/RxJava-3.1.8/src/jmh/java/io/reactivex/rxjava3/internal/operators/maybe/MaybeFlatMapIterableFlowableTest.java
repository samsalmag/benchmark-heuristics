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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeFlatMapIterableFlowableTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void emptyIterable() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Collections.<Integer>emptyList();
            }
        }).test().assertResult();
    }

    @Test
    public void error() {
        Maybe.<Integer>error(new TestException()).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void empty() {
        Maybe.<Integer>empty().flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertResult();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test(0);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void take() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).take(1).test().assertResult(1);
    }

    @Test
    public void take2() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).doOnSubscribe(s -> s.request(Long.MAX_VALUE)).take(1).test().assertResult(1);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2);
        ;
    }

    @Test
    public void fusedNoSync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2);
        ;
    }

    @Test
    public void iteratorCrash() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(1, 100, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextCrash() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 1, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextCrash() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 100, 1);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void hasNextCrash2() {
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 2, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void async1() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).hide().observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async2() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async3() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).take(500 * 1000).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async4() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).take(500 * 1000).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void fusedEmptyCheck() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Arrays.asList(1, 2, 3);
            }
        }).subscribe(new FlowableSubscriber<Integer>() {

            QueueSubscription<Integer> qs;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Subscription s) {
                qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.ASYNC, qs.requestFusion(QueueFuseable.ANY));
            }

            @Override
            public void onNext(Integer value) {
                assertFalse(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                qs.cancel();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void hasNextThrowsUnbounded() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return new CrashingIterable(100, 2, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void nextThrowsUnbounded() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return new CrashingIterable(100, 100, 1);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void hasNextThrows() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return new CrashingIterable(100, 2, 100);
            }
        }).to(TestHelper.<Integer>testSubscriber(2L)).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void nextThrows() {
        Maybe.just(1).flattenAsFlowable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return new CrashingIterable(100, 100, 1);
            }
        }).to(TestHelper.<Integer>testSubscriber(2L)).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void requestBefore() {
        for (int i = 0; i < 500; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            ps.singleElement().flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

                @Override
                public Iterable<Integer> apply(Integer v) throws Exception {
                    return Arrays.asList(1, 2, 3);
                }
            }).test(5L).assertEmpty();
        }
    }

    @Test
    public void requestCreateInnerRace() {
        final Integer[] a = new Integer[1000];
        Arrays.fill(a, 1);
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            ps.onNext(1);
            final TestSubscriber<Integer> ts = ps.singleElement().flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

                @Override
                public Iterable<Integer> apply(Integer v) throws Exception {
                    return Arrays.asList(a);
                }
            }).test(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                    for (int i = 0; i < 500; i++) {
                        ts.request(1);
                    }
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < 500; i++) {
                        ts.request(1);
                    }
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancelCreateInnerRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            ps.onNext(1);
            final TestSubscriber<Integer> ts = ps.singleElement().flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

                @Override
                public Iterable<Integer> apply(Integer v) throws Exception {
                    return Arrays.asList(1, 2, 3);
                }
            }).test(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void slowPathCancelAfterHasNext() {
        final Integer[] a = new Integer[1000];
        Arrays.fill(a, 1);
        final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            int count;

                            @Override
                            public boolean hasNext() {
                                if (count++ == 2) {
                                    ts.cancel();
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
        ts.request(3);
        ts.assertValues(1, 1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void fastPathCancelAfterHasNext() {
        final Integer[] a = new Integer[1000];
        Arrays.fill(a, 1);
        final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Maybe.just(1).flattenAsFlowable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            int count;

                            @Override
                            public boolean hasNext() {
                                if (count++ == 2) {
                                    ts.cancel();
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
        ts.request(Long.MAX_VALUE);
        ts.assertValues(1, 1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(MaybeSubject.create().flattenAsFlowable(v -> Arrays.asList(v)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToFlowable(m -> m.flattenAsFlowable(v -> Arrays.asList(v)));
    }

    @Test
    public void onSuccessRequestRace() {
        List<Object> list = Arrays.asList(1);
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            MaybeSubject<Integer> ms = MaybeSubject.create();
            TestSubscriber<Object> ts = ms.flattenAsFlowable(v -> list).test(0L);
            TestHelper.race(() -> ms.onSuccess(1), () -> ts.request(1));
            ts.assertResult(1);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyIterable, this.description("emptyIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take2, this.description("take2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedNoSync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedNoSync, this.description("fusedNoSync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorCrash, this.description("iteratorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextCrash, this.description("hasNextCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextCrash, this.description("nextCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextCrash2, this.description("hasNextCrash2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async1, this.description("async1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async2, this.description("async2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async3, this.description("async3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async4, this.description("async4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmptyCheck() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedEmptyCheck, this.description("fusedEmptyCheck"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsUnbounded() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrowsUnbounded, this.description("hasNextThrowsUnbounded"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsUnbounded() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextThrowsUnbounded, this.description("nextThrowsUnbounded"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrows, this.description("hasNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextThrows, this.description("nextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBefore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBefore, this.description("requestBefore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCreateInnerRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCreateInnerRace, this.description("requestCreateInnerRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelCreateInnerRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelCreateInnerRace, this.description("cancelCreateInnerRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathCancelAfterHasNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::slowPathCancelAfterHasNext, this.description("slowPathCancelAfterHasNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fastPathCancelAfterHasNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fastPathCancelAfterHasNext, this.description("fastPathCancelAfterHasNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessRequestRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSuccessRequestRace, this.description("onSuccessRequestRace"));
        }

        private MaybeFlatMapIterableFlowableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeFlatMapIterableFlowableTest();
        }

        @java.lang.Override
        public MaybeFlatMapIterableFlowableTest implementation() {
            return this.implementation;
        }
    }
}
