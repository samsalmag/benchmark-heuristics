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

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observable.ObservableEventStream.Event;
import io.reactivex.rxjava3.observables.GroupedObservable;

public class ObservableGroupByTests extends RxJavaTest {

    @Test
    public void takeUnsubscribesOnGroupBy() throws Exception {
        Observable.merge(ObservableEventStream.getEventStream("HTTP-ClusterA", 50), ObservableEventStream.getEventStream("HTTP-ClusterB", 20)).// group by type (2 clusters)
        groupBy(new Function<Event, String>() {

            @Override
            public String apply(Event event) {
                return event.type;
            }
        }).take(1).blockingForEach(new Consumer<GroupedObservable<String, Event>>() {

            @Override
            public void accept(GroupedObservable<String, Event> v) {
                System.out.println(v);
                // FIXME groups need consumption to a certain degree to cancel upstream
                v.take(1).subscribe();
            }
        });
        System.out.println("**** finished");
        // make sure the event streams receive their interrupt
        Thread.sleep(200);
    }

    @Test
    public void takeUnsubscribesOnFlatMapOfGroupBy() throws Exception {
        Observable.merge(ObservableEventStream.getEventStream("HTTP-ClusterA", 50), ObservableEventStream.getEventStream("HTTP-ClusterB", 20)).// group by type (2 clusters)
        groupBy(new Function<Event, String>() {

            @Override
            public String apply(Event event) {
                return event.type;
            }
        }).flatMap(new Function<GroupedObservable<String, Event>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(GroupedObservable<String, Event> g) {
                return g.map(new Function<Event, Object>() {

                    @Override
                    public Object apply(Event event) {
                        return event.instanceId + " - " + event.values.get("count200");
                    }
                });
            }
        }).take(20).blockingForEach(new Consumer<Object>() {

            @Override
            public void accept(Object pv) {
                System.out.println(pv);
            }
        });
        System.out.println("**** finished");
        // make sure the event streams receive their interrupt
        Thread.sleep(200);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUnsubscribesOnGroupBy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUnsubscribesOnGroupBy, this.description("takeUnsubscribesOnGroupBy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUnsubscribesOnFlatMapOfGroupBy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUnsubscribesOnFlatMapOfGroupBy, this.description("takeUnsubscribesOnFlatMapOfGroupBy"));
        }

        private ObservableGroupByTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableGroupByTests();
        }

        @java.lang.Override
        public ObservableGroupByTests implementation() {
            return this.implementation;
        }
    }
}
