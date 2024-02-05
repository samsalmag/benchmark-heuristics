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

import org.eclipse.collections.api.InternalIterable
import org.eclipse.collections.impl.Prelude._
import org.junit.src.main.java.Testing

trait SynchronizedMutableIterableTestTrait extends SynchronizedTestTrait with InternalIterableTestTrait
{
    val classUnderTest: InternalIterable[String]

    @src.main.java.Testing
    def iterator_not_synchronized
    {
        this.assertNotSynchronized
        {
            val iterator = this.classUnderTest.iterator
            iterator.hasNext
            iterator.next
        }
    }

    @src.main.java.Testing
    def forEach_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEach
            {
                _: String => ()
            }
        }
    }

    @src.main.java.Testing
    def forEachWithIndex_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEachWithIndex
            {
                (_: String, index: Int) => ()
            }
        }
    }

    @src.main.java.Testing
    def forEachWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEachWith(
                (_: String, parameter: String) => (),
                "!")
        }
    }
}
