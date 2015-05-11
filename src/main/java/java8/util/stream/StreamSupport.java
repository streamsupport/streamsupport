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
package java8.util.stream;

import java.util.Collection;
import java.util.Iterator;

import java8.util.Objects;
import java8.util.Spliterators;
import java8.util.function.Supplier;
import java8.util.function.UnaryOperator;
import java8.util.stream.Stream.Builder;
import java8.util.Spliterator;

/**
 * Low-level utility methods for creating and manipulating streams.
 *
 * <p>This class is mostly for library writers presenting stream views
 * of data structures; most static stream methods intended for end users are in
 * the various {@code Streams} classes.
 *
 * @since 1.8
 */
public final class StreamSupport {

    // Suppresses default constructor, ensuring non-instantiability.
    private StreamSupport() {}

    /**
     * Returns a builder for a {@code Stream}.
     *
     * @param <T> type of elements
     * @return a stream builder
     */
    public static<T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    /**
     * Returns an empty sequential {@code Stream}.
     *
     * @param <T> the type of stream elements
     * @return an empty sequential stream
     */
    public static<T> Stream<T> empty() {
        return stream(Spliterators.<T>emptySpliterator(), false);
    }

    /**
     * Returns a sequential {@code Stream} containing a single element.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a singleton sequential stream
     */
    public static<T> Stream<T> of(T t) {
        return stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * Returns a sequential {@code Stream} containing a single element, if
     * non-null, otherwise returns an empty {@code Stream}.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a stream with a single element if the specified element
     *         is non-null, otherwise an empty stream
     * @since 1.9
     */
    public static<T> Stream<T> ofNullable(T t) {
        return t == null ? empty()
                         : stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * Returns a sequential ordered stream whose elements are the specified values.
     *
     * @param <T> the type of stream elements
     * @param values the elements of the new stream
     * @return the new stream
     */
    public static<T> Stream<T> of(@SuppressWarnings("unchecked") T... values) { // Creating a stream from an array is safe
        return java8.util.J8Arrays.stream(values);
    }

    /**
     * Returns an infinite sequential ordered {@code Stream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code Stream} will be
     * the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * @param <T> the type of stream elements
     * @param seed the initial element
     * @param f a function to be applied to the previous element to produce
     *          a new element
     * @return a new sequential {@code Stream}
     */
    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        final Iterator<T> iterator = new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) Streams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return t = (t == Streams.NONE) ? seed : f.apply(t);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
        return stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    /**
     * Returns an infinite sequential unordered stream where each element is
     * generated by the provided {@code Supplier}.  This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param <T> the type of stream elements
     * @param s the {@code Supplier} of generated elements
     * @return a new infinite sequential unordered {@code Stream}
     */
    public static<T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     *
     * <p><b>Implementation Note:</b><br>
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowError}.
     *
     * @param <T> The type of stream elements
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    /**
     * Adds an element to the stream being built represented by the Stream.Builder
     * argument.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation behaves as if:
     * <pre>{@code
     *     builder.accept(t)
     *     return builder;
     * }</pre>
     *
     * @param <T> the type of stream elements
     * @param builder the {@code Stream.Builder} to use
     * @param t the element to add
     * @return the passed builder
     * @throws IllegalStateException if the builder has already transitioned to
     * the built state
     */
    public static <T> Stream.Builder<T> add(Stream.Builder<T> builder, T t) {
        builder.accept(t);
        return builder;
    }

    /**
     * Creates a new sequential {@code Stream} using either the given
     * collection's {@link java.util.Collection#iterator()} as the source of
     * elements for an internally created {@code Spliterator} which will report
     * the collection's {@link java.util.Collection#size()} as its initial size
     * or a specialized {@code Spliterator} implementation (effectively the same
     * one that Java 8 uses) if the passed {@code Collection} is one of the
     * types listed below.
     *
     * <ul>
     * <li>java.util.ArrayList</li>
     * <li>java.util.Arrays.ArrayList</li>
     * <li>java.util.ArrayDeque</li>
     * <li>java.util.Vector</li>
     * <li>java.util.LinkedList</li>
     * <li>java.util.HashSet</li>
     * <li>java.util.LinkedHashSet</li>
     * <li>java.util.PriorityQueue</li>
     * <li>java.util.concurrent.ArrayBlockingQueue</li>
     * <li>java.util.concurrent.LinkedBlockingQueue</li>
     * <li>java.util.concurrent.LinkedBlockingDeque</li>
     * <li>java.util.concurrent.PriorityBlockingQueue</li>
     * <li>java.util.concurrent.CopyOnWriteArrayList</li>
     * <li>java.util.concurrent.CopyOnWriteArraySet</li>
     * <li>The collections returned from the java.util.HashMap methods
     * #keySet(), #entrySet() and #values()</li>
     * </ul>
     *
     * <p>
     * The {@code Spliterator}s for {@code CopyOnWriteArrayList} and
     * {@code CopyOnWriteArraySet} provide a snapshot of the state of the
     * collection when the {@code Stream} was created, otherwise the created
     * spliterator is
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the collection's iterator, and
     * implements {@code trySplit} to permit limited parallelism.
     *
     * <p>
     * The created spliterator is only traversed, split, or queried for
     * estimated size after the terminal operation of the stream pipeline
     * commences.
     * 
     * @param <T>
     *            Type of elements
     * @param c
     *            The collection
     * @return a new sequential {@code Stream}
     * @throws NullPointerException
     *             if the given collection is {@code null}
     */
    public static <T> Stream<T> stream(Collection<? extends T> c) {
        return stream(Spliterators.spliterator(c), false);
    }

