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

import java.util.HashMap;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observable.ObservableEventStream.Event;

public class ObservableScanTests extends RxJavaTest {

    @Test
    public void unsubscribeScan() throws Exception {
        ObservableEventStream.getEventStream("HTTP-ClusterB", 20).scan(new HashMap<>(), new BiFunction<HashMap<String, String>, Event, HashMap<String, String>>() {

            @Override
            public HashMap<String, String> apply(HashMap<String, String> accum, Event perInstanceEvent) {
                accum.put("instance", perInstanceEvent.instanceId);
                return accum;
            }
        }).take(10).blockingForEach(new Consumer<HashMap<String, String>>() {

            @Override
            public void accept(HashMap<String, String> pv) {
                System.out.println(pv);
            }
        });
        // make sure the event streams receive their interrupt
        Thread.sleep(200);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeScan() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribeScan, this.description("unsubscribeScan"));
        }

        private ObservableScanTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableScanTests();
        }

        @java.lang.Override
        public ObservableScanTests implementation() {
            return this.implementation;
        }
    }
}
