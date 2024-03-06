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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ScheduledDirectPeriodicTaskTest extends RxJavaTest {

    @Test
    public void runnableThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(new Runnable() {

                @Override
                public void run() {
                    throw new TestException();
                }
            }, true);
            try {
                task.run();
                fail("Should have thrown!");
            } catch (TestException expected) {
                // expected
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::runnableThrows, this.description("runnableThrows"));
        }

        private ScheduledDirectPeriodicTaskTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScheduledDirectPeriodicTaskTest();
        }

        @java.lang.Override
        public ScheduledDirectPeriodicTaskTest implementation() {
            return this.implementation;
        }
    }
}
