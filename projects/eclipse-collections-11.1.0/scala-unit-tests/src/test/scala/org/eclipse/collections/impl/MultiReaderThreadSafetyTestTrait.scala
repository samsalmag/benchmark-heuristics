/*
 * Copyright (c) 2021 Goldman Sachs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl

import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

import org.eclipse.collections.api.collection.MutableCollection
import org.eclipse.collections.api.list.MutableList
import org.eclipse.collections.api.multimap.MutableMultimap
import org.eclipse.collections.api.tuple.Pair
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.collection.mutable.AbstractMultiReaderMutableCollection
import org.eclipse.collections.impl.list.mutable.FastList
import org.eclipse.collections.impl.multimap.list.FastListMultimap
import org.junit.{Assert, src.main.java.Testing}

trait MultiReaderThreadSafetyTestTrait
{
    val classUnderTest: AbstractMultiReaderMutableCollection[Int]

    def createReadLockHolderThread(gate: Gate): Thread

    def createWriteLockHolderThread(gate: Gate): Thread

    def sleep(gate: Gate): Unit =
    {
        gate.open()

        try
        {
            Thread.sleep(java.lang.Long.MAX_VALUE)
        }
        catch
            {
                case ignore: InterruptedException => Thread.currentThread.interrupt()
            }
    }

    def spawn(code: => Unit) =
    {
        val result = new Thread
        {
            override def run() = code
        }
        result.start()
        result
    }

    class Gate
    {
        val latch = new java.util.concurrent.CountDownLatch(1)

        def open(): Unit = this.latch.countDown()

        def await(): Unit = this.latch.await()
    }

    def time(code: => Unit) =
    {
        val before = System.currentTimeMillis
        code
        val after = System.currentTimeMillis
        after - before
    }

    def assert(readersBlocked: Boolean, writersBlocked: Boolean)(code: => Any): Unit =
    {
        if (readersBlocked)
        {
            assertReadersBlocked(code)
        }
        else
        {
            assertReadersNotBlocked(code)
        }

        if (writersBlocked)
        {
            assertWritersBlocked(code)
        }
        else
        {
            assertWritersNotBlocked(code)
        }
    }

    def assertReadersBlocked(code: => Unit): Unit =
    {
        this.assertReadSafety(threadSafe = true, 10L, TimeUnit.MILLISECONDS, code)
    }

    def assertReadersNotBlocked(code: => Unit): Unit =
    {
        this.assertReadSafety(threadSafe = false, 60L, TimeUnit.SECONDS, code)
    }

    def assertWritersBlocked(code: => Unit): Unit =
    {
        this.assertWriteSafety(threadSafe = true, 10L, TimeUnit.MILLISECONDS, code)
    }

    def assertWritersNotBlocked(code: => Unit): Unit =
    {
        this.assertWriteSafety(threadSafe = false, 60L, TimeUnit.SECONDS, code)
    }

    def assertReadSafety(threadSafe: Boolean, timeout: Long, timeUnit: TimeUnit, code: => Unit): Unit =
    {
        val gate = new Gate
        assertThreadSafety(timeout, timeUnit, gate, code, threadSafe, createReadLockHolderThread(gate))
    }

    def assertWriteSafety(threadSafe: Boolean, timeout: Long, timeUnit: TimeUnit, code: => Unit): Unit =
    {
        val gate = new Gate
        assertThreadSafety(timeout, timeUnit, gate, code, threadSafe, createWriteLockHolderThread(gate))
    }

    def assertThreadSafety(timeout: Long, timeUnit: TimeUnit, gate: MultiReaderThreadSafetyTestTrait.this.type#Gate, code: => Unit, threadSafe: Boolean, lockHolderThread: Thread): Unit =
    {
        val millisTimeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit)
        val measuredTime = time
        {
            // Don't start until the other thread is synchronized on classUnderTest
            gate.await()
            spawn(code).join(millisTimeout, 0)
        }

        Assert.assertEquals(
            "Measured " + measuredTime + " ms but timeout was " + millisTimeout + " ms.",
            threadSafe,
            measuredTime >= millisTimeout)

        lockHolderThread.interrupt()
        lockHolderThread.join()
    }

    @src.main.java.Testing
    def newEmpty_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            this.classUnderTest.newEmpty
        }

    @src.main.java.Testing
    def iterator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            try
            {
                this.classUnderTest.iterator
            }
            catch
                {
                    case e: Exception => ()
                }
        }

    @src.main.java.Testing
    def add_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.add(4)
        }

    @src.main.java.Testing
    def addAll_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.addAll(new FastList[Int])
        }

    @src.main.java.Testing
    def addAllIterable_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.addAllIterable(new FastList[Int])
        }

    @src.main.java.Testing
    def remove_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.remove(1)
        }

    @src.main.java.Testing
    def removeAll_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.removeAll(new FastList[Int])
        }

    @src.main.java.Testing
    def removeAllIterable_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.removeAllIterable(new FastList[Int])
        }

    @src.main.java.Testing
    def retainAll_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.retainAll(new FastList[Int])
        }

    @src.main.java.Testing
    def retainAllIterable_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.retainAllIterable(new FastList[Int])
        }

    @src.main.java.Testing
    def removeIf_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.removeIf((_: Int) => true)
        }

    @src.main.java.Testing
    def removeIfWith_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.removeIfWith((_: Int, _: Int) => true, 0)
        }

    @src.main.java.Testing
    def with_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.`with`(4)
        }

    @src.main.java.Testing
    def without_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.without(1)
        }

    @src.main.java.Testing
    def withAll_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.withAll(new FastList[Int])
        }

    @src.main.java.Testing
    def withoutAll_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.withoutAll(new FastList[Int])
        }

    @src.main.java.Testing
    def clear_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.clear()
        }

    @src.main.java.Testing
    def size_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.size
        }

    @src.main.java.Testing
    def getFirst_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.getFirst
        }

    @src.main.java.Testing
    def getLast_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.getLast
        }

    @src.main.java.Testing
    def isEmpty_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.isEmpty
        }

    @src.main.java.Testing
    def notEmpty_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.notEmpty
        }

    @src.main.java.Testing
    def contains_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.contains(1)
        }

    @src.main.java.Testing
    def containsAll_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.containsAll(new FastList[Int])
        }

    @src.main.java.Testing
    def containsAllIterable_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.containsAll(new FastList[Int])
        }

    @src.main.java.Testing
    def containsAllArguments_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.containsAllArguments("1", "2")
        }

    @src.main.java.Testing
    def equals_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.equals(null)
        }

    @src.main.java.Testing
    def hashCode_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.hashCode
        }

    @src.main.java.Testing
    def forEach_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.forEach((_: Int) => ())
        }

    @src.main.java.Testing
    def forEachWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.forEachWith((_: Int, _: Int) => (), 0)
        }

    @src.main.java.Testing
    def collect_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collect[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def collect_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collect[String, MutableCollection[String]]((_: Int) => "", new FastList[String])
        }

    @src.main.java.Testing
    def collectWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collectWith[String, String]((_: Int, _: String) => "", "")
        }

    @src.main.java.Testing
    def collectWith_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collectWith[String, String, MutableList[String]]((_: Int, _: String) => "", "", new FastList[String])
        }

    @src.main.java.Testing
    def flatCollect_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.flatCollect[String]((_: Int) => new FastList[String])
        }

    @src.main.java.Testing
    def flatCollect_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.flatCollect[Int, MutableList[Int]]((_: Int) => new FastList[Int], new FastList[Int])
        }

    @src.main.java.Testing
    def collectIf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collectIf[String]((_: Int) => true, (_: Int) => "")
        }

    @src.main.java.Testing
    def collectIf_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.collectIf[String, MutableCollection[String]]((_: Int) => true, (num: Int) => "", new FastList[String])
        }

    @src.main.java.Testing
    def select_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.select((_: Int) => true)
        }

    @src.main.java.Testing
    def select_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.select((_: Int) => true, new FastList[Int])
        }

    @src.main.java.Testing
    def selectWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.selectWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def selectWith_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.selectWith((_: Int, _: Int) => true, 1, new FastList[Int])
        }

    @src.main.java.Testing
    def reject_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.reject((_: Int) => true)
        }

    @src.main.java.Testing
    def reject_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.reject((_: Int) => true, new FastList[Int])
        }

    @src.main.java.Testing
    def rejectWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.rejectWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def rejectWith_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.rejectWith((_: Int, _: Int) => true, 1, new FastList[Int])
        }

    @src.main.java.Testing
    def selectInstancesOf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.selectInstancesOf(Int.getClass)
        }

    @src.main.java.Testing
    def partition_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.partition((_: Int) => true)
        }

    @src.main.java.Testing
    def partitionWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.partitionWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def selectAndRejectWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.selectAndRejectWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def count_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.count((_: Int) => true)
        }

    @src.main.java.Testing
    def countWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.countWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def min_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.min
        }

    @src.main.java.Testing
    def max_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.max
        }

    @src.main.java.Testing
    def min_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.min((_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def max_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.max((_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def minBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.minBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def maxBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.maxBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def injectInto_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.injectInto[Int](0, (_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def injectIntoWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.injectIntoWith[Int, Int](0, (_: Int, _: Int, _: Int) => 0, 0)
        }

    @src.main.java.Testing
    def sumOfInt_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.sumOfInt((_: Int) => 0)
        }

    @src.main.java.Testing
    def sumOfLong_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.sumOfLong((_: Int) => 0L)
        }

    @src.main.java.Testing
    def sumOfDouble_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.sumOfDouble((_: Int) => 0.0)
        }

    @src.main.java.Testing
    def sumOfFloat_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.sumOfFloat((_: Int) => 0.0f)
        }

    @src.main.java.Testing
    def toString_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toString
        }

    @src.main.java.Testing
    def makeString_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.makeString
        }

    @src.main.java.Testing
    def makeString_withSeparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.makeString(", ")
        }

    @src.main.java.Testing
    def makeString_withStartEndSeparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.makeString("[", ", ", "]")
        }

    @src.main.java.Testing
    def appendString_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.appendString(new StringBuilder)
        }

    @src.main.java.Testing
    def appendString_withSeparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.appendString(new StringBuilder, ", ")
        }

    @src.main.java.Testing
    def appendString_withStartEndSeparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.appendString(new StringBuilder, "[", ", ", "]")
        }

    @src.main.java.Testing
    def groupBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.groupBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def groupBy_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.groupBy[String, MutableMultimap[String, Int]]((_: Int) => "", new FastListMultimap[String, Int])
        }

    @src.main.java.Testing
    def groupByEach_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.groupByEach((_: Int) => new FastList[String])
        }

    @src.main.java.Testing
    def groupByEach_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.groupByEach[Int, MutableMultimap[Int, Int]]((_: Int) => new FastList[Int], new FastListMultimap[Int, Int])
        }

    @src.main.java.Testing
    def aggregateBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.aggregateBy[String, Int]((_: Int) => "", () => 0, (_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def aggregateInPlaceBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.aggregateInPlaceBy[String, Int]((_: Int) => "", () => 0, (_: Int, _: Int) => ())
        }

    @src.main.java.Testing
    def zip_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.zip(FastList.newListWith("1", "1", "2"))
        }

    @src.main.java.Testing
    def zip_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.zip(FastList.newListWith[String]("1", "1", "2"), new FastList[Pair[Int, String]])
        }

    @src.main.java.Testing
    def zipByIndex_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.zipWithIndex()
        }

    @src.main.java.Testing
    def zipByIndex_withTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.zipWithIndex(new FastList[Pair[Int, java.lang.Integer]])
        }

    @src.main.java.Testing
    def chunk_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.chunk(2)
        }

    @src.main.java.Testing
    def anySatisfy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.anySatisfy((_: Int) => true)
        }

    @src.main.java.Testing
    def anySatisfyWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.anySatisfyWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def allSatisfy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.allSatisfy((_: Int) => true)
        }

    @src.main.java.Testing
    def allSatisfyWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.allSatisfyWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def noneSatisfy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.noneSatisfy((_: Int) => true)
        }

    @src.main.java.Testing
    def noneSatisfyWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.noneSatisfyWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def detect_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.detect((_: Int) => true)
        }

    @src.main.java.Testing
    def detectIfNone_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.detectIfNone((_: Int) => true, () => 1)
        }

    @src.main.java.Testing
    def detectWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.detectWith((_: Int, _: Int) => true, 1)
        }

    @src.main.java.Testing
    def detectWithIfNone_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.detectWithIfNone((_: Int, _: Int) => true, 1, () => 1)
        }

    @src.main.java.Testing
    def asLazy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.asLazy
        }

    @src.main.java.Testing
    def asUnmodifiable_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.asUnmodifiable
        }

    @src.main.java.Testing
    def asSynchronized_safe(): Unit =
    {
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.asSynchronized
        }
    }

    @src.main.java.Testing
    def toImmutable_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toImmutable
        }

    @src.main.java.Testing
    def toList_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toList
        }

    @src.main.java.Testing
    def toSortedList_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedList
        }

    @src.main.java.Testing
    def toSortedList_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedList((_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedListBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedListBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def toSet_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSet
        }

    @src.main.java.Testing
    def toSortedSet_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedSet
        }

    @src.main.java.Testing
    def toSortedSet_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedSet((_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedSetBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedSetBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def toBag_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toBag
        }

    @src.main.java.Testing
    def toSortedBag_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedBag
        }

    @src.main.java.Testing
    def toSortedBag_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedBag((_: Int, _: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedBagBy_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedBagBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def toMap_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toMap((_: Int) => 0, (_: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedMap_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedMap((_: Int) => 0, (_: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedMap_withComparator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedMap[Int, Int]((_: Int, _: Int) => 0, (_: Int) => 0, (_: Int) => 0)
        }

    @src.main.java.Testing
    def toSortedMap_withFunction_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toSortedMapBy[Integer, Int, Int]((_: Int) => Integer.valueOf(0), (_: Int) => 0, (_: Int) => 0)
        }

    @src.main.java.Testing
    def toArray_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toArray
        }

    @src.main.java.Testing
    def toArrayWithTarget_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toArray(new Array[java.lang.Integer](10))
        }
}
