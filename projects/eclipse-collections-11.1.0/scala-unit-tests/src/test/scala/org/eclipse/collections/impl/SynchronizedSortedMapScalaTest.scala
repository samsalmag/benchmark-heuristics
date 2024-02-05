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

import org.eclipse.collections.api.map.sorted.MutableSortedMap
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.block.factory.Functions
import org.eclipse.collections.impl.list.mutable.FastList
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap
import org.eclipse.collections.impl.tuple.Tuples
import org.junit.src.main.java.Testing

class SynchronizedSortedMapScalaTest extends SynchronizedMapIterableTestTrait
{
    val classUnderTest: MutableSortedMap[String, String] = TreeSortedMap.newMapWith("A", "1", "B", "2", "C", "3").asSynchronized()

    @src.main.java.Testing
    def newEmpty_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.newEmpty
        }
    }

    @src.main.java.Testing
    def removeKey_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.remove("1")
        }
    }

    @src.main.java.Testing
    def getIfAbsentPut_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.getIfAbsentPut("Nine", () => "foo")
        }
    }

    @src.main.java.Testing
    def getIfAbsentPutWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.getIfAbsentPutWith("Nine", Functions.getPassThru[String], "foo")
        }
    }

    @src.main.java.Testing
    def asUnmodifiable_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.asUnmodifiable
        }
    }

    @src.main.java.Testing
    def toImmutable_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toImmutable
        }
    }

    @src.main.java.Testing
    def collectKeysAndValues_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectKeysAndValues(FastList.newListWith[java.lang.Integer](4, 5, 6),
            {
                _: java.lang.Integer => ""
            },
            {
                _: java.lang.Integer => ""
            })
        }
    }

    @src.main.java.Testing
    def comparator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.comparator
        }
    }

    @src.main.java.Testing
    def values_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.values
        }
    }

    @src.main.java.Testing
    def keySet_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.keySet
        }
    }

    @src.main.java.Testing
    def entrySet_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.entrySet
        }
    }

    @src.main.java.Testing
    def headMap_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.headMap("B")
        }
    }

    @src.main.java.Testing
    def tailMap_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.tailMap("B")
        }
    }

    @src.main.java.Testing
    def subMap_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.subMap("A", "C")
        }
    }

    @src.main.java.Testing
    def firstKey_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.firstKey
        }
    }

    @src.main.java.Testing
    def lastKey_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.lastKey
        }
    }

    @src.main.java.Testing
    def with_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.`with`(Tuples.pair("D", "4"))
        }
    }
}
