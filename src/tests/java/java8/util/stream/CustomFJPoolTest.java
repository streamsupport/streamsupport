/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package java8.util.stream;

/*
 * @test
 * @summary Tests stream execution in a custom ForkJoinPool
 * @bug 8190974
 * @run testng/othervm CustomFJPoolTest
 * @run testng/othervm -Djava.util.concurrent.ForkJoinPool.common.parallelism=0 CustomFJPoolTest
 */

import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import java8.util.Spliterator;
import java8.util.concurrent.ForkJoinPool;
import java8.util.concurrent.ForkJoinTask;
import java8.util.function.Consumer;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;

import static org.testng.Assert.assertEquals;

@Test
public class CustomFJPoolTest {

    // A Spliterator that counts the number of spliterators created
    // including itself, thus the count starts at 1
    static class SplitCountingSpliterator<T> implements Spliterator<T> {
        final Spliterator<T> s;
        final AtomicInteger nsplits;

        // Top-level constructor
        public SplitCountingSpliterator(Spliterator<T> s) {
            this.s = s;
            nsplits = new AtomicInteger(1);
        }

        // Splitting constructor
        SplitCountingSpliterator(Spliterator<T> s, AtomicInteger nsplits) {
            this.s = s;
            this.nsplits = nsplits;
        }

        int splits() {
            return nsplits.get();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return s.tryAdvance(action);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            s.forEachRemaining(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            Spliterator<T> split = s.trySplit();
            if (split != null) {
                nsplits.incrementAndGet();
                return new SplitCountingSpliterator<>(split, nsplits);
            }
            else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return s.estimateSize();
        }

        @Override
        public long getExactSizeIfKnown() {
            return s.getExactSizeIfKnown();
        }

        @Override
        public int characteristics() {
            return s.characteristics();
        }

        @Override
        public boolean hasCharacteristics(int characteristics) {
            return s.hasCharacteristics(characteristics);
        }

        @Override
        public Comparator<? super T> getComparator() {
            return s.getComparator();
        }
    }

    public void testCustomPools() throws Exception {
        int splitsForP1 = countSplits(new ForkJoinPool(1));
        int splitsForP2 = countSplits(new ForkJoinPool(2));
        assertEquals(splitsForP2, splitsForP1 * 2);

        int commonParallelism = ForkJoinPool.getCommonPoolParallelism();
        if (commonParallelism > 1 && commonParallelism < 128) {
            int splitsForPHalfC = countSplits(new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism() / 2));
            int splitsForPC = countSplits(ForkJoinPool.commonPool());

            if (Integer.bitCount(commonParallelism - 1) != 1) {
                assertEquals(splitsForPC, splitsForPHalfC * 2);
            } else {
                assertEquals(splitsForPC, splitsForPHalfC * 4);
            }
        }
    }

    static int countSplits(ForkJoinPool fjp) throws Exception {
        ForkJoinTask<Integer> fInteger = fjp.submit(() -> {
            Spliterator<Integer> s = IntStreams.range(0, 1024).boxed().parallel().spliterator();
            SplitCountingSpliterator<Integer> cs = new SplitCountingSpliterator<>(s);
            StreamSupport.stream(cs, true).forEach(e -> {});
            return cs.splits();
        });
        return fInteger.get();
    }
}
