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

import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDematerializeTest extends RxJavaTest {

    @Test
    public void success() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(Functions.<Notification<Integer>>identity()).test().assertResult(1);
    }

    @Test
    public void empty() {
        Maybe.just(Notification.<Integer>createOnComplete()).dematerialize(Functions.<Notification<Integer>>identity()).test().assertResult();
    }

    @Test
    public void emptySource() throws Throwable {
        @SuppressWarnings("unchecked")
        Function<Notification<Integer>, Notification<Integer>> function = mock(Function.class);
        Maybe.<Notification<Integer>>empty().dematerialize(function).test().assertResult();
        verify(function, never()).apply(any());
    }

    @Test
    public void error() {
        Maybe.<Notification<Integer>>error(new TestException()).dematerialize(Functions.<Notification<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void errorNotification() {
        Maybe.just(Notification.<Integer>createOnError(new TestException())).dematerialize(Functions.<Notification<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.dematerialize((Function) Functions.identity());
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(MaybeSubject.<Notification<Integer>>create().dematerialize(Functions.<Notification<Integer>>identity()));
    }

    @Test
    public void selectorCrash() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(new Function<Notification<Integer>, Notification<Integer>>() {

            @Override
            public Notification<Integer> apply(Notification<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorNull() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(Functions.justFunction((Notification<Integer>) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void selectorDifferentType() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(new Function<Notification<Integer>, Notification<String>>() {

            @Override
            public Notification<String> apply(Notification<Integer> v) throws Exception {
                return Notification.createOnNext("Value-" + 1);
            }
        }).test().assertResult("Value-1");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::success, this.description("success"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptySource, this.description("emptySource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotification() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorNotification, this.description("errorNotification"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorCrash, this.description("selectorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorNull, this.description("selectorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorDifferentType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::selectorDifferentType, this.description("selectorDifferentType"));
        }

        private MaybeDematerializeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDematerializeTest();
        }

        @java.lang.Override
        public MaybeDematerializeTest implementation() {
            return this.implementation;
        }
    }
}
