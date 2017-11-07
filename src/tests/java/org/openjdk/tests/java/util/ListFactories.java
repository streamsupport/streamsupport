/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.tests.java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java8.util.Iterators;
import java8.util.Lists;
import java8.util.Lists2;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

/*
 * @test
 * @bug 8048330
 * @summary Test convenience static factory methods on List.
 * @run testng ListFactories
 */

public class ListFactories {

    static final int NUM_STRINGS = 20; // should be larger than the largest fixed-arg overload
    static final String[] stringArray;
    static {
        String[] sa = new String[NUM_STRINGS];
        for (int i = 0; i < NUM_STRINGS; i++) {
            sa[i] = String.valueOf((char)('a' + i));
        }
        stringArray = sa;
    }

    // returns array of [actual, expected]
    static Object[] a(List<String> act, List<String> exp) {
        return new Object[] { act, exp };
    }

    @DataProvider(name="empty")
    public Iterator<Object[]> empty() {
        return Collections.singletonList(
            a(Lists.of(), Collections.emptyList())
        ).iterator();
    }

    @DataProvider(name="nonempty")
    public Iterator<Object[]> nonempty() {
        return asList(
            a(Lists2.of("a"),
               asList("a")),
            a(Lists2.of("a", "b"),
               asList("a", "b")),
            a(Lists2.of("a", "b", "c"),
               asList("a", "b", "c")),
            a(Lists2.of("a", "b", "c", "d"),
               asList("a", "b", "c", "d")),
            a(Lists2.of("a", "b", "c", "d", "e"),
               asList("a", "b", "c", "d", "e")),
            a(Lists2.of("a", "b", "c", "d", "e", "f"),
               asList("a", "b", "c", "d", "e", "f")),
            a(Lists2.of("a", "b", "c", "d", "e", "f", "g"),
               asList("a", "b", "c", "d", "e", "f", "g")),
            a(Lists2.of("a", "b", "c", "d", "e", "f", "g", "h"),
               asList("a", "b", "c", "d", "e", "f", "g", "h")),
            a(Lists2.of("a", "b", "c", "d", "e", "f", "g", "h", "i"),
               asList("a", "b", "c", "d", "e", "f", "g", "h", "i")),
            a(Lists2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
               asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")),
            a(Lists2.of(stringArray),
               asList(stringArray)),
            a(Lists.of("a"),
               asList("a")),
            a(Lists.of("a", "b"),
               asList("a", "b")),
            a(Lists.of("a", "b", "c"),
               asList("a", "b", "c")),
            a(Lists.of("a", "b", "c", "d"),
               asList("a", "b", "c", "d")),
            a(Lists.of("a", "b", "c", "d", "e"),
               asList("a", "b", "c", "d", "e")),
            a(Lists.of("a", "b", "c", "d", "e", "f"),
               asList("a", "b", "c", "d", "e", "f")),
            a(Lists.of("a", "b", "c", "d", "e", "f", "g"),
               asList("a", "b", "c", "d", "e", "f", "g")),
            a(Lists.of("a", "b", "c", "d", "e", "f", "g", "h"),
               asList("a", "b", "c", "d", "e", "f", "g", "h")),
            a(Lists.of("a", "b", "c", "d", "e", "f", "g", "h", "i"),
               asList("a", "b", "c", "d", "e", "f", "g", "h", "i")),
            a(Lists.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
               asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")),
            a(Lists.of(stringArray),
               asList(stringArray))
        ).iterator();
    }

    @DataProvider(name="all")
    public Iterator<Object[]> all() {
        List<Object[]> all = new ArrayList<>();
        Iterators.forEachRemaining(empty(), all::add);
        Iterators.forEachRemaining(nonempty(), all::add);
        return all.iterator();
    }

    @Test(dataProvider="all", expectedExceptions=UnsupportedOperationException.class)
    public void cannotAddLast(List<String> act, List<String> exp) {
        act.add("x");
    }

    @Test(dataProvider="all", expectedExceptions=UnsupportedOperationException.class)
    public void cannotAddFirst(List<String> act, List<String> exp) {
        act.add(0, "x");
    }

    @Test(dataProvider="nonempty", expectedExceptions=UnsupportedOperationException.class)
    public void cannotRemove(List<String> act, List<String> exp) {
        act.remove(0);
    }

    @Test(dataProvider="nonempty", expectedExceptions=UnsupportedOperationException.class)
    public void cannotSet(List<String> act, List<String> exp) {
        act.set(0, "x");
    }

    @Test(dataProvider="all")
    public void contentsMatch(List<String> act, List<String> exp) {
        assertEquals(act, exp);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed1_2() {
        Lists2.of((Object) null); // force one-arg overload
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed1() {
        Lists.of((Object) null); // force one-arg overload
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2a_2() {
        Lists2.of("a", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2a() {
        Lists.of("a", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2b_2() {
        Lists2.of(null, "b");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2b() {
        Lists.of(null, "b");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed3_2() {
        Lists2.of("a", "b", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed3() {
        Lists.of("a", "b", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed4_2() {
        Lists2.of("a", "b", "c", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed4() {
        Lists.of("a", "b", "c", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed5_2() {
        Lists2.of("a", "b", "c", "d", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed5() {
        Lists.of("a", "b", "c", "d", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed6_2() {
        Lists2.of("a", "b", "c", "d", "e", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed6() {
        Lists.of("a", "b", "c", "d", "e", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed7_2() {
        Lists2.of("a", "b", "c", "d", "e", "f", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed7() {
        Lists.of("a", "b", "c", "d", "e", "f", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed8_2() {
        Lists2.of("a", "b", "c", "d", "e", "f", "g", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed8() {
        Lists.of("a", "b", "c", "d", "e", "f", "g", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed9_2() {
        Lists2.of("a", "b", "c", "d", "e", "f", "g", "h", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed9() {
        Lists.of("a", "b", "c", "d", "e", "f", "g", "h", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed10_2() {
        Lists2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed10() {
        Lists.of("a", "b", "c", "d", "e", "f", "g", "h", "i", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowedN2() {
        String[] array = stringArray.clone();
        array[0] = null;
        Lists2.of(array);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowedN() {
        String[] array = stringArray.clone();
        array[0] = null;
        Lists.of(array);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullArrayDisallowed2() {
        Lists2.of((Object[])null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullArrayDisallowed() {
        Lists.of((Object[])null);
    }

    @Test
    public void ensureArrayCannotModifyList2() {
        String[] array = stringArray.clone();
        List<String> list = Lists2.of(array);
        array[0] = "xyzzy";
        assertEquals(list, Arrays.asList(stringArray));
    }

    @Test
    public void ensureArrayCannotModifyList() {
        String[] array = stringArray.clone();
        List<String> list = Lists.of(array);
        array[0] = "xyzzy";
        assertEquals(list, Arrays.asList(stringArray));
    }

    @Test(dataProvider="all")
    public void serialEquality(List<String> act, List<String> exp) {
        // assume that act.equals(exp) tested elsewhere
        List<String> copy = serialClone(act);
        assertEquals(act, copy);
        assertEquals(copy, exp);
    }

    @SuppressWarnings("unchecked")
    static <T> T serialClone(T obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    List<Integer> genList() {
        return new ArrayList<>(Arrays.asList(1, 2, 3));
    }

    @Test
    public void copyOfResultsEqual2() {
        List<Integer> orig = genList();
        List<Integer> copy = Lists2.copyOf(orig);

        assertEquals(orig, copy);
        assertEquals(copy, orig);
    }

    @Test
    public void copyOfResultsEqual() {
        List<Integer> orig = genList();
        List<Integer> copy = Lists.copyOf(orig);

        assertEquals(orig, copy);
        assertEquals(copy, orig);
    }

    @Test
    public void copyOfModifiedUnequal2() {
        List<Integer> orig = genList();
        List<Integer> copy = Lists2.copyOf(orig);
        orig.add(4);

        assertNotEquals(orig, copy);
        assertNotEquals(copy, orig);
    }

    @Test
    public void copyOfModifiedUnequal() {
        List<Integer> orig = genList();
        List<Integer> copy = Lists.copyOf(orig);
        orig.add(4);

        assertNotEquals(orig, copy);
        assertNotEquals(copy, orig);
    }

    @Test
    public void copyOfIdentity2() {
        List<Integer> orig = genList();
        List<Integer> copy1 = Lists2.copyOf(orig);
        List<Integer> copy2 = Lists2.copyOf(copy1);

        assertNotSame(orig, copy1);
        assertSame(copy1, copy2);
    }

    @Test
    public void copyOfIdentity() {
        List<Integer> orig = genList();
        List<Integer> copy1 = Lists.copyOf(orig);
        List<Integer> copy2 = Lists.copyOf(copy1);

        assertNotSame(orig, copy1);
        assertSame(copy1, copy2);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullCollection2() {
        @SuppressWarnings("unused")
        List<Integer> list = Lists2.copyOf(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullCollection() {
        @SuppressWarnings("unused")
        List<Integer> list = Lists.copyOf(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullElements2() {
        @SuppressWarnings("unused")
        List<Integer> list = Lists2.copyOf(Arrays.asList(1, null, 3));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullElements() {
        @SuppressWarnings("unused")
        List<Integer> list = Lists.copyOf(Arrays.asList(1, null, 3));
    }
}
