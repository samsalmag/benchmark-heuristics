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

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableCovarianceTest.*;
import io.reactivex.rxjava3.functions.BiFunction;

public class FlowableReduceTests extends RxJavaTest {

    @Test
    public void reduceIntsFlowable() {
        Flowable<Integer> f = Flowable.just(1, 2, 3);
        int value = f.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().blockingSingle();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjectsFlowable() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Flowable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toFlowable();
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjectsFlowable() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toFlowable();
        assertNotNull(reduceResult2);
    }

    @Test
    public void reduceInts() {
        Flowable<Integer> f = Flowable.just(1, 2, 3);
        int value = f.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().blockingSingle();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjects() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjects() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceCovariance() {
        // must type it to <Movie>
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        libraryFunctionActingOnMovieObservables(horrorMovies);
    }

    /*
     * This accepts <Movie> instead of <? super Movie> since `reduce` can't handle covariants
     */
    public void libraryFunctionActingOnMovieObservables(Flowable<Movie> obs) {
        obs.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceIntsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceIntsFlowable, this.description("reduceIntsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjectsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithObjectsFlowable, this.description("reduceWithObjectsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjectsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithCovariantObjectsFlowable, this.description("reduceWithCovariantObjectsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceInts() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceInts, this.description("reduceInts"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjects() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithObjects, this.description("reduceWithObjects"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjects() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithCovariantObjects, this.description("reduceWithCovariantObjects"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceCovariance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceCovariance, this.description("reduceCovariance"));
        }

        private FlowableReduceTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableReduceTests();
        }

        @java.lang.Override
        public FlowableReduceTests implementation() {
            return this.implementation;
        }
    }
}
