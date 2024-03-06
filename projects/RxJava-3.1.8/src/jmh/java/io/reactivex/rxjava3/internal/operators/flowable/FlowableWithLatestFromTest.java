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
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.CrashingMappedIterable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableWithLatestFromTest extends RxJavaTest {

    static final BiFunction<Integer, Integer, Integer> COMBINER = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return (t1 << 8) + t2;
        }
    };

    static final BiFunction<Integer, Integer, Integer> COMBINER_ERROR = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            throw new TestException("Forced failure");
        }
    };

    @Test
    public void simple() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        result.subscribe(subscriber);
        source.onNext(1);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        other.onNext(1);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        source.onNext(2);
        inOrder.verify(subscriber).onNext((2 << 8) + 1);
        other.onNext(2);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        other.onComplete();
        inOrder.verify(subscriber, never()).onComplete();
        source.onNext(3);
        inOrder.verify(subscriber).onNext((3 << 8) + 2);
        source.onComplete();
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void emptySource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onComplete();
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertNoValues();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void emptyOther() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        source.onNext(1);
        source.onComplete();
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertNoValues();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void unsubscription() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        ts.cancel();
        ts.assertValue((1 << 8) + 1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void sourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        source.onError(new TestException());
        ts.assertTerminated();
        ts.assertValue((1 << 8) + 1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void otherThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        other.onError(new TestException());
        ts.assertTerminated();
        ts.assertValue((1 << 8) + 1);
        ts.assertNotComplete();
        ts.assertError(TestException.class);
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void functionThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER_ERROR);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        ts.assertTerminated();
        ts.assertNotComplete();
        ts.assertNoValues();
        ts.assertError(TestException.class);
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void noDownstreamUnsubscribe() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        result.subscribe(ts);
        source.onComplete();
        assertFalse(ts.isCancelled());
    }

    @Test
    public void backpressure() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        result.subscribe(ts);
        assertTrue("Other has no observers!", other.hasSubscribers());
        ts.request(1);
        source.onNext(1);
        assertTrue("Other has no observers!", other.hasSubscribers());
        ts.assertNoValues();
        other.onNext(1);
        source.onNext(2);
        ts.assertValue((2 << 8) + 1);
        ts.request(5);
        source.onNext(3);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        source.onNext(7);
        ts.assertValues((2 << 8) + 1, (3 << 8) + 1, (4 << 8) + 1, (5 << 8) + 1, (6 << 8) + 1, (7 << 8) + 1);
        ts.cancel();
        assertFalse("Other has observers!", other.hasSubscribers());
        ts.assertNoErrors();
    }

    static final Function<Object[], String> toArray = new Function<Object[], String>() {

        @Override
        public String apply(Object[] args) {
            return Arrays.toString(args);
        }
    };

    @Test
    public void manySources() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        PublishProcessor<String> pp3 = PublishProcessor.create();
        PublishProcessor<String> main = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>();
        main.withLatestFrom(new Flowable[] { pp1, pp2, pp3 }, toArray).subscribe(ts);
        main.onNext("1");
        ts.assertNoValues();
        pp1.onNext("a");
        ts.assertNoValues();
        pp2.onNext("A");
        ts.assertNoValues();
        pp3.onNext("=");
        ts.assertNoValues();
        main.onNext("2");
        ts.assertValues("[2, a, A, =]");
        pp2.onNext("B");
        ts.assertValues("[2, a, A, =]");
        pp3.onComplete();
        ts.assertValues("[2, a, A, =]");
        pp1.onNext("b");
        main.onNext("3");
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        main.onComplete();
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
        assertFalse("ps3 has subscribers?", pp3.hasSubscribers());
    }

    @Test
    public void manySourcesIterable() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        PublishProcessor<String> pp3 = PublishProcessor.create();
        PublishProcessor<String> main = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>();
        main.withLatestFrom(Arrays.<Flowable<?>>asList(pp1, pp2, pp3), toArray).subscribe(ts);
        main.onNext("1");
        ts.assertNoValues();
        pp1.onNext("a");
        ts.assertNoValues();
        pp2.onNext("A");
        ts.assertNoValues();
        pp3.onNext("=");
        ts.assertNoValues();
        main.onNext("2");
        ts.assertValues("[2, a, A, =]");
        pp2.onNext("B");
        ts.assertValues("[2, a, A, =]");
        pp3.onComplete();
        ts.assertValues("[2, a, A, =]");
        pp1.onNext("b");
        main.onNext("3");
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        main.onComplete();
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
        assertFalse("ps3 has subscribers?", pp3.hasSubscribers());
    }

    @Test
    public void manySourcesIterableSweep() {
        for (String val : new String[] { "1" /*, null*/
        }) {
            int n = 35;
            for (int i = 0; i < n; i++) {
                List<Flowable<?>> sources = new ArrayList<>();
                List<String> expected = new ArrayList<>();
                expected.add(val);
                for (int j = 0; j < i; j++) {
                    sources.add(Flowable.just(val));
                    expected.add(String.valueOf(val));
                }
                TestSubscriber<String> ts = new TestSubscriber<>();
                PublishProcessor<String> main = PublishProcessor.create();
                main.withLatestFrom(sources, toArray).subscribe(ts);
                ts.assertNoValues();
                main.onNext(val);
                main.onComplete();
                ts.assertValue(expected.toString());
                ts.assertNoErrors();
                ts.assertComplete();
            }
        }
    }

    @Test
    public void backpressureNoSignal() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 10).withLatestFrom(new Flowable<?>[] { pp1, pp2 }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void backpressureWithSignal() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { pp1, pp2 }, toArray).subscribe(ts);
        ts.assertNoValues();
        pp1.onNext("1");
        pp2.onNext("1");
        ts.request(1);
        ts.assertValue("[1, 1, 1]");
        ts.request(1);
        ts.assertValues("[1, 1, 1]", "[2, 1, 1]");
        ts.request(1);
        ts.assertValues("[1, 1, 1]", "[2, 1, 1]", "[3, 1, 1]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void withEmpty() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.empty() }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.error(new TestException()) }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void withMainError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.error(new TestException()).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.just(1) }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void with2Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, new Function3<Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c) {
                return Arrays.asList(a, b, c);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void with3Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, just, new Function4<Integer, Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c, Integer d) {
                return Arrays.asList(a, b, c, d);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void with4Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, just, just, new Function5<Integer, Integer, Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c, Integer d, Integer e) {
                return Arrays.asList(a, b, c, d, e);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).withLatestFrom(Flowable.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a;
            }
        }));
        TestHelper.checkDisposed(Flowable.just(1).withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void manyIteratorThrows() {
        Flowable.just(1).withLatestFrom(new CrashingMappedIterable<>(1, 100, 100, new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) throws Exception {
                return Flowable.just(2);
            }
        }), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return a;
            }
        }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void manyCombinerThrows() {
        Flowable.just(1).withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void manyErrors() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(1);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            }.withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Integer a, Integer b, Integer c) throws Exception {
                    return a;
                }
            }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void otherErrors() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).withLatestFrom(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            }, new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void combineToNull1() {
        Flowable.just(1).withLatestFrom(Flowable.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void combineToNull2() {
        Flowable.just(1).withLatestFrom(Arrays.asList(Flowable.just(2), Flowable.just(3)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] o) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void zeroOtherCombinerReturnsNull() {
        Flowable.just(1).withLatestFrom(new Flowable[0], Functions.justFunction(null)).to(TestHelper.testConsumer()).assertFailureAndMessage(NullPointerException.class, "The combiner returned a null value");
    }

    @Test
    public void singleRequestNotForgottenWhenNoData() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        result.subscribe(ts);
        ts.request(1);
        source.onNext(1);
        ts.assertNoValues();
        other.onNext(1);
        ts.assertNoValues();
        source.onNext(2);
        ts.assertValue((2 << 8) + 1);
    }

    @Test
    public void coldSourceConsumedWithoutOther() {
        Flowable.range(1, 10).withLatestFrom(Flowable.never(), new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) throws Exception {
                return a;
            }
        }).test(1).assertResult();
    }

    @Test
    public void coldSourceConsumedWithoutManyOthers() {
        Flowable.range(1, 10).withLatestFrom(Flowable.never(), Flowable.never(), Flowable.never(), new Function4<Integer, Object, Object, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b, Object c, Object d) throws Exception {
                return a;
            }
        }).test(1).assertResult();
    }

    @Test
    public void otherOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp0 = PublishProcessor.create();
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            final PublishProcessor<Integer> pp3 = PublishProcessor.create();
            final Flowable<Object> source = pp0.withLatestFrom(pp1, pp2, pp3, new Function4<Object, Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Object a, Integer b, Integer c, Integer d) throws Exception {
                    return a;
                }
            });
            final TestSubscriber<Object> ts = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    source.subscribe(ts);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertEmpty();
            assertFalse(pp0.hasSubscribers());
            assertFalse(pp1.hasSubscribers());
            assertFalse(pp2.hasSubscribers());
            assertFalse(pp3.hasSubscribers());
        }
    }

    @Test
    public void otherCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp0 = PublishProcessor.create();
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            final PublishProcessor<Integer> pp3 = PublishProcessor.create();
            final Flowable<Object> source = pp0.withLatestFrom(pp1, pp2, pp3, new Function4<Object, Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Object a, Integer b, Integer c, Integer d) throws Exception {
                    return a;
                }
            });
            final TestSubscriber<Object> ts = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    source.subscribe(ts);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp1.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult();
            assertFalse(pp0.hasSubscribers());
            assertFalse(pp1.hasSubscribers());
            assertFalse(pp2.hasSubscribers());
            assertFalse(pp3.hasSubscribers());
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple, this.description("simple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptySource, this.description("emptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyOther, this.description("emptyOther"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscription, this.description("unsubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sourceThrows, this.description("sourceThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherThrows, this.description("otherThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::functionThrows, this.description("functionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDownstreamUnsubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noDownstreamUnsubscribe, this.description("noDownstreamUnsubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manySources, this.description("manySources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySourcesIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manySourcesIterable, this.description("manySourcesIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySourcesIterableSweep() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manySourcesIterableSweep, this.description("manySourcesIterableSweep"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoSignal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureNoSignal, this.description("backpressureNoSignal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithSignal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithSignal, this.description("backpressureWithSignal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withEmpty, this.description("withEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withError, this.description("withError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withMainError, this.description("withMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with2Others() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::with2Others, this.description("with2Others"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with3Others() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::with3Others, this.description("with3Others"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with4Others() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::with4Others, this.description("with4Others"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyIteratorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyIteratorThrows, this.description("manyIteratorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyCombinerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyCombinerThrows, this.description("manyCombinerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyErrors, this.description("manyErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherErrors, this.description("otherErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineToNull1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::combineToNull1, this.description("combineToNull1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineToNull2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::combineToNull2, this.description("combineToNull2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zeroOtherCombinerReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zeroOtherCombinerReturnsNull, this.description("zeroOtherCombinerReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleRequestNotForgottenWhenNoData() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleRequestNotForgottenWhenNoData, this.description("singleRequestNotForgottenWhenNoData"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldSourceConsumedWithoutOther() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::coldSourceConsumedWithoutOther, this.description("coldSourceConsumedWithoutOther"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldSourceConsumedWithoutManyOthers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::coldSourceConsumedWithoutManyOthers, this.description("coldSourceConsumedWithoutManyOthers"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnSubscribeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherOnSubscribeRace, this.description("otherOnSubscribeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherCompleteRace, this.description("otherCompleteRace"));
        }

        private FlowableWithLatestFromTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableWithLatestFromTest();
        }

        @java.lang.Override
        public FlowableWithLatestFromTest implementation() {
            return this.implementation;
        }
    }
}
