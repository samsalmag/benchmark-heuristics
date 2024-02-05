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

import org.eclipse.collections.api.collection.MutableCollection
import org.eclipse.collections.api.list.MutableList
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.list.mutable.FastList
import org.eclipse.collections.impl.{SynchronizedCollectionTestTrait, SynchronizedRichIterableTestTrait}
import org.junit.src.main.java.Testing

trait SynchronizedMutableCollectionTestTrait
        extends SynchronizedRichIterableTestTrait
        with SynchronizedCollectionTestTrait
        with MutableCollectionTestTrait
{
    val classUnderTest: MutableCollection[String]

    @src.main.java.Testing
    def newEmpty_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.newEmpty
        }
    }

    /**
     * {@link SynchronizedRichIterableTestTrait} and {@link SynchronizedCollectionTestTrait} both define these methods
     * the same way.  They need to be overridden to point to one.  Which one to pick was an arbitrary choice.
     */
    override def size_synchronized = super[SynchronizedRichIterableTestTrait].size_synchronized

    override def isEmpty_synchronized = super[SynchronizedRichIterableTestTrait].isEmpty_synchronized

    override def contains_synchronized = super[SynchronizedRichIterableTestTrait].contains_synchronized

    override def iterator_not_synchronized = super[SynchronizedRichIterableTestTrait].iterator_not_synchronized

    override def toArray_synchronized = super[SynchronizedRichIterableTestTrait].toArray_synchronized

    override def toArray_with_target_synchronized = super[SynchronizedRichIterableTestTrait].toArray_with_target_synchronized

    @src.main.java.Testing
    def addAllIterable_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.addAllIterable(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def removeAllIterable_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.removeAllIterable(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def retainAllIterable_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.retainAllIterable(FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def selectWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.selectWith({
                (_: String, _: String) => false
            }, "")
        }
    }

    @src.main.java.Testing
    def selectWith_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.selectWith({
                (_: String, _: String) => false
            }, "", FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def rejectWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.rejectWith({
                (_: String, _: String) => true
            }, "")
        }
    }

    @src.main.java.Testing
    def rejectWith_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.rejectWith({
                (_: String, _: String) => true
            }, "", FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def selectAndRejectWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.selectAndRejectWith({
                (_: String, _: String) => true
            }, "")
        }
    }

    @src.main.java.Testing
    def removeIf_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.removeIf({
                (_: String) => false
            })
        }
    }

    @src.main.java.Testing
    def removeIfWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.removeIfWith({
                (_: String, _: String) => false
            }, "")
        }
    }

    @src.main.java.Testing
    def collectWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectWith({
                (_: String, _: String) => ""
            }, "")
        }
    }

    @src.main.java.Testing
    def collectWith_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectWith[String, String, MutableList[String]](
            {
                (_: String, _: String) => ""
            },
            "",
            FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def detectWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.detectWith({
                (_: String, _: String) => true
            }, "")
        }
    }

    @src.main.java.Testing
    def detectWithIfNone_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.detectWithIfNone({
                (_: String, _: String) => true
            }, "", null)
        }
    }

    @src.main.java.Testing
    def countWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.countWith({
                (_: String, _: String) => true
            }, "")
        }
    }

    @src.main.java.Testing
    def injectIntoWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.injectIntoWith[String, String]("", (_: String, _: String, _: String) => "", "")
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
    def asSynchronized_not_synchronized
    {
        this.assertNotSynchronized
        {
            this.classUnderTest.asSynchronized
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
    def with_synchronized
    {
        this.assertSynchronized(this.classUnderTest.`with`("4"))
    }

    @src.main.java.Testing
    def withAll_synchronized
    {
        this.assertSynchronized(this.classUnderTest.withAll(FastList.newListWith("4")))
    }

    @src.main.java.Testing
    def without_synchronized
    {
        this.assertSynchronized(this.classUnderTest.without("4"))
    }

    @src.main.java.Testing
    def withoutAll_synchronized
    {
        this.assertSynchronized(this.classUnderTest.withoutAll(FastList.newListWith("4")))
    }
}
