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
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.testsupport.TestObserverEx;

public class NotificationLiteTest extends RxJavaTest {

    @Test
    public void acceptFullObserver() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Disposable d = Disposable.empty();
        assertFalse(NotificationLite.acceptFull(NotificationLite.disposable(d), to));
        to.assertSubscribed();
        to.dispose();
        assertTrue(d.isDisposed());
    }

    @Test
    public void errorNotificationCompare() {
        TestException ex = new TestException();
        Object n1 = NotificationLite.error(ex);
        assertEquals(ex.hashCode(), n1.hashCode());
        assertNotEquals(n1, NotificationLite.complete());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_acceptFullObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::acceptFullObserver, this.description("acceptFullObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotificationCompare() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorNotificationCompare, this.description("errorNotificationCompare"));
        }

        private NotificationLiteTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new NotificationLiteTest();
        }

        @java.lang.Override
        public NotificationLiteTest implementation() {
            return this.implementation;
        }
    }
}
