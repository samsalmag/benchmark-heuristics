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
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableMapOptionalTest extends RxJavaTest {

    static final Function<? super Integer, Optional<? extends Integer>> MODULO = v -> v % 2 == 0 ? Optional.of(v) : Optional.<Integer>empty();

    @Test
    public void allPresent() {
        Observable.range(1, 5).mapOptional(Optional::of).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmpty() {
        Observable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).test().assertResult();
    }

    @Test
    public void mixed() {
        Observable.range(1, 10).mapOptional(MODULO).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChash() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void mapperNull() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> null).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void crashDropsOnNexts() {
        Observable<Integer> source = new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void syncFusedAll() {
        Observable.range(1, 5).mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAll() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAll() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNone() {
        Observable.range(1, 5).mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNone() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNone() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixed() {
        Observable.range(1, 10).mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixed() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixed() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void allPresentConditional() {
        Observable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmptyConditional() {
        Observable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).filter(v -> true).test().assertResult();
    }

    @Test
    public void mixedConditional() {
        Observable.range(1, 10).mapOptional(MODULO).filter(v -> true).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChashConditional() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void mapperNullConditional() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> null).filter(v -> true).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void crashDropsOnNextsConditional() {
        Observable<Integer> source = new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void syncFusedAllConditional() {
        Observable.range(1, 5).mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAllConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAllConditiona() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNoneConditional() {
        Observable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNoneConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNoneConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixedConditional() {
        Observable.range(1, 10).mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixedConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixedConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
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

        private ObservableMapOptionalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableMapOptionalTest();
        }

        @java.lang.Override
        public ObservableMapOptionalTest implementation() {
            return this.implementation;
        }
    }
}
