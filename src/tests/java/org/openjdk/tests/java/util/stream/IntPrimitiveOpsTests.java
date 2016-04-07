/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import java8.util.function.IntConsumer;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import java8.util.J8Arrays;
import java8.util.Spliterator;
import java8.util.stream.IntStreams;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @test
 * @bug 8153293
 */
@Test
public class IntPrimitiveOpsTests {

    public void testSum() {
        long sum = IntStreams.range(1, 10).filter(i -> i % 2 == 0).sum();
        assertEquals(sum, 20);
    }

    public void testMap() {
        long sum = IntStreams.range(1, 10).filter(i -> i % 2 == 0).map(i -> i * 2).sum();
        assertEquals(sum, 40);
    }

    public void testParSum() {
        long sum = IntStreams.range(1, 10).parallel().filter(i -> i % 2 == 0).sum();
        assertEquals(sum, 20);
    }

    @Test(groups = { "serialization-hostile" })
    public void testTee() {
        int[] teeSum = new int[1];
        long sum = IntStreams.range(1, 10).filter(i -> i % 2 == 0).peek(i -> { teeSum[0] = teeSum[0] + i; }).sum();
        assertEquals(teeSum[0], sum);
    }

    @Test(groups = { "serialization-hostile" })
    public void testForEach() {
        int[] sum = new int[1];
        IntStreams.range(1, 10).filter(i -> i % 2 == 0).forEach(i -> { sum[0] = sum[0] + i; });
        assertEquals(sum[0], 20);
    }

    @Test(groups = { "serialization-hostile" })
    public void testParForEach() {
        AtomicInteger ai = new AtomicInteger(0);
        IntStreams.range(1, 10).parallel().filter(i -> i % 2 == 0).forEach(ai::addAndGet);
        assertEquals(ai.get(), 20);
    }

    public void testBox() {
        List<Integer> l = IntStreams.range(1, 10).parallel().boxed().collect(Collectors.toList());
        int sum = StreamSupport.stream(l).reduce(0, (a, b) -> a + b);
        assertEquals(sum, 45);
    }

    public void testUnBox() {
        long sum = StreamSupport.stream(Arrays.asList(1, 2, 3, 4, 5)).mapToInt(i -> (int) i).sum();
        assertEquals(sum, 15);
    }

    public void testFlags() {
        assertTrue(IntStreams.range(1, 10).boxed().spliterator()
                      .hasCharacteristics(Spliterator.SORTED | Spliterator.DISTINCT));
        assertFalse(IntStreams.of(1, 10).boxed().spliterator()
                      .hasCharacteristics(Spliterator.SORTED));
        assertFalse(IntStreams.of(1, 10).boxed().spliterator()
                      .hasCharacteristics(Spliterator.DISTINCT));

        assertTrue(IntStreams.range(1, 10).asLongStream().spliterator()
                      .hasCharacteristics(Spliterator.SORTED | Spliterator.DISTINCT));
        assertFalse(IntStreams.of(1, 10).asLongStream().spliterator()
                      .hasCharacteristics(Spliterator.SORTED));
        assertFalse(IntStreams.of(1, 10).asLongStream().spliterator()
                      .hasCharacteristics(Spliterator.DISTINCT));

        assertTrue(IntStreams.range(1, 10).asDoubleStream().spliterator()
                      .hasCharacteristics(Spliterator.SORTED | Spliterator.DISTINCT));
//        assertFalse(IntStreams.range(1, 10).asDoubleStream().spliterator()
//                      .hasCharacteristics(Spliterator.DISTINCT));
//        assertFalse(IntStreams.of(1, 10).boxed().spliterator()
//                      .hasCharacteristics(Spliterator.SORTED));
    }

    public void testToArray() {
        {
            int[] array =  IntStreams.range(1, 10).map(i -> i * 2).toArray();
            assertEquals(array, new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18});
        }

        {
            int[] array =  IntStreams.range(1, 10).parallel().map(i -> i * 2).toArray();
            assertEquals(array, new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18});
        }
    }

    public void testSort() {
        Random r = new Random();

        int[] content = IntStreams.generate(() -> r.nextInt(100)).limit(10).toArray();
        int[] sortedContent = content.clone();
        Arrays.sort(sortedContent);

        {
            int[] array =  J8Arrays.stream(content).sorted().toArray();
            assertEquals(array, sortedContent);
        }

        {
            int[] array =  J8Arrays.stream(content).parallel().sorted().toArray();
            assertEquals(array, sortedContent);
        }
    }

    public void testSortSort() {
        Random r = new Random();

        int[] content = IntStreams.generate(() -> r.nextInt(100)).limit(10).toArray();
        int[] sortedContent = content.clone();
        Arrays.sort(sortedContent);

        {
            int[] array =  J8Arrays.stream(content).sorted().sorted().toArray();
            assertEquals(array, sortedContent);
        }

        {
            int[] array =  J8Arrays.stream(content).parallel().sorted().sorted().toArray();
            assertEquals(array, sortedContent);
        }
    }

    public void testSequential() {

        int[] expected = IntStreams.range(1, 1000).toArray();

        class AssertingConsumer implements IntConsumer {
            private final int[] array;
            int offset;

            AssertingConsumer(int[] array) {
                this.array = array;
            }

            @Override
            public void accept(int value) {
                assertEquals(array[offset++], value);
            }

            public int getCount() { return offset; }
        }

        {
            AssertingConsumer consumer = new AssertingConsumer(expected);
            IntStreams.range(1, 1000).sequential().forEach(consumer);
            assertEquals(expected.length, consumer.getCount());
        }

        {
            AssertingConsumer consumer = new AssertingConsumer(expected);
            IntStreams.range(1, 1000).parallel().sequential().forEach(consumer);
            assertEquals(expected.length, consumer.getCount());
        }
    }

    public void testLimit() {
        int[] expected = IntStreams.range(1, 10).toArray();

        {
            int[] actual = IntStreams.iterate(1, i -> i + 1).limit(9).toArray();
            Assert.assertTrue(Arrays.equals(expected, actual));
        }

        {
            int[] actual = IntStreams.range(1, 100).parallel().limit(9).toArray();
            Assert.assertTrue(Arrays.equals(expected, actual));
        }
    }
}
