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
package io.reactivex.rxjava3.validators;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.Test;
import io.reactivex.rxjava3.testsupport.TestHelper;

/**
 * Checks for commonly copy-pasted but not-renamed local variables in unit tests.
 * <ul>
 * <li>{@code TestSubscriber} named as {@code to*}</li>
 * <li>{@code TestObserver} named as {@code ts*}</li>
 * <li>{@code PublishProcessor} named as {@code ps*}</li>
 * <li>{@code PublishSubject} named as {@code pp*}</li>
 * <li>{@code Subscription} with single letter name such as "s" or "d"</li>
 * <li>{@code Disposable} with single letter name such as "s" or "d"</li>
 * <li>{@code Flowable} named as {@code o|observable} + number</li>
 * <li>{@code Observable} named as {@code f|flowable} + number</li>
 * <li>{@code Subscriber} named as "o" or "observer"</li>
 * <li>{@code Observer} named as "s" or "subscriber"</li>
 * </ul>
 */
public class CheckLocalVariablesInTests {

    static void findPattern(String pattern) throws Exception {
        findPattern(pattern, false);
    }

    static void findPattern(String pattern, boolean checkMain) throws Exception {
        File f = TestHelper.findSource("Flowable");
        if (f == null) {
            System.out.println("Unable to find sources of RxJava");
            return;
        }
        Queue<File> dirs = new ArrayDeque<>();
        StringBuilder fail = new StringBuilder();
        fail.append("The following code pattern was found: ").append(pattern).append("\n");
        File parent = f.getParentFile().getParentFile();
        if (checkMain) {
            dirs.offer(new File(parent.getAbsolutePath().replace('\\', '/')));
        }
        dirs.offer(new File(parent.getAbsolutePath().replace('\\', '/').replace("src/main/java", "src/test/java")));
        Pattern p = Pattern.compile(pattern);
        int total = 0;
        while (!dirs.isEmpty()) {
            f = dirs.poll();
            File[] list = f.listFiles();
            if (list != null && list.length != 0) {
                for (File u : list) {
                    if (u.isDirectory()) {
                        dirs.offer(u);
                    } else {
                        String fname = u.getName();
                        if (fname.endsWith(".java")) {
                            int lineNum = 0;
                            BufferedReader in = new BufferedReader(new FileReader(u));
                            try {
                                for (; ; ) {
                                    String line = in.readLine();
                                    if (line != null) {
                                        lineNum++;
                                        line = line.trim();
                                        if (!line.startsWith("//") && !line.startsWith("*")) {
                                            if (p.matcher(line).find()) {
                                                fail.append(fname).append("#L").append(lineNum).append("    ").append(line).append("\n").append(" at ").append(fname.replace(".java", "")).append(".method(").append(fname).append(":").append(lineNum).append(")\n");
                                                total++;
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } finally {
                                in.close();
                            }
                        }
                    }
                }
            }
        }
        if (total != 0) {
            fail.insert(0, "Found " + total + " instances");
            System.out.println(fail);
            throw new AssertionError(fail.toString());
        }
    }

    @Test
    public void subscriberAsTo() throws Exception {
        findPattern("TestSubscriber(Ex)?<.*>\\s+to");
    }

    @Test
    public void observerAsTs() throws Exception {
        findPattern("TestObserver(Ex)?<.*>\\s+ts");
    }

    @Test
    public void subscriberNoArgAsTo() throws Exception {
        findPattern("TestSubscriber(Ex)?\\s+to");
    }

    @Test
    public void observerNoArgAsTs() throws Exception {
        findPattern("TestObserver(Ex)?\\s+ts");
    }

    @Test
    public void publishSubjectAsPp() throws Exception {
        findPattern("PublishSubject<.*>\\s+pp");
    }

    @Test
    public void publishProcessorAsPs() throws Exception {
        findPattern("PublishProcessor<.*>\\s+ps");
    }

    @Test
    public void unicastSubjectAsUp() throws Exception {
        findPattern("UnicastSubject<.*>\\s+up");
    }

    @Test
    public void unicastProcessorAsUs() throws Exception {
        findPattern("UnicastProcessor<.*>\\s+us");
    }

    @Test
    public void behaviorProcessorAsBs() throws Exception {
        findPattern("BehaviorProcessor<.*>\\s+bs");
    }

    @Test
    public void behaviorSubjectAsBp() throws Exception {
        findPattern("BehaviorSubject<.*>\\s+bp");
    }

    @Test
    public void connectableFlowableAsCo() throws Exception {
        findPattern("ConnectableFlowable<.*>\\s+co(0-9|\\b)");
    }

    @Test
    public void connectableObservableAsCf() throws Exception {
        findPattern("ConnectableObservable<.*>\\s+cf(0-9|\\b)");
    }

    @Test
    public void queueDisposableInsteadOfQueueFuseable() throws Exception {
        findPattern("QueueDisposable\\.(NONE|SYNC|ASYNC|ANY|BOUNDARY)");
    }

    @Test
    public void queueSubscriptionInsteadOfQueueFuseable() throws Exception {
        findPattern("QueueSubscription\\.(NONE|SYNC|ASYNC|ANY|BOUNDARY)");
    }

    @Test
    public void singleSourceAsMs() throws Exception {
        findPattern("SingleSource<.*>\\s+ms");
    }

    @Test
    public void singleSourceAsCs() throws Exception {
        findPattern("SingleSource<.*>\\s+cs");
    }

    @Test
    public void maybeSourceAsSs() throws Exception {
        findPattern("MaybeSource<.*>\\s+ss");
    }

    @Test
    public void maybeSourceAsCs() throws Exception {
        findPattern("MaybeSource<.*>\\s+cs");
    }

    @Test
    public void completableSourceAsSs() throws Exception {
        findPattern("CompletableSource<.*>\\s+ss");
    }

    @Test
    public void completableSourceAsMs() throws Exception {
        findPattern("CompletableSource<.*>\\s+ms");
    }

    @Test
    public void observableAsC() throws Exception {
        findPattern("Observable<.*>\\s+c\\b");
    }

    @Test
    public void subscriberAsObserver() throws Exception {
        findPattern("Subscriber<.*>\\s+observer[0-9]?\\b");
    }

    @Test
    public void subscriberAsO() throws Exception {
        findPattern("Subscriber<.*>\\s+o[0-9]?\\b");
    }

    @Test
    public void singleAsObservable() throws Exception {
        findPattern("Single<.*>\\s+observable\\b");
    }

    @Test
    public void singleAsFlowable() throws Exception {
        findPattern("Single<.*>\\s+flowable\\b");
    }

    @Test
    public void observerAsSubscriber() throws Exception {
        findPattern("Observer<.*>\\s+subscriber[0-9]?\\b");
    }

    @Test
    public void observerAsS() throws Exception {
        findPattern("Observer<.*>\\s+s[0-9]?\\b");
    }

    @Test
    public void observerNoArgAsSubscriber() throws Exception {
        findPattern("Observer\\s+subscriber[0-9]?\\b");
    }

    @Test
    public void observerNoArgAsS() throws Exception {
        findPattern("Observer\\s+s[0-9]?\\b");
    }

    @Test
    public void flowableAsObservable() throws Exception {
        findPattern("Flowable<.*>\\s+observable[0-9]?\\b");
    }

    @Test
    public void flowableAsO() throws Exception {
        findPattern("Flowable<.*>\\s+o[0-9]?\\b");
    }

    @Test
    public void flowableNoArgAsO() throws Exception {
        findPattern("Flowable\\s+o[0-9]?\\b");
    }

    @Test
    public void flowableNoArgAsObservable() throws Exception {
        findPattern("Flowable\\s+observable[0-9]?\\b");
    }

    @Test
    public void processorAsSubject() throws Exception {
        findPattern("Processor<.*>\\s+subject(0-9)?\\b");
    }

    @Test
    public void maybeAsObservable() throws Exception {
        findPattern("Maybe<.*>\\s+observable\\b");
    }

    @Test
    public void maybeAsFlowable() throws Exception {
        findPattern("Maybe<.*>\\s+flowable\\b");
    }

    @Test
    public void completableAsObservable() throws Exception {
        findPattern("Completable\\s+observable\\b");
    }

    @Test
    public void completableAsFlowable() throws Exception {
        findPattern("Completable\\s+flowable\\b");
    }

    @Test
    public void subscriptionAsFieldS() throws Exception {
        findPattern("Subscription\\s+s[0-9]?;", true);
    }

    @Test
    public void subscriptionAsD() throws Exception {
        findPattern("Subscription\\s+d[0-9]?", true);
    }

    @Test
    public void subscriptionAsSubscription() throws Exception {
        findPattern("Subscription\\s+subscription[0-9]?;", true);
    }

    @Test
    public void subscriptionAsDParenthesis() throws Exception {
        findPattern("Subscription\\s+d[0-9]?\\)", true);
    }

    @Test
    public void queueSubscriptionAsD() throws Exception {
        findPattern("Subscription<.*>\\s+q?d[0-9]?\\b", true);
    }

    @Test
    public void booleanSubscriptionAsbd() throws Exception {
        findPattern("BooleanSubscription\\s+bd[0-9]?;", true);
    }

    @Test
    public void atomicSubscriptionAsS() throws Exception {
        findPattern("AtomicReference<Subscription>\\s+s[0-9]?;", true);
    }

    @Test
    public void atomicSubscriptionAsSInit() throws Exception {
        findPattern("AtomicReference<Subscription>\\s+s[0-9]?\\s", true);
    }

    @Test
    public void atomicSubscriptionAsSubscription() throws Exception {
        findPattern("AtomicReference<Subscription>\\s+subscription[0-9]?", true);
    }

    @Test
    public void atomicSubscriptionAsD() throws Exception {
        findPattern("AtomicReference<Subscription>\\s+d[0-9]?", true);
    }

    @Test
    public void disposableAsS() throws Exception {
        // the space before makes sure it doesn't match onSubscribe(Subscription) unnecessarily
        findPattern("Disposable\\s+s[0-9]?\\b", true);
    }

    @Test
    public void disposableAsFieldD() throws Exception {
        findPattern("Disposable\\s+d[0-9]?;", true);
    }

    @Test
    public void atomicDisposableAsS() throws Exception {
        findPattern("AtomicReference<Disposable>\\s+s[0-9]?", true);
    }

    @Test
    public void atomicDisposableAsD() throws Exception {
        findPattern("AtomicReference<Disposable>\\s+d[0-9]?;", true);
    }

    @Test
    public void subscriberAsFieldActual() throws Exception {
        findPattern("Subscriber<.*>\\s+actual[;\\)]", true);
    }

    @Test
    public void subscriberNoArgAsFieldActual() throws Exception {
        findPattern("Subscriber\\s+actual[;\\)]", true);
    }

    @Test
    public void subscriberAsFieldS() throws Exception {
        findPattern("Subscriber<.*>\\s+s[0-9]?;", true);
    }

    @Test
    public void observerAsFieldActual() throws Exception {
        findPattern("Observer<.*>\\s+actual[;\\)]", true);
    }

    @Test
    public void observerAsFieldSO() throws Exception {
        findPattern("Observer<.*>\\s+[so][0-9]?;", true);
    }

    @Test
    public void observerNoArgAsFieldActual() throws Exception {
        findPattern("Observer\\s+actual[;\\)]", true);
    }

    @Test
    public void observerNoArgAsFieldCs() throws Exception {
        findPattern("Observer\\s+cs[;\\)]", true);
    }

    @Test
    public void observerNoArgAsFieldSO() throws Exception {
        findPattern("Observer\\s+[so][0-9]?;", true);
    }

    @Test
    public void queueDisposableAsD() throws Exception {
        findPattern("Disposable<.*>\\s+q?s[0-9]?\\b", true);
    }

    @Test
    public void disposableAsDParenthesis() throws Exception {
        findPattern("Disposable\\s+s[0-9]?\\)", true);
    }

    @Test
    public void compositeDisposableAsCs() throws Exception {
        findPattern("CompositeDisposable\\s+cs[0-9]?", true);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberAsTo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberAsTo, this.description("subscriberAsTo"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerAsTs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerAsTs, this.description("observerAsTs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberNoArgAsTo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberNoArgAsTo, this.description("subscriberNoArgAsTo"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsTs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsTs, this.description("observerNoArgAsTs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectAsPp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishSubjectAsPp, this.description("publishSubjectAsPp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishProcessorAsPs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishProcessorAsPs, this.description("publishProcessorAsPs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unicastSubjectAsUp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unicastSubjectAsUp, this.description("unicastSubjectAsUp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unicastProcessorAsUs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unicastProcessorAsUs, this.description("unicastProcessorAsUs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorProcessorAsBs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorProcessorAsBs, this.description("behaviorProcessorAsBs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectAsBp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectAsBp, this.description("behaviorSubjectAsBp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectableFlowableAsCo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::connectableFlowableAsCo, this.description("connectableFlowableAsCo"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectableObservableAsCf() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::connectableObservableAsCf, this.description("connectableObservableAsCf"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueDisposableInsteadOfQueueFuseable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueDisposableInsteadOfQueueFuseable, this.description("queueDisposableInsteadOfQueueFuseable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueSubscriptionInsteadOfQueueFuseable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueSubscriptionInsteadOfQueueFuseable, this.description("queueSubscriptionInsteadOfQueueFuseable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceAsMs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSourceAsMs, this.description("singleSourceAsMs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceAsCs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSourceAsCs, this.description("singleSourceAsCs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSourceAsSs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeSourceAsSs, this.description("maybeSourceAsSs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSourceAsCs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeSourceAsCs, this.description("maybeSourceAsCs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSourceAsSs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableSourceAsSs, this.description("completableSourceAsSs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSourceAsMs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableSourceAsMs, this.description("completableSourceAsMs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableAsC() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observableAsC, this.description("observableAsC"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberAsObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberAsObserver, this.description("subscriberAsObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberAsO() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberAsO, this.description("subscriberAsO"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleAsObservable, this.description("singleAsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleAsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleAsFlowable, this.description("singleAsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerAsSubscriber() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerAsSubscriber, this.description("observerAsSubscriber"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerAsS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerAsS, this.description("observerAsS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsSubscriber() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsSubscriber, this.description("observerNoArgAsSubscriber"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsS, this.description("observerNoArgAsS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableAsObservable, this.description("flowableAsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableAsO() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableAsO, this.description("flowableAsO"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableNoArgAsO() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableNoArgAsO, this.description("flowableNoArgAsO"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableNoArgAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::flowableNoArgAsObservable, this.description("flowableNoArgAsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_processorAsSubject() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::processorAsSubject, this.description("processorAsSubject"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeAsObservable, this.description("maybeAsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeAsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeAsFlowable, this.description("maybeAsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableAsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableAsObservable, this.description("completableAsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableAsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completableAsFlowable, this.description("completableAsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriptionAsFieldS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriptionAsFieldS, this.description("subscriptionAsFieldS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriptionAsD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriptionAsD, this.description("subscriptionAsD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriptionAsSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriptionAsSubscription, this.description("subscriptionAsSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriptionAsDParenthesis() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriptionAsDParenthesis, this.description("subscriptionAsDParenthesis"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueSubscriptionAsD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueSubscriptionAsD, this.description("queueSubscriptionAsD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_booleanSubscriptionAsbd() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::booleanSubscriptionAsbd, this.description("booleanSubscriptionAsbd"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicSubscriptionAsS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicSubscriptionAsS, this.description("atomicSubscriptionAsS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicSubscriptionAsSInit() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicSubscriptionAsSInit, this.description("atomicSubscriptionAsSInit"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicSubscriptionAsSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicSubscriptionAsSubscription, this.description("atomicSubscriptionAsSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicSubscriptionAsD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicSubscriptionAsD, this.description("atomicSubscriptionAsD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposableAsS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposableAsS, this.description("disposableAsS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposableAsFieldD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposableAsFieldD, this.description("disposableAsFieldD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicDisposableAsS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicDisposableAsS, this.description("atomicDisposableAsS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_atomicDisposableAsD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::atomicDisposableAsD, this.description("atomicDisposableAsD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberAsFieldActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberAsFieldActual, this.description("subscriberAsFieldActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberNoArgAsFieldActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberNoArgAsFieldActual, this.description("subscriberNoArgAsFieldActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberAsFieldS() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberAsFieldS, this.description("subscriberAsFieldS"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerAsFieldActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerAsFieldActual, this.description("observerAsFieldActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerAsFieldSO() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerAsFieldSO, this.description("observerAsFieldSO"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsFieldActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsFieldActual, this.description("observerNoArgAsFieldActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsFieldCs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsFieldCs, this.description("observerNoArgAsFieldCs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerNoArgAsFieldSO() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerNoArgAsFieldSO, this.description("observerNoArgAsFieldSO"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueDisposableAsD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::queueDisposableAsD, this.description("queueDisposableAsD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposableAsDParenthesis() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposableAsDParenthesis, this.description("disposableAsDParenthesis"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeDisposableAsCs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::compositeDisposableAsCs, this.description("compositeDisposableAsCs"));
        }

        private CheckLocalVariablesInTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CheckLocalVariablesInTests();
        }

        @java.lang.Override
        public CheckLocalVariablesInTests implementation() {
            return this.implementation;
        }
    }
}
