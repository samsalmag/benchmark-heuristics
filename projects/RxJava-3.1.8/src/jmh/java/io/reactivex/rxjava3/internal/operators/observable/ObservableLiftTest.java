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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.testsupport.SuppressUndeliverable;

public class ObservableLiftTest extends RxJavaTest {

    @Test
    @SuppressUndeliverable
    public void callbackCrash() {
        try {
            Observable.just(1).lift(new ObservableOperator<Object, Integer>() {

                @Override
                public Observer<? super Integer> apply(Observer<? super Object> o) throws Exception {
                    throw new TestException();
                }
            }).test();
            fail("Should have thrown");
        } catch (NullPointerException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof TestException);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callbackCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::callbackCrash, this.description("callbackCrash"));
        }

        private ObservableLiftTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableLiftTest();
        }

        @java.lang.Override
        public ObservableLiftTest implementation() {
            return this.implementation;
        }
    }
}
