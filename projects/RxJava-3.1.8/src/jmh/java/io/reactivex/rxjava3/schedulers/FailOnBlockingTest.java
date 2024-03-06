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
package io.reactivex.rxjava3.schedulers;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class FailOnBlockingTest extends RxJavaTest {

    @Test
    public void failComputationFlowableBlockingFirst() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingFirst();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingLast() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingLast();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingIterable() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingIterable().iterator().next();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingSubscribe() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingSubscribe();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingSingle() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingSingle();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingForEach() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingForEach(Functions.emptyConsumer());
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingLatest() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingLatest().iterator().hasNext();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableBlockingNext() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingNext().iterator().hasNext();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationFlowableToFuture() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).toFuture().get();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingFirst() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingFirst();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingLast() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingLast();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingIterable() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingIterable().iterator().next();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingSubscribe() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingSubscribe();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingSingle() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingSingle();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingForEach() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingForEach(Functions.emptyConsumer());
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingLatest() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingLatest().iterator().hasNext();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableBlockingNext() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingNext().iterator().hasNext();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failComputationObservableToFuture() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).toFuture().get();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failSingleObservableBlockingFirst() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.single()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Observable.just(1).delay(10, TimeUnit.SECONDS).blockingFirst();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failSingleSingleBlockingGet() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Single.just(1).subscribeOn(Schedulers.single()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Single.just(1).delay(10, TimeUnit.SECONDS).blockingGet();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failSingleMaybeBlockingGet() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Maybe.just(1).subscribeOn(Schedulers.single()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Maybe.just(1).delay(10, TimeUnit.SECONDS).blockingGet();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failSingleCompletableBlockingGet() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Completable.complete().subscribeOn(Schedulers.single()).doOnComplete(new Action() {

                @Override
                public void run() throws Exception {
                    Completable.complete().delay(10, TimeUnit.SECONDS).blockingAwait();
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failSingleCompletableBlockingAwait() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Completable.complete().subscribeOn(Schedulers.single()).doOnComplete(new Action() {

                @Override
                public void run() throws Exception {
                    Completable.complete().delay(10, TimeUnit.SECONDS).blockingAwait();
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dontfailIOObservableBlockingFirst() {
        try {
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Observable.just(1).subscribeOn(Schedulers.io()).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    return Observable.just(2).delay(100, TimeUnit.MILLISECONDS).blockingFirst();
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failWithCustomHandler() {
        try {
            RxJavaPlugins.setOnBeforeBlocking(new BooleanSupplier() {

                @Override
                public boolean getAsBoolean() throws Exception {
                    return true;
                }
            });
            RxJavaPlugins.setFailOnNonBlockingScheduler(true);
            Flowable.just(1).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    Flowable.just(1).delay(10, TimeUnit.SECONDS).blockingLast();
                    return v;
                }
            }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
        Flowable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                return Flowable.just(2).delay(100, TimeUnit.MILLISECONDS).blockingLast();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingFirst, this.description("failComputationFlowableBlockingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingLast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingLast, this.description("failComputationFlowableBlockingLast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingIterable, this.description("failComputationFlowableBlockingIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingSubscribe, this.description("failComputationFlowableBlockingSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingSingle, this.description("failComputationFlowableBlockingSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingForEach() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingForEach, this.description("failComputationFlowableBlockingForEach"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingLatest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingLatest, this.description("failComputationFlowableBlockingLatest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableBlockingNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableBlockingNext, this.description("failComputationFlowableBlockingNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationFlowableToFuture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationFlowableToFuture, this.description("failComputationFlowableToFuture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingFirst, this.description("failComputationObservableBlockingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingLast() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingLast, this.description("failComputationObservableBlockingLast"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingIterable, this.description("failComputationObservableBlockingIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingSubscribe, this.description("failComputationObservableBlockingSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingSingle, this.description("failComputationObservableBlockingSingle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingForEach() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingForEach, this.description("failComputationObservableBlockingForEach"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingLatest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingLatest, this.description("failComputationObservableBlockingLatest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableBlockingNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableBlockingNext, this.description("failComputationObservableBlockingNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failComputationObservableToFuture() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failComputationObservableToFuture, this.description("failComputationObservableToFuture"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failSingleObservableBlockingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failSingleObservableBlockingFirst, this.description("failSingleObservableBlockingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failSingleSingleBlockingGet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failSingleSingleBlockingGet, this.description("failSingleSingleBlockingGet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failSingleMaybeBlockingGet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failSingleMaybeBlockingGet, this.description("failSingleMaybeBlockingGet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failSingleCompletableBlockingGet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failSingleCompletableBlockingGet, this.description("failSingleCompletableBlockingGet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failSingleCompletableBlockingAwait() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failSingleCompletableBlockingAwait, this.description("failSingleCompletableBlockingAwait"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dontfailIOObservableBlockingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dontfailIOObservableBlockingFirst, this.description("dontfailIOObservableBlockingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failWithCustomHandler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::failWithCustomHandler, this.description("failWithCustomHandler"));
        }

        private FailOnBlockingTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FailOnBlockingTest();
        }

        @java.lang.Override
        public FailOnBlockingTest implementation() {
            return this.implementation;
        }
    }
}
