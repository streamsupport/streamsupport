/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

import java8.lang.Longs;
import java8.util.Objects;
import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.LongConsumer;
import java8.util.function.LongPredicate;
import java8.util.function.LongSupplier;
import java8.util.function.LongUnaryOperator;
import java8.util.stream.LongStream.Builder;

/**
 * A place for static default implementations of the new Java 8/9 static
 * interface methods and default interface methods ({@code takeWhile()},
 * {@code dropWhile()}) in the {@link LongStream} interface. 
 */
public final class LongStreams {
    /**
     * Returns a stream consisting of elements of the passed stream that match
     * the given predicate up to, but discarding, the first element encountered
     * that does not match the predicate.  All subsequently encountered elements
     * are discarded.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * @param stream the stream to wrap for the {@code takeWhile()} operation
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  predicate to apply to each element to determine if it
     *                  should be included, or it and all subsequently
     *                  encountered elements be discarded.
     * @return the new stream
     * @since 9
     */
    public static LongStream takeWhile(LongStream stream, LongPredicate predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.longStream(
                new WhileOps.UnorderedWhileSpliterator.OfLong.Taking(stream.spliterator(), true, predicate),
                stream.isParallel()).onClose(stream::close);
    }

    /**
     * Returns a stream consisting of the remaining elements of the passed
     * stream after discarding elements that match the given predicate up to,
     * but not discarding, the first element encountered that does not match
     * the predicate.  All subsequently encountered elements are not discarded.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @param stream the stream to wrap for the {@code dropWhile()} operation
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  predicate to apply to each element to determine if it
     *                  should be discarded, or it and all subsequently
     *                  encountered elements be included.
     * @return the new stream
     * @since 9
     */
    public static LongStream dropWhile(LongStream stream, LongPredicate predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.longStream(
                new WhileOps.UnorderedWhileSpliterator.OfLong.Dropping(stream.spliterator(), true, predicate),
                stream.isParallel()).onClose(stream::close);
    }

    // Static factories

    /**
     * Returns a builder for a {@code LongStream}.
     *
     * @return a stream builder
     */
    public static Builder builder() {
        return new Streams.LongStreamBuilderImpl();
    }

    /**
     * Returns an empty sequential {@code LongStream}.
     *
     * @return an empty sequential stream
     */
    public static LongStream empty() {
        return StreamSupport.longStream(Spliterators.emptyLongSpliterator(), false);
    }

    /**
     * Returns a sequential {@code LongStream} containing a single element.
     *
     * @param t the single element
     * @return a singleton sequential stream
     */
    public static LongStream of(long t) {
        return StreamSupport.longStream(new Streams.LongStreamBuilderImpl(t), false);
    }

    /**
     * Returns a sequential ordered stream whose elements are the specified values.
     *
     * @param values the elements of the new stream
     * @return the new stream
     */
    public static LongStream of(long... values) {
        return java8.util.J8Arrays.stream(values);
    }

