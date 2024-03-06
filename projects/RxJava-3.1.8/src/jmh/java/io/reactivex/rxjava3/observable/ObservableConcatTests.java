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

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.observable.ObservableCovarianceTest.*;

public class ObservableConcatTests extends RxJavaTest {

    @Test
    public void concatSimple() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        List<String> values = Observable.concat(o1, o2).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
    }

    @Test
    public void concatWithObservableOfObservable() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        Observable<String> o3 = Observable.just("five", "six");
        Observable<Observable<String>> os = Observable.just(o1, o2, o3);
        List<String> values = Observable.concat(os).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
        assertEquals("five", values.get(4));
        assertEquals("six", values.get(5));
    }

    @Test
    public void concatWithIterableOfObservable() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        Observable<String> o3 = Observable.just("five", "six");
        Iterable<Observable<String>> is = Arrays.asList(o1, o2, o3);
        List<String> values = Observable.concat(Observable.fromIterable(is)).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
        assertEquals("five", values.get(4));
        assertEquals("six", values.get(5));
    }

    @Test
    public void concatCovariance() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Observable<Media> o1 = Observable.<Media>just(horrorMovie1, movie);
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        Observable<Observable<Media>> os = Observable.just(o1, o2);
        List<Media> values = Observable.concat(os).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @Test
    public void concatCovariance2() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media1 = new Media();
        Media media2 = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Observable<Media> o1 = Observable.just(horrorMovie1, movie, media1);
        Observable<Media> o2 = Observable.just(media2, horrorMovie2);
        Observable<Observable<Media>> os = Observable.just(o1, o2);
        List<Media> values = Observable.concat(os).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media1, values.get(2));
        assertEquals(media2, values.get(3));
        assertEquals(horrorMovie2, values.get(4));
        assertEquals(5, values.size());
    }

    @Test
    public void concatCovariance3() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Observable<Movie> o1 = Observable.just(horrorMovie1, movie);
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        List<Media> values = Observable.concat(o1, o2).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @Test
    public void concatCovariance4() {
        final HorrorMovie horrorMovie1 = new HorrorMovie();
        final Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Observable<Movie> o1 = Observable.unsafeCreate(new ObservableSource<Movie>() {

            @Override
            public void subscribe(Observer<? super Movie> o) {
                o.onNext(horrorMovie1);
                o.onNext(movie);
                // o.onNext(new Media()); // correctly doesn't compile
                o.onComplete();
            }
        });
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        List<Media> values = Observable.concat(o1, o2).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSimple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatSimple, this.description("concatSimple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithObservableOfObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatWithObservableOfObservable, this.description("concatWithObservableOfObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithIterableOfObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatWithIterableOfObservable, this.description("concatWithIterableOfObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatCovariance, this.description("concatCovariance"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatCovariance2, this.description("concatCovariance2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatCovariance3, this.description("concatCovariance3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatCovariance4, this.description("concatCovariance4"));
        }

        private ObservableConcatTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableConcatTests();
        }

        @java.lang.Override
        public ObservableConcatTests implementation() {
            return this.implementation;
        }
    }
}
