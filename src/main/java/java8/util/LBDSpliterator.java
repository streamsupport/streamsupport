/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util;

import java.util.Comparator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

import java8.util.function.Consumer;

/**
 * A customized variant of Spliterators.IteratorSpliterator for
 * LinkedBlockingDeques.
 * <p>
 * The returned spliterator is <i>weakly consistent</i>.
 * <p>
 * The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
 * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
 * <p>
 * The {@code Spliterator} implements {@code trySplit} to permit limited
 * parallelism.
 * 
 * @param <E>
 *            the type of elements held in the LinkedBlockingDeque
 */
final class LBDSpliterator<E> implements Spliterator<E> {
// CVS rev. 1.65
    private static final int MAX_BATCH = 1 << 25; // max batch array size
    private final LinkedBlockingDeque<E> queue;
    private final ReentrantLock queueLock;
    private Object current; // current node; null until initialized
    private int batch; // batch size for splits
    private boolean exhausted; // true when no more nodes
    private long est; // size estimate

    private LBDSpliterator(LinkedBlockingDeque<E> queue) {
        this.queue = queue;
        this.est = queue.size();
        this.queueLock = getQueueLock(queue);
    }

    static <T> Spliterator<T> spliterator(LinkedBlockingDeque<T> queue) {
        return new LBDSpliterator<T>(queue);
    }

    @Override
    public int characteristics() {
        return (Spliterator.ORDERED |
                Spliterator.NONNULL |
                Spliterator.CONCURRENT);
    }

    @Override
    public long estimateSize() {
        return est;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        if (exhausted)
            return;
        exhausted = true;
        LinkedBlockingDeque<E> q = queue;
        ReentrantLock lock = queueLock;
        Object p = current;
        current = null;
        do {
            E e = null;
            lock.lock();
            try {
                if ((p != null && p != getNextNode(p)) || (p = getQueueFirst(q)) != null) {
                    e = getNodeItem(p);
                    p = getNextNode(p);
                }
            } finally {
                lock.unlock();
            }
            if (e != null)
                action.accept(e);
        } while (p != null);
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
        if (exhausted)
            return false;
        LinkedBlockingDeque<E> q = queue;
        ReentrantLock lock = queueLock;
        Object p = current;
        E e = null;
        lock.lock();
        try {
            if ((p != null && p != getNextNode(p)) || (p = getQueueFirst(q)) != null) {
                e = getNodeItem(p);
                p = getNextNode(p);
            }
        } finally {
            lock.unlock();
        }
        exhausted = ((current = p) == null);
        if (e == null)
            return false;
        action.accept(e);
        return true;
    }

    @Override
    public Spliterator<E> trySplit() {
        Object h;
        LinkedBlockingDeque<E> q = queue;
        int b = batch;
        int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
        if (!exhausted &&
            (((h = current) != null && h != getNextNode(h))
             || (h = getQueueFirst(q)) != null)
            && getNextNode(h) != null) {
            Object[] a = new Object[n];
            ReentrantLock lock = queueLock;
            int i = 0;
            Object p = current;
            lock.lock();
            try {
                if (((p != null && p != getNextNode(p)) || (p = getQueueFirst(q)) != null)
                    && getNodeItem(p) != null)
                    for (; p != null && i < n; p = getNextNode(p))
                        a[i++] = getNodeItem(p);
            } finally {
                lock.unlock();
            }
            if ((current = p) == null) {
                est = 0L;
                exhausted = true;
            }
            else if ((est -= i) < 0L)
                est = 0L;
            if (i > 0) {
                batch = i;
                return Spliterators.spliterator
                    (a, 0, i, (Spliterator.ORDERED |
                               Spliterator.NONNULL |
                               Spliterator.CONCURRENT));
            }
        }
        return null;
    }

    private static ReentrantLock getQueueLock(LinkedBlockingDeque<?> queue) {
        return (ReentrantLock) U.getObject(queue, LOCK_OFF);
    }

    private static Object getQueueFirst(LinkedBlockingDeque<?> queue) {
        return U.getObject(queue, FIRST_OFF);
    }

    /**
     * Returns node.next as an Object
     */
    private static Object getNextNode(Object node) {
        return U.getObject(node, NODE_NEXT_OFF);
    }

    /**
     * Returns node.item as a T
     */
    private static <T> T getNodeItem(Object node) {
        return (T) U.getObject(node, NODE_ITEM_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long FIRST_OFF;
    private static final long LOCK_OFF;
    private static final long NODE_ITEM_OFF;
    private static final long NODE_NEXT_OFF;
    static {
        try {
            Class<?> nc = Class
                    .forName("java.util.concurrent.LinkedBlockingDeque$Node");
            FIRST_OFF = U.objectFieldOffset(LinkedBlockingDeque.class
                    .getDeclaredField("first"));
            LOCK_OFF = U.objectFieldOffset(LinkedBlockingDeque.class
                    .getDeclaredField("lock"));
            NODE_ITEM_OFF = U.objectFieldOffset(nc
                    .getDeclaredField("item"));
            NODE_NEXT_OFF = U.objectFieldOffset(nc
                    .getDeclaredField("next"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
