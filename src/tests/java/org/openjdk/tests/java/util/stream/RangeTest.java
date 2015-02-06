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

import java.util.Arrays;

import java8.util.Optional;
import java8.util.Spliterator;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreams;
import java8.util.stream.LongStream;
import java8.util.stream.LongStreams;
import java8.util.stream.OpTestCase;
import java8.util.stream.SpliteratorTestHelper;
import java8.util.stream.StreamSupport;
import java8.util.stream.TestData;

import org.testng.annotations.Test;

/**
 * Primitive range tests
 *
 * @author Brian Goetz
 */
@Test
public class RangeTest extends OpTestCase {

    public void testInfiniteRangeFindFirst() {
        Integer first = StreamSupport.iterate(0, i -> i + 1).filter(i -> i > 10000).findFirst().get();
        assertEquals(first, StreamSupport.iterate(0, i -> i + 1).parallel().filter(i -> i > 10000).findFirst().get());

        // Limit is required to transform the infinite stream to a finite stream
        // since the exercising requires a finite stream
        withData(TestData.Factory.ofSupplier(
                "", () -> StreamSupport.iterate(0, i -> i + 1).filter(i -> i > 10000).limit(20000))).
                terminal(s->s.findFirst()).expectedResult(Optional.of(10001)).exercise();
    }

    //

    public void testIntRange() {
        // Half-open
        for (int start : Arrays.asList(1, 10, -1, -10)) {
            setContext("start", start);
            for (int end : Arrays.asList(1, 10, -1, -10)) {
                setContext("end", end);
                int size = (start < end) ? end - start : 0;
                int[] exp = new int[size];
                for (int i = start, p = 0; i < end; i++, p++) {
                    exp[p] = i;
                }

                int[] inc = IntStreams.range(start, end).toArray();
                assertEquals(inc.length, size);
                assertTrue(Arrays.equals(exp, inc));

                withData(intRangeData(start, end)).stream((IntStream s) -> s).
                        expectedResult(exp).exercise();
            }
        }

        // Closed
        for (int start : Arrays.asList(1, 10, -1, -10)) {
            setContext("start", start);
            for (int end : Arrays.asList(1, 10, -1, -10)) {
                setContext("end", end);
                int size = (start <= end) ? end - start + 1 : 0;
                int[] exp = new int[size];
                for (int i = start, p = 0; i <= end; i++, p++) {
                    exp[p] = i;
                }

                int[] inc = IntStreams.rangeClosed(start, end).toArray();
                assertEquals(inc.length, size);
                assertTrue(Arrays.equals(exp, inc));

                withData(intRangeClosedData(start, end)).stream((IntStream s) -> s).
                        expectedResult(exp).exercise();
            }
        }

        // Closed, maximum upper bound of Integer.MAX_VALUE
        {
            int[] inc = IntStreams.rangeClosed(Integer.MAX_VALUE - 1, Integer.MAX_VALUE).toArray();
            assertEquals(2, inc.length);
            assertEquals(Integer.MAX_VALUE - 1, inc[0]);
            assertEquals(Integer.MAX_VALUE, inc[1]);

            inc = IntStreams.rangeClosed(Integer.MAX_VALUE, Integer.MAX_VALUE).toArray();
            assertEquals(1, inc.length);
            assertEquals(Integer.MAX_VALUE, inc[0]);

            SpliteratorTestHelper.testIntSpliterator(
                    () -> IntStreams.rangeClosed(Integer.MAX_VALUE - 8, Integer.MAX_VALUE).spliterator());
        }

        // Range wider than Integer.MAX_VALUE
        {
            Spliterator.OfInt s = IntStreams.rangeClosed(Integer.MIN_VALUE, Integer.MAX_VALUE).
                    spliterator();
            assertEquals(s.estimateSize(), 1L << 32);
        }
    }

    TestData.OfInt intRangeData(int start, int end) {
        return TestData.Factory.ofIntSupplier("int range", () -> IntStreams.range(start, end));
    }

    TestData.OfInt intRangeClosedData(int start, int end) {
        return TestData.Factory.ofIntSupplier("int rangeClosed", () -> IntStreams.rangeClosed(start, end));
    }

    public void tesIntRangeReduce() {
        withData(intRangeData(0, 10000)).
                terminal(s -> s.reduce(0, java8.lang.Integers::sum)).exercise();
    }

