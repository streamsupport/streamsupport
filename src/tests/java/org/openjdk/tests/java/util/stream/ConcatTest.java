/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.tests.java.util.stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import java8.util.Spliterators;
import java8.util.Spliterator;
import java8.util.stream.Characteristics;
import java8.util.stream.DoubleStream;
import java8.util.stream.DoubleStreams;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreams;
import java8.util.stream.LongStream;
import java8.util.stream.LongStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import static java8.util.stream.LambdaTestHelpers.*;
import static org.testng.Assert.*;

@Test
public class ConcatTest {
    private static Object[][] cases;

    static {
        List<Integer> part1 = Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4);
        List<Integer> part2 = Arrays.asList(8, 8, 6, 6, 9, 7, 10, 9);
        List<Integer> p1p2 = Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 8, 8, 6, 6, 9, 7, 10, 9);
        List<Integer> p2p1 = Arrays.asList(8, 8, 6, 6, 9, 7, 10, 9, 5, 3, 4, 1, 2, 6, 2, 4);
        List<Integer> empty = new LinkedList<>(); // To be ordered
        assertTrue(empty.isEmpty());
        LinkedHashSet<Integer> distinctP1 = new LinkedHashSet<>(part1);
        LinkedHashSet<Integer> distinctP2 = new LinkedHashSet<>(part2);
        TreeSet<Integer> sortedP1 = new TreeSet<>(part1);
        TreeSet<Integer> sortedP2 = new TreeSet<>(part2);

        cases = new Object[][] {
            { "regular", part1, part2, p1p2 },
            { "reverse regular", part2, part1, p2p1 },
            { "front distinct", distinctP1, part2, Arrays.asList(5, 3, 4, 1, 2, 6, 8, 8, 6, 6, 9, 7, 10, 9) },
            { "back distinct", part1, distinctP2, Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 8, 6, 9, 7, 10) },
            { "both distinct", distinctP1, distinctP2, Arrays.asList(5, 3, 4, 1, 2, 6, 8, 6, 9, 7, 10) },
            { "front sorted", sortedP1, part2, Arrays.asList(1, 2, 3, 4, 5, 6, 8, 8, 6, 6, 9, 7, 10, 9) },
            { "back sorted", part1, sortedP2, Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 6, 7, 8, 9, 10) },
            { "both sorted", sortedP1, sortedP2, Arrays.asList(1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10) },
            { "reverse both sorted", sortedP2, sortedP1, Arrays.asList(6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6) },
            { "empty something", empty, part1, part1 },
            { "something empty", part1, empty, part1 },
            { "empty empty", empty, empty, empty }
        };
    }

    @DataProvider(name = "cases")
    private static Object[][] getCases() {
        return cases;
    }

    @Factory(dataProvider = "cases")
    public static Object[] createTests(String scenario, Collection<Integer> c1, Collection<Integer> c2, Collection<Integer> expected) {
        return new Object[] {
            new ConcatTest(scenario, c1, c2, expected)
        };
    }

    protected final String scenario;
    protected final Collection<Integer> c1;
    protected final Collection<Integer> c2;
    protected final Collection<Integer> expected;

    public ConcatTest(String scenario, Collection<Integer> c1, Collection<Integer> c2, Collection<Integer> expected) {
        this.scenario = scenario;
        this.c1 = c1;
        this.c2 = c2;
        this.expected = expected;


        // verify prerequisite
        Stream<Integer> s1s = StreamSupport.stream(c1, Characteristics.get(c1));
        Stream<Integer> s2s = StreamSupport.stream(c2, Characteristics.get(c2));
        Stream<Integer> s1p = StreamSupport.stream(c1, Characteristics.get(c1), true);
        Stream<Integer> s2p = StreamSupport.stream(c2, Characteristics.get(c2), true);
        assertTrue(s1p.isParallel());
        assertTrue(s2p.isParallel());
        assertFalse(s1s.isParallel());
        assertFalse(s2s.isParallel());

        assertTrue(s1s.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s1p.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s2s.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s2p.spliterator().hasCharacteristics(Spliterator.ORDERED));
    }

    private <T> void assertConcatContent(Spliterator<T> sp, boolean ordered, Spliterator<T> expected) {
        // concat stream cannot guarantee uniqueness
        assertFalse(sp.hasCharacteristics(Spliterator.DISTINCT), scenario);
        // concat stream cannot guarantee sorted
        assertFalse(sp.hasCharacteristics(Spliterator.SORTED), scenario);
        // concat stream is ordered if both are ordered
        assertEquals(sp.hasCharacteristics(Spliterator.ORDERED), ordered, scenario);

        // Verify elements
        if (ordered) {
            assertEquals(toBoxedList(sp),
                         toBoxedList(expected),
                         scenario);
        } else {
            assertEquals(toBoxedMultiset(sp),
                         toBoxedMultiset(expected),
                         scenario);
        }
    }

    private void assertRefConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        Stream<Integer> result = StreamSupport.concat(s1, s2);
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered, Spliterators.spliterator(expected, 0));
    }

    private void assertIntConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        IntStream result = IntStreams.concat(s1.mapToInt(Integer::intValue),
                                            s2.mapToInt(Integer::intValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered,
                            StreamSupport.stream(expected).mapToInt(Integer::intValue).spliterator());
    }

    private void assertLongConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        LongStream result = LongStreams.concat(s1.mapToLong(Integer::longValue),
                                              s2.mapToLong(Integer::longValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered,
                            StreamSupport.stream(expected).mapToLong(Integer::longValue).spliterator());
    }

    private void assertDoubleConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        DoubleStream result = DoubleStreams.concat(s1.mapToDouble(Integer::doubleValue),
                                                  s2.mapToDouble(Integer::doubleValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered,
                            StreamSupport.stream(expected).mapToDouble(Integer::doubleValue).spliterator());
    }

    public void testRefConcat() {
        // sequential + sequential -> sequential
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)), false, true);
        // parallel + parallel -> parallel
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // sequential + parallel -> parallel
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // parallel + sequential -> parallel
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2)), true, true);

        // not ordered
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1)).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)), false, false);
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), false, false);
        assertRefConcat(StreamSupport.stream(c1, Characteristics.get(c1), true).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), true, false);
    }

    public void testIntConcat() {
        // sequential + sequential -> sequential
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)), false, true);
        // parallel + parallel -> parallel
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // sequential + parallel -> parallel
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // parallel + sequential -> parallel
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2)), true, true);

        // not ordered
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1)).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)), false, false);
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), false, false);
        assertIntConcat(StreamSupport.stream(c1, Characteristics.get(c1), true).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), true, false);
    }

    public void testLongConcat() {
        // sequential + sequential -> sequential
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)), false, true);
        // parallel + parallel -> parallel
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // sequential + parallel -> parallel
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // parallel + sequential -> parallel
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2)), true, true);

        // not ordered
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1)).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)), false, false);
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), false, false);
        assertLongConcat(StreamSupport.stream(c1, Characteristics.get(c1), true).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), true, false);
    }

    public void testDoubleConcat() {
        // sequential + sequential -> sequential
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)), false, true);
        // parallel + parallel -> parallel
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // sequential + parallel -> parallel
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2), true), true, true);
        // parallel + sequential -> parallel
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1), true), StreamSupport.stream(c2, Characteristics.get(c2)), true, true);

        // not ordered
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1)).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)), false, false);
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1)), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), false, false);
        assertDoubleConcat(StreamSupport.stream(c1, Characteristics.get(c1), true).unordered(), StreamSupport.stream(c2, Characteristics.get(c2)).unordered(), true, false);
    }
}
