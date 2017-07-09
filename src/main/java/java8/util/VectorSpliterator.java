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
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Vector;

import java8.util.Spliterator;
import java8.util.function.Consumer;

/** Similar to ArrayListSpliterator */
final class VectorSpliterator<E> implements Spliterator<E> {
// CVS rev. 1.46
    private final Vector<E> list;
    private Object[] array;
    private int index; // current index, modified on advance/split
    private int fence; // -1 until used; then one past last index
    private int expectedModCount; // initialized when fence set

    /** Create new spliterator covering the given range */
    private VectorSpliterator(Vector<E> list, Object[] array, int origin, int fence,
                      int expectedModCount) {
        this.list = list;
        this.array = array;
        this.index = origin;
        this.fence = fence;
        this.expectedModCount = expectedModCount;
    }

    static <T> Spliterator<T> spliterator(Vector<T> vec) {
        return new VectorSpliterator<T>(vec, null, 0, -1, 0);
    }

    private int getFence() { // initialize on first use
        int hi;
        if ((hi = fence) < 0) {
            synchronized (list) {
                array = getData(list);
                expectedModCount = getModCount(list);
                hi = fence = getSize(list);
            }
        }
        return hi;
    }

    @Override
    public Spliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null :
            new VectorSpliterator<E>(list, array, lo, index = mid,
                                     expectedModCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int i;
        if (getFence() > (i = index)) {
            index = i + 1;
            action.accept((E) array[i]);
            if (expectedModCount != getModCount(list)) {
                throw new ConcurrentModificationException();
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int hi = getFence();
        Object[] a = array;
        int i;
        for (i = index, index = hi; i < hi; i++) {
            action.accept((E) a[i]);
        }
        if (getModCount(list) != expectedModCount) {
            throw new ConcurrentModificationException();
        }
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
    	throw new IllegalStateException();
    }

    @Override
    public long getExactSizeIfKnown() {
        return Spliterators.getExactSizeIfKnown(this);
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return Spliterators.hasCharacteristics(this, characteristics);
    }

    private static <T> int getSize(Vector<T> lst) {
        return U.getInt(lst, SIZE_OFF);
    }

    private static <T> int getModCount(Vector<T> lst) {
        return U.getInt(lst, MODCOUNT_OFF);
    }

    private static <T> Object[] getData(Vector<T> lst) {
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
            SIZE_OFF = U.objectFieldOffset(Vector.class
                    .getDeclaredField("elementCount"));
            DATA_OFF = U.objectFieldOffset(Vector.class
                    .getDeclaredField("elementData"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
