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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.assertFalse;
import java.util.Optional;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableMapOptionalTest extends RxJavaTest {

    static final Function<? super Integer, Optional<? extends Integer>> MODULO = v -> v % 2 == 0 ? Optional.of(v) : Optional.<Integer>empty();

    @Test
    public void allPresent() {
        Flowable.range(1, 5).mapOptional(Optional::of).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmpty() {
        Flowable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 10).mapOptional(MODULO).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChash() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void mapperNull() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> null).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void crashDropsOnNexts() {
        Flowable<Integer> source = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureAll() {
        Flowable.range(1, 5).mapOptional(Optional::of).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureNone() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).test(1L).assertResult();
    }

    @Test
    public void backpressureMixed() {
        Flowable.range(1, 10).mapOptional(MODULO).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 4).requestMore(2).assertValuesOnly(2, 4, 6, 8).requestMore(1).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void syncFusedAll() {
        Flowable.range(1, 5).mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAll() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAll() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNone() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNone() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNone() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixed() {
        Flowable.range(1, 10).mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixed() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixed() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void allPresentConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmptyConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).filter(v -> true).test().assertResult();
    }

    @Test
    public void mixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChashConditional() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void mapperNullConditional() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> null).filter(v -> true).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void crashDropsOnNextsConditional() {
        Flowable<Integer> source = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureAllConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureNoneConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).test(1L).assertResult();
    }

    @Test
    public void backpressureMixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 4).requestMore(2).assertValuesOnly(2, 4, 6, 8).requestMore(1).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void syncFusedAllConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAllConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAllConditiona() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNoneConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNoneConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNoneConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixedConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixedConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void conditionalFusionNoNPE() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.empty().observeOn(ImmediateThinScheduler.INSTANCE).filter(v -> true).mapOptional(Optional::of).filter(v -> true).subscribe(ts);
        ts.assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allPresent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allPresent, this.description("allPresent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allEmpty, this.description("allEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixed, this.description("mixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperChash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperChash, this.description("mapperChash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperNull, this.description("mapperNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashDropsOnNexts() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crashDropsOnNexts, this.description("crashDropsOnNexts"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureAll, this.description("backpressureAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureNone, this.description("backpressureNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureMixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureMixed, this.description("backpressureMixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedAll, this.description("syncFusedAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedAll, this.description("asyncFusedAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedAll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedAll, this.description("boundaryFusedAll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedNone, this.description("syncFusedNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedNone, this.description("asyncFusedNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedNone() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedNone, this.description("boundaryFusedNone"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedMixed, this.description("syncFusedMixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedMixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedMixed, this.description("asyncFusedMixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedMixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedMixed, this.description("boundaryFusedMixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allPresentConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allPresentConditional, this.description("allPresentConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allEmptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::allEmptyConditional, this.description("allEmptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixedConditional, this.description("mixedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperChashConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperChashConditional, this.description("mapperChashConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNullConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperNullConditional, this.description("mapperNullConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashDropsOnNextsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::crashDropsOnNextsConditional, this.description("crashDropsOnNextsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureAllConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureAllConditional, this.description("backpressureAllConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoneConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureNoneConditional, this.description("backpressureNoneConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureMixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureMixedConditional, this.description("backpressureMixedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedAllConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedAllConditional, this.description("syncFusedAllConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedAllConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedAllConditional, this.description("asyncFusedAllConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedAllConditiona() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedAllConditiona, this.description("boundaryFusedAllConditiona"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedNoneConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedNoneConditional, this.description("syncFusedNoneConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedNoneConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedNoneConditional, this.description("asyncFusedNoneConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedNoneConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedNoneConditional, this.description("boundaryFusedNoneConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedMixedConditional, this.description("syncFusedMixedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedMixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedMixedConditional, this.description("asyncFusedMixedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedMixedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryFusedMixedConditional, this.description("boundaryFusedMixedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusionNoNPE() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalFusionNoNPE, this.description("conditionalFusionNoNPE"));
        }

        private FlowableMapOptionalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableMapOptionalTest();
        }

        @java.lang.Override
        public FlowableMapOptionalTest implementation() {
            return this.implementation;
        }
    }
}
