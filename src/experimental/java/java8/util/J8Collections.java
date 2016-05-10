package java8.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class J8Collections {

    /**
     * Returns an iterator that has no elements. More precisely,
     *
     * <ul>
     * <li>{@link Iterator#hasNext hasNext} always returns {@code false}.</li>
     * <li>{@link Iterator#next next} always throws
     * {@link NoSuchElementException}.</li>
     * <li>{@link Iterator#remove remove} always throws
     * {@link IllegalStateException}.</li>
     * </ul>
     *
     * <p>
     * Implementations of this method are permitted, but not required, to return
     * the same object from multiple invocations.
     *
     * @param <T>
     *            type of elements, if there were any, in the iterator
     * @return an empty iterator
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EmptyIt.EMPTY_ITERATOR;
    }

    static <E> Iterator<E> singletonIterator(final E e) {
        return new ImmutableIt<E>() {
            private boolean hasNext = true;
            public boolean hasNext() {
                return hasNext;
            }
            public E next() {
                if (hasNext) {
                    hasNext = false;
                    return e;
                }
                throw new NoSuchElementException();
            }
        };
    }

    static final class EmptyIt<E> extends ImmutableIt<E> {
        static final EmptyIt<Object> EMPTY_ITERATOR = new EmptyIt<Object>();

        public boolean hasNext() {
            return false;
        }

        public E next() {
            throw new NoSuchElementException();
        }
    }

    static abstract class ImmutableIt<T> implements Iterator<T> {
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private J8Collections() {
    }
}
