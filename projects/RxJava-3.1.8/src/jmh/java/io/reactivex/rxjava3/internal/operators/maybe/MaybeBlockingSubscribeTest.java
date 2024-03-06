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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeBlockingSubscribeTest {

    @Test
    public void noArgSuccess() {
        Maybe.just(1).blockingSubscribe();
    }

    @Test
    public void noArgSuccessAsync() {
        Maybe.just(1).delay(100, TimeUnit.MILLISECONDS).blockingSubscribe();
    }

    @Test
    public void noArgEmpty() {
        Maybe.empty().blockingSubscribe();
    }

    @Test
    public void noArgEmptyAsync() {
        Maybe.empty().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe();
    }

    @Test
    public void noArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Maybe.error(new TestException()).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void noArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Maybe.error(new TestException()).delay(100, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void oneArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.just(1).blockingSubscribe(success);
        verify(success).accept(1);
    }

    @Test
    public void oneArgSuccessAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success);
        verify(success).accept(1);
    }

    @Test
    public void oneArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.<Integer>empty().blockingSubscribe(success);
        verify(success, never()).accept(any());
    }

    @Test
    public void oneArgEmptyAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success);
        verify(success, never()).accept(any());
    }

    @Test
    public void oneArgSuccessFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            doThrow(new TestException()).when(success).accept(any());
            Maybe.just(1).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success).accept(1);
        });
    }

    @Test
    public void oneArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
        });
    }

    @Test
    public void oneArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
        });
    }

    @Test
    public void twoArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.just(1).blockingSubscribe(success, consumer);
        verify(success).accept(1);
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgSuccessAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success, consumer);
        verify(success).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.<Integer>empty().blockingSubscribe(success, consumer);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgEmptyAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success, consumer);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgSuccessFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            doThrow(new TestException()).when(success).accept(any());
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.just(1).blockingSubscribe(success, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success).accept(any());
            verify(consumer, never()).accept(any());
        });
    }

    @Test
    public void twoArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).blockingSubscribe(success, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            doThrow(new TestException()).when(consumer).accept(any());
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void threeArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.just(1).blockingSubscribe(success, consumer, action);
        verify(success).accept(any());
        verify(consumer, never()).accept(any(Throwable.class));
        verify(action, never()).run();
    }

    @Test
    public void threeArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.<Integer>empty().blockingSubscribe(success, consumer, action);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any(Throwable.class));
        verify(action).run();
    }

    @Test
    public void threeArgError() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.<Integer>error(new TestException()).blockingSubscribe(success, consumer, action);
        verify(success, never()).accept(any());
        verify(consumer).accept(any(TestException.class));
        verify(action, never()).run();
    }

    @Test
    public void threeArgEmptyFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Action action = mock(Action.class);
            doThrow(new TestException()).when(action).run();
            Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer, action);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
            verify(consumer, never()).accept(any());
            verify(action).run();
        });
    }

    @Test
    public void threeArgInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Action action = mock(Action.class);
            Thread.currentThread().interrupt();
            Maybe.<Integer>never().doOnDispose(onDispose).blockingSubscribe(success, consumer, action);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            verify(success, never()).accept(any());
            verify(action, never()).run();
            verify(consumer).accept(any(InterruptedException.class));
        });
    }

    @Test
    public void observerSuccess() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).blockingSubscribe(to);
        to.assertResult(1);
    }

    @Test
    public void observerSuccessAsync() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertResult(1);
    }

    @Test
    public void observerEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.<Integer>empty().blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerEmptyAsync() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerError() {
        TestObserver<Object> to = new TestObserver<>();
        Maybe.error(new TestException()).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerErrorAsync() {
        TestObserver<Object> to = new TestObserver<>();
        Maybe.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerDispose() throws Throwable {
        Action onDispose = mock(Action.class);
        TestObserver<Object> to = new TestObserver<>();
        to.dispose();
        Maybe.never().doOnDispose(onDispose).blockingSubscribe(to);
        to.assertEmpty();
        verify(onDispose).run();
    }

    @Test
    public void ovserverInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            TestObserver<Object> to = new TestObserver<>();
            Thread.currentThread().interrupt();
            Maybe.never().doOnDispose(onDispose).blockingSubscribe(to);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            to.assertFailure(InterruptedException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgSuccess, this.description("noArgSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgSuccessAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgSuccessAsync, this.description("noArgSuccessAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgEmpty, this.description("noArgEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgEmptyAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgEmptyAsync, this.description("noArgEmptyAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgError, this.description("noArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgErrorAsync, this.description("noArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgSuccess, this.description("oneArgSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccessAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgSuccessAsync, this.description("oneArgSuccessAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgEmpty, this.description("oneArgEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgEmptyAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgEmptyAsync, this.description("oneArgEmptyAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccessFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgSuccessFails, this.description("oneArgSuccessFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgError, this.description("oneArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgErrorAsync, this.description("oneArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgSuccess, this.description("twoArgSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccessAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgSuccessAsync, this.description("twoArgSuccessAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgEmpty, this.description("twoArgEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgEmptyAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgEmptyAsync, this.description("twoArgEmptyAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccessFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgSuccessFails, this.description("twoArgSuccessFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgError, this.description("twoArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgErrorAsync, this.description("twoArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgErrorFails, this.description("twoArgErrorFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::threeArgSuccess, this.description("threeArgSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::threeArgEmpty, this.description("threeArgEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::threeArgError, this.description("threeArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgEmptyFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::threeArgEmptyFails, this.description("threeArgEmptyFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::threeArgInterrupted, this.description("threeArgInterrupted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerSuccess, this.description("observerSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerSuccessAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerSuccessAsync, this.description("observerSuccessAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerEmpty, this.description("observerEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerEmptyAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerEmptyAsync, this.description("observerEmptyAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerError, this.description("observerError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerErrorAsync, this.description("observerErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerDispose, this.description("observerDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ovserverInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ovserverInterrupted, this.description("ovserverInterrupted"));
        }

        private MaybeBlockingSubscribeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeBlockingSubscribeTest();
        }

        @java.lang.Override
        public MaybeBlockingSubscribeTest implementation() {
            return this.implementation;
        }
    }
}
