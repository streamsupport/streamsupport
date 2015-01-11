/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import java8.util.function.Consumer;
import java8.util.function.DoubleConsumer;
import java8.util.function.IntConsumer;
import java8.util.function.LongConsumer;

/**
 * Static classes and methods for operating on or creating instances of
 * {@link Spliterator} and its primitive specializations
 * {@link Spliterator.OfInt}, {@link Spliterator.OfLong}, and
 * {@link Spliterator.OfDouble}.
 *
 * @see Spliterator
 * @since 1.8
 */
public final class Spliterators {

	private static final String NATIVE_OPT_ENABLED_PROP = Spliterators.class.getName() + ".assume.oracle.collections.impl";
	private static final String JRE8_DELEGATION_ENABLED_PROP = Spliterators.class.getName() + ".jre8.delegation.enabled";

	// defaults to true
	static final boolean NATIVE_SPECIALIZATION = getBooleanPropertyValue(NATIVE_OPT_ENABLED_PROP);
	// defaults to true
	static final boolean JRE8_DELEGATION = getBooleanPropertyValue(JRE8_DELEGATION_ENABLED_PROP);
	// defaults to false
	static final boolean IS_ANDROID = isAndroid();

    // Suppresses default constructor, ensuring non-instantiability.
    private Spliterators() {}

    /**
     * Performs the given action for each remaining element, sequentially in
     * the current thread, until all elements have been processed or the action
     * throws an exception.  If the {@code this_} Spliterator is
     * {@link Spliterator#ORDERED}, actions are performed in encounter order.
     * Exceptions thrown by the action are relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation repeatedly invokes {@link Spliterator#tryAdvance} until
     * it returns {@code false}.  It should be overridden whenever possible.
     * 
     * @param <T> the type of elements returned by the passed Spliterator
     * @param this_ the Spliterator whose remaining elements should be processed
     * @param action The action
     * @throws NullPointerException if the specified action is null
     */
    public static <T> void forEachRemaining(Spliterator<T> this_, Consumer<? super T> action) {
        do { } while (this_.tryAdvance(action));
    }

    /**
     * Convenience method that returns {@link Spliterator#estimateSize()} if the
     * {@code this_} Spliterator is {@link Spliterator#SIZED}, else {@code -1}.
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation returns the result of {@code estimateSize()}
     * if the Spliterator reports a characteristic of {@code Spliterator#SIZED}, and
     * {@code -1} otherwise.
     *
     * @param <T> the type of elements returned by the passed Spliterator
     * @param this_ the Spliterator whose exact size should be queried
     * @return the exact size, if known, else {@code -1}.
     */
    public static <T> long getExactSizeIfKnown(Spliterator<T> this_) {
        return (this_.characteristics() & Spliterator.SIZED) == 0 ? -1L : this_.estimateSize();
    }

    /**
     * Returns {@code true} if the {@code this_} Spliterator's {@link
     * Spliterator#characteristics} contain all of the given characteristics.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation returns true if the corresponding bits
     * of the given characteristics are set.
     *
     * 
     * @param <T> the type of elements returned by the passed Spliterator
     * @param this_ the Spliterator whose characteristics should be queried
     * @param characteristics the characteristics to check for
     * @return {@code true} if all the specified characteristics are present,
     * else {@code false}
     */
    public static <T> boolean hasCharacteristics(Spliterator<T> this_, int characteristics) {
        return (this_.characteristics() & characteristics) == characteristics;
    }

    /**
     * If the Spliterator's source is {@link Spliterator#SORTED} by a {@link Comparator},
     * returns that {@code Comparator}. If the source is {@code SORTED} in
     * {@linkplain Comparable natural order}, returns {@code null}.  Otherwise,
     * if the source is not {@code SORTED}, throws {@link IllegalStateException}.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation always throws {@link IllegalStateException}.
     *
     * @param <T> the type of elements returned by the passed Spliterator
     * @param this_ the Spliterator for which a Comparator is requested
     * @return a Comparator, or {@code null} if the elements are sorted in the
     * natural order.
     * @throws IllegalStateException if the spliterator does not report
     *         a characteristic of {@code SORTED}.
     */
    public static <T> Comparator<? super T> getComparator(Spliterator<T> this_) {
    	//return this_.getComparator();
        throw new IllegalStateException();
    }

    /**
     * Static default implementations for the Java 8 default method of {@link Spliterator.OfPrimitive} 
     */
    public static final class OfPrimitive {
        /**
         * Performs the given action for each remaining element, sequentially in
         * the current thread, until all elements have been processed or the
         * action throws an exception.  If the {@code this_} Spliterator is
         * {@link Spliterator#ORDERED}, actions are performed in encounter order.
         * Exceptions thrown by the action are relayed to the caller.
         *
         * <p><b>Implementation Requirements:</b><br>
         * The default implementation repeatedly invokes
         * {@link Spliterator.OfPrimitive#tryAdvance} until it returns {@code false}.
         * It should be overridden whenever possible.
         *
         * @param <T> the type of elements returned by this Spliterator. The type
         *            must be a wrapper type for a primitive type, such as Integer
         *            for the primitive int type.
         * @param <T_CONS> the type of primitive consumer. The type must be a primitive
         *            specialization of Consumer for T, such as IntConsumer for Integer.
         * @param <T_SPLITR> the type of primitive Spliterator. The type must be a
         *            primitive specialization of Spliterator for T, such as
         *            {@link Spliterator.OfInt} for Integer.
         * @param this_ the Spliterator whose remaining elements should be processed
         * @param action The action to execute
         * @throws NullPointerException if the specified {@code this_} Spliterator is null
         * @throws NullPointerException if the specified action is null
         */
        public static <T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> void forEachRemaining(Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> this_, T_CONS action) {
            do { } while (this_.tryAdvance(action));
        }

