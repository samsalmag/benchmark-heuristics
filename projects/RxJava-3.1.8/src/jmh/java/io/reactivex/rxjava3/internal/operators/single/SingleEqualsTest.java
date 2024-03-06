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
package io.reactivex.rxjava3.internal.operators.single;

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleEqualsTest extends RxJavaTest {

    @Test
    public void bothSucceedEqual() {
        Single.sequenceEqual(Single.just(1), Single.just(1)).test().assertResult(true);
    }

    @Test
    public void bothSucceedNotEqual() {
        Single.sequenceEqual(Single.just(1), Single.just(2)).test().assertResult(false);
    }

    @Test
    public void firstSucceedOtherError() {
        Single.sequenceEqual(Single.just(1), Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void firstErrorOtherSucceed() {
        Single.sequenceEqual(Single.error(new TestException()), Single.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void bothError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.sequenceEqual(Single.error(new TestException("One")), Single.error(new TestException("Two"))).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "One");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothSucceedEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothSucceedEqual, this.description("bothSucceedEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothSucceedNotEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothSucceedNotEqual, this.description("bothSucceedNotEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstSucceedOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstSucceedOtherError, this.description("firstSucceedOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstErrorOtherSucceed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstErrorOtherSucceed, this.description("firstErrorOtherSucceed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bothError, this.description("bothError"));
        }

        private SingleEqualsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleEqualsTest();
        }

        @java.lang.Override
        public SingleEqualsTest implementation() {
            return this.implementation;
        }
    }
}
