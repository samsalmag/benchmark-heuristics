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
package io.reactivex.rxjava3.flowable;

import org.junit.*;
import io.reactivex.rxjava3.core.*;

public class FlowableNotificationTest extends RxJavaTest {

    @Test(expected = NullPointerException.class)
    public void onNextIntegerNotificationDoesNotEqualNullNotification() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> nullNotification = Notification.createOnNext(null);
        Assert.assertNotEquals(integerNotification, nullNotification);
    }

    @Test(expected = NullPointerException.class)
    public void onNextNullNotificationDoesNotEqualIntegerNotification() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> nullNotification = Notification.createOnNext(null);
        Assert.assertNotEquals(nullNotification, integerNotification);
    }

    @Test
    public void onNextIntegerNotificationsWhenEqual() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> integerNotification2 = Notification.createOnNext(1);
        Assert.assertEquals(integerNotification, integerNotification2);
    }

    @Test
    public void onNextIntegerNotificationsWhenNotEqual() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> integerNotification2 = Notification.createOnNext(2);
        Assert.assertNotEquals(integerNotification, integerNotification2);
    }

    @Test
    public void onErrorIntegerNotificationsWhenEqual() {
        final Exception exception = new Exception();
        final Notification<Integer> onErrorNotification = Notification.createOnError(exception);
        final Notification<Integer> onErrorNotification2 = Notification.createOnError(exception);
        Assert.assertEquals(onErrorNotification, onErrorNotification2);
    }

    @Test
    public void onErrorIntegerNotificationWhenNotEqual() {
        final Notification<Integer> onErrorNotification = Notification.createOnError(new Exception());
        final Notification<Integer> onErrorNotification2 = Notification.createOnError(new Exception());
        Assert.assertNotEquals(onErrorNotification, onErrorNotification2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationDoesNotEqualNullNotification() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onNextIntegerNotificationDoesNotEqualNullNotification, this.description("onNextIntegerNotificationDoesNotEqualNullNotification"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextNullNotificationDoesNotEqualIntegerNotification() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onNextNullNotificationDoesNotEqualIntegerNotification, this.description("onNextNullNotificationDoesNotEqualIntegerNotification"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationsWhenEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextIntegerNotificationsWhenEqual, this.description("onNextIntegerNotificationsWhenEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationsWhenNotEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextIntegerNotificationsWhenNotEqual, this.description("onNextIntegerNotificationsWhenNotEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorIntegerNotificationsWhenEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorIntegerNotificationsWhenEqual, this.description("onErrorIntegerNotificationsWhenEqual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorIntegerNotificationWhenNotEqual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorIntegerNotificationWhenNotEqual, this.description("onErrorIntegerNotificationWhenNotEqual"));
        }

        private FlowableNotificationTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableNotificationTest();
        }

        @java.lang.Override
        public FlowableNotificationTest implementation() {
            return this.implementation;
        }
    }
}
