/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import java8.util.function.Consumer;

/**
 * An index-based split-by-two, lazily initialized Spliterator covering
 * a List that access elements via {@link List#get}.
 *
 * If access results in an IndexOutOfBoundsException then a
 * ConcurrentModificationException is thrown instead (since the list has
 * been structurally modified while traversing).
 *
 * If the List is an instance of AbstractList then concurrent modification
 * checking is performed using the AbstractList's modCount field.
 */
final class RASpliterator<E> implements Spliterator<E> {
    private final List<E> list;
    private int index; // current index, modified on advance/split
    private int fence; // -1 until used; then one past last index

    // The following fields are valid if covering an AbstractList
    private final AbstractList<E> alist;
    private int expectedModCount; // initialized when fence set

    /** Create new spliterator covering the given range */
    private RASpliterator(List<E> list, int origin, int fence,
            int expectedModCount) {
        this.list = list;
        this.index = origin;
        this.fence = fence;

        this.alist = list instanceof AbstractList ? (AbstractList<E>) list
                : null;
        this.expectedModCount = expectedModCount;
    }

    static <T> Spliterator<T> spliterator(List<T> list) {
        return new RASpliterator<T>(list, 0, -1, 0);
    }

    private int getFence() { // initialize fence to size on first use
        int hi; // (a specialized variant appears in method forEachRemaining)
        List<E> lst = list;
        if ((hi = fence) < 0) {
            if (alist != null) {
                expectedModCount = getModCount(alist);
            }
            hi = fence = lst.size();
        }
        return hi;
    }

    public Spliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new RASpliterator<E>(list, lo, index = mid,
                        expectedModCount);
    }

    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int hi = getFence(), i = index;
        if (i < hi) {
            index = i + 1;
            action.accept(list.get(i));
            checkAbsListModCount(alist, expectedModCount);
            return true;
        }
        return false;
    }

    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        List<E> lst = list;
        int hi = getFence();
        int i = index;
        index = hi;
        try {
            for (; i < hi; ++i) {
                action.accept(lst.get(i));
            }
        } catch (IndexOutOfBoundsException e) {
            // action must have modified the list
            throw new ConcurrentModificationException();
        }
        checkAbsListModCount(alist, expectedModCount);
    }

    public long estimateSize() {
        return (long) (getFence() - index);
    }

    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public long getExactSizeIfKnown() {
        return Spliterators.getExactSizeIfKnown(this);
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return Spliterators.hasCharacteristics(this, characteristics);
    }

    @Override
    public Comparator<? super E> getComparator() {
    	throw new IllegalStateException();
    }

    private static void checkAbsListModCount(AbstractList<?> alist, int expectedModCount) {
        if (alist != null && getModCount(alist) != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    private static <T> int getModCount(List<T> lst) {
        return U.getInt(lst, MODCOUNT_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long MODCOUNT_OFF;
    static {
        try {
            MODCOUNT_OFF = U.objectFieldOffset(AbstractList.class
                    .getDeclaredField("modCount"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
