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
package io.reactivex.rxjava3.internal.operators.completable;

import static org.junit.Assert.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableUnsafeTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void unsafeCreateRejectsCompletable() {
        Completable.unsafeCreate(Completable.complete());
    }

    @Test
    public void wrapAlreadyCompletable() {
        assertSame(Completable.complete(), Completable.wrap(Completable.complete()));
    }

    @Test
    public void wrapCustomCompletable() {
        Completable.wrap(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onComplete();
            }
        }).test().assertResult();
    }

    @Test(expected = NullPointerException.class)
    public void unsafeCreateThrowsNPE() {
        Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                throw new NullPointerException();
            }
        }).test();
    }

    @Test
    public void unsafeCreateThrowsIAE() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable.unsafeCreate(new CompletableSource() {

                @Override
                public void subscribe(CompletableObserver observer) {
                    throw new IllegalArgumentException();
                }
            }).test();
            fail("Should have thrown!");
        } catch (NullPointerException ex) {
            if (!(ex.getCause() instanceof IllegalArgumentException)) {
                fail(ex.toString() + ": should have thrown NPA(IAE)");
            }
            TestHelper.assertError(errors, 0, IllegalArgumentException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateRejectsCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::unsafeCreateRejectsCompletable, this.description("unsafeCreateRejectsCompletable"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapAlreadyCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::wrapAlreadyCompletable, this.description("wrapAlreadyCompletable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapCustomCompletable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::wrapCustomCompletable, this.description("wrapCustomCompletable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateThrowsNPE() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::unsafeCreateThrowsNPE, this.description("unsafeCreateThrowsNPE"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateThrowsIAE() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsafeCreateThrowsIAE, this.description("unsafeCreateThrowsIAE"));
        }

        private CompletableUnsafeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableUnsafeTest();
        }

        @java.lang.Override
        public CompletableUnsafeTest implementation() {
            return this.implementation;
        }
    }
}
