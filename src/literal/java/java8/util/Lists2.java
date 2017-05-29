/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;

/**
 * A place for the new Java 9 <a href="http://openjdk.java.net/jeps/269">JEP
 * 269</a> {@code "Immutable List Static Factory Methods"} in the {@link List}
 * interface.
 * 
 * <h2><a id="immutable">Immutable List Static Factory Methods</a></h2>
 * <p>
 * The {@link Lists2#of(Object...) Lists.of()} static factory methods provide a
 * convenient way to create immutable lists. The {@code List} instances created
 * by these methods have the following characteristics:
 *
 * <ul>
 * <li>They are <em>structurally immutable</em>. Elements cannot be added,
 * removed, or replaced. Calling any mutator method will always cause
 * {@code UnsupportedOperationException} to be thrown.
 * However, if the contained elements are themselves mutable,
 * this may cause the List's contents to appear to change.
 * <li>They disallow {@code null} elements. Attempts to create them with
 * {@code null} elements result in {@code NullPointerException}.
 * <li>They are serializable if all elements are serializable.
 * <li>The order of elements in the list is the same as the order of the
 * provided arguments, or of the elements in the provided array.
 * <li>They are <a
 * href="package-summary.html#Value-based-Classes">value-based</a>. Callers
 * should make no assumptions about the identity of the returned instances.
 * Factories are free to create new instances or reuse existing ones. Therefore,
 * identity-sensitive operations on these instances (reference equality (
 * {@code ==}), identity hash code, and synchronization) are unreliable and
 * should be avoided.
 * </ul>
 */
public final class Lists2 {
    /**
     * Returns an immutable list containing zero elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @return an empty {@code List}
     *
     * @since 9
     */
    public static <E> List<E> of() {
        return ImmutableCollections.List0.instance();
    }

    /**
     * Returns an immutable list containing one element.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the single element
     * @return a {@code List} containing the specified element
     * @throws NullPointerException if the element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1) {
        return new ImmutableCollections.List1<E>(e1);
    }

    /**
     * Returns an immutable list containing two elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2) {
        return new ImmutableCollections.List2<E>(e1, e2);
    }

    /**
     * Returns an immutable list containing three elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3);
    }

    /**
     * Returns an immutable list containing four elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4);
    }

    /**
     * Returns an immutable list containing five elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5);
    }

    /**
     * Returns an immutable list containing six elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @param e6 the sixth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5,
                                                 e6);
    }

    /**
     * Returns an immutable list containing seven elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @param e6 the sixth element
     * @param e7 the seventh element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5,
                                                 e6, e7);
    }

    /**
     * Returns an immutable list containing eight elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @param e6 the sixth element
     * @param e7 the seventh element
     * @param e8 the eighth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5,
                                                 e6, e7, e8);
    }

    /**
     * Returns an immutable list containing nine elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @param e6 the sixth element
     * @param e7 the seventh element
     * @param e8 the eighth element
     * @param e9 the ninth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5,
                                                 e6, e7, e8, e9);
    }

    /**
     * Returns an immutable list containing ten elements.
     *
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * @param <E> the {@code List}'s element type
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @param e6 the sixth element
     * @param e7 the seventh element
     * @param e8 the eighth element
     * @param e9 the ninth element
     * @param e10 the tenth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return new ImmutableCollections.ListN<E>(e1, e2, e3, e4, e5,
                                                 e6, e7, e8, e9, e10);
    }

    /**
     * Returns an immutable list containing an arbitrary number of elements.
     * See <a href="#immutable">Immutable List Static Factory Methods</a> for details.
     *
     * <p><b>API Note:</b><br>
     * This method also accepts a single array as an argument. The element type of
     * the resulting list will be the component type of the array, and the size of
     * the list will be equal to the length of the array. To create a list with
     * a single element that is an array, do the following:
     *
     * <pre>{@code
     *     String[] array = ... ;
     *     List<String[]> list = Lists.<String[]>of(array);
     * }</pre>
     *
     * This will cause the {@link Lists2#of(Object) Lists.of(E)} method
     * to be invoked instead.
     *
     * @param <E> the {@code List}'s element type
     * @param elements the elements to be contained in the list
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null} or if the array is {@code null}
     *
     * @since 9
     */
    public static <E> List<E> of(E... elements) {
        return ImmutableCollections.listOf(elements);
    }

    private Lists2() {
    }
}
