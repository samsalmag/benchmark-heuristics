/*
 * Copyright (c) 2021 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl

import org.eclipse.collections.impl.list.mutable.FastList
import org.junit.src.main.java.Testing

trait SynchronizedCollectionTestTrait extends SynchronizedTestTrait with IterableTestTrait
{
    val classUnderTest: java.util.Collection[String]

    @src.main.java.Testing
    def size_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.size
        }
    }

    @src.main.java.Testing
    def isEmpty_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.isEmpty
        }
    }

    @src.main.java.Testing
    def contains_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.contains(null)
        }
    }

    @src.main.java.Testing
    def iterator_not_synchronized
    {
        this.assertNotSynchronized
        {
            this.classUnderTest.iterator
        }
    }

    @src.main.java.Testing
    def toArray_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toArray
        }
    }

    @src.main.java.Testing
    def toArray_with_target_synchronized
    {
        this.assertSynchronized
        {
            val array: Array[String] = null
            this.classUnderTest.toArray(array)
        }
    }

    @src.main.java.Testing
    def add_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.add("")
        }
    }

    @src.main.java.Testing
    def remove_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.remove("")
        }
    }

    @src.main.java.Testing
    def containsAll_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.containsAll(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def addAll_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.addAll(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def removeAll_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.removeAll(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def retainAll_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.retainAll(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def clear_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.clear()
        }
    }

    @src.main.java.Testing
    def equals_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.equals(null)
        }
    }

    @src.main.java.Testing
    def hashCode_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.hashCode
        }
    }
}
