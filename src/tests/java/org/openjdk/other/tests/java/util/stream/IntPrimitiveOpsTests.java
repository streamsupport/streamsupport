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
package org.openjdk.other.tests.java.util.stream;

import java8.util.function.IntPredicate;
import java8.util.function.IntSupplier;
import java8.util.function.IntUnaryOperator;
import java8.util.function.BinaryOperator;
import java8.util.function.ToIntFunction;
import java8.util.function.IntConsumer;
import java8.util.stream.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import java8.util.J8Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

@Test
public class IntPrimitiveOpsTests {

    public void testSum() {
        long sum = IntStreams.range(1, 10).filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).sum();
        assertEquals(sum, 20);
    }

    public void testMap() {
        long sum = IntStreams.range(1, 10).filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).map(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int i) {
                return i * 2;
            }
        }).sum();
        assertEquals(sum, 40);
    }

    public void testParSum() {
        long sum = IntStreams.range(1, 10).parallel().filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).sum();
        assertEquals(sum, 20);
    }

    @Test(groups = { "serialization-hostile" })
    public void testTee() {
        final int[] teeSum = new int[1];
        long sum;
        sum = IntStreams.range(1, 10).filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).peek(new IntConsumer() {
            @Override
            public void accept(int i) {
                teeSum[0] = teeSum[0] + i;
            }
        }).sum();
        assertEquals(teeSum[0], sum);
    }

    @Test(groups = { "serialization-hostile" })
    public void testForEach() {
        final int[] sum = new int[1];
        IntStreams.range(1, 10).filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).forEach(new IntConsumer() {
            @Override
            public void accept(int i) {
                sum[0] = sum[0] + i;
            }
        });
        assertEquals(sum[0], 20);
    }

    @Test(groups = { "serialization-hostile" })
    public void testParForEach() {
        final AtomicInteger ai = new AtomicInteger(0);
        IntStreams.range(1, 10).parallel().filter(new IntPredicate() {
            @Override
            public boolean test(int i) {
                return i % 2 == 0;
            }
        }).forEach((IntConsumer) new IntConsumer() {
            @Override
            public void accept(int i) {
            	ai.addAndGet(i);
            }
        });
        assertEquals(ai.get(), 20);
    }

    public void testBox() {
        List<Integer> l = IntStreams.range(1, 10).parallel().boxed().collect(Collectors.<Integer>toList());
        Stream<Integer> stream = StreamSupport.stream(l);
        int sum = stream.reduce(0, new BinaryOperator<Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) {
                return a + b;
            }
        });
        assertEquals(sum, 45);
    }

    public void testUnBox() {
        List<Integer> l = java.util.Arrays.asList(1, 2, 3, 4, 5);
        Stream<Integer> stream = StreamSupport.stream(l);
        long sum = stream.mapToInt(new ToIntFunction<Integer>() {
            @Override
            public int applyAsInt(Integer i) {
                return (int) i;
            }
        }).sum();
        assertEquals(sum, 15);
    }

    public void testToArray() {
        {
            int[] array = IntStreams.range(1, 10).map(new IntUnaryOperator() {
                @Override
                public int applyAsInt(int i) {
                    return i * 2;
                }
            }).toArray();
            assertEquals(array, new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18});
        }

        {
            int[] array =  IntStreams.range(1, 10).parallel().map(new IntUnaryOperator() {
                @Override
                public int applyAsInt(int i) {
                    return i * 2;
                }
            }).toArray();
            assertEquals(array, new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18});
        }
    }

    public void testSort() {
        final Random r = new Random();

        int[] content = IntStreams.generate(new IntSupplier() {
            @Override
            public int getAsInt() {
                return r.nextInt(100);
            }
        }).limit(10).toArray();
        int[] sortedContent = content.clone();
        java.util.Arrays.sort(sortedContent);

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
        final Random r = new Random();

        int[] content = IntStreams.generate(new IntSupplier() {
            @Override
            public int getAsInt() {
                return r.nextInt(100);
            }
        }).limit(10).toArray();
        int[] sortedContent = content.clone();
        java.util.Arrays.sort(sortedContent);

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
            int[] actual;
            actual = IntStreams.iterate(1, new IntUnaryOperator() {
                @Override
                public int applyAsInt(int i) {
                    return i + 1;
                }
            }).limit(9).toArray();
            Assert.assertTrue(java.util.Arrays.equals(expected, actual));
        }

        {
            int[] actual = IntStreams.range(1, 100).parallel().limit(9).toArray();
            Assert.assertTrue(java.util.Arrays.equals(expected, actual));
        }
    }
}
