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

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Consumer;

final class ArrayDequeSpliterator<E> implements Spliterator<E> {
// CVS rev. 1.74
    private final ArrayDeque<E> deq;
    private int fence;  // -1 until first use
    private int index;  // current index, modified on traverse/split

    /** Constructs a new spliterator covering the given array and range. */
    private ArrayDequeSpliterator(ArrayDeque<E> deq, int origin, int fence) {
        this.deq = deq;
        this.index = origin;
        this.fence = fence;
    }

    static <T> Spliterator<T> spliterator(ArrayDeque<T> deque) {
        return new ArrayDequeSpliterator<T>(deque, -1, -1);
    }

    /** Ensures late-binding initialization; then returns fence. */
    private int getFence() { // force initialization
        int t;
        if ((t = fence) < 0) {
            t = fence = getTail(deq);
            index = getHead(deq);
        }
        return t;
    }

    public ArrayDequeSpliterator<E> trySplit() {
        int t = getFence(), h = index, n = getData(deq).length;
        if (h != t && ((h + 1) & (n - 1)) != t) {
            if (h > t) {
                t += n;
            }
            int m = ((h + t) >>> 1) & (n - 1);
            return new ArrayDequeSpliterator<E>(deq, h, index = m);
        }
        return null;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] a = getData(deq);
        int m = a.length - 1, f = getFence(), i = index;
        index = f;
        while (i != f) {
            @SuppressWarnings("unchecked") E e = (E) a[i];
            i = (i + 1) & m;
            if (e == null) {
                throw new ConcurrentModificationException();
            }
            action.accept(e);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] a = getData(deq);
        int m = a.length - 1, f = getFence(), i = index;
        if (i != fence) {
            @SuppressWarnings("unchecked") E e = (E) a[i];
            index = (i + 1) & m;
            if (e == null) {
                throw new ConcurrentModificationException();
            }
            action.accept(e);
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        int n = getFence() - index;
        if (n < 0) {
            n += getData(deq).length;
        }
        return (long) n;
    }

    @Override
    public int characteristics() {
        return Spliterator.NONNULL
            | Spliterator.ORDERED
            | Spliterator.SIZED
            | Spliterator.SUBSIZED;
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

    private static <T> int getTail(ArrayDeque<T> deq) {
        return U.getInt(deq, TAIL_OFF);
    }

    private static <T> int getHead(ArrayDeque<T> deq) {
        return U.getInt(deq, HEAD_OFF);
    }

    private static <T> Object[] getData(ArrayDeque<T> deq) {
        return (Object[]) U.getObject(deq, DATA_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long TAIL_OFF;
    private static final long HEAD_OFF;
    private static final long DATA_OFF;
    static {
        try {
            TAIL_OFF = U.objectFieldOffset(ArrayDeque.class
                    .getDeclaredField("tail"));
            HEAD_OFF = U.objectFieldOffset(ArrayDeque.class
                    .getDeclaredField("head"));
            DATA_OFF = U.objectFieldOffset(ArrayDeque.class
                    .getDeclaredField("elements"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
