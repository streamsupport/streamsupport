/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * Index-based split-by-two, lazily initialized Spliterator for AbstractLists
 * that implement RandomAccess.
 */
final class RAAbstractListSpliterator<E> implements Spliterator<E> {

    private final List<E> list;
    private int index; // current index, modified on advance/split
    private int fence; // -1 until used; then one past last index
    private int expectedModCount; // initialized when fence set

    private RAAbstractListSpliterator(List<E> list, int origin, int fence,
            int expectedModCount) {
        this.list = list;
        this.index = origin;
        this.fence = fence;
        this.expectedModCount = expectedModCount;
    }

    static <T> Spliterator<T> spliterator(AbstractList<T> list) {
        return new RAAbstractListSpliterator<T>(list, 0, -1, 0);
    }

    private int getFence() { // initialize fence to size on first use
        int hi;
        if ((hi = fence) < 0) {
            expectedModCount = getModCount(list);
            hi = fence = list.size();
        }
        return hi;
    }

    @Override
    public Spliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new RAAbstractListSpliterator<>(list, lo, index = mid,
                        expectedModCount);
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int hi = getFence(), i = index;
        if (i < hi) {
            index = i + 1;
            action.accept(list.get(i));
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
        int hi = getFence(), i = index;
        List<E> lst = list;
        if (i < hi) {
            if ((index = hi) > lst.size()) {
                throw new ConcurrentModificationException();
            }
            try {
                for (; i < hi; ++i) {
                    action.accept(lst.get(i));
                }
            } catch (IndexOutOfBoundsException e) {
                // action must have modified the list
                throw new ConcurrentModificationException();
            }
            if (expectedModCount != getModCount(lst)) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public long estimateSize() {
        return (long) (getFence() - index);
    }

    @Override
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
        return Spliterators.getComparator(this);
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
