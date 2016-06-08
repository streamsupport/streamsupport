/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.Iterators;
import java8.util.Maps2;
import java8.util.stream.IntStreams;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @bug 8048330
 * @summary Test convenience static factory methods on Map.
 * @run testng MapFactories
 */

public class MapFactories {

    static final int MAX_ENTRIES = 20; // should be larger than the largest fixed-arg overload
    static String valueFor(int i) {
        // the String literal below should be of length MAX_ENTRIES
        return "abcdefghijklmnopqrst".substring(i, i+1);
    }

    // for "expected" values
    Map<Integer,String> genMap(int n) {
        Map<Integer,String> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(i, valueFor(i));
        }
        return result;
    }

    // for varargs Map.Entry methods
    @SuppressWarnings("unchecked")
    Map.Entry<Integer,String>[] genEntries(int n) {
        return IntStreams.range(0, n)
            .mapToObj(i -> Maps2.entry(i, valueFor(i)))
            .toArray(Map.Entry[]::new);
    }

    // returns array of [actual, expected]
    static Object[] a(Map<Integer,String> act, Map<Integer,String> exp) {
        return new Object[] { act, exp };
    }

    @DataProvider(name="empty")
    public Iterator<Object[]> empty() {
        return Collections.singletonList(
            a(Maps2.of(), genMap(0))
        ).iterator();
    }

    @DataProvider(name="nonempty")
    public Iterator<Object[]> nonempty() {
        return Arrays.asList(
            a(Maps2.of(0, "a"), genMap(1)),
            a(Maps2.of(0, "a", 1, "b"), genMap(2)),
            a(Maps2.of(0, "a", 1, "b", 2, "c"), genMap(3)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d"), genMap(4)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e"), genMap(5)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f"), genMap(6)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6, "g"), genMap(7)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6, "g", 7, "h"), genMap(8)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6, "g", 7, "h", 8, "i"), genMap(9)),
            a(Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6, "g", 7, "h", 8, "i", 9, "j"), genMap(10)),
            a(Maps2.ofEntries(genEntries(MAX_ENTRIES)), genMap(MAX_ENTRIES))
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
    public void cannotPutNew(Map<Integer,String> act, Map<Integer,String> exp) {
        act.put(-1, "xyzzy");
    }

    @Test(dataProvider="nonempty", expectedExceptions=UnsupportedOperationException.class)
    public void cannotPutOld(Map<Integer,String> act, Map<Integer,String> exp) {
        act.put(0, "a");
    }

    @Test(dataProvider="nonempty", expectedExceptions=UnsupportedOperationException.class)
    public void cannotRemove(Map<Integer,String> act, Map<Integer,String> exp) {
        act.remove(act.keySet().iterator().next());
    }

    @Test(dataProvider="all")
    public void contentsMatch(Map<Integer,String> act, Map<Integer,String> exp) {
        assertEquals(act, exp);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed2() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 0, "b");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed3() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 0, "c");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed4() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 0, "d");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed5() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 0, "e");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed6() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            0, "f");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed7() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 0, "g");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed8() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 0, "h");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed9() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", 0, "i");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowed10() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", 8, "i", 0, "j");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupKeysDisallowedN() {
        Map.Entry<Integer,String>[] entries = genEntries(MAX_ENTRIES);
        entries[MAX_ENTRIES-1] = Maps2.entry(0, "xxx");
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.ofEntries(entries);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed1() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(null, "a");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed1() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed2() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", null, "b");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed2() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed3() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", null, "c");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed3() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed4() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", null, "d");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed4() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed5() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", null, "e");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed5() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed6() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            null, "f");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed6() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed7() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", null, "g");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed7() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed8() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", null, "h");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed8() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed9() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", null, "i");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed9() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", 8, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowed10() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", 8, "i", null, "j");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowed10() {
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.of(0, "a", 1, "b", 2, "c", 3, "d", 4, "e",
                                            5, "f", 6, "g", 7, "h", 8, "i", 9, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expectedExceptions=NullPointerException.class)
    public void nullKeyDisallowedN() {
        Map.Entry<Integer,String>[] entries = genEntries(MAX_ENTRIES);
        entries[0] = new AbstractMap.SimpleImmutableEntry(null, "a");
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.ofEntries(entries);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expectedExceptions=NullPointerException.class)
    public void nullValueDisallowedN() {
        Map.Entry<Integer,String>[] entries = genEntries(MAX_ENTRIES);
        entries[0] = new AbstractMap.SimpleImmutableEntry(0, null);
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.ofEntries(entries);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullEntryDisallowedN() {
        Map.Entry<Integer,String>[] entries = genEntries(MAX_ENTRIES);
        entries[5] = null;
        @SuppressWarnings("unused")
        Map<Integer, String> map = Maps2.ofEntries(entries);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullArrayDisallowed() {
        Maps2.ofEntries((Map.Entry<Object, Object>[]) null);
    }

    @Test(dataProvider="all")
    public void serialEquality(Map<Integer, String> act, Map<Integer, String> exp) {
        // assume that act.equals(exp) tested elsewhere
        Map<Integer, String> copy = serialClone(act);
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

    // Map.entry() tests

    @Test(expectedExceptions=NullPointerException.class)
    public void entryWithNullKeyDisallowed() {
        @SuppressWarnings("unused")
        Map.Entry<Integer,String> e = Maps2.entry(null, "x");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void entryWithNullValueDisallowed() {
        @SuppressWarnings("unused")
        Map.Entry<Integer,String> e = Maps2.entry(0, null);
    }

    @Test
    public void entryBasicTests() {
        Map.Entry<String,String> kvh1 = Maps2.entry("xyzzy", "plugh");
        Map.Entry<String,String> kvh2 = Maps2.entry("foobar", "blurfl");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map.Entry<String,String> sie = new AbstractMap.SimpleImmutableEntry("xyzzy", "plugh");

        assertTrue(kvh1.equals(sie));
        assertTrue(sie.equals(kvh1));
        assertFalse(kvh2.equals(sie));
        assertFalse(sie.equals(kvh2));
        assertEquals(sie.hashCode(), kvh1.hashCode());
        assertEquals(sie.toString(), kvh1.toString());
    }

    // compile-time test of wildcards
    @Test
    public void entryWildcardTests() {
        Map.Entry<Integer,Double> e1 = Maps2.entry(1, 2.0);
        Map.Entry<Float,Long> e2 = Maps2.entry(3.0f, 4L);
        @SuppressWarnings("unchecked")
        Map<Number,Number> map = Maps2.ofEntries(e1, e2);
        assertEquals(map.size(), 2);
    }
}
