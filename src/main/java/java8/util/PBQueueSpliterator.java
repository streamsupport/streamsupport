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
import java.util.concurrent.PriorityBlockingQueue;

import java8.util.function.Consumer;
import java8.util.Spliterator;

/*
 * Spliterator for java.util.concurrent.PriorityBlockingQueue.
 * Immutable snapshot spliterator that binds to elements "late".
 */
final class PBQueueSpliterator<E> implements Spliterator<E> {
// CVS rev. 1.117
    private final PriorityBlockingQueue<E> queue;
    private Object[] array;
    private int index;
    private int fence;

    private PBQueueSpliterator(PriorityBlockingQueue<E> queue,
            Object[] array, int index, int fence) {
        this.queue = queue;
        this.array = array;
        this.index = index;
        this.fence = fence;
    }

    static <T> Spliterator<T> spliterator(PriorityBlockingQueue<T> queue) {
        return new PBQueueSpliterator<T>(queue, null, 0, -1);
    }

    private int getFence() {
        int hi;
        if ((hi = fence) < 0) {
            hi = fence = (array = queue.toArray()).length;
        }
        return hi;
    }

    @Override
    public Spliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : new PBQueueSpliterator<E>(
                queue, array, lo, index = mid);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] a;
        int i, hi; // hoist accesses and checks from loop
        if ((a = array) == null) {
            fence = (a = queue.toArray()).length;
        }
        if ((hi = fence) <= a.length && (i = index) >= 0 && i < (index = hi)) {
            do {
                action.accept((E) a[i]);
            } while (++i < hi);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        if (getFence() > index && index >= 0) {
            @SuppressWarnings("unchecked")
            E e = (E) array[index++];
            action.accept(e);
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
        return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
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
}
