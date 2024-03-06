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
package io.reactivex.rxjava3.parallel;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelPeekTest extends RxJavaTest {

    @Test
    public void subscriberCount() {
        ParallelFlowableTest.checkSubscriberCount(Flowable.range(1, 5).parallel().doOnNext(Functions.emptyConsumer()));
    }

    @Test
    @SuppressUndeliverable
    public void onSubscribeCrash() {
        Flowable.range(1, 5).parallel().doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                throw new TestException();
            }
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().doOnNext(Functions.emptyConsumer()).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void requestCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.range(1, 5).parallel().doOnRequest(new LongConsumer() {

                @Override
                public void accept(long n) throws Exception {
                    throw new TestException();
                }
            }).sequential().test().assertResult(1, 2, 3, 4, 5);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.<Integer>never().parallel().doOnCancel(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).sequential().test().cancel();
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    @SuppressUndeliverable
    public void onCompleteCrash() {
        Flowable.just(1).parallel().doOnComplete(new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }).sequential().test().assertFailure(TestException.class, 1);
    }

    @Test
    public void onAfterTerminatedCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).parallel().doAfterTerminated(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).sequential().test().assertResult(1);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onAfterTerminatedCrash2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.<Integer>error(new IOException()).parallel().doAfterTerminated(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).sequential().test().assertFailure(IOException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                Throwable exc = ex.getCause();
                assertTrue(ex.toString(), exc instanceof TestException || exc instanceof IOException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> ParallelFlowable.fromArray(f).doOnComplete(() -> {
        }).sequential());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberCount, this.description("subscriberCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribeCrash, this.description("onSubscribeCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError, this.description("doubleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCrash, this.description("requestCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelCrash, this.description("cancelCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteCrash, this.description("onCompleteCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onAfterTerminatedCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onAfterTerminatedCrash, this.description("onAfterTerminatedCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onAfterTerminatedCrash2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onAfterTerminatedCrash2, this.description("onAfterTerminatedCrash2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ParallelPeekTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelPeekTest();
        }

        @java.lang.Override
        public ParallelPeekTest implementation() {
            return this.implementation;
        }
    }
}
