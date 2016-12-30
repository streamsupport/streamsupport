/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
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
// CVS rev. 1.124
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
    public PBQueueSpliterator<E> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null :
            new PBQueueSpliterator<E>(queue, array, lo, index = mid);
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
        if ((hi = fence) <= a.length &&
            (i = index) >= 0 && i < (index = hi)) {
            do { action.accept((E) a[i]); } while (++i < hi);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        if (getFence() > index && index >= 0) {
            @SuppressWarnings("unchecked") E e = (E) array[index++];
            action.accept(e);
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() { return getFence() - index; }

    @Override
    public int characteristics() {
        return (Spliterator.NONNULL |
                Spliterator.SIZED |
                Spliterator.SUBSIZED);
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
