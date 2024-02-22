/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.matchers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

@SuppressWarnings("unchecked")
public class AnyXMatchersAcceptNullsTest extends TestBase {

    private IMethods mock;

    @Before
    public void setUp() {
        mock = Mockito.mock(IMethods.class);
    }

    @Test
    public void shouldAcceptNullsInAnyMatcher() {
        when(mock.oneArg((Object) any())).thenReturn("matched");
        assertEquals(null, mock.forObject(null));
    }

    @Test
    public void shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers() {
        when(mock.forInteger(anyInt())).thenReturn("0");
        when(mock.forCharacter(anyChar())).thenReturn("1");
        when(mock.forShort(anyShort())).thenReturn("2");
        when(mock.forByte(anyByte())).thenReturn("3");
        when(mock.forBoolean(anyBoolean())).thenReturn("4");
        when(mock.forLong(anyLong())).thenReturn("5");
        when(mock.forFloat(anyFloat())).thenReturn("6");
        when(mock.forDouble(anyDouble())).thenReturn("7");
        assertEquals(null, mock.forInteger(null));
        assertEquals(null, mock.forCharacter(null));
        assertEquals(null, mock.forShort(null));
        assertEquals(null, mock.forByte(null));
        assertEquals(null, mock.forBoolean(null));
        assertEquals(null, mock.forLong(null));
        assertEquals(null, mock.forFloat(null));
        assertEquals(null, mock.forDouble(null));
    }
}