    	private OfPrimitive() {
    		throw new AssertionError();
    	}
    }

    /**
     * Static default implementations for the Java 8 default methods of {@link Spliterator.OfInt} 
     */
    public static final class OfInt {
        /**
         * Performs the given action for each remaining element of the passed Spliterator,
         * sequentially in the current thread, until all elements have been processed or
         * the action throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED},
         * actions are performed in encounter order.  Exceptions thrown by the
         * action are relayed to the caller.
         *
         * <p><b>Implementation Requirements:</b><br>
         * The default implementation repeatedly invokes {@link #tryAdvance}
         * until it returns {@code false}.  It should be overridden whenever
         * possible.
         * 
         * @param this_ the Spliterator whose remaining elements should be processed
         * @param action The action to execute
         * @throws NullPointerException if the specified {@code this_} Spliterator is null
         * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfInt this_, IntConsumer action) {
            do { } while (this_.tryAdvance(action));
        }

        /**
	     * If a remaining element exists, performs the given action on it,
	     * returning {@code true}; else returns {@code false}.  If the {@code this_}
	     * Spliterator is {@link Spliterator#ORDERED} the action is performed on the
	     * next element in encounter order.  Exceptions thrown by the
	     * action are relayed to the caller.
	     * 
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code IntConsumer} then it is cast
         * to {@code IntConsumer} and passed to
         * {@link Spliterator.OfInt#tryAdvance(java8.util.function.IntConsumer)}; otherwise
         * the action is adapted to an instance of {@code IntConsumer}, by
         * boxing the argument of {@code IntConsumer}, and then passed to
         * {@link Spliterator.OfInt#tryAdvance(java8.util.function.IntConsumer)}.
	     *
	     * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @return {@code false} if no remaining elements existed
	     * upon entry to this method, else {@code true}.
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static boolean tryAdvance(Spliterator.OfInt this_, Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                return this_.tryAdvance((IntConsumer) action);
            }
            else {
               return this_.tryAdvance((IntConsumer) action::accept);
            }
        }

        /**
	     * Performs the given action for each remaining element, sequentially in
	     * the current thread, until all elements have been processed or the action
	     * throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED}, actions
	     * are performed in encounter order.  Exceptions thrown by the action
	     * are relayed to the caller.
	     *
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code IntConsumer} then it is cast
         * to {@code IntConsumer} and passed to
         * {@link Spliterator.OfInt#forEachRemaining(java8.util.function.IntConsumer)}; otherwise
         * the action is adapted to an instance of {@code IntConsumer}, by
         * boxing the argument of {@code IntConsumer}, and then passed to
         * {@link Spliterator.OfInt#forEachRemaining(java8.util.function.IntConsumer)}.
         * 
         * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfInt this_, Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
            	this_.forEachRemaining((IntConsumer) action);
            }
            else {
                this_.forEachRemaining((IntConsumer) action::accept);
            }
        }

    	private OfInt() {
    		throw new AssertionError();
    	}
    }

    /**
     * Static default implementations for the Java 8 default methods of {@link Spliterator.OfLong} 
     */
    public static final class OfLong {
        /**
         * Performs the given action for each remaining element of the passed Spliterator,
         * sequentially in the current thread, until all elements have been processed or
         * the action throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED},
         * actions are performed in encounter order.  Exceptions thrown by the
         * action are relayed to the caller.
         *
         * <p><b>Implementation Requirements:</b><br>
         * The default implementation repeatedly invokes {@link #tryAdvance}
         * until it returns {@code false}.  It should be overridden whenever
         * possible.
         * 
         * @param this_ the Spliterator whose remaing elements should be processed
         * @param action The action to execute
         * @throws NullPointerException if the specified {@code this_} Spliterator is null
         * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfLong this_, LongConsumer action) {
            do { } while (this_.tryAdvance(action));
        }

        /**
	     * If a remaining element exists, performs the given action on it,
	     * returning {@code true}; else returns {@code false}.  If the {@code this_}
	     * Spliterator is {@link Spliterator#ORDERED} the action is performed on the
	     * next element in encounter order.  Exceptions thrown by the
	     * action are relayed to the caller.
	     * 
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code LongConsumer} then it is cast
         * to {@code LongConsumer} and passed to
         * {@link Spliterator.OfLong#tryAdvance(java8.util.function.LongConsumer)}; otherwise
         * the action is adapted to an instance of {@code LongConsumer}, by
         * boxing the argument of {@code LongConsumer}, and then passed to
         * {@link Spliterator.OfLong#tryAdvance(java8.util.function.LongConsumer)}.
	     *
	     * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @return {@code false} if no remaining elements existed
	     * upon entry to this method, else {@code true}.
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static boolean tryAdvance(Spliterator.OfLong this_, Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                return this_.tryAdvance((LongConsumer) action);
            }
            else {
                return this_.tryAdvance((LongConsumer) action::accept);
            }
        }

        /**
	     * Performs the given action for each remaining element, sequentially in
	     * the current thread, until all elements have been processed or the action
	     * throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED}, actions
	     * are performed in encounter order.  Exceptions thrown by the action
	     * are relayed to the caller.
	     *
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code LongConsumer} then it is cast
         * to {@code LongConsumer} and passed to
         * {@link Spliterator.OfLong#forEachRemaining(java8.util.function.LongConsumer)}; otherwise
         * the action is adapted to an instance of {@code LongConsumer}, by
         * boxing the argument of {@code LongConsumer}, and then passed to
         * {@link Spliterator.OfLong#forEachRemaining(java8.util.function.LongConsumer)}.
         * 
         * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfLong this_, Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
            	this_.forEachRemaining((LongConsumer) action);
            }
            else {
                this_.forEachRemaining((LongConsumer) action::accept);
            }
        }

    	private OfLong() {
    		throw new AssertionError();
    	}
    }

    /**
     * Static default implementations for the Java 8 default methods of {@link Spliterator.OfDouble} 
     */
    public static final class OfDouble {
        /**
         * Performs the given action for each remaining element of the passed Spliterator,
         * sequentially in the current thread, until all elements have been processed or
         * the action throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED},
         * actions are performed in encounter order.  Exceptions thrown by the
         * action are relayed to the caller.
         *
         * <p><b>Implementation Requirements:</b><br>
         * The default implementation repeatedly invokes {@link #tryAdvance}
         * until it returns {@code false}.  It should be overridden whenever
         * possible.
         * 
         * @param this_ the Spliterator whose remaining elements should be processed
         * @param action The action to execute
         * @throws NullPointerException if the specified {@code this_} Spliterator is null
         * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfDouble this_, DoubleConsumer action) {
            do { } while (this_.tryAdvance(action));
        }

        /**
	     * If a remaining element exists, performs the given action on it,
	     * returning {@code true}; else returns {@code false}.  If the {@code this_}
	     * Spliterator is {@link Spliterator#ORDERED} the action is performed on the
	     * next element in encounter order.  Exceptions thrown by the
	     * action are relayed to the caller.
	     * 
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code DoubleConsumer} then it is
         * cast to {@code DoubleConsumer} and passed to
         * {@link Spliterator.OfDouble#tryAdvance(java8.util.function.DoubleConsumer)}; otherwise
         * the action is adapted to an instance of {@code DoubleConsumer}, by
         * boxing the argument of {@code DoubleConsumer}, and then passed to
         * {@link Spliterator.OfDouble#tryAdvance(java8.util.function.DoubleConsumer)}.
	     *
	     * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @return {@code false} if no remaining elements existed
	     * upon entry to this method, else {@code true}.
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static boolean tryAdvance(Spliterator.OfDouble this_, Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                return this_.tryAdvance((DoubleConsumer) action);
            }
            else {
                return this_.tryAdvance((DoubleConsumer) action::accept);
            }
        }

        /**
	     * Performs the given action for each remaining element, sequentially in
	     * the current thread, until all elements have been processed or the action
	     * throws an exception.  If the {@code this_} Spliterator is {@link Spliterator#ORDERED}, actions
	     * are performed in encounter order.  Exceptions thrown by the action
	     * are relayed to the caller.
	     *
         * <p><b>Implementation Requirements:</b><br>
         * If the action is an instance of {@code DoubleConsumer} then it is
         * cast to {@code DoubleConsumer} and passed to
         * {@link Spliterator.OfDouble#forEachRemaining(java8.util.function.DoubleConsumer)};
         * otherwise the action is adapted to an instance of
         * {@code DoubleConsumer}, by boxing the argument of
         * {@code DoubleConsumer}, and then passed to
         * {@link Spliterator.OfDouble#forEachRemaining(java8.util.function.DoubleConsumer)}.
         * 
         * @param this_ the Spliterator to use
	     * @param action The action to execute
	     * @throws NullPointerException if the specified {@code this_} Spliterator is null
	     * @throws NullPointerException if the specified action is null
         */
        public static void forEachRemaining(Spliterator.OfDouble this_, Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
            	this_.forEachRemaining((DoubleConsumer) action);
            }
            else {
                this_.forEachRemaining((DoubleConsumer) action::accept);
            }
        }

