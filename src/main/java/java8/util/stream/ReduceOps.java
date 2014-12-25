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

import java8.util.concurrent.CountedCompleter;
import java8.util.Objects;
import java8.util.function.BiConsumer;
import java8.util.function.BiFunction;
import java8.util.function.BinaryOperator;
import java8.util.function.DoubleBinaryOperator;
import java8.util.function.IntBinaryOperator;
import java8.util.function.LongBinaryOperator;
import java8.util.function.ObjDoubleConsumer;
import java8.util.function.ObjIntConsumer;
import java8.util.function.ObjLongConsumer;
import java8.util.function.Supplier;
import java8.util.Optional;
import java8.util.OptionalDouble;
import java8.util.OptionalInt;
import java8.util.OptionalLong;
import java8.util.Spliterator;

/**
 * Factory for creating instances of {@code TerminalOp} that implement
 * reductions.
 *
 * @since 1.8
 */
final class ReduceOps {

    private ReduceOps() { }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <U> the type of the result
     * @param seed the identity element for the reduction
     * @param reducer the accumulating function that incorporates an additional
     *        input element into the result
     * @param combiner the combining function that combines two intermediate
     *        results
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <T, U> TerminalOp<T, U>
    makeRef(final U seed, final BiFunction<U, ? super T, U> reducer, final BinaryOperator<U> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<U> implements AccumulatingSink<T, U, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seed;
            }

