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
import io.reactivex.rxjava3.observable.ObservableCovarianceTest.*;

public class ObservableCombineLatestTests extends RxJavaTest {

    /**
     * This won't compile if super/extends isn't done correctly on generics.
     */
    @Test
    public void covarianceOfCombineLatest() {
        Observable<HorrorMovie> horrors = Observable.just(new HorrorMovie());
        Observable<CoolRating> ratings = Observable.just(new CoolRating());
        Observable.<Movie, CoolRating, Result>combineLatest(horrors, ratings, combine).blockingForEach(action);
        Observable.<Movie, CoolRating, Result>combineLatest(horrors, ratings, combine).blockingForEach(action);
        Observable.<Media, Rating, ExtendedResult>combineLatest(horrors, ratings, combine).blockingForEach(extendedAction);
        Observable.<Media, Rating, Result>combineLatest(horrors, ratings, combine).blockingForEach(action);
        Observable.<Media, Rating, ExtendedResult>combineLatest(horrors, ratings, combine).blockingForEach(action);
        Observable.<Movie, CoolRating, Result>combineLatest(horrors, ratings, combine);
    }

    BiFunction<Media, Rating, ExtendedResult> combine = new BiFunction<Media, Rating, ExtendedResult>() {

        @Override
        public ExtendedResult apply(Media m, Rating r) {
            return new ExtendedResult();
        }
    };

    Consumer<Result> action = new Consumer<Result>() {

        @Override
        public void accept(Result t1) {
            System.out.println("Result: " + t1);
        }
    };

    Consumer<ExtendedResult> extendedAction = new Consumer<ExtendedResult>() {

        @Override
        public void accept(ExtendedResult t1) {
            System.out.println("Result: " + t1);
        }
    };

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfCombineLatest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::covarianceOfCombineLatest, this.description("covarianceOfCombineLatest"));
        }

        private ObservableCombineLatestTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableCombineLatestTests();
        }

        @java.lang.Override
        public ObservableCombineLatestTests implementation() {
            return this.implementation;
        }
    }
}
