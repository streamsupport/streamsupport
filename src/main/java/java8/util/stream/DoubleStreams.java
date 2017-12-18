/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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
import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.DoubleConsumer;
import java8.util.function.DoublePredicate;
import java8.util.function.DoubleSupplier;
import java8.util.function.DoubleUnaryOperator;
import java8.util.stream.DoubleStream.Builder;

/**
 * A place for static default implementations of the new Java 8/9 static
 * interface methods and default interface methods ({@code takeWhile()},
 * {@code dropWhile()}) in the {@link DoubleStream} interface.
 */
public final class DoubleStreams {

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
    public static DoubleStream takeWhile(DoubleStream stream, DoublePredicate predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.doubleStream(
                new WhileOps.UnorderedWhileSpliterator.OfDouble.Taking(stream.spliterator(), true, predicate),
                stream.isParallel()).onClose(StreamSupport.closeHandler(stream));
    }

    /**
     * Returns a stream consisting of the remaining elements of the passed
     * stream after discarding elements that match the given predicate up to,
     * but not discarding, the first element encountered that does not match the
     * predicate.  All subsequently encountered elements are not discarded.
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
    public static DoubleStream dropWhile(DoubleStream stream, DoublePredicate predicate) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.doubleStream(
                new WhileOps.UnorderedWhileSpliterator.OfDouble.Dropping(stream.spliterator(), true, predicate),
                stream.isParallel()).onClose(StreamSupport.closeHandler(stream));
    }

    // Static factories

    /**
     * Returns a builder for a {@code DoubleStream}.
     *
     * @return a stream builder
     */
    public static Builder builder() {
        return new Streams.DoubleStreamBuilderImpl();
    }

    /**
     * Returns an empty sequential {@code DoubleStream}.
     *
     * @return an empty sequential stream
     */
    public static DoubleStream empty() {
        return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator(), false);
    }

    /**
     * Returns a sequential {@code DoubleStream} containing a single element.
     *
     * @param t the single element
     * @return a singleton sequential stream
     */
    public static DoubleStream of(double t) {
        return StreamSupport.doubleStream(new Streams.DoubleStreamBuilderImpl(t), false);
    }

    /**
     * Returns a sequential ordered stream whose elements are the specified values.
     *
     * @param values the elements of the new stream
     * @return the new stream
     */
    public static DoubleStream of(double... values) {
        return java8.util.J8Arrays.stream(values);
    }

    /**
     * Returns an infinite sequential ordered {@code DoubleStream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code DoubleStream}
     * will be the provided {@code seed}.  For {@code n > 0}, the element at
     * position {@code n}, will be the result of applying the function {@code f}
     * to the element at position {@code n - 1}.
     *
     * <p>The action of applying {@code f} for one element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying {@code f} for subsequent elements.  For any given
     * element the action may be performed in whatever thread the library
     * chooses.
     *
     * @param seed the initial element
     * @param f a function to be applied to the previous element to produce
     *          a new element
     * @return a new sequential {@code DoubleStream}
     */
    public static DoubleStream iterate(double seed, DoubleUnaryOperator f) {
        Objects.requireNonNull(f);
        Spliterator.OfDouble spliterator = new Spliterators.AbstractDoubleSpliterator(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL) {
            double prev;
            boolean started;

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);
                double t;
                if (started) {
                    t = f.applyAsDouble(prev);
                } else {
                    t = seed;
                    started = true;
                }
                action.accept(prev = t);
                return true;
            }
        };
        return StreamSupport.doubleStream(spliterator, false);
    }

    /**
     * Returns a sequential ordered {@code DoubleStream} produced by iterative
     * application of the given {@code next} function to an initial element,
     * conditioned on satisfying the given {@code hasNext} predicate.  The
     * stream terminates as soon as the {@code hasNext} predicate returns false.
     *
     * <p>{@code DoubleStreams.iterate} should produce the same sequence of
     * elements as produced by the corresponding for-loop:
     * <pre>{@code
     *     for (double index=seed; hasNext.test(index); index = next.applyAsDouble(index)) {
     *         ... 
     *     }
     * }</pre>
     *
     * <p>The resulting sequence may be empty if the {@code hasNext} predicate
     * does not hold on the seed value.  Otherwise the first element will be the
     * supplied {@code seed} value, the next element (if present) will be the
     * result of applying the {@code next} function to the {@code seed} value,
     * and so on iteratively until the {@code hasNext} predicate indicates that
     * the stream should terminate.
     *
     * <p>The action of applying the {@code hasNext} predicate to an element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying the {@code next} function to that element.  The
     * action of applying the {@code next} function for one element
     * <i>happens-before</i> the action of applying the {@code hasNext}
     * predicate for subsequent elements.  For any given element an action may
     * be performed in whatever thread the library chooses.
     *
     * @param seed the initial element
     * @param hasNext a predicate to apply to elements to determine when the 
     *                stream must terminate
     * @param next a function to be applied to the previous element to produce
     *             a new element
     * @return a new sequential {@code DoubleStream}
     * @since 9
     */
    public static DoubleStream iterate(double seed, DoublePredicate hasNext, DoubleUnaryOperator next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator.OfDouble spliterator = new Spliterators.AbstractDoubleSpliterator(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL) {
            double prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return false;
                }
                double t;
                if (started) {
                    t = next.applyAsDouble(prev);
                } else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return;
                }
                finished = true;
                double t = started ? next.applyAsDouble(prev) : seed;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.applyAsDouble(t);
                }
            }
        };
        return StreamSupport.doubleStream(spliterator, false);
    }

    /**
     * Returns an infinite sequential unordered stream where each element is
     * generated by the provided {@code DoubleSupplier}.  This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param s the {@code DoubleSupplier} for generated elements
     * @return a new infinite sequential unordered {@code DoubleStream}
     */
    public static DoubleStream generate(DoubleSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.doubleStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfDouble(Long.MAX_VALUE, s), false);
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     * 
     * <p>This method operates on the two input streams and binds each stream
     * to its source.  As a result subsequent modifications to an input stream
     * source may not be reflected in the concatenated stream result.
     * 
     * <p><b>Implementation Note:</b><br>
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowError}.
     * <p>Subsequent changes to the sequential/parallel execution mode of the
     * returned stream are not guaranteed to be propagated to the input streams.
     * 
     * <p><b>API Note:</b><br>
     * To preserve optimization opportunities this method binds each stream to
     * its source and accepts only two streams as parameters.  For example, the
     * exact size of the concatenated stream source can be computed if the exact
     * size of each input stream source is known.
     * To concatenate more streams without binding, or without nested calls to
     * this method, try creating a stream of streams and flat-mapping with the
     * identity function, for example:
     * <pre>{@code
     *     DoubleStream concat = RefStreams.of(s1, s2, s3, s4).flatMapToDouble(s -> s);
     * }</pre>
     * 
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public static DoubleStream concat(DoubleStream a, DoubleStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        Spliterator.OfDouble split = new Streams.ConcatSpliterator.OfDouble(
                a.spliterator(), b.spliterator());
        DoubleStream stream = StreamSupport.doubleStream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    private DoubleStreams() {
    }
}
