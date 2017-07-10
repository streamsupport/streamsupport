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
// CVS rev. 1.128
    private final PriorityBlockingQueue<E> queue;
    private Object[] array;        // null until late-bound-initialized
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
        if (array == null)
            fence = (array = queue.toArray()).length;
        return fence;
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
        int hi = getFence(), lo = index;
        Object[] a = array;
        index = hi;                 // ensure exhaustion
        for (int i = lo; i < hi; i++)
            action.accept((E) a[i]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        if (getFence() > index && index >= 0) {
            action.accept((E) array[index++]);
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
}
