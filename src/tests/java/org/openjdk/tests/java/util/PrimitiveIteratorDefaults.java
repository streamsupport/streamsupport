/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java8.util.Iterators;
import java8.util.PrimitiveIterator;
import java8.util.function.DoubleConsumer;
import java8.util.function.IntConsumer;
import java8.util.function.LongConsumer;

import org.testng.annotations.Test;

import static org.testng695.Assert.assertThrowsNPE;

/**
 * @test
 * @run testng PrimitiveIteratorDefaults
 * @summary test default methods on PrimitiveIterator
 */
@Test
public class PrimitiveIteratorDefaults {

    public void testIntForEachRemainingWithNull() {
        PrimitiveIterator.OfInt i = new PrimitiveIterator.OfInt() {
            @Override
            public int nextInt() {
                return 0;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                Iterators.forEachRemaining(this, action);
            }

            @Override
            public Integer next() {
                return nextInt();
            }
        };

        assertThrowsNPE(() -> i.forEachRemaining((IntConsumer) null));
//        assertThrowsNPE(() -> i.forEachRemaining((Consumer<Integer>) null));
    }

    public void testLongForEachRemainingWithNull() {
        PrimitiveIterator.OfLong i = new PrimitiveIterator.OfLong() {
            @Override
            public long nextLong() {
                return 0;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                Iterators.forEachRemaining(this, action);   
            }

            @Override
            public Long next() {
                return nextLong();
            }
        };

        assertThrowsNPE(() -> i.forEachRemaining((LongConsumer) null));
//        assertThrowsNPE(() -> i.forEachRemaining((Consumer<Long>) null));
    }

    public void testDoubleForEachRemainingWithNull() {
        PrimitiveIterator.OfDouble i = new PrimitiveIterator.OfDouble() {
            @Override
            public double nextDouble() {
                return 0;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                Iterators.forEachRemaining(this, action);
            }

            @Override
            public Double next() {
                return nextDouble();
            }
        };

        assertThrowsNPE(() -> i.forEachRemaining((DoubleConsumer) null));
//        assertThrowsNPE(() -> i.forEachRemaining((Consumer<Double>) null));
    }
}
