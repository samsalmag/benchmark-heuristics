/*
 * Copyright (c) 2022 Goldman Sachs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.set.mutable

import org.eclipse.collections.api.set.MutableSet
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.list.mutable.FastList
import org.junit.src.main.java.Testing

class MultiReaderUnifiedSetScalaTest extends MultiReaderUnifiedSetTestTrait
{
    val classUnderTest = MultiReaderUnifiedSet.newSetWith(1, 2, 3)

    @src.main.java.Testing
    override def equals_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.equals(MultiReaderUnifiedSet.newSet())
        }

    @src.main.java.Testing
    def newSet_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderUnifiedSet.newSet
        }

    @src.main.java.Testing
    def newBagCapacity_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderUnifiedSet.newSet(5)
        }

    @src.main.java.Testing
    def newSetIterable_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderUnifiedSet.newSet(new FastList[Int])
        }

    @src.main.java.Testing
    def newSetWith_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = false)
        {
            MultiReaderUnifiedSet.newSetWith(1, 2)
        }

    @src.main.java.Testing
    def union_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.union(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def unionInto_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.unionInto(new UnifiedSet[Int], new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def intersect_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.intersect(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def intersectInto_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.intersectInto(new UnifiedSet[Int], new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def difference_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.difference(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def differenceInto_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.differenceInto(new UnifiedSet[Int], new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def symmetricDifference_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.symmetricDifference(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def symmetricDifferenceInto_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.symmetricDifferenceInto(new UnifiedSet[Int], new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def isSubsetOf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.isSubsetOf(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def isProperSubsetOf_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.isProperSubsetOf(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def powerSet_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.powerSet
        }

    @src.main.java.Testing
    def cartesianProduct_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.cartesianProduct(new UnifiedSet[Int])
        }

    @src.main.java.Testing
    def clone_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.clone
        }

    @src.main.java.Testing
    def iteratorWithReadLock_safe(): Unit =
        this.assert(readersBlocked = false, writersBlocked = true)
        {
            this.classUnderTest.withReadLockAndDelegate((each: MutableSet[Int]) =>
            {
                each.iterator
                ()
            })
        }

    @src.main.java.Testing
    def iteratorWithWriteLock_safe(): Unit =
        this.assert(readersBlocked = true, writersBlocked = true)
        {
            this.classUnderTest.withWriteLockAndDelegate((each: MutableSet[Int]) =>
            {
                each.iterator
                ()
            })
        }
}