    public void testIntInfiniteRangeLimit() {
        withData(TestData.Factory.ofIntSupplier(
                "int range", () -> IntStreams.iterate(0, i -> i + 1).limit(10000))).
                terminal(s -> s.reduce(0, java8.lang.Integers::sum)).exercise();
    }

    public void testIntInfiniteRangeFindFirst() {
        int first = IntStreams.iterate(0, i -> i + 1).filter(i -> i > 10000).findFirst().getAsInt();
        assertEquals(first, IntStreams.iterate(0, i -> i + 1).parallel().filter(i -> i > 10000).findFirst().getAsInt());
    }

    //

    public void testLongRange() {
        // Half-open
        for (long start : Arrays.asList(1, 1000, -1, -1000)) {
            setContext("start", start);
            for (long end : Arrays.asList(1, 1000, -1, -1000)) {
                setContext("end", end);
                long size = start < end ? end - start : 0;
                long[] exp = new long[(int) size];
                for (long i = start, p = 0; i < end; i++, p++) {
                    exp[(int) p] = i;
                }

                long[] inc = LongStreams.range(start, end).toArray();
                assertEquals(inc.length, size);
                assertTrue(Arrays.equals(exp, inc));

                withData(longRangeData(start, end)).stream((LongStream s) -> s).
                        expectedResult(exp).exercise();
            }
        }

        // Closed
        for (long start : Arrays.asList(1, 1000, -1, -1000)) {
            setContext("start", start);
            for (long end : Arrays.asList(1, 1000, -1, -1000)) {
                setContext("end", end);
                long size = start <= end ? end - start + 1: 0;
                long[] exp = new long[(int) size];
                for (long i = start, p = 0; i <= end; i++, p++) {
                    exp[(int) p] = i;
                }

                long[] inc = LongStreams.rangeClosed(start, end).toArray();
                assertEquals(inc.length, size);
                assertTrue(Arrays.equals(exp, inc));

                withData(longRangeClosedData(start, end)).stream((LongStream s) -> s).
                        expectedResult(exp).exercise();
            }
        }

        // Closed, maximum upper bound of Long.MAX_VALUE
        {
            long[] inc = LongStreams.rangeClosed(Long.MAX_VALUE - 1, Long.MAX_VALUE).toArray();
            assertEquals(2, inc.length);
            assertEquals(Long.MAX_VALUE - 1, inc[0]);
            assertEquals(Long.MAX_VALUE, inc[1]);

            inc = LongStreams.rangeClosed(Long.MAX_VALUE, Long.MAX_VALUE).toArray();
            assertEquals(1, inc.length);
            assertEquals(Long.MAX_VALUE, inc[0]);

            SpliteratorTestHelper.testLongSpliterator(
                    () -> LongStreams.rangeClosed(Long.MAX_VALUE - 8, Long.MAX_VALUE).spliterator());
        }
    }

    TestData.OfLong longRangeData(long start, long end) {
        return TestData.Factory.ofLongSupplier("long range", () -> LongStreams.range(start, end));
    }

    TestData.OfLong longRangeClosedData(long start, long end) {
        return TestData.Factory.ofLongSupplier("long rangeClosed", () -> LongStreams.rangeClosed(start, end));
    }

    public void testLongRangeReduce() {
        withData(longRangeData(0, 10000)).
                terminal(s -> s.reduce(0, java8.lang.Longs::sum)).exercise();
    }

    public void testLongInfiniteRangeLimit() {
        withData(TestData.Factory.ofLongSupplier(
                "long range", () -> LongStreams.iterate(0, i -> i + 1).limit(10000))).
                terminal(s -> s.reduce(0, java8.lang.Longs::sum)).exercise();
    }

    public void testLongInfiniteRangeFindFirst() {
        long first = LongStreams.iterate(0, i -> i + 1).filter(i -> i > 10000).findFirst().getAsLong();
        assertEquals(first, LongStreams.iterate(0, i -> i + 1).parallel().filter(i -> i > 10000).findFirst().getAsLong());
    }

