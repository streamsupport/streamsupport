/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import java8.util.function.Consumer;

/**
 * A customized variant of Spliterators.IteratorSpliterator for
 * LinkedBlockingQueues.
 * <p>
 * The returned spliterator is <i>weakly consistent</i>.
 * <p>
 * The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
 * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
 * <p>
 * The {@code Spliterator} implements {@code trySplit} to permit limited
 * parallelism.
 * 
 * @param <E> the type of elements held in the LinkedBlockingQueue
 */
final class LBQSpliterator<E> implements Spliterator<E> {

    private static final int MAX_BATCH = 1 << 25; // max batch array size
    private final LinkedBlockingQueue<E> queue;
    private final ReentrantLock putLock;
    private final ReentrantLock takeLock;
    private Object current; // current node; null until initialized
    private int batch; // batch size for splits
    private boolean exhausted; // true when no more nodes
    private long est; // size estimate

    private LBQSpliterator(LinkedBlockingQueue<E> queue) {
        this.queue = queue;
        this.est = queue.size();
        this.putLock = getPutLock(queue);
        this.takeLock = getTakeLock(queue);
    }

    static <T> Spliterator<T> spliterator(LinkedBlockingQueue<T> queue) {
        return new LBQSpliterator<T>(queue);
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL
                | Spliterator.CONCURRENT;
    }

    @Override
    public long estimateSize() {
        return est;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        LinkedBlockingQueue<E> q = this.queue;
        if (!exhausted) {
            exhausted = true;
            Object p = current;
            do {
                E e = null;
                fullyLock();
                try {
                    if (p == null) {
                        p = getHeadNext(q);
                    }
                    while (p != null) {
                        e = getNodeItem(p);
                        p = getNextNode(p);
                        if (e != null) {
                            break;
                        }
                    }
                } finally {
                    fullyUnlock();
                }
                if (e != null) {
                    action.accept(e);
                }
            } while (p != null);
        }
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

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        LinkedBlockingQueue<E> q = this.queue;
        if (!exhausted) {
            E e = null;
            fullyLock();
            try {
                if (current == null) {
                    current = getHeadNext(q);
                }
                while (current != null) {
                    e = getNodeItem(current);
                    current = getNextNode(current);
                    if (e != null) {
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
            if (current == null) {
                exhausted = true;
            }
            if (e != null) {
                action.accept(e);
                return true;
            }
        }
        return false;
    }

    @Override
    public Spliterator<E> trySplit() {
        Object h;
        final LinkedBlockingQueue<E> q = this.queue;
        int b = batch;
        int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
        if (!exhausted
                && ((h = current) != null || (h = getHeadNext(q)) != null)
                && getNextNode(h) != null) {
            Object[] a = new Object[n];
            int i = 0;
            Object p = current;
            fullyLock();
            try {
                if (p != null || (p = getHeadNext(q)) != null) {
                    do {
                        if ((a[i] = getNodeItem(p)) != null) {
                            ++i;
                        }
                    } while ((p = getNextNode(p)) != null && i < n);
                }
            } finally {
                fullyUnlock();
            }
            if ((current = p) == null) {
                est = 0L;
                exhausted = true;
            } else if ((est -= i) < 0L) {
                est = 0L;
            }
            if (i > 0) {
                batch = i;
                return Spliterators.spliterator(a, 0, i, Spliterator.ORDERED
                        | Spliterator.NONNULL | Spliterator.CONCURRENT);
            }
        }
        return null;
    }

    /**
     * Lock to prevent both puts and takes.
     */
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes.
     */
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    private static ReentrantLock getPutLock(LinkedBlockingQueue<?> queue) {
        return (ReentrantLock) UNSAFE.getObject(queue, PUT_LOCK_OFF);
    }

    private static ReentrantLock getTakeLock(LinkedBlockingQueue<?> queue) {
        return (ReentrantLock) UNSAFE.getObject(queue, TAKE_LOCK_OFF);
    }

    /**
     * Returns queue.head.next as an Object
     */
    private static <T> Object getHeadNext(LinkedBlockingQueue<T> queue) {
        return getNextNode(UNSAFE.getObject(queue, HEAD_OFF));
    }

    /**
     * Returns node.next as an Object
     */
    private static Object getNextNode(Object node) {
        return UNSAFE.getObject(node, NODE_NEXT_OFF);
    }

    /**
     * Returns node.item as a T
     */
    private static <T> T getNodeItem(Object node) {
        return (T) UNSAFE.getObject(node, NODE_ITEM_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long HEAD_OFF;
    private static final long NODE_ITEM_OFF;
    private static final long NODE_NEXT_OFF;
    private static final long PUT_LOCK_OFF;
    private static final long TAKE_LOCK_OFF;
    static {
        try {
            UNSAFE = UnsafeAccess.unsafe;
            Class<?> lbqc = LinkedBlockingQueue.class;
            Class<?> nc = Class
                    .forName("java.util.concurrent.LinkedBlockingQueue$Node");
            HEAD_OFF = UNSAFE.objectFieldOffset(lbqc.getDeclaredField("head"));
            NODE_ITEM_OFF = UNSAFE.objectFieldOffset(nc
                    .getDeclaredField("item"));
            NODE_NEXT_OFF = UNSAFE.objectFieldOffset(nc
                    .getDeclaredField("next"));
            PUT_LOCK_OFF = UNSAFE.objectFieldOffset(lbqc.getDeclaredField("putLock"));
            TAKE_LOCK_OFF = UNSAFE.objectFieldOffset(lbqc.getDeclaredField("takeLock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
