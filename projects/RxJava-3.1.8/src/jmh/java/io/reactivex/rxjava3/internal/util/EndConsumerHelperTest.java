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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.*;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class EndConsumerHelperTest extends RxJavaTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(EndConsumerHelper.class);
    }

    @Test
    public void checkDoubleDefaultSubscriber() {
        Subscriber<Integer> consumer = new DefaultSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    static final class EndDefaultSubscriber extends DefaultSubscriber<Integer> {

        @Override
        public void onNext(Integer t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    }

    @Test
    public void checkDoubleDefaultSubscriberNonAnonymous() {
        Subscriber<Integer> consumer = new EndDefaultSubscriber();
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        // with this consumer, the class name should be predictable
        assertEquals(EndConsumerHelper.composeMessage("io.reactivex.rxjava3.internal.util.EndConsumerHelperTest$EndDefaultSubscriber"), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSubscriber() {
        Subscriber<Integer> consumer = new DisposableSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSubscriber() {
        Subscriber<Integer> consumer = new ResourceSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDefaultObserver() {
        Observer<Integer> consumer = new DefaultObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableObserver() {
        Observer<Integer> consumer = new DisposableObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceObserver() {
        Observer<Integer> consumer = new ResourceObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSingleObserver() {
        SingleObserver<Integer> consumer = new DisposableSingleObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSingleObserver() {
        SingleObserver<Integer> consumer = new ResourceSingleObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableMaybeObserver() {
        MaybeObserver<Integer> consumer = new DisposableMaybeObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceMaybeObserver() {
        MaybeObserver<Integer> consumer = new ResourceMaybeObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableCompletableObserver() {
        CompletableObserver consumer = new DisposableCompletableObserver() {

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceCompletableObserver() {
        CompletableObserver consumer = new ResourceCompletableObserver() {

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void validateDisposable() {
        Disposable d1 = Disposable.empty();
        assertFalse(EndConsumerHelper.validate(DisposableHelper.DISPOSED, d1, getClass()));
        assertTrue(d1.isDisposed());
        assertTrue(errors.toString(), errors.isEmpty());
    }

    @Test
    public void validateSubscription() {
        BooleanSubscription bs1 = new BooleanSubscription();
        assertFalse(EndConsumerHelper.validate(SubscriptionHelper.CANCELLED, bs1, getClass()));
        assertTrue(bs1.isCancelled());
        assertTrue(errors.toString(), errors.isEmpty());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultSubscriber() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDefaultSubscriber, this.description("checkDoubleDefaultSubscriber"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultSubscriberNonAnonymous() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDefaultSubscriberNonAnonymous, this.description("checkDoubleDefaultSubscriberNonAnonymous"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableSubscriber() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDisposableSubscriber, this.description("checkDoubleDisposableSubscriber"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceSubscriber() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleResourceSubscriber, this.description("checkDoubleResourceSubscriber"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDefaultObserver, this.description("checkDoubleDefaultObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDisposableObserver, this.description("checkDoubleDisposableObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleResourceObserver, this.description("checkDoubleResourceObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableSingleObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDisposableSingleObserver, this.description("checkDoubleDisposableSingleObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceSingleObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleResourceSingleObserver, this.description("checkDoubleResourceSingleObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableMaybeObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDisposableMaybeObserver, this.description("checkDoubleDisposableMaybeObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceMaybeObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleResourceMaybeObserver, this.description("checkDoubleResourceMaybeObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableCompletableObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleDisposableCompletableObserver, this.description("checkDoubleDisposableCompletableObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceCompletableObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::checkDoubleResourceCompletableObserver, this.description("checkDoubleResourceCompletableObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateDisposable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::validateDisposable, this.description("validateDisposable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::validateSubscription, this.description("validateSubscription"));
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

        private EndConsumerHelperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new EndConsumerHelperTest();
        }

        @java.lang.Override
        public EndConsumerHelperTest implementation() {
            return this.implementation;
        }
    }
}
