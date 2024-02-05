/*
 * Copyright (c) 2021 Goldman Sachs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.list.mutable

import org.eclipse.collections.api.list.MutableList
import org.junit.src.main.java.Testing

class MultiReaderFastListScalaTest extends MultiReaderFastListTestTrait
{
    override val classUnderTest = MultiReaderFastList.newListWith(1, 2, 3)

    @src.main.java.Testing
    def listIterator_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            try
            {
                this.classUnderTest.listIterator
            }
            catch
            {
                case e: Exception => ()
            }
        }

    @src.main.java.Testing
    def listIteratorIndex_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            try
            {
                this.classUnderTest.listIterator(1)
            }
            catch
            {
                case e: Exception => ()
            }
        }

    @src.main.java.Testing
    def iteratorWithReadLock_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.withReadLockAndDelegate((each: MutableList[Int]) =>
            {
                each.iterator
                ()
            })
        }

    @src.main.java.Testing
    def iteratorWithWriteLock_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.withWriteLockAndDelegate((each: MutableList[Int]) =>
            {
                each.iterator
                ()
            })
        }

    @src.main.java.Testing
    def newList_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderFastList.newList
        }

    @src.main.java.Testing
    def newListCapacity_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderFastList.newList(5)
        }

    @src.main.java.Testing
    def newListIterable_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderFastList.newList(new FastList[Int])
        }

    @src.main.java.Testing
    def newListWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderFastList.newListWith(1, 2)
        }

    @src.main.java.Testing
    def clone_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.clone
        }

    @src.main.java.Testing
    def addWithIndex_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.add(1, 4)
        }

    @src.main.java.Testing
    def addAllWithIndex_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.addAll(1, FastList.newListWith(3, 4, 5))
        }

    @src.main.java.Testing
    def removeWithIndex_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.remove(1)
        }

    @src.main.java.Testing
    def set_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.set(1, 4)
        }

    @src.main.java.Testing
    def reverseThis_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.reverseThis
        }

    @src.main.java.Testing
    def sort_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sort(null)
        }

    @src.main.java.Testing
    def sortThis_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThis
        }

    @src.main.java.Testing
    def sortThis_withComparator_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThis(null)
        }

    @src.main.java.Testing
    def sortThisBy_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisBy[String]((_: Int) => "")
        }

    @src.main.java.Testing
    def sortThisByInt_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByInt((_: Int) => 0)
        }

    @src.main.java.Testing
    def sortThisByChar_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByChar((_: Int) => 0)
        }

    @src.main.java.Testing
    def sortThisByByte_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByByte((_: Int) => 0)
        }

    @src.main.java.Testing
    def sortThisByBoolean_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByBoolean((_: Int) => true)
        }

    @src.main.java.Testing
    def sortThisByShort_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByShort((_: Int) => 0)
        }

    @src.main.java.Testing
    def sortThisByFloat_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByFloat((_: Int) => 0.0f)
        }

    @src.main.java.Testing
    def sortThisByLong_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByLong((_: Int) => 0L)
        }

    @src.main.java.Testing
    def sortThisByDouble_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.sortThisByDouble((_: Int) => 0.0d)
        }

    @src.main.java.Testing
    def distinct_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.distinct
        }

    @src.main.java.Testing
    def subList_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.subList(0, 1)
        }

    @src.main.java.Testing
    def get_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.get(1)
        }

    @src.main.java.Testing
    def indexOf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.get(1)
        }

    @src.main.java.Testing
    def lastIndexOf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.get(1)
        }

    @src.main.java.Testing
    def reverseForEach_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.reverseForEach((_: Int) => ())
        }

    @src.main.java.Testing
    def asReversed_safe(): Unit =
    {
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.asReversed()
        }

        val reverseIterable = this.classUnderTest.asReversed()
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            reverseIterable.each((_: Int) => ())
        }
    }

    @src.main.java.Testing
    def forEachWithIndex_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.forEachWithIndex(0, 2, (_: Int, _: Int) => ())
        }

    @src.main.java.Testing
    def toReversed_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toReversed
        }

    @src.main.java.Testing
    def toStack_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.toStack
        }

    @src.main.java.Testing
    def takeWhile_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.takeWhile((_: Int) => true)
        }

    @src.main.java.Testing
    def dropWhile_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.dropWhile((_: Int) => true)
        }

    @src.main.java.Testing
    def partitionWhile_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.partitionWhile((_: Int) => true)
        }
}
