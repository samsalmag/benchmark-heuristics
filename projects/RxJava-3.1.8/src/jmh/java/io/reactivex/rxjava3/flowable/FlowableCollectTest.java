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

import static io.reactivex.rxjava3.internal.util.TestingHelper.*;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public final class FlowableCollectTest extends RxJavaTest {

    @Test
    public void collectToListFlowable() {
        Flowable<List<Integer>> f = Flowable.just(1, 2, 3).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer v) {
                list.add(v);
            }
        }).toFlowable();
        List<Integer> list = f.blockingLast();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).intValue());
        assertEquals(2, list.get(1).intValue());
        assertEquals(3, list.get(2).intValue());
        // test multiple subscribe
        List<Integer> list2 = f.blockingLast();
        assertEquals(3, list2.size());
        assertEquals(1, list2.get(0).intValue());
        assertEquals(2, list2.get(1).intValue());
        assertEquals(3, list2.get(2).intValue());
    }

    @Test
    public void collectToStringFlowable() {
        String value = Flowable.just(1, 2, 3).collect(new Supplier<StringBuilder>() {

            @Override
            public StringBuilder get() {
                return new StringBuilder();
            }
        }, new BiConsumer<StringBuilder, Integer>() {

            @Override
            public void accept(StringBuilder sb, Integer v) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(v);
            }
        }).toFlowable().blockingLast().toString();
        assertEquals("1-2-3", value);
    }

    @Test
    public void factoryFailureResultsInErrorEmissionFlowable() {
        final RuntimeException e = new RuntimeException();
        Flowable.just(1).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                throw e;
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t) {
                list.add(t);
            }
        }).test().assertNoValues().assertError(e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInTwoErrorEmissionsFlowable() {
        try {
            final List<Throwable> list = new CopyOnWriteArrayList<>();
            RxJavaPlugins.setErrorHandler(addToList(list));
            final RuntimeException e1 = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            // 
            Burst.items(1).error(e2).collect(supplierListCreator(), biConsumerThrows(e1)).toFlowable().// 
            test().assertError(// 
            e1).assertNotComplete();
            assertEquals(1, list.size());
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable() {
        final RuntimeException e = new RuntimeException();
        // 
        Burst.item(1).create().collect(supplierListCreator(), // 
        biConsumerThrows(e)).toFlowable().// 
        test().assertError(// 
        e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable() {
        final RuntimeException e = new RuntimeException();
        final AtomicBoolean added = new AtomicBoolean();
        BiConsumer<Object, Integer> throwOnFirstOnly = new BiConsumer<Object, Integer>() {

            boolean once = true;

            @Override
            public void accept(Object o, Integer t) {
                if (once) {
                    once = false;
                    throw e;
                } else {
                    added.set(true);
                }
            }
        };
        // 
        Burst.items(1, 2).create().collect(supplierListCreator(), // 
        throwOnFirstOnly).toFlowable().// 
        test().assertError(// 
        e).// 
        assertNoValues().assertNotComplete();
        assertFalse(added.get());
    }

    @Test
    public void collectIntoFlowable() {
        Flowable.just(1, 1, 1, 1, 2).collectInto(new HashSet<>(), new BiConsumer<HashSet<Integer>, Integer>() {

            @Override
            public void accept(HashSet<Integer> s, Integer v) throws Exception {
                s.add(v);
            }
        }).toFlowable().test().assertResult(new HashSet<>(Arrays.asList(1, 2)));
    }

    @Test
    public void collectToList() {
        Single<List<Integer>> o = Flowable.just(1, 2, 3).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer v) {
                list.add(v);
            }
        });
        List<Integer> list = o.blockingGet();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).intValue());
        assertEquals(2, list.get(1).intValue());
        assertEquals(3, list.get(2).intValue());
        // test multiple subscribe
        List<Integer> list2 = o.blockingGet();
        assertEquals(3, list2.size());
        assertEquals(1, list2.get(0).intValue());
        assertEquals(2, list2.get(1).intValue());
        assertEquals(3, list2.get(2).intValue());
    }

    @Test
    public void collectToString() {
        String value = Flowable.just(1, 2, 3).collect(new Supplier<StringBuilder>() {

            @Override
            public StringBuilder get() {
                return new StringBuilder();
            }
        }, new BiConsumer<StringBuilder, Integer>() {

            @Override
            public void accept(StringBuilder sb, Integer v) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(v);
            }
        }).blockingGet().toString();
        assertEquals("1-2-3", value);
    }

    @Test
    public void factoryFailureResultsInErrorEmission() {
        final RuntimeException e = new RuntimeException();
        Flowable.just(1).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                throw e;
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t) {
                list.add(t);
            }
        }).test().assertNoValues().assertError(e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInTwoErrorEmissions() {
        try {
            final List<Throwable> list = new CopyOnWriteArrayList<>();
            RxJavaPlugins.setErrorHandler(addToList(list));
            final RuntimeException e1 = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            // 
            Burst.items(1).error(e2).collect(supplierListCreator(), // 
            biConsumerThrows(e1)).// 
            test().assertError(// 
            e1).assertNotComplete();
            assertEquals(1, list.size());
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndCompletedEmissions() {
        final RuntimeException e = new RuntimeException();
        // 
        Burst.item(1).create().collect(supplierListCreator(), // 
        biConsumerThrows(e)).// 
        test().assertError(// 
        e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndOnNextEmissions() {
        final RuntimeException e = new RuntimeException();
        final AtomicBoolean added = new AtomicBoolean();
        BiConsumer<Object, Integer> throwOnFirstOnly = new BiConsumer<Object, Integer>() {

            boolean once = true;

            @Override
            public void accept(Object o, Integer t) {
                if (once) {
                    once = false;
                    throw e;
                } else {
                    added.set(true);
                }
            }
        };
        // 
        Burst.items(1, 2).create().collect(supplierListCreator(), // 
        throwOnFirstOnly).// 
        test().assertError(// 
        e).// 
        assertNoValues().assertNotComplete();
        assertFalse(added.get());
    }

    @Test
    public void collectInto() {
        Flowable.just(1, 1, 1, 1, 2).collectInto(new HashSet<>(), new BiConsumer<HashSet<Integer>, Integer>() {

            @Override
            public void accept(HashSet<Integer> s, Integer v) throws Exception {
                s.add(v);
            }
        }).test().assertResult(new HashSet<>(Arrays.asList(1, 2)));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1, 2).collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

            @Override
            public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                a.add(b);
            }
        }));
        TestHelper.checkDisposed(Flowable.just(1, 2).collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

            @Override
            public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                a.add(b);
            }
        }).toFlowable());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Integer>, Flowable<ArrayList<Integer>>>() {

            @Override
            public Flowable<ArrayList<Integer>> apply(Flowable<Integer> f) throws Exception {
                return f.collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

                    @Override
                    public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                        a.add(b);
                    }
                }).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Integer>, Single<ArrayList<Integer>>>() {

            @Override
            public Single<ArrayList<Integer>> apply(Flowable<Integer> f) throws Exception {
                return f.collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

                    @Override
                    public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                        a.add(b);
                    }
                });
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToListFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectToListFlowable, this.description("collectToListFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToStringFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectToStringFlowable, this.description("collectToStringFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_factoryFailureResultsInErrorEmissionFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::factoryFailureResultsInErrorEmissionFlowable, this.description("factoryFailureResultsInErrorEmissionFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInTwoErrorEmissionsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInTwoErrorEmissionsFlowable, this.description("collectorFailureDoesNotResultInTwoErrorEmissionsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable, this.description("collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable, this.description("collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectIntoFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectIntoFlowable, this.description("collectIntoFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectToList, this.description("collectToList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectToString, this.description("collectToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_factoryFailureResultsInErrorEmission() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::factoryFailureResultsInErrorEmission, this.description("factoryFailureResultsInErrorEmission"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInTwoErrorEmissions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInTwoErrorEmissions, this.description("collectorFailureDoesNotResultInTwoErrorEmissions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndCompletedEmissions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInErrorAndCompletedEmissions, this.description("collectorFailureDoesNotResultInErrorAndCompletedEmissions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndOnNextEmissions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectorFailureDoesNotResultInErrorAndOnNextEmissions, this.description("collectorFailureDoesNotResultInErrorAndOnNextEmissions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectInto() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::collectInto, this.description("collectInto"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private FlowableCollectTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableCollectTest();
        }

        @java.lang.Override
        public FlowableCollectTest implementation() {
            return this.implementation;
        }
    }
}