    /**
     * Returns an infinite sequential ordered {@code LongStream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code LongStream} will
     * be the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * @param seed the initial element
     * @param f a function to be applied to the previous element to produce
     *          a new element
     * @return a new sequential {@code LongStream}
     */
    public static LongStream iterate(long seed, LongUnaryOperator f) {
        Objects.requireNonNull(f);
        Spliterator.OfLong spliterator = new Spliterators.AbstractLongSpliterator(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL) {
            long prev;
            boolean started;

            @Override
            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);
                long t;
                if (started) {
                    t = f.applyAsLong(prev);
                } else {
                    t = seed;
                    started = true;
                }
                action.accept(prev = t);
                return true;
            }
        };
        return StreamSupport.longStream(spliterator, false);
    }

    /**
     * Returns a sequential ordered {@code LongStream} produced by iterative
     * application of a function to an initial element, conditioned on 
     * satisfying the supplied predicate.  The stream terminates as soon as
     * the predicate function returns false.
     *
     * <p>
     * {@code LongStreams.iterate} should produce the same sequence of elements
     * as produced by the corresponding for-loop:
     * <pre>{@code
     *     for (long index=seed; predicate.test(index); index = f.applyAsLong(index)) { 
     *         ... 
     *     }
     * }</pre>
     *
     * <p>
     * The resulting sequence may be empty if the predicate does not hold on 
     * the seed value.  Otherwise the first element will be the supplied seed
     * value, the next element (if present) will be the result of applying the
     * function f to the seed value, and so on iteratively until the predicate
     * indicates that the stream should terminate.
     *
     * @param seed the initial element
     * @param predicate a predicate to apply to elements to determine when the 
     *          stream must terminate.
     * @param f a function to be applied to the previous element to produce
     *          a new element
     * @return a new sequential {@code LongStream}
     * @since 9
     */
    public static LongStream iterate(long seed, LongPredicate predicate, LongUnaryOperator f) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        Spliterator.OfLong spliterator = new Spliterators.AbstractLongSpliterator(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL) {
            long prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return false;
                }
                long t;
                if (started) {
                    t = f.applyAsLong(prev);
                } else {
                    t = seed;
                    started = true;
                }
                if (!predicate.test(t)) {
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return;
                }
                finished = true;
                long t = started ? f.applyAsLong(prev) : seed;
                while (predicate.test(t)) {
                    action.accept(t);
                    t = f.applyAsLong(t);
                }
            }
        };
        return StreamSupport.longStream(spliterator, false);
    }

    /**
     * Returns an infinite sequential unordered stream where each element is
     * generated by the provided {@code LongSupplier}.  This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param s the {@code LongSupplier} for generated elements
     * @return a new infinite sequential unordered {@code LongStream}
     */
    public static LongStream generate(LongSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.longStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfLong(Long.MAX_VALUE, s), false);
    }

    /**
     * Returns a sequential ordered {@code LongStream} from {@code startInclusive}
     * (inclusive) to {@code endExclusive} (exclusive) by an incremental step of
     * {@code 1}.
     *
     * <p><b>API Note:</b><br>
     * <p>An equivalent sequence of increasing values can be produced
     * sequentially using a {@code for} loop as follows:
     * <pre>{@code
     *     for (long i = startInclusive; i < endExclusive ; i++) { ... }
     * }</pre>
     *
     * @param startInclusive the (inclusive) initial value
     * @param endExclusive the exclusive upper bound
     * @return a sequential {@code LongStream} for the range of {@code long}
     *         elements
     */
    public static LongStream range(long startInclusive, final long endExclusive) {
        if (startInclusive >= endExclusive) {
            return empty();
        } else if (endExclusive - startInclusive < 0) {
            // Size of range > Long.MAX_VALUE
            // Split the range in two and concatenate
            // Note: if the range is [Long.MIN_VALUE, Long.MAX_VALUE) then
            // the lower range, [Long.MIN_VALUE, 0) will be further split in two
            long m = startInclusive + Longs.divideUnsigned(endExclusive - startInclusive, 2) + 1;
            return concat(range(startInclusive, m), range(m, endExclusive));
        } else {
            return StreamSupport.longStream(
                    new Streams.RangeLongSpliterator(startInclusive, endExclusive, false), false);
        }
    }

    /**
     * Returns a sequential ordered {@code LongStream} from {@code startInclusive}
     * (inclusive) to {@code endInclusive} (inclusive) by an incremental step of
     * {@code 1}.
     *
     * <p><b>API Note:</b><br>
     * <p>An equivalent sequence of increasing values can be produced
     * sequentially using a {@code for} loop as follows:
     * <pre>{@code
     *     for (long i = startInclusive; i <= endInclusive ; i++) { ... }
     * }</pre>
     *
     * @param startInclusive the (inclusive) initial value
     * @param endInclusive the inclusive upper bound
     * @return a sequential {@code LongStream} for the range of {@code long}
     *         elements
     */
    public static LongStream rangeClosed(long startInclusive, final long endInclusive) {
        if (startInclusive > endInclusive) {
            return empty();
        } else if (endInclusive - startInclusive + 1 <= 0) {
            // Size of range > Long.MAX_VALUE
            // Split the range in two and concatenate
            // Note: if the range is [Long.MIN_VALUE, Long.MAX_VALUE] then
            // the lower range, [Long.MIN_VALUE, 0), and upper range,
            // [0, Long.MAX_VALUE], will both be further split in two
            long m = startInclusive + Longs.divideUnsigned(endInclusive - startInclusive, 2) + 1;
            return concat(range(startInclusive, m), rangeClosed(m, endInclusive));
        } else {
            return StreamSupport.longStream(
                    new Streams.RangeLongSpliterator(startInclusive, endInclusive, true), false);
        }
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
     * <p>Subsequent changes to the sequential/parallel execution mode of the
     * returned stream are not guaranteed to be propagated to the input streams.
     *
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public static LongStream concat(LongStream a, LongStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        Spliterator.OfLong split = new Streams.ConcatSpliterator.OfLong(
                a.spliterator(), b.spliterator());
        LongStream stream = StreamSupport.longStream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    private LongStreams() {
    }
}
