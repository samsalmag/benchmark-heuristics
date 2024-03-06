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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableToMapTest extends RxJavaTest {

    Observer<Object> objectObserver;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        objectObserver = TestHelper.mockObserver();
        singleObserver = TestHelper.mockSingleObserver();
    }

    Function<String, Integer> lengthFunc = new Function<String, Integer>() {

        @Override
        public Integer apply(String t1) {
            return t1.length();
        }
    };

    Function<String, String> duplicate = new Function<String, String>() {

        @Override
        public String apply(String t1) {
            return t1 + t1;
        }
    };

    @Test
    public void toMapObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFunc).toObservable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMapWithValueSelectorObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicate).toObservable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMapWithErrorObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFuncErr).toObservable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
        verify(objectObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithErrorInValueSelectorObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicateErr).toObservable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
        verify(objectObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithFactoryObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                return new LinkedHashMap<Integer, String>() {

                    private static final long serialVersionUID = -3296811238780863394L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                        return size() > 3;
                    }
                };
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toObservable();
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMapWithErrorThrowingFactoryObservable() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Observable<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toObservable();
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
        verify(objectObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMap() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithValueSelector() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicate);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithError() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFuncErr);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithErrorInValueSelector() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicateErr);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithFactory() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                return new LinkedHashMap<Integer, String>() {

                    private static final long serialVersionUID = -3296811238780863394L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                        return size() > 3;
                    }
                };
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory);
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithErrorThrowingFactory() {
        Observable<String> source = Observable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory);
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapObservable, this.description("toMapObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithValueSelectorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithValueSelectorObservable, this.description("toMapWithValueSelectorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithErrorObservable, this.description("toMapWithErrorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorInValueSelectorObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithErrorInValueSelectorObservable, this.description("toMapWithErrorInValueSelectorObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithFactoryObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithFactoryObservable, this.description("toMapWithFactoryObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorThrowingFactoryObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithErrorThrowingFactoryObservable, this.description("toMapWithErrorThrowingFactoryObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMap, this.description("toMap"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithValueSelector() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithValueSelector, this.description("toMapWithValueSelector"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithError, this.description("toMapWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorInValueSelector() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithErrorInValueSelector, this.description("toMapWithErrorInValueSelector"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithFactory() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithFactory, this.description("toMapWithFactory"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorThrowingFactory() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toMapWithErrorThrowingFactory, this.description("toMapWithErrorThrowingFactory"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private ObservableToMapTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableToMapTest();
        }

        @java.lang.Override
        public ObservableToMapTest implementation() {
            return this.implementation;
        }
    }
}