            @Override
            public void accept(T t) {
                state = reducer.apply(state, t);
            }

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, U, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * reference values producing an optional reference result.
     *
     * @param <T> The type of the input elements, and the type of the result
     * @param operator The reducing function
     * @return A {@code TerminalOp} implementing the reduction
     */
    public static <T> TerminalOp<T, Optional<T>>
    makeRef(final BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<T, Optional<T>, ReducingSink> {
            private boolean empty;
            private T state;

            public void begin(long size) {
                empty = true;
                state = null;
            }

            @Override
            public void accept(T t) {
                if (empty) {
                    empty = false;
                    state = t;
                } else {
                    state = operator.apply(state, t);
                }
            }

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public Optional<T> get() {
                return empty ? Optional.empty() : Optional.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<T, Optional<T>, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <I> the type of the intermediate reduction result
     * @param collector a {@code Collector} defining the reduction
     * @return a {@code ReduceOp} implementing the reduction
     */
    public static <T, I> TerminalOp<T, I>
    makeRef(final Collector<? super T, I, ?> collector) {
    	final Supplier<I> supplier = Objects.requireNonNull(collector).supplier();
        final BiConsumer<I, ? super T> accumulator = collector.accumulator();
        final BinaryOperator<I> combiner = collector.combiner();
        class ReducingSink extends Box<I>
                implements AccumulatingSink<T, I, ReducingSink> {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, I, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }

            @Override
            public int getOpFlags() {
                return collector.characteristics().contains(Collector.Characteristics.UNORDERED)
                       ? StreamOpFlag.NOT_ORDERED
                       : 0;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <R> the type of the result
     * @param seedFactory a factory to produce a new base accumulator
     * @param accumulator a function to incorporate an element into an
     *        accumulator
     * @param reducer a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <T, R> TerminalOp<T, R>
    makeRef(final Supplier<R> seedFactory,
    		final BiConsumer<R, ? super T> accumulator,
    		final BiConsumer<R,R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<T, R, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seedFactory.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public void combine(ReducingSink other) {
                reducer.accept(state, other.state);
            }
        }
        return new ReduceOp<T, R, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code int} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Integer, Integer>
    makeInt(final int identity, final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, Integer, ReducingSink>, Sink.OfInt {
            private int state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(int t) {
                state = operator.applyAsInt(state, t);
            }

            @Override
			public void accept(Integer t) {
				SinkDefaults.OfInt.accept(this, t);
			}

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public Integer get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Integer, Integer, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code int} values, producing an optional integer result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Integer, OptionalInt>
    makeInt(final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, OptionalInt, ReducingSink>, Sink.OfInt {
            private boolean empty;
            private int state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(int t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsInt(state, t);
                }
            }

            @Override
			public void accept(Integer t) {
				SinkDefaults.OfInt.accept(this, t);
			}

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public OptionalInt get() {
                return empty ? OptionalInt.empty() : OptionalInt.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Integer, OptionalInt, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code int} values.
     *
     * @param <R> The type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return A {@code ReduceOp} implementing the reduction
     */
    public static <R> TerminalOp<Integer, R>
    makeInt(final Supplier<R> supplier,
    		final ObjIntConsumer<R> accumulator,
    		final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Integer, R, ReducingSink>, Sink.OfInt {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(int t) {
                accumulator.accept(state, t);
            }

            @Override
			public void accept(Integer t) {
				SinkDefaults.OfInt.accept(this, t);
			}

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Integer, R, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code long} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Long, Long>
    makeLong(final long identity, final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, Long, ReducingSink>, Sink.OfLong {
            private long state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(long t) {
                state = operator.applyAsLong(state, t);
            }

            @Override
			public void accept(Long t) {
				SinkDefaults.OfLong.accept(this, t);
			}

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public Long get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Long, Long, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code long} values, producing an optional long result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Long, OptionalLong>
    makeLong(final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, OptionalLong, ReducingSink>, Sink.OfLong {
            private boolean empty;
            private long state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(long t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsLong(state, t);
                }
            }

            @Override
			public void accept(Long t) {
				SinkDefaults.OfLong.accept(this, t);
			}

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public OptionalLong get() {
                return empty ? OptionalLong.empty() : OptionalLong.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Long, OptionalLong, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code long} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Long, R>
    makeLong(final Supplier<R> supplier,
    		final ObjLongConsumer<R> accumulator,
    		final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Long, R, ReducingSink>, Sink.OfLong {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(long t) {
                accumulator.accept(state, t);
            }

            @Override
			public void accept(Long t) {
				SinkDefaults.OfLong.accept(this, t);
			}

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(double value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Long, R, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Double, Double>
    makeDouble(final double identity, final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, Double, ReducingSink>, Sink.OfDouble {
            private double state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(double t) {
                state = operator.applyAsDouble(state, t);
            }

    		@Override
    		public void accept(Double i) {
    			SinkDefaults.OfDouble.accept(this, i);
    		}

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public Double get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Double, Double, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Double, OptionalDouble>
    makeDouble(final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, OptionalDouble, ReducingSink>, Sink.OfDouble {
            private boolean empty;
            private double state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(double t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsDouble(state, t);
                }
            }

    		@Override
    		public void accept(Double i) {
    			SinkDefaults.OfDouble.accept(this, i);
    		}

            @Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}

			@Override
            public OptionalDouble get() {
                return empty ? OptionalDouble.empty() : OptionalDouble.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Double, OptionalDouble, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Double, R>
    makeDouble(final Supplier<R> supplier,
    		final ObjDoubleConsumer<R> accumulator,
    		final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Double, R, ReducingSink>, Sink.OfDouble {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(double t) {
                accumulator.accept(state, t);
            }

    		@Override
    		public void accept(Double i) {
    			SinkDefaults.OfDouble.accept(this, i);
    		}

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }

			@Override
			public void end() {
				SinkDefaults.end(this);
			}

			@Override
			public boolean cancellationRequested() {
				return SinkDefaults.cancellationRequested(this);
			}

			@Override
			public void accept(int value) {
				SinkDefaults.accept(this, value);
			}

			@Override
			public void accept(long value) {
				SinkDefaults.accept(this, value);
			}
        }
        return new ReduceOp<Double, R, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * A type of {@code TerminalSink} that implements an associative reducing
     * operation on elements of type {@code T} and producing a result of type
     * {@code R}.
     *
     * @param <T> the type of input element to the combining operation
     * @param <R> the result type
     * @param <K> the type of the {@code AccumulatingSink}.
     */
    private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>>
            extends TerminalSink<T, R> {
        public void combine(K other);
    }

    /**
     * State box for a single state element, used as a base class for
     * {@code AccumulatingSink} instances
     *
     * @param <U> The type of the state element
     */
    private static abstract class Box<U> {
        U state;

        Box() {} // Avoid creation of special accessor

        public U get() {
            return state;
        }
    }

    /**
     * A {@code TerminalOp} that evaluates a stream pipeline and sends the
     * output into an {@code AccumulatingSink}, which performs a reduce
     * operation. The {@code AccumulatingSink} must represent an associative
     * reducing operation.
     *
     * @param <T> the output type of the stream pipeline
     * @param <R> the result type of the reducing operation
     * @param <S> the type of the {@code AccumulatingSink}
     */
    private static abstract class ReduceOp<T, R, S extends AccumulatingSink<T, R, S>>
            implements TerminalOp<T, R> {
        private final StreamShape inputShape;

        /**
         * Create a {@code ReduceOp} of the specified stream shape which uses
         * the specified {@code Supplier} to create accumulating sinks.
         *
         * @param shape The shape of the stream pipeline
         */
        ReduceOp(StreamShape shape) {
            inputShape = shape;
        }

        public abstract S makeSink();

        @Override
        public StreamShape inputShape() {
            return inputShape;
        }

        @Override
		public int getOpFlags() {
			return TerminalOpDefaults.getOpFlags();
		}

		@Override
        public <P_IN> R evaluateSequential(PipelineHelper<T> helper,
                                           Spliterator<P_IN> spliterator) {
            return helper.wrapAndCopyInto(makeSink(), spliterator).get();
        }

        @Override
        public <P_IN> R evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, helper, spliterator).invoke().get();
        }
    }

    /**
     * A {@code ForkJoinTask} for performing a parallel reduce operation.
     */
    @SuppressWarnings("serial")
    private static final class ReduceTask<P_IN, P_OUT, R,
                                          S extends AccumulatingSink<P_OUT, R, S>>
            extends AbstractTask<P_IN, P_OUT, S, ReduceTask<P_IN, P_OUT, R, S>> {
        private final ReduceOp<P_OUT, R, S> op;

        ReduceTask(ReduceOp<P_OUT, R, S> op,
                   PipelineHelper<P_OUT> helper,
                   Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        ReduceTask(ReduceTask<P_IN, P_OUT, R, S> parent,
                   Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected ReduceTask<P_IN, P_OUT, R, S> makeChild(Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, spliterator);
        }

        @Override
        protected S doLeaf() {
            return helper.wrapAndCopyInto(op.makeSink(), spliterator);
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                S leftResult = leftChild.getLocalResult();
                leftResult.combine(rightChild.getLocalResult());
                setLocalResult(leftResult);
            }
            // GC spliterator, left and right child
            super.onCompletion(caller);
        }
    }
}
