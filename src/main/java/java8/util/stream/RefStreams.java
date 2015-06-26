/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java8.util.Objects;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.function.UnaryOperator;
import java8.util.stream.Stream.Builder;

/**
 * A place for static default implementations of the new Java 8/9 static
 * interface methods and default interface methods ({@code takeWhile()},
 * {@code dropWhile()}) in the {@link Stream} interface.
 */
public final class RefStreams {

    /**
     * Returns a stream consisting of the longest prefix of elements of the
     * passed stream that match the given predicate.  If the passed stream is
     * unordered then the resulting prefix (of unordered elements) is
     * nondeterministic; the prefix will be selected from any one of the
     * possible permutations of the elements of the unordered stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * <p>This operation can accept a non-interfering stateful predicate to
     * support <em>cancellation</em> of the upstream pipeline.  The stateful
     * predicate may test against external state, such as time, or an
     * accumulating summary value of the elements of this stream.  A false
     * match, which triggers short-circuiting, is said to cancel the upstream
     * pipeline.  Cancellation is a necessary, but not sufficient, condition
     * for a) the stream to terminate normally in a finite time; and b) in a
     * time less than that if cancellation was not performed.
     *
     * <p>Cancellation is more appropriate for unordered stream or sequential
     * stream pipelines, and likely inappropriate for ordered and parallel
     * stream pipelines.  As is ordinarily the case, the resulting prefix will
     * be nondeterministic for unordered stream pipelines.  However, in such
     * cases the resulting prefix will also be nondeterministic for ordered and
     * parallel stream pipelines; the prefix will be a sub-prefix of that
     * produced by an equivalent ordered and sequential stream pipeline.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation obtains the {@link Stream#spliterator() spliterator}
     * of the passed stream, wraps that spliterator so as to support the
     * semantics of this operation on traversal, and returns a new stream
     * associated with the wrapped spliterator.  The returned stream preserves
     * the execution characteristics of the passed stream (namely parallel or
     * sequential execution as per {@link Stream#isParallel() isParallel()})
     * but the wrapped spliterator may choose to not support splitting.
     *
     * <p><b>API Note:</b><br>
     * While {@code takeWhile()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * Using an unordered stream source (such as {@link #generate(Supplier)})
     * or removing the ordering constraint with {@link Stream#unordered() unordered()}
     * may result in significant speedups of {@code takeWhile()} in parallel
     * pipelines, if the semantics of your situation permit.  If consistency
     * with encounter order is required, and you are experiencing poor
     * performance or memory utilization with {@code takeWhile()} in parallel
     * pipelines, switching to sequential execution with
     * {@link Stream#sequential() sequential()} may improve performance.
     *
     * <p>A use-case for stream cancellation is executing a stream pipeline
     * for a certain duration.  The following example will calculate as many
     * probable primes as is possible, in parallel, during 5 seconds:
     * <pre>{@code
     *     long t = System.currentTimeMillis();
     *     List<BigInteger> pps = RefStreams
     *         .generate(() -> BigInteger.probablePrime(1024, ThreadLocalRandom.current()))
     *         .parallel()
     *         .takeWhile(e -> (System.currentTimeMillis() - t) < TimeUnit.SECONDS.toMillis(5))
     *         .collect(toList());
     *
     * }</pre>
     *
     * @param <T> the type of the stream elements
     * @param stream the stream to wrap for the {@code takeWhile()} operation
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  predicate to apply elements to determine the longest
     *                  prefix of elements.
     * @return the new stream
     */
    public static<T> Stream<T> takeWhile(Stream<? extends T> stream, Predicate<? super T> predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);

