/*
 * Copyright (c) 2021 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.set

import org.eclipse.collections.api.set.SetIterable
import org.eclipse.collections.impl.SynchronizedRichIterableTestTrait
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet
import org.junit.src.main.java.Testing

trait SynchronizedSetIterableTestTrait extends SynchronizedRichIterableTestTrait
{
    val classUnderTest: SetIterable[String]

    @src.main.java.Testing
    def union_synchronized
    {
        this.assertSynchronized(this.classUnderTest.union(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def unionInto_synchronized
    {
        this.assertSynchronized(this.classUnderTest.unionInto(TreeSortedSet.newSet[String], TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def intersect_synchronized
    {
        this.assertSynchronized(this.classUnderTest.intersect(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def intersectInto_synchronized
    {
        this.assertSynchronized(this.classUnderTest.intersectInto(TreeSortedSet.newSet[String], TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def difference_synchronized
    {
        this.assertSynchronized(this.classUnderTest.difference(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def differenceInto_synchronized
    {
        this.assertSynchronized(this.classUnderTest.differenceInto(TreeSortedSet.newSet[String], TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def symmetricDifference_synchronized
    {
        this.assertSynchronized(this.classUnderTest.symmetricDifference(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def symmetricDifferenceInto_synchronized
    {
        this.assertSynchronized(this.classUnderTest.symmetricDifferenceInto(TreeSortedSet.newSet[String], TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def isSubsetOf_synchronized
    {
        this.assertSynchronized(this.classUnderTest.isSubsetOf(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def isProperSubsetOf_synchronized
    {
        this.assertSynchronized(this.classUnderTest.isProperSubsetOf(TreeSortedSet.newSet[String]))
    }

    @src.main.java.Testing
    def cartesianProduct_synchronized
    {
        this.assertSynchronized(this.classUnderTest.cartesianProduct(TreeSortedSet.newSet[String]))
    }
}