    /**
     * Creates a new possibly parallel {@code Stream} using either the given
     * collection's {@link java.util.Collection#iterator()} as the source of
     * elements for an internally created {@code Spliterator} which will report
     * the collection's {@link java.util.Collection#size()} as its initial size
     * or a specialized {@code Spliterator} implementation (effectively the same
     * one that Java 8 uses) if the passed {@code Collection} is one of the
     * types listed below.
     *
     * <ul>
     * <li>java.util.ArrayList</li>
     * <li>java.util.Arrays.ArrayList</li>
     * <li>java.util.ArrayDeque</li>
     * <li>java.util.Vector</li>
     * <li>java.util.LinkedList</li>
     * <li>java.util.HashSet</li>
     * <li>java.util.LinkedHashSet</li>
     * <li>java.util.PriorityQueue</li>
     * <li>java.util.concurrent.ArrayBlockingQueue</li>
     * <li>java.util.concurrent.LinkedBlockingQueue</li>
     * <li>java.util.concurrent.LinkedBlockingDeque</li>
     * <li>java.util.concurrent.PriorityBlockingQueue</li>
     * <li>java.util.concurrent.CopyOnWriteArrayList</li>
     * <li>java.util.concurrent.CopyOnWriteArraySet</li>
     * <li>The collections returned from the java.util.HashMap methods
     * #keySet(), #entrySet() and #values()</li>
     * </ul>
     *
     * <p>
     * The {@code Spliterator}s for {@code CopyOnWriteArrayList} and
     * {@code CopyOnWriteArraySet} provide a snapshot of the state of the
     * collection when the {@code Stream} was created, otherwise the created
     * spliterator is
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the collection's iterator, and
     * implements {@code trySplit} to permit limited parallelism.
     *
     * <p>
     * The created spliterator is only traversed, split, or queried for
     * estimated size after the terminal operation of the stream pipeline
     * commences.
     * 
     * @param <T>
     *            Type of elements
     * @param c
     *            The collection
     * @return a new possibly parallel {@code Stream}
     * @throws NullPointerException
     *             if the given collection is {@code null}
     */
    public static <T> Stream<T> parallelStream(Collection<? extends T> c) {
        return stream(Spliterators.spliterator(c), true);
    }

    /**
     * Creates a new sequential {@code Stream} using the given collection's
     * {@link java.util.Collection#iterator()} as the source of elements for an
     * internally created {@code Spliterator} which will report the collection's
     * {@link java.util.Collection#size()} as its initial size.
     *
     * <p>
     * The created spliterator is
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the collection's iterator, and
     * implements {@code trySplit} to permit limited parallelism.
     *
     * <p>
     * The created spliterator is only traversed, split, or queried for
     * estimated size after the terminal operation of the stream pipeline
     * commences.
     * 
     * <p>
     * If the collection is immutable it is recommended to report a
     * characteristic of {@code IMMUTABLE}. The characteristics {@code SIZED}
     * and {@code SUBSIZED} are additionally (automatically) reported unless
     * {@code CONCURRENT} is supplied.
     * 
     * @param <T>
     *            Type of elements
     * @param c
     *            The collection
     * @param characteristics
     *            Characteristics of the source collection's iterator or
     *            elements. The characteristics {@code SIZED} and
     *            {@code SUBSIZED} are additionally reported unless
     *            {@code CONCURRENT} is supplied.
     * @return a new sequential {@code Stream}
     * @throws NullPointerException if the given collection is {@code null}
     */
    public static <T> Stream<T> stream(Collection<? extends T> c, int characteristics) {
        return stream(c, characteristics, false);
    }

