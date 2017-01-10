/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java8.lang;

import java.util.Collection;
import java.util.Iterator;

import java8.util.Objects;
import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

/**
 * A place for static default implementations of the new Java 8
 * default interface methods and static interface methods in the
 * {@link Iterable} interface (and also for {@code removeIf}
 * from {@link Collection}).
 */
public final class Iterables {
    /**
     * Performs the given action for each element of the passed {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * <p><b>Implementation Requirements:</b><br>
     * <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     *
     * @param <T> the type of elements of the Iterable
     * @param it the Iterable to call {@code forEach} on
     * @param action The action to be performed for each element
     * @throws NullPointerException if one of the specified {@code it} or
     *         {@code action} arguments is null
     * @since 1.8
     */
    public static <T> void forEach(Iterable<? extends T> it, Consumer<? super T> action) {
        Objects.requireNonNull(it);
        Objects.requireNonNull(action);
        for (T t : it) {
            action.accept(t);
        }
    }

    /**
     * Removes all of the elements of the passed {@code Iterable} that satisfy
     * the given predicate.  Errors or runtime exceptions thrown during iteration
     * or by the predicate are relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation traverses all elements of the {@code Iterable}
     * using its {@link Iterable#iterator}.  Each matching element is removed
     * using {@link Iterator#remove()}.  If the Iterable's iterator does not
     * support removal then an {@code UnsupportedOperationException} will be
     * thrown on the first matching element.
     *
     * @param <T> the type of elements of the Iterable
     * @param it the Iterable to call {@code removeIf} on
     * @param filter a predicate which returns {@code true} for elements to be
     *        removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException if one of the specified {@code it} or
     *         {@code filter} arguments is null
     * @throws UnsupportedOperationException if elements cannot be removed
     *         from the passed Iterable.  Implementations may throw this exception
     *         if a matching element cannot be removed or if, in general, removal
     *         is not supported.
     */
    public static <T> boolean removeIf(Iterable<? extends T> it, Predicate<? super T> filter) {
        Objects.requireNonNull(it);
        Objects.requireNonNull(filter);
        boolean removed = false;
        @SuppressWarnings("unchecked")
        Iterator<T> each = (Iterator<T>) it.iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is an instance of {@link Collection}
     * the implementation delegates to
     * {@link Spliterators#spliterator(java.util.Collection)} so it is
     * effectively the same as calling
     * <pre>{@code
     *     Spliterators.spliterator((Collection<T>) it);
     * }</pre>
     *
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is <b>not</b> an instance of
     * {@link Collection} this implementation creates an
     * <em><a href="../util/Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * The spliterator returned for {@code non-Collection} sources has poor
     * splitting capabilities, is unsized, and does not report any spliterator
     * characteristics.  Implementing classes could nearly always provide
     * a better implementation.
     *
     * @param <T> the type of elements of the Iterable.
     * @param it the Iterable for which the Spliterator should be created. 
     * @return a {@code Spliterator} over the elements described by the
     * passed {@code Iterable}.
     * @since 1.8
     */
    public static <T> Spliterator<T> spliterator(Iterable<? extends T> it) {
        if (it instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<T> c = (Collection<T>) it;
            return Spliterators.spliterator(c);
        }
        return Spliterators.spliteratorUnknownSize(it.iterator(), 0);
    }

    /**
     * Returns a sequential {@link Stream} with the passed {@code Iterable}
     * as its source.
     *
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is an instance of {@link Collection}
     * the implementation delegates to
     * {@link StreamSupport#stream(java.util.Collection)} so it is
     * effectively the same as calling
     * <pre>{@code
     *     StreamSupport.stream((Collection<T>) it);
     * }</pre>
     * 
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is <b>not</b> an instance of
     * {@link Collection} this implementation creates a stream
     * using a {@link Supplier} of the Iterable's spliterator obtained
     * via {@link #spliterator(Iterable)}, as in:
     * <pre>{@code
     *     Stream<T> s = StreamSupport.stream(() -> spliterator(it), spliteratorCharacteristics, false)
     * }</pre>
     * where the supplied spliterator characteristics are taken from
     * the spliterator's {@link Spliterator#characteristics()} method.
     *
     * @param <T> the type of elements of the Iterable.
     * @param it the Iterable for which the Stream should be created.
     * @return a sequential {@code Stream} over the elements in the passed
     * {@code Iterable}.
     */
//    public static <T> Stream<T> stream(Iterable<? extends T> it) {
//        if (it instanceof Collection) {
//            @SuppressWarnings("unchecked")
//            Collection<T> c = (Collection<T>) it;
//            return StreamSupport.stream(c);
//        } else {
//            Spliterator<T> splitter = spliterator(it);
//            int characteristics = splitter.characteristics();
//            return StreamSupport.stream(new Supplier<Spliterator<T>>() {
//                @Override
//                public Spliterator<T> get() {
//                    return splitter;
//                }
//            }, characteristics, false);
//        }
//    }

    /**
     * Returns a possibly parallel {@link Stream} with the passed
     * {@code Iterable} as its source.  It is allowable for this
     * method to return a sequential stream.
     *
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is an instance of {@link Collection}
     * the implementation delegates to
     * {@link StreamSupport#parallelStream(java.util.Collection)} so it is
     * effectively the same as calling
     * <pre>{@code
     *     StreamSupport.parallelStream((Collection<T>) it);
     * }</pre>
     * 
     * <p><b>Implementation Note:</b>
     * If the passed {@code Iterable} is <b>not</b> an instance of
     * {@link Collection} this implementation creates a stream
     * using a {@link Supplier} of the Iterable's spliterator obtained
     * via {@link #spliterator(Iterable)}, as in:
     * <pre>{@code
     *     Stream<T> s = StreamSupport.stream(() -> spliterator(it), spliteratorCharacteristics, true)
     * }</pre>
     * where the supplied spliterator characteristics are taken from
     * the spliterator's {@link Spliterator#characteristics()} method.
     * 
     * @param <T> the type of elements of the Iterable.
     * @param it the Iterable for which the Stream should be created.
     * @return a possibly parallel {@code Stream} over the elements in the
     * passed {@code Iterable}.
     */
//    public static <T> Stream<T> parallelStream(Iterable<? extends T> it) {
//        if (it instanceof Collection) {
//            @SuppressWarnings("unchecked")
//            Collection<T> c = (Collection<T>) it;
//            return StreamSupport.parallelStream(c);
//        } else {
//            Spliterator<T> splitter = spliterator(it);
//            int characteristics = splitter.characteristics();
//            return StreamSupport.stream(new Supplier<Spliterator<T>>() {
//                @Override
//                public Spliterator<T> get() {
//                    return splitter;
//                }
//            }, characteristics, true);
//        }
//    }

    private Iterables() {
    }
}
