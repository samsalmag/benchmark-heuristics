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
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class BlockingObservableLatestTest extends RxJavaTest {

    @Test
    public void simple() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> source = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        // only 9 because take(10) will immediately call onComplete when receiving the 10th item
        // which onComplete will overwrite the previous value
        for (int i = 0; i < 9; i++) {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(Long.valueOf(i), it.next());
        }
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void sameSourceMultipleIterators() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> source = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        for (int j = 0; j < 3; j++) {
            Iterator<Long> it = iter.iterator();
            // only 9 because take(10) will immediately call onComplete when receiving the 10th item
            // which onComplete will overwrite the previous value
            for (int i = 0; i < 9; i++) {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                Assert.assertTrue(it.hasNext());
                Assert.assertEquals(Long.valueOf(i), it.next());
            }
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertFalse(it.hasNext());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        Observable<Long> source = Observable.<Long>empty();
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        Assert.assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void simpleJustNext() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> source = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        // only 9 because take(10) will immediately call onComplete when receiving the 10th item
        // which onComplete will overwrite the previous value
        for (int i = 0; i < 10; i++) {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertEquals(Long.valueOf(i), it.next());
        }
    }

    @Test(expected = RuntimeException.class)
    public void hasNextThrows() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> source = Observable.<Long>error(new RuntimeException("Forced failure!")).subscribeOn(scheduler);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        it.hasNext();
    }

    @Test(expected = RuntimeException.class)
    public void nextThrows() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> source = Observable.<Long>error(new RuntimeException("Forced failure!")).subscribeOn(scheduler);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        it.next();
    }

    @Test
    public void fasterSource() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> blocker = source;
        Iterable<Integer> iter = blocker.blockingLatest();
        Iterator<Integer> it = iter.iterator();
        source.onNext(1);
        Assert.assertEquals(Integer.valueOf(1), it.next());
        source.onNext(2);
        source.onNext(3);
        Assert.assertEquals(Integer.valueOf(3), it.next());
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        Assert.assertEquals(Integer.valueOf(6), it.next());
        source.onNext(7);
        source.onComplete();
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        Observable.never().blockingLatest().iterator().remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void empty2() {
        Observable.empty().blockingLatest().iterator().next();
    }

    @Test(expected = TestException.class)
    public void error() {
        Observable.error(new TestException()).blockingLatest().iterator().next();
    }

    @Test
    public void error2() {
        Iterator<Object> it = Observable.error(new TestException()).blockingLatest().iterator();
        for (int i = 0; i < 3; i++) {
            try {
                it.hasNext();
                fail("Should have thrown");
            } catch (TestException ex) {
                // expected
            }
        }
    }

    @Test
    public void interrupted() {
        Iterator<Object> it = Observable.never().blockingLatest().iterator();
        Thread.currentThread().interrupt();
        try {
            it.hasNext();
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
        Thread.interrupted();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onError() {
        Iterator<Object> it = Observable.never().blockingLatest().iterator();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ((Observer<Object>) it).onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::simple, this.description("simple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sameSourceMultipleIterators() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sameSourceMultipleIterators, this.description("sameSourceMultipleIterators"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::empty, this.description("empty"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleJustNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::simpleJustNext, this.description("simpleJustNext"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::hasNextThrows, this.description("hasNextThrows"), java.lang.RuntimeException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::nextThrows, this.description("nextThrows"), java.lang.RuntimeException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fasterSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fasterSource, this.description("fasterSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::remove, this.description("remove"), java.lang.UnsupportedOperationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty2() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::empty2, this.description("empty2"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::error, this.description("error"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error2, this.description("error2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interrupted, this.description("interrupted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onError, this.description("onError"));
        }

        private BlockingObservableLatestTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BlockingObservableLatestTest();
        }

        @java.lang.Override
        public BlockingObservableLatestTest implementation() {
            return this.implementation;
        }
    }
}
