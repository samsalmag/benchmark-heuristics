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
/*
 * The code was inspired by the similarly named JCTools class:
 * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic
 */
package io.reactivex.rxjava3.operators;

import static org.junit.Assert.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.internal.queue.MpscLinkedQueue;

public class SimpleQueueTest extends RxJavaTest {

    @Test(expected = NullPointerException.class)
    public void spscArrayQueueNull() {
        SpscArrayQueue<Object> q = new SpscArrayQueue<>(16);
        q.offer(null);
    }

    @Test(expected = NullPointerException.class)
    public void spscLinkedArrayQueueNull() {
        SpscLinkedArrayQueue<Object> q = new SpscLinkedArrayQueue<>(16);
        q.offer(null);
    }

    @Test(expected = NullPointerException.class)
    public void mpscLinkedQueueNull() {
        MpscLinkedQueue<Object> q = new MpscLinkedQueue<>();
        q.offer(null);
    }

    @Test
    public void spscArrayQueueBiOffer() {
        SpscArrayQueue<Object> q = new SpscArrayQueue<>(16);
        q.offer(1, 2);
        assertEquals(1, q.poll());
        assertEquals(2, q.poll());
        assertNull(q.poll());
    }

    @Test
    public void spscLinkedArrayQueueBiOffer() {
        SpscLinkedArrayQueue<Object> q = new SpscLinkedArrayQueue<>(16);
        q.offer(1, 2);
        assertEquals(1, q.poll());
        assertEquals(2, q.poll());
        assertNull(q.poll());
    }

    @Test
    public void mpscLinkedQueueBiOffer() {
        MpscLinkedQueue<Object> q = new MpscLinkedQueue<>();
        q.offer(1, 2);
        assertEquals(1, q.poll());
        assertEquals(2, q.poll());
        assertNull(q.poll());
    }

    @Test
    public void spscBiOfferCapacity() {
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(8);
        assertTrue(q.offer(1, 2));
        assertTrue(q.offer(3, 4));
        assertTrue(q.offer(5, 6));
        assertTrue(q.offer(7));
        assertFalse(q.offer(8, 9));
        assertFalse(q.offer(9, 10));
    }

    @Test
    public void spscLinkedNewBufferPeek() {
        SpscLinkedArrayQueue<Integer> q = new SpscLinkedArrayQueue<>(8);
        assertTrue(q.offer(1, 2));
        assertTrue(q.offer(3, 4));
        assertTrue(q.offer(5, 6));
        // this should trigger a new buffer
        assertTrue(q.offer(7, 8));
        for (int i = 0; i < 8; i++) {
            assertEquals(i + 1, q.peek().intValue());
            assertEquals(i + 1, q.poll().intValue());
        }
        assertNull(q.peek());
        assertNull(q.poll());
    }

    @Test
    public void mpscOfferPollRace() throws Exception {
        final MpscLinkedQueue<Integer> q = new MpscLinkedQueue<>();
        final AtomicInteger c = new AtomicInteger(3);
        Thread t1 = new Thread(new Runnable() {

            int i;

            @Override
            public void run() {
                c.decrementAndGet();
                while (c.get() != 0) {
                }
                while (i++ < 10000) {
                    q.offer(i);
                }
            }
        });
        t1.start();
        Thread t2 = new Thread(new Runnable() {

            int i = 10000;

            @Override
            public void run() {
                c.decrementAndGet();
                while (c.get() != 0) {
                }
                while (i++ < 10000) {
                    q.offer(i);
                }
            }
        });
        t2.start();
        Runnable r3 = new Runnable() {

            int i = 20000;

            @Override
            public void run() {
                c.decrementAndGet();
                while (c.get() != 0) {
                }
                while (--i > 0) {
                    q.poll();
                }
            }
        };
        r3.run();
        t1.join();
        t2.join();
    }

    @Test
    public void spscLinkedArrayQueueNoNepotism() {
        SpscLinkedArrayQueue<Integer> q = new SpscLinkedArrayQueue<>(16);
        AtomicReferenceArray<Object> ara = q.producerBuffer;
        for (int i = 0; i < 20; i++) {
            q.offer(i);
        }
        assertNotNull(ara.get(16));
        for (int i = 0; i < 20; i++) {
            assertEquals(i, q.poll().intValue());
        }
        assertNull(ara.get(16));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscArrayQueueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::spscArrayQueueNull, this.description("spscArrayQueueNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscLinkedArrayQueueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::spscLinkedArrayQueueNull, this.description("spscLinkedArrayQueueNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mpscLinkedQueueNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mpscLinkedQueueNull, this.description("mpscLinkedQueueNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscArrayQueueBiOffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spscArrayQueueBiOffer, this.description("spscArrayQueueBiOffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscLinkedArrayQueueBiOffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spscLinkedArrayQueueBiOffer, this.description("spscLinkedArrayQueueBiOffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mpscLinkedQueueBiOffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mpscLinkedQueueBiOffer, this.description("mpscLinkedQueueBiOffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscBiOfferCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spscBiOfferCapacity, this.description("spscBiOfferCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscLinkedNewBufferPeek() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spscLinkedNewBufferPeek, this.description("spscLinkedNewBufferPeek"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mpscOfferPollRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mpscOfferPollRace, this.description("mpscOfferPollRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spscLinkedArrayQueueNoNepotism() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spscLinkedArrayQueueNoNepotism, this.description("spscLinkedArrayQueueNoNepotism"));
        }

        private SimpleQueueTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SimpleQueueTest();
        }

        @java.lang.Override
        public SimpleQueueTest implementation() {
            return this.implementation;
        }
    }
}
