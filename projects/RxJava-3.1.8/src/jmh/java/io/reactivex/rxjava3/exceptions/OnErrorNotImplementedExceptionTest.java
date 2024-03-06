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
package io.reactivex.rxjava3.exceptions;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class OnErrorNotImplementedExceptionTest extends RxJavaTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
        assertFalse("" + errors, errors.isEmpty());
        TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        Throwable c = errors.get(0).getCause();
        assertTrue("" + c, c instanceof TestException);
    }

    @Test
    public void flowableSubscribe0() {
        Flowable.error(new TestException()).subscribe();
    }

    @Test
    public void flowableSubscribe1() {
        Flowable.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void flowableForEachWhile() {
        Flowable.error(new TestException()).forEachWhile(Functions.alwaysTrue());
    }

    @Test
    public void flowableBlockingSubscribe1() {
        Flowable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer());
    }

    @Test
    public void flowableBoundedBlockingSubscribe1() {
        Flowable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer(), 128);
    }

    @Test
    public void observableSubscribe0() {
        Observable.error(new TestException()).subscribe();
    }

    @Test
    public void observableSubscribe1() {
        Observable.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void observableForEachWhile() {
        Observable.error(new TestException()).forEachWhile(Functions.alwaysTrue());
    }

    @Test
    public void observableBlockingSubscribe1() {
        Observable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer());
    }

    @Test
    public void singleSubscribe0() {
        Single.error(new TestException()).subscribe();
    }

    @Test
    public void singleSubscribe1() {
        Single.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void maybeSubscribe0() {
        Maybe.error(new TestException()).subscribe();
    }

    @Test
    public void maybeSubscribe1() {
        Maybe.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void completableSubscribe0() {
        Completable.error(new TestException()).subscribe();
    }

    @Test
    public void completableSubscribe1() {
        Completable.error(new TestException()).subscribe(Functions.EMPTY_ACTION);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableSubscribe0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableSubscribe0, this.description("flowableSubscribe0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableSubscribe1, this.description("flowableSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableForEachWhile() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableForEachWhile, this.description("flowableForEachWhile"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableBlockingSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableBlockingSubscribe1, this.description("flowableBlockingSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableBoundedBlockingSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableBoundedBlockingSubscribe1, this.description("flowableBoundedBlockingSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableSubscribe0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observableSubscribe0, this.description("observableSubscribe0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observableSubscribe1, this.description("observableSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableForEachWhile() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observableForEachWhile, this.description("observableForEachWhile"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableBlockingSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observableBlockingSubscribe1, this.description("observableBlockingSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSubscribe0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSubscribe0, this.description("singleSubscribe0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSubscribe1, this.description("singleSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSubscribe0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeSubscribe0, this.description("maybeSubscribe0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeSubscribe1, this.description("maybeSubscribe1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSubscribe0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableSubscribe0, this.description("completableSubscribe0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSubscribe1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableSubscribe1, this.description("completableSubscribe1"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().after();
            super.after();
        }

        private OnErrorNotImplementedExceptionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new OnErrorNotImplementedExceptionTest();
        }

        @java.lang.Override
        public OnErrorNotImplementedExceptionTest implementation() {
            return this.implementation;
        }
    }
}
