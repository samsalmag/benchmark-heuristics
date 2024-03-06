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

import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeTimeoutTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).timeout(1, TimeUnit.DAYS).test().assertResult(1);
    }

    @Test
    public void normalMaybe() {
        Maybe.just(1).timeout(Maybe.timer(1, TimeUnit.DAYS)).test().assertResult(1);
    }

    @Test
    public void never() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void neverMaybe() {
        Maybe.never().timeout(Maybe.timer(1, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void normalFallback() {
        Maybe.just(1).timeout(1, TimeUnit.DAYS, Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void normalMaybeFallback() {
        Maybe.just(1).timeout(Maybe.timer(1, TimeUnit.DAYS), Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void neverFallback() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverMaybeFallback() {
        Maybe.never().timeout(Maybe.timer(1, TimeUnit.MILLISECONDS), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverFallbackScheduler() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Schedulers.single(), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverScheduler() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void normalFlowableFallback() {
        Maybe.just(1).timeout(Flowable.timer(1, TimeUnit.DAYS), Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void neverFlowableFallback() {
        Maybe.never().timeout(Flowable.timer(1, TimeUnit.MILLISECONDS), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void normalFlowable() {
        Maybe.just(1).timeout(Flowable.timer(1, TimeUnit.DAYS)).test().assertResult(1);
    }

    @Test
    public void neverFlowable() {
        Maybe.never().timeout(Flowable.timer(1, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void mainError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void fallbackError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement(), Maybe.<Integer>error(new TestException())).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(1);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void fallbackComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement(), Maybe.<Integer>empty()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(1);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void mainComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void otherComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TimeoutException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2.singleElement()));
    }

    @Test
    public void dispose2() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2.singleElement(), Maybe.just(1)));
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserverEx<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp1.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp2.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertSubscribed().assertNoValues();
            if (to.errors().size() != 0) {
                to.assertError(TimeoutException.class).assertNotComplete();
            } else {
                to.assertNoErrors().assertComplete();
            }
        }
    }

    @Test
    public void mainSuccessAfterOtherSignal() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        new Maybe<Integer>() {

            @Override
            protected void subscribeActual(@NonNull MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                ms.onSuccess(2);
                observer.onSuccess(1);
            }
        }.timeout(ms).test().assertFailure(TimeoutException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalMaybe, this.description("normalMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::never, this.description("never"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverMaybe, this.description("neverMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalFallback, this.description("normalFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybeFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalMaybeFallback, this.description("normalMaybeFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverFallback, this.description("neverFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverMaybeFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverMaybeFallback, this.description("neverMaybeFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFallbackScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverFallbackScheduler, this.description("neverFallbackScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverScheduler, this.description("neverScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowableFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalFlowableFallback, this.description("normalFlowableFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFlowableFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverFlowableFallback, this.description("neverFlowableFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalFlowable, this.description("normalFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::neverFlowable, this.description("neverFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherError, this.description("otherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fallbackError, this.description("fallbackError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fallbackComplete, this.description("fallbackComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainComplete, this.description("mainComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherComplete, this.description("otherComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose2, this.description("dispose2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorRace, this.description("onErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteRace, this.description("onCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessAfterOtherSignal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainSuccessAfterOtherSignal, this.description("mainSuccessAfterOtherSignal"));
        }

        private MaybeTimeoutTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeTimeoutTest();
        }

        @java.lang.Override
        public MaybeTimeoutTest implementation() {
            return this.implementation;
        }
    }
}
