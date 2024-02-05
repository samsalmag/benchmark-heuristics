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

import java.lang.StringBuilder

import org.eclipse.collections.api.RichIterable
import org.eclipse.collections.api.collection.MutableCollection
import org.eclipse.collections.api.list.MutableList
import org.eclipse.collections.api.multimap.MutableMultimap
import org.eclipse.collections.api.tuple.Pair
import org.eclipse.collections.impl.Prelude._
import org.eclipse.collections.impl.list.mutable.FastList
import org.eclipse.collections.impl.multimap.list.FastListMultimap
import org.junit.src.main.java.Testing

trait SynchronizedRichIterableTestTrait extends SynchronizedMutableIterableTestTrait /* with RichIterableTestTrait */
{
    val classUnderTest: RichIterable[String]

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
    def notEmpty_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.notEmpty
        }
    }

    @src.main.java.Testing
    def getFirst_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.getFirst
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
    def containsAllIterable_synchronized
    {
        this.assertSynchronized
        {
            val iterable: java.lang.Iterable[_] = FastList.newList[AnyRef]
            this.classUnderTest.containsAllIterable(iterable)
        }
    }

    @src.main.java.Testing
    def containsAllArguments_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.containsAllArguments("", "", "")
        }
    }

    @src.main.java.Testing
    def select_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.select({
                _: String => false
            })
        }
    }

    @src.main.java.Testing
    def select_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.select({
                _: String => false
            }, FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def reject_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.reject({
                _: String => true
            })
        }
    }

    @src.main.java.Testing
    def reject_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.reject({
                _: String => true
            }, null)
        }
    }

    @src.main.java.Testing
    def partition_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.partition({
                _: String => true
            })
        }
    }

    @src.main.java.Testing
    def partitionWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.partitionWith({
                (_: String, _: AnyRef) => true
            }, null)
        }
    }

    @src.main.java.Testing
    def collect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collect({
                _: String => null
            })
        }
    }

    @src.main.java.Testing
    def collect_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collect[String, MutableCollection[String]](
            {
                _: String => ""
            },
            FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def collectIf_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectIf({
                _: String => false
            },
            {
                _: String => ""
            })
        }
    }

    @src.main.java.Testing
    def collectIf_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.collectIf[String, MutableCollection[String]](
            {
                _: String => false
            },
            {
                _: String => ""
            },
            FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def flatCollect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.flatCollect({
                _: String => FastList.newList[String]
            })
        }
    }

    @src.main.java.Testing
    def flatCollect_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.flatCollect[String, MutableList[String]]({
                _: String => FastList.newList[String]
            }, FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def detect_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.detect({
                _: String => false
            })
        }
    }

    @src.main.java.Testing
    def detectIfNone_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.detectIfNone({
                _: String => false
            },
            {
                () => ""
            })
        }
    }

    @src.main.java.Testing
    def count_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.count({
                _: String => false
            })
        }
    }

    @src.main.java.Testing
    def anySatisfy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.anySatisfy({
                _: String => true
            })
        }
    }

    @src.main.java.Testing
    def anySatisfyWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.anySatisfyWith({
                (_: String, _: AnyRef) => true
            }, null)
        }
    }

    @src.main.java.Testing
    def allSatisfy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.allSatisfy({
                _: String => false
            })
        }
    }

    @src.main.java.Testing
    def allSatisfyWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.allSatisfyWith({
                (_: String, _: AnyRef) => false
            }, null)
        }
    }

    @src.main.java.Testing
    def noneSatisfy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.noneSatisfy({
                _: String => false
            })
        }
    }

    @src.main.java.Testing
    def noneSatisfyWith_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.noneSatisfyWith({
                (_: String, _: AnyRef) => false
            }, null)
        }
    }

    @src.main.java.Testing
    def injectInto_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.injectInto[String]("", (_: String, _: String) => "")
        }
    }

    @src.main.java.Testing
    def toList_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toList
        }
    }

    @src.main.java.Testing
    def toSortedList_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toSortedList
        }
    }

    @src.main.java.Testing
    def toSortedList_with_comparator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toSortedList(null)
        }
    }

    @src.main.java.Testing
    def toSortedListBy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toSortedListBy[String]((string: String) => string)
        }
    }

    @src.main.java.Testing
    def toSet_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toSet
        }
    }

    @src.main.java.Testing
    def toMap_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toMap({
                _: String => ""
            },
            {
                _: String => ""
            })
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
            val array: Array[String] = new Array[String](this.classUnderTest.size())
            this.classUnderTest.toArray(array)
        }
    }

    @src.main.java.Testing
    def max_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.max({
                (_: String, _: String) => 0
            })
        }
    }

    @src.main.java.Testing
    def min_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.min({
                (_: String, _: String) => 0
            })
        }
    }

    @src.main.java.Testing
    def max_without_comparator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.max()
        }
    }

    @src.main.java.Testing
    def min_without_comparator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.min()
        }
    }

    @src.main.java.Testing
    def maxBy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.maxBy[String]((string: String) => string)
        }
    }

    @src.main.java.Testing
    def minBy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.minBy[String]((string: String) => string)
        }
    }

    @src.main.java.Testing
    def makeString_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.makeString
        }
    }

    @src.main.java.Testing
    def makeString_with_separator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.makeString(", ")
        }
    }

    @src.main.java.Testing
    def makeString_with_start_separator_end_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.makeString("[", ", ", "]")
        }
    }

    @src.main.java.Testing
    def appendString_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.appendString(new StringBuilder)
        }
    }

    @src.main.java.Testing
    def appendString_with_separator_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.appendString(new StringBuilder, ", ")
        }
    }

    @src.main.java.Testing
    def appendString_with_start_separator_end_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.appendString(new StringBuilder, "[", ", ", "]")
        }
    }

    @src.main.java.Testing
    def groupBy_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.groupBy({
                _: String => ""
            })
        }
    }

    @src.main.java.Testing
    def groupBy_with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.groupBy[String, MutableMultimap[String, String]](
            {
                _: String => ""
            },
            FastListMultimap.newMultimap[String, String])
        }
    }

    @src.main.java.Testing
    def toString_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.toString
        }
    }

    @src.main.java.Testing
    def zip_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.zip[String](FastList.newList[String])
        }
    }

    @src.main.java.Testing
    def zip__with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.zip[String, FastList[Pair[String, String]]](FastList.newList[String](),
                FastList.newList[Pair[String, String]]())
        }
    }

    @src.main.java.Testing
    def zipWithIndex_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.zipWithIndex()
        }
    }

    @src.main.java.Testing
    def zipWithIndex__with_target_synchronized
    {
        this.assertSynchronized
        {
            this.classUnderTest.zipWithIndex(FastList.newList[Pair[String, java.lang.Integer]]())
        }
    }
}