    /**
     * Creates a new sequential or parallel {@code Stream} using the given
     * collection's {@link java.util.Collection#iterator()} as the source of
     * elements for an internally created {@code Spliterator} which will report
     * the collection's {@link java.util.Collection#size()} as its initial size.
     *
     * <p>
     * The created spliterator is
     * <em><a href="../Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the collection's iterator, and
     * implements {@code trySplit} to permit limited parallelism.
     *
     * <p>
     * The created spliterator is only traversed, split, or queried for
     * estimated size after the terminal operation of the stream pipeline
     * commences.
     * <p>
     * If possible for the collection, it is strongly recommended to report a
     * characteristic of {@code IMMUTABLE} or {@code CONCURRENT} especially if
     * you want a parallel {@code Stream}. The characteristics {@code SIZED} and
     * {@code SUBSIZED} are additionally (automatically) reported unless
     * {@code CONCURRENT} is supplied.
     * 
     * @param <T>
     *            Type of elements
     * @param c
     *            The collection
     * @param characteristics
     *            Characteristics of the source collection's iterator or
     *            elements. The characteristics {@code SIZED} and
     *            {@code SUBSIZED} are additionally reported unless
     *            {@code CONCURRENT} is supplied.
     * @param parallel
     *            if {@code true} then the returned stream is a parallel stream;
     *            if {@code false} the returned stream is a sequential stream.
     * @return a new sequential or parallel {@code Stream}
     * @throws NullPointerException if the given collection is {@code null}
     */
    public static <T> Stream<T> stream(Collection<? extends T> c, int characteristics, boolean parallel) {
        Objects.requireNonNull(c);
        return stream(Spliterators.spliterator(c, characteristics), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code Stream} from a
     * {@code Spliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated
     * size after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #stream(java8.util.function.Supplier, int, boolean)} should be used
     * to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param <T> the type of stream elements
     * @param spliterator a {@code Spliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code Stream}
     */
    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            parallel);
    }

    /**
     * Creates a new sequential or parallel {@code Stream} from a
     * {@code Supplier} of {@code Spliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #stream(java8.util.Spliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param <T> the type of stream elements
     * @param supplier a {@code Supplier} of a {@code Spliterator}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code Spliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code Stream}
     * @see #stream(java8.util.Spliterator, boolean)
     */
    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier,
                                       int characteristics,
                                       boolean parallel) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head<>(supplier,
                                            StreamOpFlag.fromCharacteristics(characteristics),
                                            parallel);
    }

    /**
     * Creates a new sequential or parallel {@code IntStream} from a
     * {@code Spliterator.OfInt}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #intStream(java8.util.function.Supplier, int, boolean)} should be
     * used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator a {@code Spliterator.OfInt} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code IntStream}
     */
    public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head<>(spliterator,
                                      StreamOpFlag.fromCharacteristics(spliterator),
                                      parallel);
    }

    /**
     * Creates a new sequential or parallel {@code IntStream} from a
     * {@code Supplier} of {@code Spliterator.OfInt}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #intStream(java8.util.Spliterator.OfInt, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier a {@code Supplier} of a {@code Spliterator.OfInt}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code Spliterator.OfInt}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code IntStream}
     * @see #intStream(java8.util.Spliterator.OfInt, boolean)
     */
    public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier,
                                      int characteristics,
                                      boolean parallel) {
        return new IntPipeline.Head<>(supplier,
                                      StreamOpFlag.fromCharacteristics(characteristics),
                                      parallel);
    }

    /**
     * Creates a new sequential or parallel {@code LongStream} from a
     * {@code Spliterator.OfLong}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated
     * size after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #longStream(java8.util.function.Supplier, int, boolean)} should be
     * used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator a {@code Spliterator.OfLong} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code LongStream}
     */
    public static LongStream longStream(Spliterator.OfLong spliterator,
                                        boolean parallel) {
        return new LongPipeline.Head<>(spliterator,
                                       StreamOpFlag.fromCharacteristics(spliterator),
                                       parallel);
    }

    /**
     * Creates a new sequential or parallel {@code LongStream} from a
     * {@code Supplier} of {@code Spliterator.OfLong}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #longStream(java8.util.Spliterator.OfLong, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier a {@code Supplier} of a {@code Spliterator.OfLong}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code Spliterator.OfLong}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code LongStream}
     * @see #longStream(java8.util.Spliterator.OfLong, boolean)
     */
    public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier,
                                        int characteristics,
                                        boolean parallel) {
        return new LongPipeline.Head<>(supplier,
                                       StreamOpFlag.fromCharacteristics(characteristics),
                                       parallel);
    }

    /**
     * Creates a new sequential or parallel {@code DoubleStream} from a
     * {@code Spliterator.OfDouble}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #doubleStream(java8.util.function.Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code Spliterator.OfDouble} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code DoubleStream}
     */
    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator,
                                            boolean parallel) {
        return new DoublePipeline.Head<>(spliterator,
                                         StreamOpFlag.fromCharacteristics(spliterator),
                                         parallel);
    }

    /**
     * Creates a new sequential or parallel {@code DoubleStream} from a
     * {@code Supplier} of {@code Spliterator.OfDouble}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #doubleStream(java8.util.Spliterator.OfDouble, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfDouble}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code Spliterator.OfDouble}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code DoubleStream}
     * @see #doubleStream(java8.util.Spliterator.OfDouble, boolean)
     */
    public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier,
                                            int characteristics,
                                            boolean parallel) {
        return new DoublePipeline.Head<>(supplier,
                                         StreamOpFlag.fromCharacteristics(characteristics),
                                         parallel);
    }
}
