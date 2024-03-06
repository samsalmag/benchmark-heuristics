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
package io.reactivex.rxjava3.observable;

import static org.mockito.Mockito.inOrder;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableThrottleWithTimeoutTests extends RxJavaTest {

    @Test
    public void throttle() {
        Observer<Integer> observer = TestHelper.mockObserver();
        TestScheduler s = new TestScheduler();
        PublishSubject<Integer> o = PublishSubject.create();
        o.throttleWithTimeout(500, TimeUnit.MILLISECONDS, s).subscribe(observer);
        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(1);
        // deliver
        o.onNext(2);
        s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(3);
        s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(4);
        s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(5);
        // deliver at 1300 after 500ms has passed since onNext(5)
        o.onNext(6);
        s.advanceTimeTo(1300, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(7);
        s.advanceTimeTo(1800, TimeUnit.MILLISECONDS);
        o.onComplete();
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext(2);
        inOrder.verify(observer).onNext(6);
        inOrder.verify(observer).onNext(7);
        inOrder.verify(observer).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttleFirstDefaultScheduler() {
        Observable.just(1).throttleWithTimeout(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttle, this.description("throttle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttleFirstDefaultScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttleFirstDefaultScheduler, this.description("throttleFirstDefaultScheduler"));
        }

        private ObservableThrottleWithTimeoutTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableThrottleWithTimeoutTests();
        }

        @java.lang.Override
        public ObservableThrottleWithTimeoutTests implementation() {
            return this.implementation;
        }
    }
}
