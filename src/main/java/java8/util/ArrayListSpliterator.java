/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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
package java8.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Consumer;

/**
 * Index-based split-by-two, lazily initialized Spliterator for ArrayLists.
 */
final class ArrayListSpliterator<E> implements Spliterator<E> {
// CVS rev. 1.50
    /*
     * If ArrayLists were immutable, or structurally immutable (no
     * adds, removes, etc), we could implement their spliterators
     * with Arrays.spliterator. Instead we detect as much
     * interference during traversal as practical without
     * sacrificing much performance. We rely primarily on
     * modCounts. These are not guaranteed to detect concurrency
     * violations, and are sometimes overly conservative about
     * within-thread interference, but detect enough problems to
     * be worthwhile in practice. To carry this out, we (1) lazily
     * initialize fence and expectedModCount until the latest
     * point that we need to commit to the state we are checking
     * against; thus improving precision.  (This doesn't apply to
     * SubLists, that create spliterators with current non-lazy
     * values).  (2) We perform only a single
     * ConcurrentModificationException check at the end of forEach
     * (the most performance-sensitive method). When using forEach
     * (as opposed to iterators), we can normally only detect
     * interference after actions, not before. Further
     * CME-triggering checks apply to all other possible
     * violations of assumptions for example null or too-small
     * elementData array given its size(), that could only have
     * occurred due to interference.  This allows the inner loop
     * of forEach to run without any further checks, and
     * simplifies lambda-resolution. While this does entail a
     * number of checks, note that in the common case of
     * list.stream().forEach(a), no checks or other computation
     * occur anywhere other than inside forEach itself.  The other
     * less-often-used methods cannot take advantage of most of
     * these streamlinings.
     */

    private final ArrayList<E> list;
    private int index; // current index, modified on advance/split
    private int fence; // -1 until used; then one past last index
    private int expectedModCount; // initialized when fence set

    /** Create new spliterator covering the given range */
    private ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                         int expectedModCount) {
        this.list = list; // OK if null unless traversed
        this.index = origin;
        this.fence = fence;
        this.expectedModCount = expectedModCount;
    }

    static <T> Spliterator<T> spliterator(ArrayList<T> list) {
        return new ArrayListSpliterator<T>(list, 0, -1, 0);
    }

    private int getFence() { // initialize fence to size on first use
        int hi; // (a specialized variant appears in method forEach)
        if ((hi = fence) < 0) {
            ArrayList<E> lst = list;
            expectedModCount = getModCount(lst);
            hi = fence = getSize(lst);
        }
        return hi;
    }

    @Override
    public ArrayListSpliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
            new ArrayListSpliterator<E>(list, lo, index = mid,
                                        expectedModCount);
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int hi = getFence(), i = index;
        if (i < hi) {
            index = i + 1;
            @SuppressWarnings("unchecked") E e = (E) getData(list)[i];
            action.accept(e);
            if (expectedModCount != getModCount(list)) {
                throw new ConcurrentModificationException();
            }
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int i, hi, mc; // hoist accesses and checks from loop
        Object[] a;
        ArrayList<E> lst = list;
        if ((a = getData(lst)) != null) {
            if ((hi = fence) < 0) {
                mc = getModCount(lst);
                hi = getSize(lst);
            }
            else {
                mc = expectedModCount;
            }
            if ((i = index) >= 0 && (index = hi) <= a.length) {
                for (; i < hi; ++i) {
                    @SuppressWarnings("unchecked") E e = (E) a[i];
                    action.accept(e);
                }
                if (mc == getModCount(lst)) {
                    return;
                }
            }
        }
        throw new ConcurrentModificationException();
    }

    @Override
    public long estimateSize() {
        return getFence() - index;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public Comparator<? super E> getComparator() {
        return Spliterators.getComparator(this);
    }

    @Override
    public long getExactSizeIfKnown() {
        return Spliterators.getExactSizeIfKnown(this);
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return Spliterators.hasCharacteristics(this, characteristics);
    }

    private static <T> int getSize(ArrayList<T> lst) {
        return U.getInt(lst, SIZE_OFF);
    }

    private static <T> int getModCount(ArrayList<T> lst) {
        return U.getInt(lst, MODCOUNT_OFF);
    }

    private static <T> Object[] getData(ArrayList<T> lst) {
        return (Object[]) U.getObject(lst, DATA_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long SIZE_OFF;
    private static final long MODCOUNT_OFF;
    private static final long DATA_OFF;
    static {
        try {
            MODCOUNT_OFF = U.objectFieldOffset(AbstractList.class
                    .getDeclaredField("modCount"));
            SIZE_OFF = U.objectFieldOffset(ArrayList.class
                    .getDeclaredField("size"));
            String arrayFieldName = Spliterators.IS_HARMONY_ANDROID ? "array"
                    : "elementData";
            DATA_OFF = U.objectFieldOffset(ArrayList.class
                    .getDeclaredField(arrayFieldName));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