    private static void assertSizedAndSubSized(Spliterator<?> s) {
        assertTrue(s.hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED));
    }

    private static void assertNotSizedAndSubSized(Spliterator<?> s) {
        assertFalse(s.hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED));
    }

    public void testLongLongRange() {
        // Test [Long.MIN_VALUE, Long.MAX_VALUE)
        // This will concatenate streams of three ranges
        //   [Long.MIN_VALUE, x) [x, 0) [0, Long.MAX_VALUE)
        // where x = Long.divideUnsigned(0 - Long.MIN_VALUE, 2) + 1
        {
            Spliterator.OfLong s = LongStreams.range(Long.MIN_VALUE, Long.MAX_VALUE).spliterator();

            assertEquals(s.estimateSize(), Long.MAX_VALUE);
            assertNotSizedAndSubSized(s);

            Spliterator.OfLong s1 = s.trySplit();
            assertNotSizedAndSubSized(s1);
            assertSizedAndSubSized(s);

            Spliterator.OfLong s2 = s1.trySplit();
            assertSizedAndSubSized(s1);
            assertSizedAndSubSized(s2);

            assertTrue(s.estimateSize() == Long.MAX_VALUE);
            assertTrue(s1.estimateSize() < Long.MAX_VALUE);
            assertTrue(s2.estimateSize() < Long.MAX_VALUE);

            assertEquals(s.estimateSize() + s1.estimateSize() + s2.estimateSize(),
                         Long.MAX_VALUE - Long.MIN_VALUE);
        }

        long[][] ranges = { {Long.MIN_VALUE, 0}, {-1, Long.MAX_VALUE} };
        for (int i = 0; i < ranges.length; i++) {
            long start = ranges[i][0];
            long end = ranges[i][1];

            Spliterator.OfLong s = LongStreams.range(start, end).spliterator();

            assertEquals(s.estimateSize(), Long.MAX_VALUE);
            assertNotSizedAndSubSized(s);

            Spliterator.OfLong s1 = s.trySplit();
            assertSizedAndSubSized(s1);
            assertSizedAndSubSized(s);

            assertTrue(s.estimateSize() < Long.MAX_VALUE);
            assertTrue(s1.estimateSize() < Long.MAX_VALUE);

            assertEquals(s.estimateSize() + s1.estimateSize(), end - start);
        }
    }

    public void testLongLongRangeClosed() {
        // Test [Long.MIN_VALUE, Long.MAX_VALUE]
        // This will concatenate streams of four ranges
        //   [Long.MIN_VALUE, x) [x, 0) [0, y) [y, Long.MAX_VALUE]
        // where x = Long.divideUnsigned(0 - Long.MIN_VALUE, 2) + 1
        //       y = Long.divideUnsigned(Long.MAX_VALUE, 2) + 1

        {
            Spliterator.OfLong s = LongStreams.rangeClosed(Long.MIN_VALUE, Long.MAX_VALUE).spliterator();

            assertEquals(s.estimateSize(), Long.MAX_VALUE);
            assertNotSizedAndSubSized(s);

            Spliterator.OfLong s1 = s.trySplit();
            assertNotSizedAndSubSized(s1);
            assertNotSizedAndSubSized(s);

            Spliterator.OfLong s2 = s1.trySplit();
            assertSizedAndSubSized(s1);
            assertSizedAndSubSized(s2);

            Spliterator.OfLong s3 = s.trySplit();
            assertSizedAndSubSized(s3);
            assertSizedAndSubSized(s);

            assertTrue(s.estimateSize() < Long.MAX_VALUE);
            assertTrue(s3.estimateSize() < Long.MAX_VALUE);
            assertTrue(s1.estimateSize() < Long.MAX_VALUE);
            assertTrue(s2.estimateSize() < Long.MAX_VALUE);

            assertEquals(s.estimateSize() + s3.estimateSize() + s1.estimateSize() + s2.estimateSize(),
                         Long.MAX_VALUE - Long.MIN_VALUE + 1);
        }

        long[][] ranges = { {Long.MIN_VALUE, 0}, {-1, Long.MAX_VALUE} };
        for (int i = 0; i < ranges.length; i++) {
            long start = ranges[i][0];
            long end = ranges[i][1];

            Spliterator.OfLong s = LongStreams.rangeClosed(start, end).spliterator();

            assertEquals(s.estimateSize(), Long.MAX_VALUE);
            assertNotSizedAndSubSized(s);

            Spliterator.OfLong s1 = s.trySplit();
            assertSizedAndSubSized(s1);
            assertSizedAndSubSized(s);

            assertTrue(s.estimateSize() < Long.MAX_VALUE);
            assertTrue(s1.estimateSize() < Long.MAX_VALUE);

            assertEquals(s.estimateSize() + s1.estimateSize(), end - start + 1);
        }
    }
}
