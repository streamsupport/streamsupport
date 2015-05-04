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

import java8.util.Spliterator;
import java8.util.stream.BaseStream;
import java8.util.stream.OpTestCase;
import java8.util.stream.StreamTestDataProvider;

import org.testng.annotations.Test;

import java8.util.stream.TestData;
import java8.util.stream.StreamSupport;
import java8.util.stream.IntStreams;
import java8.util.stream.LongStreams;
import java8.util.stream.DoubleStreams;

/**
 * @test
 * @bug 8021863
 */
public class ConcatOpTest extends OpTestCase {

    // Sanity to make sure all type of stream source works
    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testOps(String name, TestData.OfRef<Integer> data) {
        exerciseOpsInt(data,
                       s -> java8.util.stream.StreamSupport.concat(s, data.stream()),
                       s -> java8.util.stream.IntStreams.concat(s, data.stream().mapToInt(Integer::intValue)),
                       s -> java8.util.stream.LongStreams.concat(s, data.stream().mapToLong(Integer::longValue)),
                       s -> java8.util.stream.DoubleStreams.concat(s, data.stream().mapToDouble(Integer::doubleValue)));
    }

    public void testSize() {
        assertSized(StreamSupport.concat(
                LongStreams.range(0, Long.MAX_VALUE / 2).boxed(),
                LongStreams.range(0, Long.MAX_VALUE / 2).boxed()));

        assertUnsized(StreamSupport.concat(
                LongStreams.range(0, Long.MAX_VALUE).boxed(),
                LongStreams.range(0, Long.MAX_VALUE).boxed()));

        assertUnsized(StreamSupport.concat(
                LongStreams.range(0, Long.MAX_VALUE).boxed(),
                StreamSupport.iterate(0, i -> i + 1)));

        assertUnsized(StreamSupport.concat(
                StreamSupport.iterate(0, i -> i + 1),
                LongStreams.range(0, Long.MAX_VALUE).boxed()));
    }

    public void testLongSize() {
        assertSized(LongStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE / 2),
                LongStreams.range(0, Long.MAX_VALUE / 2)));

        assertUnsized(LongStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE),
                LongStreams.range(0, Long.MAX_VALUE)));

        assertUnsized(LongStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE),
                LongStreams.iterate(0, i -> i + 1)));

        assertUnsized(LongStreams.concat(
                LongStreams.iterate(0, i -> i + 1),
                LongStreams.range(0, Long.MAX_VALUE)));
    }

    public void testIntSize() {
        assertSized(IntStreams.concat(
                IntStreams.range(0, Integer.MAX_VALUE),
                IntStreams.range(0, Integer.MAX_VALUE)));

        assertUnsized(IntStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE).mapToInt(i -> (int) i),
                LongStreams.range(0, Long.MAX_VALUE).mapToInt(i -> (int) i)));

        assertUnsized(IntStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE).mapToInt(i -> (int) i),
                IntStreams.iterate(0, i -> i + 1)));

        assertUnsized(IntStreams.concat(
                IntStreams.iterate(0, i -> i + 1),
                LongStreams.range(0, Long.MAX_VALUE).mapToInt(i -> (int) i)));
    }

    public void testDoubleSize() {
        assertSized(DoubleStreams.concat(
                IntStreams.range(0, Integer.MAX_VALUE).mapToDouble(i -> i),
                IntStreams.range(0, Integer.MAX_VALUE).mapToDouble(i -> i)));

        assertUnsized(DoubleStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE).mapToDouble(i -> i),
                LongStreams.range(0, Long.MAX_VALUE).mapToDouble(i -> i)));

        assertUnsized(DoubleStreams.concat(
                LongStreams.range(0, Long.MAX_VALUE).mapToDouble(i -> i),
                DoubleStreams.iterate(0, i -> i + 1)));

        assertUnsized(DoubleStreams.concat(
                DoubleStreams.iterate(0, i -> i + 1),
                LongStreams.range(0, Long.MAX_VALUE).mapToDouble(i -> i)));
    }

    void assertUnsized(BaseStream<?, ?> s) {
        Spliterator<?> sp = s.spliterator();

        assertFalse(sp.hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED));
        assertEquals(sp.estimateSize(), Long.MAX_VALUE);
    }

    void assertSized(BaseStream<?, ?> s) {
        Spliterator<?> sp = s.spliterator();

        assertTrue(sp.hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED));
        assertTrue(sp.estimateSize() < Long.MAX_VALUE);
    }
}
