/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
