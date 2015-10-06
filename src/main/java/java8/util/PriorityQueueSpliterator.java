/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.PriorityQueue;

import java8.util.Spliterator;
import java8.util.function.Consumer;

final class PriorityQueueSpliterator<E> implements Spliterator<E> {
    /*
     * This is very similar to ArrayList Spliterator, except for
     * extra null checks.
     */
    private final PriorityQueue<E> pq;
    private int index;            // current index, modified on advance/split
    private int fence;            // -1 until first use
    private int expectedModCount; // initialized when fence set

    /** Creates new spliterator covering the given range */
    private PriorityQueueSpliterator(PriorityQueue<E> pq, int origin, int fence,
                         int expectedModCount) {
        this.pq = pq;
        this.index = origin;
        this.fence = fence;
        this.expectedModCount = expectedModCount;
    }

    static <T> Spliterator<T> spliterator(PriorityQueue<T> pq) {
        return new PriorityQueueSpliterator<T>(pq, 0, -1, 0);
    }

    private int getFence() { // initialize fence to size on first use
        int hi;
        if ((hi = fence) < 0) {
            expectedModCount = getModCount(pq);
            hi = fence = getSize(pq);
        }
        return hi;
    }

    @Override
    public PriorityQueueSpliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null :
            new PriorityQueueSpliterator<E>(pq, lo, index = mid,
                                            expectedModCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int i, hi, mc; // hoist accesses and checks from loop
        PriorityQueue<E> q;
        Object[] a;
        if ((q = pq) != null && (a = getQueue(q)) != null) {
            if ((hi = fence) < 0) {
                mc = getModCount(q);
                hi = getSize(q);
            } else {
                mc = expectedModCount;
            }
            if ((i = index) >= 0 && (index = hi) <= a.length) {
                for (E e;; ++i) {
                    if (i < hi) {
                        if ((e = (E) a[i]) == null) { // must be CME
                            break;
                        }
                        action.accept(e);
                    } else if (mc != getModCount(q)) {
                        break;
                    } else {
                        return;
                    }
                }
            }
        }
        throw new ConcurrentModificationException();
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int hi = getFence(), lo = index;
        if (lo >= 0 && lo < hi) {
            index = lo + 1;
            @SuppressWarnings("unchecked") E e = (E) getQueue(pq)[lo];
            if (e == null) {
                throw new ConcurrentModificationException();
            }
            action.accept(e);
            if (expectedModCount != getModCount(pq)) {
                throw new ConcurrentModificationException();
            }
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return (long) (getFence() - index);
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
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

    private static <T> int getSize(PriorityQueue<T> pq) {
        return U.getInt(pq, SIZE_OFF);
    }

    private static <T> int getModCount(PriorityQueue<T> pq) {
        if (IS_ANDROID) {
            return 0;
        }
        return U.getInt(pq, MODCOUNT_OFF);
    }

    private static <T> Object[] getQueue(PriorityQueue<T> pq) {
        return (Object[]) U.getObject(pq, QUEUE_OFF);
    }

    // Unsafe mechanics
    private static final boolean IS_ANDROID = Spliterators.IS_ANDROID;
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long SIZE_OFF;
    private static final long MODCOUNT_OFF;
    private static final long QUEUE_OFF;
    static {
        try {
            SIZE_OFF = U.objectFieldOffset(PriorityQueue.class
                    .getDeclaredField("size"));
            if (!IS_ANDROID) {
                MODCOUNT_OFF = U.objectFieldOffset(PriorityQueue.class
                        .getDeclaredField("modCount"));
            } else {
                MODCOUNT_OFF = 0L; // unused
            }
            String queueFieldName = IS_ANDROID ? "elements" : "queue";
            QUEUE_OFF = U.objectFieldOffset(PriorityQueue.class
                    .getDeclaredField(queueFieldName));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
