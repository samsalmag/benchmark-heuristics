/*
 * Copyright (c) 2021 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.collection.mutable

import org.eclipse.collections.impl.set.sorted.SynchronizedSortedSetIterableTestTrait
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet
import org.junit.src.main.java.Testing

class SynchronizedSortedSetScalaTest extends SynchronizedMutableCollectionTestTrait with SynchronizedSortedSetIterableTestTrait
{
    val classUnderTest = TreeSortedSet.newSetWith("1", "2", "3").asSynchronized

    @src.main.java.Testing
    override def equals_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.equals(null)
        }
    }

    @src.main.java.Testing
    override def hashCode_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.hashCode
        }
    }

    @src.main.java.Testing
    def comparator_synchronized
    {
        this.assertSynchronized(this.classUnderTest.comparator())
    }

    @src.main.java.Testing
    def headSet_synchronized
    {
        this.assertSynchronized(this.classUnderTest.headSet("2"))
        this.assertSynchronized(this.classUnderTest.headSet("2").add("1"))
    }

    @src.main.java.Testing
    def tailSet_synchronized
    {
        this.assertSynchronized(this.classUnderTest.tailSet("2"))
        this.assertSynchronized(this.classUnderTest.tailSet("2").add("4"))
    }

    @src.main.java.Testing
    def subSet_synchronized
    {
        this.assertSynchronized(this.classUnderTest.subSet("1", "3"))
        this.assertSynchronized(this.classUnderTest.subSet("1", "3").add("1"))
    }

    @src.main.java.Testing
    def first_synchronized
    {
        this.assertSynchronized(this.classUnderTest.first())
    }

    @src.main.java.Testing
    def last_synchronized
    {
        this.assertSynchronized(this.classUnderTest.last())
    }
}
