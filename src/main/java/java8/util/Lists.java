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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import java8.util.function.UnaryOperator;

/**
 * A place for static default implementations of the new Java 8
 * default interface methods and static interface methods in the
 * {@link List} interface. 
 */
public final class Lists {
    /**
     * Sorts the passed list using the supplied {@code Comparator} to compare
     * elements.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the passed {@code list}:
     * <pre>Collections.sort(list, c)</pre>
     *
     * @param <E> the type of the elements of the list to be sorted
     * @param list the list that should be sorted
     * @param c the {@code Comparator} used to compare list elements.
     *          A {@code null} value indicates that the elements'
     *          {@linkplain Comparable natural ordering} should be used
     * @throws ClassCastException if the list contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator
     * @throws UnsupportedOperationException if the list's list-iterator does
     *         not support the {@code set} operation
     * @throws IllegalArgumentException (optional)
     *         if the comparator is found to violate the {@link Comparator}
     *         contract
     * @throws NullPointerException if the specified list is null
     * @since 1.8
     */
    public static <E> void sort(List<E> list, Comparator<? super E> c) {
        Collections.sort(list, c);
    }

    /**
     * Replaces each element of the passed list with the result of applying the
     * operator to that element.  Errors or runtime exceptions thrown by
     * the operator are relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the passed {@code list}:
     * <pre>{@code
     *     final ListIterator<E> li = list.listIterator();
     *     while (li.hasNext()) {
     *         li.set(operator.apply(li.next()));
     *     }
     * }</pre>
     *
     * If the list's list-iterator does not support the {@code set} operation
     * then an {@code UnsupportedOperationException} will be thrown when
     * replacing the first element.
     *
     * @param <E> the type of the elements of the list to be replaced
     * @param list the list whose elements should be replaced
     * @param operator the operator to apply to each element
     * @throws UnsupportedOperationException if the passed list is unmodifiable.
     *         Implementations may throw this exception if an element
     *         cannot be replaced or if, in general, modification is not
     *         supported
     * @throws NullPointerException if the specified list is null or the specified
     *         operator is null or if the operator result is a null value and the
     *         passed list does not permit null elements (optional)
     * @since 1.8
     */
    public static <E> void replaceAll(List<E> list, UnaryOperator<E> operator) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(operator);
        ListIterator<E> li = list.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    /**
     * Creates a {@link Spliterator} over the elements in the passed list.
     *
     * <p>The {@code Spliterator} reports at least {@link Spliterator#SIZED},
     * {@link Spliterator#ORDERED} and {@link Spliterator#SUBSIZED}.
     *
     * <p><b>Implementation Note:</b>
     * This implementation delegates to {@link Spliterators#spliterator(java.util.Collection)}
     * so it is effectively the same as calling
     * <pre>{@code
     *     Spliterators.spliterator(list);
     * }</pre> 
     *
     * @param <E> the type of the elements of the list to be splitted
     * @param list the list to be splitted
     * @return a {@code Spliterator} over the elements in the passed list
     * @since 1.8
     */
    public static <E> Spliterator<E> spliterator(List<E> list) {
        return Spliterators.spliterator(list);
    }

    private Lists() {
    }
}
