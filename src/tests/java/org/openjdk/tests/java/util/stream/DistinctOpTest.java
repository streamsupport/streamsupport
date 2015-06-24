/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import java8.lang.Iterables;

import java8.util.Lists;
import java8.util.Optional;
import java8.util.Spliterator;
import java8.util.Spliterators;

import java8.util.concurrent.ThreadLocalRandom;

import java8.util.stream.*;
import static java8.util.stream.LambdaTestHelpers.*;

/**
 * DistinctOpTest
 */
@Test
public class DistinctOpTest extends OpTestCase {

    public void testUniqOp() {
        assertCountSum(StreamSupport.stream(repeat(0, 10)).distinct(), 1, 0);
        assertCountSum(StreamSupport.stream(repeat(1, 10)).distinct(), 1, 1);
        assertCountSum(StreamSupport.stream(countTo(0)).distinct(), 0, 0);
        assertCountSum(StreamSupport.stream(countTo(10)).distinct(), 10, 55);
        assertCountSum(StreamSupport.stream(countTo(10)).distinct(), 10, 55);
    }

    public void testWithUnorderedInfiniteStream() {
        // These tests should short-circuit, otherwise will fail with a time-out
        // or an OOME

        // Note that since the streams are unordered and any element is requested
        // (a non-deterministic process) the only assertion that can be made is
        // that an element should be found

        Optional<Integer> oi = StreamSupport.iterate(1, i -> i + 1).unordered().parallel().distinct().findAny();
        assertTrue(oi.isPresent());

        oi = ThreadLocalRandom.current().ints().boxed().parallel().distinct().findAny();
        assertTrue(oi.isPresent());
    }

    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testOp(String name, TestData.OfRef<Integer> data) {
        Collection<Integer> result = exerciseOpsInt(
        		data,
        		Stream::distinct,
        		IntStream::distinct,
        		LongStream::distinct,
        		DoubleStream::distinct);

        assertUnique(result);
        assertTrue((data.size() > 0) ? result.size() > 0 : result.size() == 0);
        assertTrue(result.size() <= data.size());
    }

    @Test(dataProvider = "withNull:StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testOpWithNull(String name, TestData.OfRef<Integer> data) {
        Collection<Integer> node = exerciseOps(data, Stream::distinct);
        assertUnique(node);

        node = withData(data).
                stream(s -> s.unordered().distinct()).
                exercise();
        assertUnique(node);

        node = exerciseOps(data, s -> s.distinct().distinct());
        assertUnique(node);
    }

    @Test(dataProvider = "withNull:StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testOpWithNullSorted(String name, TestData.OfRef<Integer> data) {
        List<Integer> l = new ArrayList<>();
        Lists.sort(data.into(l), cNullInteger);
        // Need to inject SORTED into the sorted list source since
        // sorted() with a comparator ironically clears SORTED
        Collection<Integer> node = exerciseOps(new SortedTestData<>(l), Stream::distinct);
        assertUnique(node);
        assertSorted(node, cNullInteger);
    }

    @SuppressWarnings("serial")
    static class SortedTestData<T> extends TestData.AbstractTestData.RefTestData<T, List<T>> {
        SortedTestData(List<T> coll) {
            super("SortedTestData", coll,
                  c -> StreamSupport.stream(Spliterators.spliterator(c.toArray(), Spliterator.ORDERED | Spliterator.SORTED), false),
                  c -> StreamSupport.stream(Spliterators.spliterator(c.toArray(), Spliterator.ORDERED | Spliterator.SORTED), true),
                  c -> Spliterators.spliterator(c.toArray(), Spliterator.ORDERED | Spliterator.SORTED),
                  List::size);
        }
    }

    public static final Comparator<Integer> cNullInteger = (a, b) -> {
        if (a == null && b == null) {
            return 0;
        }
        else if (a == null) {
            return -1;
        }
        else if (b == null) {
            return 1;
        }
        else {
            return java8.lang.Integers.compare(a, b);
        }
    };

    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testDistinctDistinct(String name, TestData.OfRef<Integer> data) {
//        Collection<Integer> result = withData(data)
////                .stream(s -> s.distinct().distinct(), new CollectorOps.TestParallelSizedOp<>())
//                // @bug 8075307
//                .stream(s -> s.distinct().distinct())
//                .exercise();
        Collection<Integer> result = exerciseOpsInt(
                data,
                s -> s.distinct().distinct(),
                s -> s.distinct().distinct(),
                s -> s.distinct().distinct(),
                s -> s.distinct().distinct());

        assertUnique(result);
    }

    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testDistinctSorted(String name, TestData.OfRef<Integer> data) {
        Collection<Integer> result = withData(data)
                .stream(s -> s.distinct().sorted(),
                        new CollectorOps.TestParallelSizedOp<>())
                .exercise();
        assertUnique(result);
        assertSorted(result);
    }

    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testSortedDistinct(String name, TestData.OfRef<Integer> data) {
        Collection<Integer> result = withData(data)
                .stream(s -> s.sorted().distinct(),
                        new CollectorOps.TestParallelSizedOp<>())
                .exercise();
        assertUnique(result);
        assertSorted(result);
    }

    @Test(groups = { "serialization-hostile" })
    public void testStable() {
        // Create N instances of Integer all with the same value
        List<Integer> input = IntStreams.rangeClosed(0, 1000)
                .mapToObj(i -> new Integer(1000)) // explicit construction
                .collect(Collectors.toList());
        Integer expectedElement = input.get(0);
        TestData<Integer, Stream<Integer>> data = TestData.Factory.ofCollection(
                "1000 instances of Integer with the same value", input);

        withData(data)
                .stream(Stream::distinct)
                .resultAsserter((actual, expected, isOrdered, isParallel) -> {
                    List<Integer> l = new ArrayList<>();
                    Iterables.forEach(actual, l::add);

                    // Assert stability
                    // The single result element should be equal in identity to
                    // the first input element
                    assertEquals(l.size(), 1);
                    assertEquals(System.identityHashCode(l.get(0)),
                                 System.identityHashCode(expectedElement));

                })
                .exercise();
    }
}
