/*
 * Copyright (c) 2021 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.stack.mutable

import org.eclipse.collections.impl.SynchronizedRichIterableTestTrait
import org.junit.src.main.java.Testing

class SynchronizedStackScalaTest extends SynchronizedRichIterableTestTrait
{
    val classUnderTest = ArrayStack.newStackFromTopToBottom[String]("1", "2", "3").asSynchronized

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

    @src.main.java.Testing
    def push
    {
        this.assertSynchronized
        {
            this.classUnderTest.push("4")
        }
    }

    @src.main.java.Testing
    def pop
    {
        this.assertSynchronized
        {
            this.classUnderTest.pop
        }
    }

    @src.main.java.Testing
    def pop_int
    {
        this.assertSynchronized
        {
            this.classUnderTest.pop(2)
        }
    }

    @src.main.java.Testing
    def peek
    {
        this.assertSynchronized
        {
            this.classUnderTest.peek()
        }
    }

    @src.main.java.Testing
    def peek_int
    {
        this.assertSynchronized
        {
            this.classUnderTest.peek(2)
        }
    }

    @src.main.java.Testing
    def clear
    {
        this.assertSynchronized
        {
            this.classUnderTest.clear
        }
    }

    @src.main.java.Testing
    def to_immutable
    {
        this.assertSynchronized
        {
            this.classUnderTest.toImmutable
        }
    }
}
