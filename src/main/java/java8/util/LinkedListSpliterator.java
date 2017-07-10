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

import java.util.AbstractList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import java8.util.function.Consumer;

/**
 * A customized variant of Spliterators.IteratorSpliterator for
 * {@code LinkedList}s
 */
final class LinkedListSpliterator<T> implements Spliterator<T> {

    private static final int BATCH_UNIT = 1 << 10; // batch array size increment
    private static final int MAX_BATCH = 1 << 25; // max batch array size
    private final LinkedList<T> list; // null OK unless traversed
    private final Object endOfList; // end marker in list nodes
    private Object current; // current node; null until initialized
    private int est; // size estimate; -1 until first needed
    private int expectedModCount; // initialized when est is set
    private int batch; // batch size for splits

    private LinkedListSpliterator(LinkedList<T> list, int est,
            int expectedModCount) {
        this.list = list;
        this.est = est;
        this.expectedModCount = expectedModCount;
        this.endOfList = (IS_JAVA6 || IS_HARMONY) ? getHeader(list) : null;
    }

    static <E> Spliterator<E> spliterator(LinkedList<E> list) {
        return new LinkedListSpliterator<E>(list, -1, 0);
    }

    private int getEst() {
        int s; // force initialization
        LinkedList<T> lst;
        if ((s = est) < 0) {
            if ((lst = list) == null) {
                s = est = 0;
            } else {
                expectedModCount = getModCount(lst);
                current = getFirst(lst);
                s = est = getSize(lst);
            }
        }
        return s;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public long estimateSize() {
        return (long) getEst();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        Object eol = endOfList;
        Object p;
        int n;
        if ((n = getEst()) > 0 && (p = current) != eol) {
            current = eol;
            est = 0;
            do {
                T item = getNodeItem(p);
                p = getNextNode(p);
                action.accept(item);
            } while (p != eol && --n > 0);
        }
        if (expectedModCount != getModCount(list)) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public Comparator<? super T> getComparator() {
        return Spliterators.getComparator(null);
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
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        Object eol = endOfList;
        Object p;
        if (getEst() > 0 && (p = current) != eol) {
            --est;
            T item = getNodeItem(p);
            current = getNextNode(p);
            action.accept(item);
            if (expectedModCount != getModCount(list)) {
                throw new ConcurrentModificationException();
            }
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        Object eol = endOfList;
        Object p;
        int s = getEst();
        if (s > 1 && (p = current) != eol) {
            int n = batch + BATCH_UNIT;
            if (n > s) {
                n = s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            Object[] a = new Object[n];
            int j = 0;
            do {
                a[j++] = getNodeItem(p);
            } while ((p = getNextNode(p)) != eol && j < n);
            current = p;
            batch = j;
            est = s - j;
            return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
        }
        return null;
    }

    private static Object getHeader(LinkedList<?> list) {
        if (list == null) {
            return null;
        }
        return U.getObject(list, FIRST_OFF);
    }

    private Object getFirst(LinkedList<?> list) {
        if (IS_JAVA6 || IS_HARMONY) {
            // endOfList is the 'header'/'voidLink' member
            return getNextNode(endOfList);
        }
        // Java 7 & Java 8
        return U.getObject(list, FIRST_OFF);
    }

    private static Object getNextNode(Object node) {
        if (node == null) {
            throw new ConcurrentModificationException();
        }
        return U.getObject(node, NODE_NEXT_OFF);
    }

    private static <E> E getNodeItem(Object node) {
        if (node == null) {
            throw new ConcurrentModificationException();
        }
        return (E) U.getObject(node, NODE_ITEM_OFF);
    }

    private static int getSize(LinkedList<?> list) {
        return U.getInt(list, SIZE_OFF);
    }

    private static int getModCount(LinkedList<?> list) {
        return U.getInt(list, MODCOUNT_OFF);
    }

    // Unsafe mechanics
    private static final boolean IS_HARMONY = Spliterators.IS_HARMONY_ANDROID;
    private static final boolean IS_JAVA6 = Spliterators.IS_JAVA6;
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long SIZE_OFF;
    private static final long MODCOUNT_OFF;
    private static final long FIRST_OFF;
    private static final long NODE_ITEM_OFF;
    private static final long NODE_NEXT_OFF;
    static {
        try {
            MODCOUNT_OFF = U.objectFieldOffset(AbstractList.class
                    .getDeclaredField("modCount"));
            String firstFieldName = IS_HARMONY ? "voidLink"
                    : IS_JAVA6 ? "header" : "first";
            String nodeClassName = IS_HARMONY ? "java.util.LinkedList$Link"
                    : IS_JAVA6 ? "java.util.LinkedList$Entry"
                            : "java.util.LinkedList$Node";
            String nodeItemName = IS_HARMONY ? "data" : IS_JAVA6 ? "element"
                    : "item";
            Class<?> nc = Class.forName(nodeClassName);
            SIZE_OFF = U.objectFieldOffset(LinkedList.class
                    .getDeclaredField("size"));
            FIRST_OFF = U.objectFieldOffset(LinkedList.class
                    .getDeclaredField(firstFieldName));
            NODE_ITEM_OFF = U.objectFieldOffset(nc
                    .getDeclaredField(nodeItemName));
            NODE_NEXT_OFF = U.objectFieldOffset(nc
                    .getDeclaredField("next"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
