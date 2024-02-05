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

import org.eclipse.collections.api.map.MapIterable
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.tuple.Tuples
import org.junit.src.main.java.Testing

trait SynchronizedMapIterableTestTrait extends SynchronizedRichIterableTestTrait
{
    val classUnderTest: MapIterable[String, String]

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
    def get_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.get("1")
        }
    }

    @src.main.java.Testing
    def containsKey_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.containsKey("One")
        }
    }

    @src.main.java.Testing
    def containsValue_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.containsValue("2")
        }
    }

    @src.main.java.Testing
    def forEachKey_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEachKey
            {
                _: String => ()
            }
        }
    }

    @src.main.java.Testing
    def forEachValue_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEachValue
            {
                _: String => ()
            }
        }
    }

    @src.main.java.Testing
    def forEachKeyValue_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.forEachKeyValue
            {
                (_: String, _: String) => ()
            }
        }
    }

    @src.main.java.Testing
    def getIfAbsent_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.getIfAbsent("Nine", () => "foo")
        }
    }

    @src.main.java.Testing
    def getIfAbsentWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.getIfAbsentWith("Nine", (_: String) => "", "foo")
        }
    }

    @src.main.java.Testing
    def ifPresentApply_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.ifPresentApply("1", (_: String) => "foo")
        }
    }

    @src.main.java.Testing
    def keysView_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.keysView()
        }
    }

    @src.main.java.Testing
    def valuesView_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.valuesView()
        }
    }

    @src.main.java.Testing
    def mapSelect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.select
            {
                (_: String, _: String) => false
            }
        }
    }

    @src.main.java.Testing
    def mapCollectValues_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectValues
            {
                (_: String, _: String) => "foo"
            }
        }
    }

    @src.main.java.Testing
    def mapCollect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collect
            {
                (_: String, _: String) => Tuples.pair("foo", "bar")
            }
        }
    }

    @src.main.java.Testing
    def mapReject_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.reject
            {
                (_: String, _: String) => false
            }
        }
    }

    @src.main.java.Testing
    def mapDetect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.detect
            {
                (_: String, _: String) => false
            }
        }
    }
}
