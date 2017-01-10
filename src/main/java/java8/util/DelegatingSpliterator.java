/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util;

import java.util.Comparator;

import build.IgnoreJava8API;

import java8.util.function.Consumer;
import java8.util.function.Consumers;

/**
 * A j8.u.Spliterator implementation that delegates to a j.u.Spliterator.
 *
 * @param <T>
 *            the type of the input to the operation
 */
@IgnoreJava8API
final class DelegatingSpliterator<T> implements Spliterator<T> {

    private final java.util.Spliterator<T> spliter;

    DelegatingSpliterator(java.util.Spliterator<T> spliterator) {
        Objects.requireNonNull(spliterator);
        this.spliter = spliterator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return spliter.tryAdvance(new ConsumerDelegate<>(action));
    }

    @Override
    public Spliterator<T> trySplit() {
        java.util.Spliterator<T> spliterator = spliter.trySplit();
        if (spliterator == null) {
            return null;
        }
        return new DelegatingSpliterator<>(spliterator);
    }

    @Override
    public long estimateSize() {
        return spliter.estimateSize();
    }

    @Override
    public int characteristics() {
        return spliter.characteristics();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        spliter.forEachRemaining(new ConsumerDelegate<>(action));
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliter.getExactSizeIfKnown();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return spliter.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super T> getComparator() {
        return spliter.getComparator();
    }

    // This method is only used from the test suite
    // see https://sourceforge.net/p/streamsupport/tickets/240/
    Object getDelegatee() {
        return spliter;
    }

    /**
     * A j.u.f.Consumer implementation that delegates to a j8.u.f.Consumer.
     *
     * @param <T>
     *            the type of the input to the operation
     */
    private static final class ConsumerDelegate<T> implements
            java.util.function.Consumer<T> {

        private final Consumer<T> delegate;

        ConsumerDelegate(Consumer<T> delegate) {
            Objects.requireNonNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public void accept(T t) {
            delegate.accept(t);
        }

        @Override
        @IgnoreJava8API
        public java.util.function.Consumer<T> andThen(
                java.util.function.Consumer<? super T> after) {

            Objects.requireNonNull(after);

            return new ConsumerDelegate<T>(Consumers.andThen(delegate,
                    new java8.util.function.Consumer<T>() {
                        @Override
                        public void accept(T t) {
                            after.accept(t);
                        }
                    }));
        }
    }
}