    	private OfDouble() {
    		throw new AssertionError();
    	}
    }

    // Empty spliterators

    /**
     * Creates an empty {@code Spliterator}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java8.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @param <T> Type of elements
     * @return An empty spliterator
     */
    @SuppressWarnings("unchecked")
    public static <T> Spliterator<T> emptySpliterator() {
        return (Spliterator<T>) EMPTY_SPLITERATOR;
    }

    private static final Spliterator<Object> EMPTY_SPLITERATOR =
            new EmptySpliterator.OfRef<>();

    /**
     * Creates an empty {@code Spliterator.OfInt}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java8.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfInt emptyIntSpliterator() {
        return EMPTY_INT_SPLITERATOR;
    }

    private static final Spliterator.OfInt EMPTY_INT_SPLITERATOR =
            new EmptySpliterator.OfInt();

    /**
     * Creates an empty {@code Spliterator.OfLong}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java8.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfLong emptyLongSpliterator() {
        return EMPTY_LONG_SPLITERATOR;
    }

    private static final Spliterator.OfLong EMPTY_LONG_SPLITERATOR =
            new EmptySpliterator.OfLong();

    /**
     * Creates an empty {@code Spliterator.OfDouble}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java8.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfDouble emptyDoubleSpliterator() {
        return EMPTY_DOUBLE_SPLITERATOR;
    }

    private static final Spliterator.OfDouble EMPTY_DOUBLE_SPLITERATOR =
            new EmptySpliterator.OfDouble();

    // Array-based spliterators

    /**
     * Creates a {@code Spliterator} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(Object[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param <T> Type of elements
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(Object[])
     */
    public static <T> Spliterator<T> spliterator(Object[] array,
                                                 int additionalCharacteristics) {
        return new ArraySpliterator<>(Objects.requireNonNull(array),
                                      additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator} covering a range of elements of a given
     * array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(Object[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param <T> Type of elements
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(Object[], int, int)
     */
    public static <T> Spliterator<T> spliterator(Object[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new ArraySpliterator<>(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(int[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(int[])
     */
    public static Spliterator.OfInt spliterator(int[] array,
                                                int additionalCharacteristics) {
        return new IntArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(int[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(int[], int, int)
     */
    public static Spliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex,
                                                int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new IntArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(long[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(long[])
     */
    public static Spliterator.OfLong spliterator(long[] array,
                                                 int additionalCharacteristics) {
        return new LongArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(long[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report.  (For example, if it is
     * known the array will not be further modified, specify {@code IMMUTABLE};
     * if the array data is considered to have an an encounter order, specify
     * {@code ORDERED}).  The method {@link Arrays#spliterator(long[], int, int)} can
     * often be used instead, which returns a spliterator that reports
     * {@code SIZED}, {@code SUBSIZED}, {@code IMMUTABLE}, and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(long[], int, int)
     */
    public static Spliterator.OfLong spliterator(long[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new LongArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(double[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(double[])
     */
    public static Spliterator.OfDouble spliterator(double[] array,
                                                   int additionalCharacteristics) {
        return new DoubleArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(double[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report.  (For example, if it is
     * known the array will not be further modified, specify {@code IMMUTABLE};
     * if the array data is considered to have an an encounter order, specify
     * {@code ORDERED}).  The method {@link Arrays#spliterator(long[], int, int)} can
     * often be used instead, which returns a spliterator that reports
     * {@code SIZED}, {@code SUBSIZED}, {@code IMMUTABLE}, and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(double[], int, int)
     */
    public static Spliterator.OfDouble spliterator(double[] array, int fromIndex, int toIndex,
                                                   int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new DoubleArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Validate inclusive start index and exclusive end index against the length
     * of an array.
     * @param arrayLength The length of the array
     * @param origin The inclusive start index
     * @param fence The exclusive end index
     * @throws ArrayIndexOutOfBoundsException if the start index is greater than
     * the end index, if the start index is negative, or the end index is
     * greater than the array length
     */
    private static void checkFromToBounds(int arrayLength, int origin, int fence) {
        if (origin > fence) {
            throw new ArrayIndexOutOfBoundsException(
                    "origin(" + origin + ") > fence(" + fence + ")");
        }
        if (origin < 0) {
            throw new ArrayIndexOutOfBoundsException(origin);
        }
        if (fence > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(fence);
        }
    }

    /**
	 * Creates either a specialized {@code Spliterator} (effectively the same
	 * one that Java 8 uses) for the given collection provided it is one of the
	 * types listed below or a {@code Spliterator} using the given collection's
	 * {@link java.util.Collection#iterator()} as the source of elements, and
	 * reporting its {@link java.util.Collection#size()} as its initial size.
	 * 
	 * <p>
	 * In the latter case, if the given collection implements one of the
	 * interfaces {@link java.util.List}, {@link java.util.Set} or
	 * {@link java.util.SortedSet} the returned {@code Spliterator} will
	 * resemble the corresponding interface default implementation of the
	 * respective {@code spliterator()} method from Java 8. Otherwise the Java 8
	 * default implementation of {@code java.util.Collection.spliterator()} is
	 * used (which is essentially the same as calling
	 * {@link #spliterator(Collection, int)} with a {@code characteristics}
	 * value equal to {@code 0}).
	 *
	 * <p>
	 * Currently, the collections that have specializations available are the
	 * following:
	 * 
	 * <ul>
	 * <li>java.util.ArrayList</li>
	 * <li>java.util.Arrays.ArrayList</li>
	 * <li>java.util.ArrayDeque</li>
	 * <li>java.util.Vector</li>
	 * <li>java.util.LinkedHashSet</li>
	 * <li>java.util.PriorityQueue</li>
	 * <li>java.util.concurrent.ArrayBlockingQueue</li>
	 * <li>java.util.concurrent.LinkedBlockingQueue</li>
	 * <li>java.util.concurrent.LinkedBlockingDeque</li>
	 * <li>java.util.concurrent.PriorityBlockingQueue</li>
	 * <li>java.util.concurrent.CopyOnWriteArrayList</li>
	 * <li>java.util.concurrent.CopyOnWriteArraySet</li>
	 * </ul>
	 *
	 * <p>
	 * The {@code Spliterator}s for {@code CopyOnWriteArrayList} and
	 * {@code CopyOnWriteArraySet} provide a snapshot of the state of the
	 * collection when the Spliterator was created, otherwise the spliterator is
	 * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
	 * the <em>fail-fast</em> properties of the collection's iterator, and
	 * implements {@code trySplit} to permit limited parallelism.
	 * 
	 * @param <T>
	 *            Type of elements
	 * @param c
	 *            The collection
	 * @return Either a specialized spliterator if the given collection is one
	 *         of the types listed above or a spliterator from an iterator
	 * @throws NullPointerException
	 *             if the given collection is {@code null}
	 */
    public static <T> Spliterator<T> spliterator(Collection<? extends T> c) {
		Objects.requireNonNull(c);

		if (c instanceof List) {
			return listSpliterator((List<T>) c);
		}
		if (c instanceof Set) {
			return setSpliterator((Set<T>) c);
		}
		if (c instanceof Queue) {
			return queueSpliterator((Queue<T>) c);
		}

		// default (anything else)
		return spliterator(c, 0);
    }

    private static <T> Spliterator<T> listSpliterator(List<? extends T> c) {
		if (NATIVE_SPECIALIZATION && c instanceof ArrayList) {
			return ArrayListSpliterator.spliterator((ArrayList<T>) c);
		}

		String className = c.getClass().getName();
		if (NATIVE_SPECIALIZATION && "java.util.Arrays$ArrayList".equals(className)) {
			return ArraysArrayListSpliterator.spliterator((List<T>) c);
		}

		if (NATIVE_SPECIALIZATION && c instanceof CopyOnWriteArrayList) {
			return CopyOnWriteArrayListSpliterator
					.spliterator((CopyOnWriteArrayList<T>) c);
		}
		if (NATIVE_SPECIALIZATION && c instanceof Vector) {
			return VectorSpliterator.spliterator((Vector<T>) c);
		}

		// default from j.u.List
		return spliterator(c, Spliterator.ORDERED);
    }

    private static <T> Spliterator<T> setSpliterator(Set<? extends T> c) {
		if (c instanceof LinkedHashSet) {
			return spliterator(c, Spliterator.DISTINCT | Spliterator.ORDERED);
		}
		// default from j.u.SortedSet
		if (c instanceof SortedSet) {
			return new IteratorSpliterator<T>(c, Spliterator.DISTINCT
					| Spliterator.SORTED | Spliterator.ORDERED) {
				@Override
				public Comparator<? super T> getComparator() {
					return ((SortedSet<T>) c).comparator();
				}
			};
		}

		if (NATIVE_SPECIALIZATION && c instanceof CopyOnWriteArraySet) {
			return CopyOnWriteArraySetSpliterator
					.spliterator((CopyOnWriteArraySet<T>) c);
		}

		// default from j.u.Set
		return Spliterators.spliterator(c, Spliterator.DISTINCT);
    }

    private static <T> Spliterator<T> queueSpliterator(Queue<? extends T> c) {
		if (c instanceof ArrayBlockingQueue) {
			return spliterator(c, Spliterator.ORDERED | Spliterator.NONNULL
					| Spliterator.CONCURRENT);
		}
		if (NATIVE_SPECIALIZATION && c instanceof LinkedBlockingQueue) {
			return LBQSpliterator.spliterator((LinkedBlockingQueue<T>) c);
		}
		if (NATIVE_SPECIALIZATION && c instanceof ArrayDeque) {
			return ArrayDequeSpliterator.spliterator((ArrayDeque<T>) c);
		}
		if (NATIVE_SPECIALIZATION && c instanceof LinkedBlockingDeque) {
			return LBDSpliterator.spliterator((LinkedBlockingDeque<T>) c);
		}
		if (NATIVE_SPECIALIZATION && c instanceof PriorityBlockingQueue) {
			return PriorityBlockingQueueSpliterator.spliterator((PriorityBlockingQueue<T>) c);
		}
		if (NATIVE_SPECIALIZATION && c instanceof PriorityQueue) {
			return PriorityQueueSpliterator.spliterator((PriorityQueue<T>) c);
		}

		// default from j.u.Collection
		return spliterator(c, 0);
    }

    // Iterator-based spliterators

    /**
     * Creates a {@code Spliterator} using the given collection's
     * {@link java.util.Collection#iterator()} as the source of elements, and
     * reporting its {@link java.util.Collection#size()} as its initial size.
     *
     * <p>The spliterator is
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the collection's iterator, and
     * implements {@code trySplit} to permit limited parallelism.
     *
     * @param <T> Type of elements
     * @param c The collection
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given collection is {@code null}
     */
    public static <T> Spliterator<T> spliterator(Collection<? extends T> c,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(c),
                                         characteristics);
    }

    /**
     * Creates a {@code Spliterator} using a given {@code Iterator}
     * as the source of elements, and with a given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param <T> Type of elements
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator,
                                                 long size,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), size,
                                         characteristics);
    }

    /**
     * Creates a {@code Spliterator} using a given {@code Iterator}
     * as the source of elements, with no initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param <T> Type of elements
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static <T> Spliterator<T> spliteratorUnknownSize(Iterator<? extends T> iterator,
                                                            int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} using a given
     * {@code IntStream.IntIterator} as the source of elements, and with a given
     * initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}.
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfInt spliterator(java8.util.PrimitiveIterator.OfInt iterator,
                                                long size,
                                                int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator),
                                          size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} using a given
     * {@code IntStream.IntIterator} as the source of elements, with no initial
     * size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfInt spliteratorUnknownSize(java8.util.PrimitiveIterator.OfInt iterator,
                                                           int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} using a given
     * {@code LongStream.LongIterator} as the source of elements, and with a
     * given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}.
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfLong spliterator(java8.util.PrimitiveIterator.OfLong iterator,
                                                 long size,
                                                 int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator),
                                           size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} using a given
     * {@code LongStream.LongIterator} as the source of elements, with no
     * initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfLong spliteratorUnknownSize(java8.util.PrimitiveIterator.OfLong iterator,
                                                            int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} using a given
     * {@code DoubleStream.DoubleIterator} as the source of elements, and with a
     * given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfDouble spliterator(java8.util.PrimitiveIterator.OfDouble iterator,
                                                   long size,
                                                   int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator),
                                             size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} using a given
     * {@code DoubleStream.DoubleIterator} as the source of elements, with no
     * initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfDouble spliteratorUnknownSize(java8.util.PrimitiveIterator.OfDouble iterator,
                                                              int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    // Iterators from Spliterators

    /**
     * Creates an {@code Iterator} from a {@code Spliterator}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param <T> Type of elements
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static<T> Iterator<T> iterator(final Spliterator<? extends T> spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements Iterator<T>, Consumer<T> {
            boolean valueReady = false;
            T nextElement;

            @Override
            public void accept(T t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public T next() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfInt} from a
     * {@code Spliterator.OfInt}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfInt iterator(final Spliterator.OfInt spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfInt, IntConsumer {
            boolean valueReady = false;
            int nextElement;

            @Override
            public void accept(int t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public int nextInt() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

			@Override
			public Integer next() {
				return nextInt();
			}

			@Override
			public void forEachRemaining(IntConsumer action) {
				Iterators.forEachRemaining(this, action);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfLong} from a
     * {@code Spliterator.OfLong}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfLong iterator(final Spliterator.OfLong spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfLong, LongConsumer {
            boolean valueReady = false;
            long nextElement;

            @Override
            public void accept(long t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public long nextLong() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

			@Override
			public Long next() {
				return nextLong();
			}

			@Override
			public void forEachRemaining(LongConsumer action) {
				Iterators.forEachRemaining(this, action);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfDouble} from a
     * {@code Spliterator.OfDouble}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfDouble iterator(final Spliterator.OfDouble spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfDouble, DoubleConsumer {
            boolean valueReady = false;
            double nextElement;

            @Override
            public void accept(double t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public double nextDouble() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

			@Override
			public Double next() {
				return nextDouble();
			}

			@Override
			public void forEachRemaining(DoubleConsumer action) {
				Iterators.forEachRemaining(this, action);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
        }

        return new Adapter();
    }

    // Implementations

    private static abstract class EmptySpliterator<T, S extends Spliterator<T>, C> {

        EmptySpliterator() { }

        public S trySplit() {
            return null;
        }

        public boolean tryAdvance(C consumer) {
            Objects.requireNonNull(consumer);
            return false;
        }

        public void forEachRemaining(C consumer) {
            Objects.requireNonNull(consumer);
        }

        public long estimateSize() {
            return 0;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        private static final class OfRef<T>
                extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>>
                implements Spliterator<T> {
            OfRef() { }

			@Override
			public long getExactSizeIfKnown() {
				return Spliterators.getExactSizeIfKnown(this);
			}

			@Override
			public boolean hasCharacteristics(int characteristics) {
				return Spliterators.hasCharacteristics(this, characteristics);
			}

			@Override
			public Comparator<? super T> getComparator() {
				return Spliterators.getComparator(this);
			}
        }

        private static final class OfInt
                extends EmptySpliterator<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            OfInt() { }

			@Override
			public long getExactSizeIfKnown() {
				return Spliterators.getExactSizeIfKnown(this);
			}

			@Override
			public boolean hasCharacteristics(int characteristics) {
				return Spliterators.hasCharacteristics(this, characteristics);
			}

			@Override
			public Comparator<? super Integer> getComparator() {
				return Spliterators.getComparator(this);
			}

			@Override
			public boolean tryAdvance(Consumer<? super Integer> action) {
				return Spliterators.OfInt.tryAdvance(this, action);
			}

	        @Override
			public void forEachRemaining(Consumer<? super Integer> action) {
				Spliterators.OfInt.forEachRemaining(this, action);
			}
        }

        private static final class OfLong
                extends EmptySpliterator<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            OfLong() { }

			@Override
			public long getExactSizeIfKnown() {
				return Spliterators.getExactSizeIfKnown(this);
			}

			@Override
			public boolean hasCharacteristics(int characteristics) {
				return Spliterators.hasCharacteristics(this, characteristics);
			}

			@Override
			public Comparator<? super Long> getComparator() {
				return Spliterators.getComparator(this);
			}

	        @Override
			public boolean tryAdvance(Consumer<? super Long> action) {
				return Spliterators.OfLong.tryAdvance(this, action);
			}

	        @Override
			public void forEachRemaining(Consumer<? super Long> action) {
				Spliterators.OfLong.forEachRemaining(this, action);
			}
        }

        private static final class OfDouble
                extends EmptySpliterator<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            OfDouble() { }

			@Override
			public long getExactSizeIfKnown() {
				return Spliterators.getExactSizeIfKnown(this);
			}

			@Override
			public boolean hasCharacteristics(int characteristics) {
				return Spliterators.hasCharacteristics(this, characteristics);
			}

			@Override
			public Comparator<? super Double> getComparator() {
				return Spliterators.getComparator(this);
			}

	        @Override
			public boolean tryAdvance(Consumer<? super Double> action) {
				return Spliterators.OfDouble.tryAdvance(this, action);
			}

	        @Override
			public void forEachRemaining(Consumer<? super Double> action) {
				Spliterators.OfDouble.forEachRemaining(this, action);
			}
        }
    }

    // Array-based spliterators

    /**
     * A Spliterator designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code Object[]} array.
     */
    static final class ArraySpliterator<T> implements Spliterator<T> {
        /**
         * The array, explicitly typed as Object[]. Unlike in some other
         * classes (see for example CR 6260652), we do not need to
         * screen arguments to ensure they are exactly of type Object[]
         * so long as no methods write into the array or serialize it,
         * which we ensure here by defining this class as final.
         */
        private final Object[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         * of this spliterator's source or elements beyond {@code SIZED} and
         * {@code SUBSIZED} which are are always reported
         */
        public ArraySpliterator(Object[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         * of this spliterator's source or elements beyond {@code SIZED} and
         * {@code SUBSIZED} which are are always reported
         */
        public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
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
        public Spliterator<T> trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new ArraySpliterator<>(array, lo, index = mid, characteristics);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Object[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept((T)a[i]); } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                @SuppressWarnings("unchecked") T e = (T) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfInt designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class IntArraySpliterator implements Spliterator.OfInt {
        private final int[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public IntArraySpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public IntArraySpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfInt trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new IntArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            int[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

		@Override
		public boolean tryAdvance(Consumer<? super Integer> action) {
			return Spliterators.OfInt.tryAdvance(this, action);
		}

        @Override
		public void forEachRemaining(Consumer<? super Integer> action) {
			Spliterators.OfInt.forEachRemaining(this, action);
		}

		@Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
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
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfLong designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class LongArraySpliterator implements Spliterator.OfLong {
        private final long[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public LongArraySpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public LongArraySpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfLong trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new LongArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            long[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
		public void forEachRemaining(Consumer<? super Long> action) {
			Spliterators.OfLong.forEachRemaining(this, action);
		}

		@Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
		public boolean tryAdvance(Consumer<? super Long> action) {
			return Spliterators.OfLong.tryAdvance(this, action);
		}

		@Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
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
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfDouble designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class DoubleArraySpliterator implements Spliterator.OfDouble {
        private final double[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public DoubleArraySpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are are always reported
         */
        public DoubleArraySpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfDouble trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new DoubleArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            double[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
		public void forEachRemaining(Consumer<? super Double> action) {
			Spliterators.OfDouble.forEachRemaining(this, action);
		}

		@Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
		public boolean tryAdvance(Consumer<? super Double> action) {
			return Spliterators.OfDouble.tryAdvance(this, action);
		}

		@Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
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
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    //

    /**
     * An abstract {@code Spliterator} that implements {@code trySplit} to
     * permit limited parallelism.
     *
     * <p>An extending class need only
     * implement {@link #tryAdvance(java8.util.function.Consumer) tryAdvance}.
     * The extending class should override
     * {@link #forEachRemaining(java8.util.function.Consumer) forEach} if it can
     * provide a more performant implementation.
     *
     * <p><b>API Note:</b><br>
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(Iterator, long, int)}.  Depending on the
     * circumstances using an iterator may be easier or more convenient than
     * extending this class, such as when there is already an iterator
     * available to use.
     *
     * @see #spliterator(Iterator, long, int)
     * @since 1.8
     */
    public static abstract class AbstractSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * additionalCharacteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingConsumer<T> implements Consumer<T> {
            Object value;

            @Override
            public void accept(T value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator<T> trySplit() {
            /*
             * Split into arrays of arithmetically increasing batch
             * sizes.  This will only improve parallel performance if
             * per-element Consumer actions are more costly than
             * transferring them into an array.  The use of an
             * arithmetic progression in split sizes provides overhead
             * vs parallelism bounds that do not particularly favor or
             * penalize cases of lightweight vs heavyweight element
             * operations, across combinations of #elements vs #cores,
             * whether or not either are known.  We generate
             * O(sqrt(#elements)) splits, allowing O(sqrt(#cores))
             * potential speedup.
             */
            HoldingConsumer<T> holder = new HoldingConsumer<>();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new ArraySpliterator<>(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * An abstract {@code Spliterator.OfInt} that implements {@code trySplit} to
     * permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java8.util.function.IntConsumer)}
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java8.util.function.IntConsumer)} forEach} if it
     * can provide a more performant implementation.
     *
     * <p><b>API Note:</b><br>
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java8.util.PrimitiveIterator.OfInt, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java8.util.PrimitiveIterator.OfInt, long, int)
     * @since 1.8
     */
    public static abstract class AbstractIntSpliterator implements Spliterator.OfInt {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractIntSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingIntConsumer implements IntConsumer {
            int value;

            @Override
            public void accept(int value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfInt trySplit() {
            HoldingIntConsumer holder = new HoldingIntConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new IntArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * An abstract {@code Spliterator.OfLong} that implements {@code trySplit}
     * to permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java8.util.function.LongConsumer)}
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java8.util.function.LongConsumer)} forEach} if it
     * can provide a more performant implementation.
     *
     * <p><b>API Note:</b><br>
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java8.util.PrimitiveIterator.OfLong, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java8.util.PrimitiveIterator.OfLong, long, int)
     * @since 1.8
     */
    public static abstract class AbstractLongSpliterator implements Spliterator.OfLong {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractLongSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingLongConsumer implements LongConsumer {
            long value;

            @Override
            public void accept(long value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfLong trySplit() {
            HoldingLongConsumer holder = new HoldingLongConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new LongArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * An abstract {@code Spliterator.OfDouble} that implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java8.util.function.DoubleConsumer)}
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java8.util.function.DoubleConsumer)} forEach} if
     * it can provide a more performant implementation.
     *
     * <p><b>API Note:</b><br>
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java8.util.PrimitiveIterator.OfDouble, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java8.util.PrimitiveIterator.OfDouble, long, int)
     * @since 1.8
     */
    public static abstract class AbstractDoubleSpliterator implements Spliterator.OfDouble {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractDoubleSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingDoubleConsumer implements DoubleConsumer {
            double value;

            @Override
            public void accept(double value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfDouble trySplit() {
            HoldingDoubleConsumer holder = new HoldingDoubleConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new DoubleArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br>
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    // Iterator-based Spliterators

    /**
     * A Spliterator using a given Iterator for element
     * operations. The spliterator implements {@code trySplit} to
     * permit limited parallelism.
     */
    static class IteratorSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        private final Collection<? extends T> collection; // null OK
        private Iterator<? extends T> it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given given
         * collection's {@link java.util.Collection#iterator()} for traversal,
         * and reporting its {@link java.util.Collection#size()} as its initial
         * size.
         *
         * @param collection the collection
         * @param characteristics properties of this spliterator's
         *        source or elements.
         */
        public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
            this.collection = collection;
            this.it = null;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public Spliterator<T> trySplit() {
            /*
             * Split into arrays of arithmetically increasing batch
             * sizes.  This will only improve parallel performance if
             * per-element Consumer actions are more costly than
             * transferring them into an array.  The use of an
             * arithmetic progression in split sizes provides overhead
             * vs parallelism bounds that do not particularly favor or
             * penalize cases of lightweight vs heavyweight element
             * operations, across combinations of #elements vs #cores,
             * whether or not either are known.  We generate
             * O(sqrt(#elements)) splits, allowing O(sqrt(#cores))
             * potential speedup.
             */
            Iterator<? extends T> i;
            long s;
            if ((i = it) == null) {
                i = it = collection.iterator();
                s = est = (long) collection.size();
            }
            else
                s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j] = i.next(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new ArraySpliterator<>(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            Iterator<? extends T> i;
            if ((i = it) == null) {
                i = it = collection.iterator();
                est = (long) collection.size();
            }
            Iterators.forEachRemaining(i, action);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            if (it == null) {
                it = collection.iterator();
                est = (long) collection.size();
            }
            if (it.hasNext()) {
                action.accept(it.next());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            if (it == null) {
                it = collection.iterator();
                return est = (long)collection.size();
            }
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
		public long getExactSizeIfKnown() {
			return Spliterators.getExactSizeIfKnown(this);
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return Spliterators.hasCharacteristics(this, characteristics);
		}

		@Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfInt using a given IntStream.IntIterator for element
     * operations. The spliterator implements {@code trySplit} to
     * permit limited parallelism.
     */
    static final class IntIteratorSpliterator implements Spliterator.OfInt {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private java8.util.PrimitiveIterator.OfInt it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IntIteratorSpliterator(java8.util.PrimitiveIterator.OfInt iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IntIteratorSpliterator(java8.util.PrimitiveIterator.OfInt iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfInt trySplit() {
            java8.util.PrimitiveIterator.OfInt i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do { a[j] = i.nextInt(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new IntArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            Iterators.forEachRemaining(it, action);
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextInt());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
		public long getExactSizeIfKnown() {
        	return Spliterators.getExactSizeIfKnown(this);
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return Spliterators.hasCharacteristics(this, characteristics);
		}

		@Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }

		@Override
		public boolean tryAdvance(Consumer<? super Integer> action) {
			return Spliterators.OfInt.tryAdvance(this, action);
		}

        @Override
		public void forEachRemaining(Consumer<? super Integer> action) {
			Spliterators.OfInt.forEachRemaining(this, action);
		}
    }

    static final class LongIteratorSpliterator implements Spliterator.OfLong {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private java8.util.PrimitiveIterator.OfLong it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public LongIteratorSpliterator(java8.util.PrimitiveIterator.OfLong iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public LongIteratorSpliterator(java8.util.PrimitiveIterator.OfLong iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfLong trySplit() {
            java8.util.PrimitiveIterator.OfLong i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do { a[j] = i.nextLong(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new LongArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            Iterators.forEachRemaining(it, action);
        }

        @Override
		public void forEachRemaining(Consumer<? super Long> action) {
			Spliterators.OfLong.forEachRemaining(this, action);
		}

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextLong());
                return true;
            }
            return false;
        }

        @Override
		public boolean tryAdvance(Consumer<? super Long> action) {
			return Spliterators.OfLong.tryAdvance(this, action);
		}

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
		public long getExactSizeIfKnown() {
        	return Spliterators.getExactSizeIfKnown(this);
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return Spliterators.hasCharacteristics(this, characteristics);
		}

		@Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class DoubleIteratorSpliterator implements Spliterator.OfDouble {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private java8.util.PrimitiveIterator.OfDouble it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public DoubleIteratorSpliterator(java8.util.PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public DoubleIteratorSpliterator(java8.util.PrimitiveIterator.OfDouble iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfDouble trySplit() {
            java8.util.PrimitiveIterator.OfDouble i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do { a[j] = i.nextDouble(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new DoubleArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            Iterators.forEachRemaining(it, action);
        }

        @Override
		public void forEachRemaining(Consumer<? super Double> action) {
			Spliterators.OfDouble.forEachRemaining(this, action);
		}

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextDouble());
                return true;
            }
            return false;
        }

        @Override
		public boolean tryAdvance(Consumer<? super Double> action) {
			return Spliterators.OfDouble.tryAdvance(this, action);
		}

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
		public long getExactSizeIfKnown() {
        	return Spliterators.getExactSizeIfKnown(this);
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return Spliterators.hasCharacteristics(this, characteristics);
		}

		@Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    private static boolean getBooleanPropertyValue(final String property) {
    	return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			@Override
			public Boolean run() {
		        boolean value = true;
		        try {
		        	String s = System.getProperty(property, Boolean.TRUE.toString());
		            value = (s == null) || s.trim().equalsIgnoreCase(Boolean.TRUE.toString());
		        } catch (IllegalArgumentException ignore) {
		        } catch (NullPointerException ignore) {
		        }
		        return value;
			}
		});
    }

    /**
     * Are we running on a Dalvik VM or maybe even ART? 
     */
    private static boolean isAndroid() {
    	Class<?> clazz = null;
    	try {
    		clazz = Class.forName("android.util.DisplayMetrics");
    	} catch (Throwable notPresent) {
    		// ignore
    	}
    	return clazz != null;
    }
}