        @SuppressWarnings("unchecked")
        Stream<T> s = (Stream<T>) stream;

        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.stream(
                new WhileOps.UnorderedWhileSpliterator.OfRef.Taking<>(s.spliterator(), true, predicate),
                s.isParallel());
    }

    /**
     * Returns a stream consisting of the remaining elements of the passed stream
     * after dropping the longest prefix of elements that match the given
     * predicate.  If the passed stream is unordered then the resulting prefix (of
     * unordered elements) is nondeterministic; the prefix will be selected
     * from any one of the possible permutations of the elements of the
     * unordered stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation obtains the {@link Stream#spliterator() spliterator}
     * of the passed stream, wraps that spliterator so as to support the
     * semantics of this operation on traversal, and returns a new stream
     * associated with the wrapped spliterator.  The returned stream preserves
     * the execution characteristics of the passed stream (namely parallel or
     * sequential execution as per {@link Stream#isParallel() isParallel()})
     * but the wrapped spliterator may choose to not support splitting.
     *
     * <p><b>API Note:</b><br>
     * While {@code dropWhile()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * Using an unordered stream source (such as {@link #generate(Supplier)})
     * or removing the ordering constraint with {@link Stream#unordered() unordered()}
     * may result in significant speedups of {@code dropWhile()} in parallel
     * pipelines, if the semantics of your situation permit.  If consistency
     * with encounter order is required, and you are experiencing poor
     * performance or memory utilization with {@code dropWhile()} in parallel
     * pipelines, switching to sequential execution with
     * {@link Stream#sequential() sequential()} may improve performance.
     *
     * @param <T> the type of the stream elements
     * @param stream  the stream to wrap for the {@code dropWhile()} operation
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply elements to determine the longest
     *                  prefix of elements.
     * @return the new stream
     */
    public static<T> Stream<T> dropWhile(Stream<? extends T> stream, Predicate<? super T> predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);

        @SuppressWarnings("unchecked")
        Stream<T> s = (Stream<T>) stream;

        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.stream(
                new WhileOps.UnorderedWhileSpliterator.OfRef.Dropping<>(s.spliterator(), true, predicate),
                s.isParallel());
    }

    /**
     * Returns a builder for a {@link Stream}.
     *
     * @param <T> type of elements
     * @return a stream builder
     */
    public static<T> Builder<T> builder() {
        return StreamSupport.builder();
    }

    /**
     * Returns an empty sequential {@link Stream}.
     *
     * @param <T> the type of stream elements
     * @return an empty sequential stream
     */
    public static<T> Stream<T> empty() {
        return StreamSupport.empty();
    }

    /**
     * Returns a sequential {@link Stream} containing a single element.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a singleton sequential stream
     */
    public static<T> Stream<T> of(T t) {
        return StreamSupport.of(t);
    }

    /**
     * Returns a sequential {@link Stream} containing a single element, if
     * non-null, otherwise returns an empty {@code Stream}.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a stream with a single element if the specified element
     *         is non-null, otherwise an empty stream
     * @since 1.9
     */
    public static<T> Stream<T> ofNullable(T t) {
        return StreamSupport.ofNullable(t);
    }

    /**
     * Returns a sequential ordered {@link Stream} whose elements are the
     * specified values.
     *
     * @param <T> the type of stream elements
     * @param values the elements of the new stream
     * @return the new stream
     */
    public static<T> Stream<T> of(@SuppressWarnings("unchecked") T... values) {
        return StreamSupport.of(values);
    }

    /**
     * Returns an infinite sequential ordered {@link Stream} produced by iterative
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
    public static<T> Stream<T> iterate(T seed, UnaryOperator<T> f) {
        return StreamSupport.iterate(seed, f);
    }

    /**
     * Returns an infinite sequential unordered {@link Stream} where each
     * element is generated by the provided {@code Supplier}.  This is
     * suitable for generating constant streams, streams of random elements,
     * etc.
     *
     * @param <T> the type of stream elements
     * @param s the {@code Supplier} of generated elements
     * @return a new infinite sequential unordered {@code Stream}
     */
    public static<T> Stream<T> generate(Supplier<T> s) {
        return StreamSupport.generate(s);
    }

    /**
     * Creates a lazily concatenated {@link Stream} whose elements are all the
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
        return StreamSupport.concat(a, b);
    }

    /**
     * Adds an element to the {@link Stream} being built represented by the
     * {@link Stream.Builder} argument.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation behaves as if:
     * <pre>{@code
     *     builder.accept(t)
     *     return builder;
     * }</pre>
     *
     * @param <T> the type of stream elements
     * @param builder the {@link Stream.Builder} to use
     * @param t the element to add
     * @return the passed builder
     * @throws IllegalStateException if the builder has already transitioned to
     * the built state
     */
    public static <T> Stream.Builder<T> add(Stream.Builder<T> builder, T t) {
        return StreamSupport.add(builder, t);
    }

    private RefStreams() {
    }
}
