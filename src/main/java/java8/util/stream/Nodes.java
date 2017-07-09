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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;

import java8.util.concurrent.CountedCompleter;
import java8.util.Objects;
import java8.util.function.BinaryOperator;
import java8.util.function.Consumer;
import java8.util.function.DoubleConsumer;
import java8.util.function.IntConsumer;
import java8.util.function.IntFunction;
import java8.util.function.LongConsumer;
import java8.util.function.LongFunction;
import java8.util.Spliterator;
import java8.util.Spliterators;

/**
 * Factory methods for constructing implementations of {@link Node} and
 * {@link Node.Builder} and their primitive specializations.  Fork/Join tasks
 * for collecting output from a {@link PipelineHelper} to a {@link Node} and
 * flattening {@link Node}s.
 *
 * @since 1.8
 */
final class Nodes {

    private Nodes() {
        throw new Error("no instances");
    }

    /**
     * The maximum size of an array that can be allocated.
     */
    static final long MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    // IllegalArgumentException messages
    static final String BAD_SIZE = "Stream size exceeds max array size";

    @SuppressWarnings("rawtypes")
    private static final Node EMPTY_NODE = new EmptyNode.OfRef();
    private static final Node.OfInt EMPTY_INT_NODE = new EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new EmptyNode.OfLong();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new EmptyNode.OfDouble();

    /**
     * Gets the {@code StreamShape} associated with this {@code Node}.
     *
     * <p><b>Implementation Requirements:</b><br> The default in {@code Node} returns
     * {@code StreamShape.REFERENCE}
     *
     * @return the stream shape associated with this node
     */
    static <T> StreamShape getShape() {
        return StreamShape.REFERENCE;
    }

    /**
     * Returns the number of child nodes of this node.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation returns zero.
     *
     * @return the number of child nodes
     */
    static <T> int getChildCount() {
        return 0;
    }

    /**
     * Retrieves the child {@code Node} at a given index.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation always throws
     * {@code IndexOutOfBoundsException}.
     *
     * @param i the index to the child node
     * @return the child node
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater
     *         than or equal to the number of child nodes
     */
    static <T> Node<T> getChild() {
        throw new IndexOutOfBoundsException();
    }

    /**
     * Return a node describing a subsequence of the elements of this node,
     * starting at the given inclusive start offset and ending at the given
     * exclusive end offset.
     *
     * @param from The (inclusive) starting offset of elements to include, must
     *             be in range 0..count().
     * @param to The (exclusive) end offset of elements to include, must be
     *           in range 0..count().
     * @param generator A function to be used to create a new array, if needed,
     *                  for reference nodes.
     * @return the truncated node
     */
    static <T> Node<T> truncate(Node<T> node, long from, long to, IntFunction<T[]> generator) {
        if (from == 0 && to == node.count()) {
            return node;
        }
        Spliterator<T> spliterator = node.spliterator();
        long size = to - from;
        Node.Builder<T> nodeBuilder = Nodes.builder(size, generator);
        nodeBuilder.begin(size);
        for (int i = 0; i < from && spliterator.tryAdvance(e -> { }); i++) { }
        if (to == node.count()) {
            spliterator.forEachRemaining(nodeBuilder);
        } else {
            for (int i = 0; i < size && spliterator.tryAdvance(nodeBuilder); i++) { }
        }
        nodeBuilder.end();
        return nodeBuilder.build();
    }

    static final class OfPrimitive {
        static <T, T_CONS, T_ARR, T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> T_NODE getChild() {
            throw new IndexOutOfBoundsException();
        }

        static <T, T_CONS, T_ARR, T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> T[] asArray(Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> this_, IntFunction<T[]> generator) {
            long size = this_.count();
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            T[] boxed = generator.apply((int) this_.count());
            this_.copyInto(boxed, 0);
            return boxed;
        }

        private OfPrimitive() {
        }
    }

    static final class OfDouble {

        static Node.OfDouble truncate(Node.OfDouble this_, long from, long to, IntFunction<Double[]> generator) {
            if (from == 0 && to == this_.count())
                return this_;
            long size = to - from;
            Spliterator.OfDouble spliterator = this_.spliterator();
            Node.Builder.OfDouble nodeBuilder = Nodes.doubleBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((DoubleConsumer) e -> { }); i++) { }
            if (to == this_.count()) {
                spliterator.forEachRemaining((DoubleConsumer) nodeBuilder);
            } else {
                for (int i = 0; i < size && spliterator.tryAdvance((DoubleConsumer) nodeBuilder); i++) { }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer A {@code Consumer} that is to be invoked with each
         *        element in this {@code Node}.  If this is an
         *        {@code DoubleConsumer}, it is cast to {@code DoubleConsumer}
         *        so the elements may be processed without boxing.
         */
        static void forEach(Node.OfDouble this_, Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                this_.forEach((DoubleConsumer) consumer);
            }
            else {
                this_.spliterator().forEachRemaining(consumer);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br> the default implementation invokes {@link #asPrimitiveArray()}
         * to obtain a double[] array then and copies the elements from that
         * double[] array into the boxed Double[] array.  This is not efficient
         * and it is recommended to invoke {@link #copyInto(Object, int)}.
         */
        static void copyInto(Node.OfDouble this_, Double[] boxed, int offset) {
            double[] array = this_.asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        static double[] newArray(Node.OfDouble this_, int count) {
            return new double[count];
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br> The default in {@code Node.OfDouble} returns
         * {@code StreamShape.DOUBLE_VALUE}
         */
        static StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }

        private OfDouble () {
        }
    }

    static final class OfLong {

        static Node.OfLong truncate(Node.OfLong this_, long from, long to, IntFunction<Long[]> generator) {
            if (from == 0 && to == this_.count())
                return this_;
            long size = to - from;
            Spliterator.OfLong spliterator = this_.spliterator();
            Node.Builder.OfLong nodeBuilder = Nodes.longBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((LongConsumer) e -> { }); i++) { }
            if (to == this_.count()) {
                spliterator.forEachRemaining((LongConsumer) nodeBuilder);
            } else {
                for (int i = 0; i < size && spliterator.tryAdvance((LongConsumer) nodeBuilder); i++) { }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer A {@code Consumer} that is to be invoked with each
         *        element in this {@code Node}.  If this is an
         *        {@code LongConsumer}, it is cast to {@code LongConsumer} so
         *        the elements may be processed without boxing.
         */
        static void forEach(Node.OfLong this_, Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                this_.forEach((LongConsumer) consumer);
            }
            else {
                this_.spliterator().forEachRemaining(consumer);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br> the default implementation invokes {@link #asPrimitiveArray()}
         * to obtain a long[] array then and copies the elements from that
         * long[] array into the boxed Long[] array.  This is not efficient and
         * it is recommended to invoke {@link #copyInto(Object, int)}.
         */
        static void copyInto(Node.OfLong this_, Long[] boxed, int offset) {
            long[] array = this_.asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        static long[] newArray(Node.OfLong this_, int count) {
            return new long[count];
        }

        /**
         * {@inheritDoc}
         * <p><b>Implementation Requirements:</b><br> The default in {@code Node.OfLong} returns
         * {@code StreamShape.LONG_VALUE}
         */
        static StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }

        private OfLong() {
        }
    }

    static final class OfInt {

        static Node.OfInt truncate(Node.OfInt this_, long from, long to, IntFunction<Integer[]> generator) {
            if (from == 0 && to == this_.count())
                return this_;
            long size = to - from;
            Spliterator.OfInt spliterator = this_.spliterator();
            Node.Builder.OfInt nodeBuilder = Nodes.intBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((IntConsumer) e -> { }); i++) { }
            if (to == this_.count()) {
                spliterator.forEachRemaining((IntConsumer) nodeBuilder);
            } else {
                for (int i = 0; i < size && spliterator.tryAdvance((IntConsumer) nodeBuilder); i++) { }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer a {@code Consumer} that is to be invoked with each
         *        element in this {@code Node}.  If this is an
         *        {@code IntConsumer}, it is cast to {@code IntConsumer} so the
         *        elements may be processed without boxing.
         */
        static void forEach(Node.OfInt this_, Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                this_.forEach((IntConsumer) consumer);
            }
            else {
                this_.spliterator().forEachRemaining(consumer);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>Implementation Requirements:</b><br> the default implementation invokes {@link #asPrimitiveArray()} to
         * obtain an int[] array then and copies the elements from that int[]
         * array into the boxed Integer[] array.  This is not efficient and it
         * is recommended to invoke {@link #copyInto(Object, int)}.
         */
        static void copyInto(Node.OfInt this_, Integer[] boxed, int offset) {
            int[] array = this_.asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        static int[] newArray(Node.OfInt this_, int count) {
            return new int[count];
        }

        /**
         * {@inheritDoc}
         * <p><b>Implementation Requirements:</b><br> The default in {@code Node.OfInt} returns
         * {@code StreamShape.INT_VALUE}
         */
        static StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }

        private OfInt() {
        }
    }

    /**
     * @return an array generator for an array whose elements are of type T.
     */
    @SuppressWarnings("unchecked")
    static <T> IntFunction<T[]> castingArray() {
        return size -> (T[]) new Object[size];
    }

    // General shape-based node creation methods

    /**
     * Produces an empty node whose count is zero, has no children and no content.
     *
     * @param <T> the type of elements of the created node
     * @param shape the shape of the node to be created
     * @return an empty node.
     */
    @SuppressWarnings("unchecked")
    static <T> Node<T> emptyNode(StreamShape shape) {
        switch (shape) {
            case REFERENCE:    return (Node<T>) EMPTY_NODE;
            case INT_VALUE:    return (Node<T>) EMPTY_INT_NODE;
            case LONG_VALUE:   return (Node<T>) EMPTY_LONG_NODE;
            case DOUBLE_VALUE: return (Node<T>) EMPTY_DOUBLE_NODE;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    /**
     * Produces a concatenated {@link Node} that has two or more children.
     * <p>The count of the concatenated node is equal to the sum of the count
     * of each child. Traversal of the concatenated node traverses the content
     * of each child in encounter order of the list of children. Splitting a
     * spliterator obtained from the concatenated node preserves the encounter
     * order of the list of children.
     *
     * <p>The result may be a concatenated node, the input sole node if the size
     * of the list is 1, or an empty node.
     *
     * @param <T> the type of elements of the concatenated node
     * @param shape the shape of the concatenated node to be created
     * @param left the left input node
     * @param right the right input node
     * @return a {@code Node} covering the elements of the input nodes
     * @throws IllegalStateException if all {@link Node} elements of the list
     * are an not instance of type supported by this factory.
     */
    @SuppressWarnings("unchecked")
    static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        switch (shape) {
            case REFERENCE:
                return new ConcNode<>(left, right);
            case INT_VALUE:
                return (Node<T>) new ConcNode.OfInt((Node.OfInt) left, (Node.OfInt) right);
            case LONG_VALUE:
                return (Node<T>) new ConcNode.OfLong((Node.OfLong) left, (Node.OfLong) right);
            case DOUBLE_VALUE:
                return (Node<T>) new ConcNode.OfDouble((Node.OfDouble) left, (Node.OfDouble) right);
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    // Reference-based node methods

    /**
     * Produces a {@link Node} describing an array.
     *
     * <p>The node will hold a reference to the array and will not make a copy.
     *
     * @param <T> the type of elements held by the node
     * @param array the array
     * @return a node holding an array
     */
    static <T> Node<T> node(T[] array) {
        return new ArrayNode<>(array);
    }

    /**
     * Produces a {@link Node} describing a {@link Collection}.
     * <p>
     * The node will hold a reference to the collection and will not make a copy.
     *
     * @param <T> the type of elements held by the node
     * @param c the collection
     * @return a node holding a collection
     */
    static <T> Node<T> node(Collection<T> c) {
        return new CollectionNode<>(c);
    }

    /**
     * Produces a {@link Node.Builder}.
     *
     * @param exactSizeIfKnown -1 if a variable size builder is requested,
     * otherwise the exact capacity desired.  A fixed capacity builder will
     * fail if the wrong number of elements are added to the builder.
     * @param generator the array factory
     * @param <T> the type of elements of the node builder
     * @return a {@code Node.Builder}
     */
    static <T> Node.Builder<T> builder(long exactSizeIfKnown, IntFunction<T[]> generator) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new FixedNodeBuilder<>(exactSizeIfKnown, generator)
               : builder();
    }

    /**
     * Produces a variable size @{link Node.Builder}.
     *
     * @param <T> the type of elements of the node builder
     * @return a {@code Node.Builder}
     */
    static <T> Node.Builder<T> builder() {
        return new SpinedNodeBuilder<>();
    }

    // Int nodes

    /**
     * Produces a {@link Node.OfInt} describing an int[] array.
     *
     * <p>The node will hold a reference to the array and will not make a copy.
     *
     * @param array the array
     * @return a node holding an array
     */
    static Node.OfInt node(int[] array) {
        return new IntArrayNode(array);
    }

    /**
     * Produces a {@link Node.Builder.OfInt}.
     *
     * @param exactSizeIfKnown -1 if a variable size builder is requested,
     * otherwise the exact capacity desired.  A fixed capacity builder will
     * fail if the wrong number of elements are added to the builder.
     * @return a {@code Node.Builder.OfInt}
     */
    static Node.Builder.OfInt intBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new IntFixedNodeBuilder(exactSizeIfKnown)
               : intBuilder();
    }

    /**
     * Produces a variable size @{link Node.Builder.OfInt}.
     *
     * @return a {@code Node.Builder.OfInt}
     */
    static Node.Builder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    // Long nodes

    /**
     * Produces a {@link Node.OfLong} describing a long[] array.
     * <p>
     * The node will hold a reference to the array and will not make a copy.
     *
     * @param array the array
     * @return a node holding an array
     */
    static Node.OfLong node(final long[] array) {
        return new LongArrayNode(array);
    }

    /**
     * Produces a {@link Node.Builder.OfLong}.
     *
     * @param exactSizeIfKnown -1 if a variable size builder is requested,
     * otherwise the exact capacity desired.  A fixed capacity builder will
     * fail if the wrong number of elements are added to the builder.
     * @return a {@code Node.Builder.OfLong}
     */
    static Node.Builder.OfLong longBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new LongFixedNodeBuilder(exactSizeIfKnown)
               : longBuilder();
    }

    /**
     * Produces a variable size @{link Node.Builder.OfLong}.
     *
     * @return a {@code Node.Builder.OfLong}
     */
    static Node.Builder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    // Double nodes

    /**
     * Produces a {@link Node.OfDouble} describing a double[] array.
     *
     * <p>The node will hold a reference to the array and will not make a copy.
     *
     * @param array the array
     * @return a node holding an array
     */
    static Node.OfDouble node(final double[] array) {
        return new DoubleArrayNode(array);
    }

    /**
     * Produces a {@link Node.Builder.OfDouble}.
     *
     * @param exactSizeIfKnown -1 if a variable size builder is requested,
     * otherwise the exact capacity desired.  A fixed capacity builder will
     * fail if the wrong number of elements are added to the builder.
     * @return a {@code Node.Builder.OfDouble}
     */
    static Node.Builder.OfDouble doubleBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new DoubleFixedNodeBuilder(exactSizeIfKnown)
               : doubleBuilder();
    }

    /**
     * Produces a variable size @{link Node.Builder.OfDouble}.
     *
     * @return a {@code Node.Builder.OfDouble}
     */
    static Node.Builder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    // Parallel evaluation of pipelines to nodes

    /**
     * Collect, in parallel, elements output from a pipeline and describe those
     * elements with a {@link Node}.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If the exact size of the output from the pipeline is known and the source
     * {@link Spliterator} has the {@link Spliterator#SUBSIZED} characteristic,
     * then a flat {@link Node} will be returned whose content is an array,
     * since the size is known the array can be constructed in advance and
     * output elements can be placed into the array concurrently by leaf
     * tasks at the correct offsets.  If the exact size is not known, output
     * elements are collected into a conc-node whose shape mirrors that
     * of the computation. This conc-node can then be flattened in
     * parallel to produce a flat {@code Node} if desired.
     *
     * @param helper the pipeline helper describing the pipeline
     * @param flattenTree whether a conc node should be flattened into a node
     *                    describing an array before returning
     * @param generator the array generator
     * @return a {@link Node} describing the output elements
     */
    public static <P_IN, P_OUT> Node<P_OUT> collect(PipelineHelper<P_OUT> helper,
                                                    Spliterator<P_IN> spliterator,
                                                    boolean flattenTree,
                                                    IntFunction<P_OUT[]> generator) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            P_OUT[] array = generator.apply((int) size);
            new SizedCollectorTask.OfRef<>(spliterator, helper, array).invoke();
            return node(array);
        } else {
            Node<P_OUT> node = new CollectorTask.OfRef<>(helper, generator, spliterator).invoke();
            return flattenTree ? flatten(node, generator) : node;
        }
    }

    /**
     * Collect, in parallel, elements output from an int-valued pipeline and
     * describe those elements with a {@link Node.OfInt}.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If the exact size of the output from the pipeline is known and the source
     * {@link Spliterator} has the {@link Spliterator#SUBSIZED} characteristic,
     * then a flat {@link Node} will be returned whose content is an array,
     * since the size is known the array can be constructed in advance and
     * output elements can be placed into the array concurrently by leaf
     * tasks at the correct offsets.  If the exact size is not known, output
     * elements are collected into a conc-node whose shape mirrors that
     * of the computation. This conc-node can then be flattened in
     * parallel to produce a flat {@code Node.OfInt} if desired.
     *
     * @param <P_IN> the type of elements from the source Spliterator
     * @param helper the pipeline helper describing the pipeline
     * @param flattenTree whether a conc node should be flattened into a node
     *                    describing an array before returning
     * @return a {@link Node.OfInt} describing the output elements
     */
    public static <P_IN> Node.OfInt collectInt(PipelineHelper<Integer> helper,
                                               Spliterator<P_IN> spliterator,
                                               boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            int[] array = new int[(int) size];
            new SizedCollectorTask.OfInt<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfInt node = new CollectorTask.OfInt<>(helper, spliterator).invoke();
            return flattenTree ? flattenInt(node) : node;
        }
    }

    /**
     * Collect, in parallel, elements output from a long-valued pipeline and
     * describe those elements with a {@link Node.OfLong}.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If the exact size of the output from the pipeline is known and the source
     * {@link Spliterator} has the {@link Spliterator#SUBSIZED} characteristic,
     * then a flat {@link Node} will be returned whose content is an array,
     * since the size is known the array can be constructed in advance and
     * output elements can be placed into the array concurrently by leaf
     * tasks at the correct offsets.  If the exact size is not known, output
     * elements are collected into a conc-node whose shape mirrors that
     * of the computation. This conc-node can then be flattened in
     * parallel to produce a flat {@code Node.OfLong} if desired.
     *
     * @param <P_IN> the type of elements from the source Spliterator
     * @param helper the pipeline helper describing the pipeline
     * @param flattenTree whether a conc node should be flattened into a node
     *                    describing an array before returning
     * @return a {@link Node.OfLong} describing the output elements
     */
    public static <P_IN> Node.OfLong collectLong(PipelineHelper<Long> helper,
                                                 Spliterator<P_IN> spliterator,
                                                 boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            long[] array = new long[(int) size];
            new SizedCollectorTask.OfLong<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfLong node = new CollectorTask.OfLong<>(helper, spliterator).invoke();
            return flattenTree ? flattenLong(node) : node;
        }
    }

    /**
     * Collect, in parallel, elements output from n double-valued pipeline and
     * describe those elements with a {@link Node.OfDouble}.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If the exact size of the output from the pipeline is known and the source
     * {@link Spliterator} has the {@link Spliterator#SUBSIZED} characteristic,
     * then a flat {@link Node} will be returned whose content is an array,
     * since the size is known the array can be constructed in advance and
     * output elements can be placed into the array concurrently by leaf
     * tasks at the correct offsets.  If the exact size is not known, output
     * elements are collected into a conc-node whose shape mirrors that
     * of the computation. This conc-node can then be flattened in
     * parallel to produce a flat {@code Node.OfDouble} if desired.
     *
     * @param <P_IN> the type of elements from the source Spliterator
     * @param helper the pipeline helper describing the pipeline
     * @param flattenTree whether a conc node should be flattened into a node
     *                    describing an array before returning
     * @return a {@link Node.OfDouble} describing the output elements
     */
    public static <P_IN> Node.OfDouble collectDouble(PipelineHelper<Double> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            double[] array = new double[(int) size];
            new SizedCollectorTask.OfDouble<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfDouble node = new CollectorTask.OfDouble<>(helper, spliterator).invoke();
            return flattenTree ? flattenDouble(node) : node;
        }
    }

    // Parallel flattening of nodes

    /**
     * Flatten, in parallel, a {@link Node}.  A flattened node is one that has
     * no children.  If the node is already flat, it is simply returned.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If a new node is to be created, the generator is used to create an array
     * whose length is {@link Node#count()}.  Then the node tree is traversed
     * and leaf node elements are placed in the array concurrently by leaf tasks
     * at the correct offsets.
     *
     * @param <T> type of elements contained by the node
     * @param node the node to flatten
     * @param generator the array factory used to create array instances
     * @return a flat {@code Node}
     */
    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            T[] array = generator.apply((int) size);
            new ToArrayTask.OfRef<>(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    /**
     * Flatten, in parallel, a {@link Node.OfInt}.  A flattened node is one that
     * has no children.  If the node is already flat, it is simply returned.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If a new node is to be created, a new int[] array is created whose length
     * is {@link Node#count()}.  Then the node tree is traversed and leaf node
     * elements are placed in the array concurrently by leaf tasks at the
     * correct offsets.
     *
     * @param node the node to flatten
     * @return a flat {@code Node.OfInt}
     */
    public static Node.OfInt flattenInt(Node.OfInt node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            int[] array = new int[(int) size];
            new ToArrayTask.OfInt(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    /**
     * Flatten, in parallel, a {@link Node.OfLong}.  A flattened node is one that
     * has no children.  If the node is already flat, it is simply returned.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If a new node is to be created, a new long[] array is created whose length
     * is {@link Node#count()}.  Then the node tree is traversed and leaf node
     * elements are placed in the array concurrently by leaf tasks at the
     * correct offsets.
     *
     * @param node the node to flatten
     * @return a flat {@code Node.OfLong}
     */
    public static Node.OfLong flattenLong(Node.OfLong node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            long[] array = new long[(int) size];
            new ToArrayTask.OfLong(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    /**
     * Flatten, in parallel, a {@link Node.OfDouble}.  A flattened node is one that
     * has no children.  If the node is already flat, it is simply returned.
     *
     * <p><b>Implementation Requirements:</b><br>
     * If a new node is to be created, a new double[] array is created whose length
     * is {@link Node#count()}.  Then the node tree is traversed and leaf node
     * elements are placed in the array concurrently by leaf tasks at the
     * correct offsets.
     *
     * @param node the node to flatten
     * @return a flat {@code Node.OfDouble}
     */
    public static Node.OfDouble flattenDouble(Node.OfDouble node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            double[] array = new double[(int) size];
            new ToArrayTask.OfDouble(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    // Implementations

    private abstract static class EmptyNode<T, T_ARR, T_CONS> implements Node<T> {
        EmptyNode() { }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            return generator.apply(0);
        }

        public void copyInto(T_ARR array, int offset) { }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public StreamShape getShape() {
            return Nodes.getShape();
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public Node<T> getChild(int i) {
            return Nodes.getChild();
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            return Nodes.truncate(this, from, to, generator);
        }

        public void forEach(T_CONS consumer) { }

        private static class OfRef<T> extends EmptyNode<T, T[], Consumer<? super T>> {
            private OfRef() {
                super();
            }

            @Override
            public Spliterator<T> spliterator() {
                return Spliterators.emptySpliterator();
            }
        }

        private static final class OfInt
                extends EmptyNode<Integer, int[], IntConsumer>
                implements Node.OfInt {

            OfInt() { } // Avoid creation of special accessor

            @Override
            public void forEach(Consumer<? super Integer> consumer) {
                Nodes.OfInt.forEach(this, consumer);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return Spliterators.emptyIntSpliterator();
            }

            @Override
            public Node.OfInt getChild(int i) {
                return Nodes.OfPrimitive.getChild();
            }

            @Override
            public Node.OfInt truncate(long from, long to,
                    IntFunction<Integer[]> generator) {
                return Nodes.OfInt.truncate(this, from, to, generator);
            }

            @Override
            public int[] asPrimitiveArray() {
                return EMPTY_INT_ARRAY;
            }

            @Override
            public void copyInto(Integer[] boxed, int offset) {
                Nodes.OfInt.copyInto(this, boxed, offset);
            }

            @Override
            public int[] newArray(int count) {
                return Nodes.OfInt.newArray(this, count);
            }
        }

        private static final class OfLong
                extends EmptyNode<Long, long[], LongConsumer>
                implements Node.OfLong {

            OfLong() { } // Avoid creation of special accessor

            @Override
            public void forEach(Consumer<? super Long> consumer) {
                Nodes.OfLong.forEach(this, consumer);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return Spliterators.emptyLongSpliterator();
            }

            @Override
            public Node.OfLong getChild(int i) {
                return Nodes.OfPrimitive.getChild();
            }

            @Override
            public Node.OfLong truncate(long from, long to,
                    IntFunction<Long[]> generator) {
                return Nodes.OfLong.truncate(this, from, to, generator);
            }

            @Override
            public long[] asPrimitiveArray() {
                return EMPTY_LONG_ARRAY;
            }

            @Override
            public void copyInto(Long[] boxed, int offset) {
                Nodes.OfLong.copyInto(this, boxed, offset);
            }

            @Override
            public long[] newArray(int count) {
                return Nodes.OfLong.newArray(this, count);
            }
        }

        private static final class OfDouble
                extends EmptyNode<Double, double[], DoubleConsumer>
                implements Node.OfDouble {

            OfDouble() { } // Avoid creation of special accessor

            @Override
            public void forEach(Consumer<? super Double> consumer) {
                Nodes.OfDouble.forEach(this, consumer);
            }

            @Override
            public Node.OfDouble getChild(int i) {
                return Nodes.OfPrimitive.getChild();
            }

            @Override
            public Node.OfDouble truncate(long from, long to,
                    IntFunction<Double[]> generator) {
                return Nodes.OfDouble.truncate(this, from, to, generator);
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return Spliterators.emptyDoubleSpliterator();
            }

            @Override
            public void copyInto(Double[] boxed, int offset) {
                Nodes.OfDouble.copyInto(this, boxed, offset);
            }

            @Override
            public double[] asPrimitiveArray() {
                return EMPTY_DOUBLE_ARRAY;
            }

            @Override
            public double[] newArray(int count) {
                return Nodes.OfDouble.newArray(this, count);
            }
        }
    }

    /** Node class for a reference array */
    private static class ArrayNode<T> implements Node<T> {
        final T[] array;
        int curSize;

        ArrayNode(long size, IntFunction<T[]> generator) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = generator.apply((int) size);
            this.curSize = 0;
        }

        ArrayNode(T[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        // Node

        @Override
        public Spliterator<T> spliterator() {
            return java8.util.J8Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.getShape();
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public Node<T> getChild(int i) {
            return Nodes.getChild();
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            return Nodes.truncate(this, from, to, generator);
        }

        @Override
        public void copyInto(T[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            if (array.length == curSize) {
                return array;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        //

        @Override
        public String toString() {
            return String.format("ArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    /** Node class for a Collection */
    private static final class CollectionNode<T> implements Node<T> {
        private final Collection<T> c;

        CollectionNode(Collection<T> c) {
            this.c = c;
        }

        // Node

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(c);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.getShape();
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public Node<T> getChild(int i) {
            return Nodes.getChild();
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            return Nodes.truncate(this, from, to, generator);
        }

        @Override
        public void copyInto(T[] array, int offset) {
            for (T t : c) {
                array[offset++] = t;
            }
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            return c.toArray(generator.apply(c.size()));
        }

        @Override
        public long count() {
            return c.size();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            for (T t : c) {
                consumer.accept(t);
            }
        }

        //

        @Override
        public String toString() {
            return String.format("CollectionNode[%d][%s]", c.size(), c);
        }
    }

    /**
     * Node class for an internal node with two or more children
     */
    private abstract static class AbstractConcNode<T, T_NODE extends Node<T>> implements Node<T> {
        protected final T_NODE left;
        protected final T_NODE right;
        private final long size;

        AbstractConcNode(T_NODE left, T_NODE right) {
            this.left = left;
            this.right = right;
            // The Node count will be required when the Node spliterator is
            // obtained and it is cheaper to aggressively calculate bottom up
            // as the tree is built rather than later on from the top down
            // traversing the tree
            this.size = left.count() + right.count();
        }

        @Override
        public int getChildCount() {
            return 2;
        }

        @Override
        public StreamShape getShape() {
            return Nodes.getShape();
        }

        @Override
        public T_NODE getChild(int i) {
            if (i == 0) return left;
            if (i == 1) return right;
            throw new IndexOutOfBoundsException();
        }

        @Override
        public long count() {
            return size;
        }
    }

    static final class ConcNode<T>
            extends AbstractConcNode<T, Node<T>>
            implements Node<T> {

        ConcNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Nodes.InternalNodeSpliterator.OfRef<>(this);
        }

        @Override
        public void copyInto(T[] array, int offset) {
            Objects.requireNonNull(array);
            left.copyInto(array, offset);
            // Cast to int is safe since it is the callers responsibility to
            // ensure that there is sufficient room in the array
            right.copyInto(array, offset + (int) left.count());
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            long size = count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            T[] array = generator.apply((int) size);
            copyInto(array, 0);
            return array;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            left.forEach(consumer);
            right.forEach(consumer);
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            if (from == 0 && to == count())
                return this;
            long leftCount = left.count();
            if (from >= leftCount)
                return right.truncate(from - leftCount, to - leftCount, generator);
            else if (to <= leftCount)
                return left.truncate(from, to, generator);
            else {
                return Nodes.conc(getShape(), left.truncate(from, leftCount, generator),
                                  right.truncate(0, to - leftCount, generator));
            }
        }

        @Override
        public String toString() {
            if (count() < 32) {
                return String.format("ConcNode[%s.%s]", left, right);
            } else {
                return String.format("ConcNode[size=%d]", count());
            }
        }

        private abstract static class OfPrimitive<E, T_CONS, T_ARR,
                                                  T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>,
                                                  T_NODE extends Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE>>
                extends AbstractConcNode<E, T_NODE>
                implements Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE> {

            OfPrimitive(T_NODE left, T_NODE right) {
                super(left, right);
            }

            @Override
            public void forEach(T_CONS consumer) {
                left.forEach(consumer);
                right.forEach(consumer);
            }

            @Override
            public void copyInto(T_ARR array, int offset) {
                left.copyInto(array, offset);
                // Cast to int is safe since it is the callers responsibility to
                // ensure that there is sufficient room in the array
                right.copyInto(array, offset + (int) left.count());
            }

            @Override
            public T_ARR asPrimitiveArray() {
                long size = count();
                if (size >= MAX_ARRAY_SIZE)
                    throw new IllegalArgumentException(BAD_SIZE);
                T_ARR array = newArray((int) size);
                copyInto(array, 0);
                return array;
            }

            @Override
            public E[] asArray(IntFunction<E[]> generator) {
                return Nodes.OfPrimitive.asArray(this, generator);
            }

            @Override
            public String toString() {
                if (count() < 32)
                    return String.format("%s[%s.%s]", this.getClass().getName(), left, right);
                else
                    return String.format("%s[size=%d]", this.getClass().getName(), count());
            }
        }

        static final class OfInt
                extends ConcNode.OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Node.OfInt {

            OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            @Override
            public void forEach(Consumer<? super Integer> consumer) {
                Nodes.OfInt.forEach(this, consumer);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }

            @Override
            public Node.OfInt truncate(long from, long to,
                    IntFunction<Integer[]> generator) {
                return Nodes.OfInt.truncate(this, from, to, generator);
            }

            @Override
            public void copyInto(Integer[] boxed, int offset) {
                Nodes.OfInt.copyInto(this, boxed, offset);
            }

            @Override
            public int[] newArray(int count) {
                return Nodes.OfInt.newArray(this, count);
            }
        }

        static final class OfLong
                extends ConcNode.OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Node.OfLong {

            OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            @Override
            public void forEach(Consumer<? super Long> consumer) {
                Nodes.OfLong.forEach(this, consumer);
            }

            @Override
            public java8.util.stream.Node.OfLong truncate(long from, long to,
                    IntFunction<Long[]> generator) {
                return Nodes.OfLong.truncate(this, from, to, generator);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }

            @Override
            public void copyInto(Long[] boxed, int offset) {
                Nodes.OfLong.copyInto(this, boxed, offset);
            }

            @Override
            public long[] newArray(int count) {
                return Nodes.OfLong.newArray(this, count);
            }
        }

        static final class OfDouble
                extends ConcNode.OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Node.OfDouble {

            OfDouble(Node.OfDouble left, Node.OfDouble right) {
                super(left, right);
            }

            @Override
            public void forEach(Consumer<? super Double> consumer) {
                Nodes.OfDouble.forEach(this, consumer);
            }

            @Override
            public Node.OfDouble truncate(long from, long to,
                    IntFunction<Double[]> generator) {
                return Nodes.OfDouble.truncate(this, from, to, generator);
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return new InternalNodeSpliterator.OfDouble(this);
            }

            @Override
            public void copyInto(Double[] boxed, int offset) {
                Nodes.OfDouble.copyInto(this, boxed, offset);
            }

            @Override
            public double[] newArray(int count) {
                return Nodes.OfDouble.newArray(this, count);
            }
        }
    }

    /** Abstract class for spliterator for all internal node classes */
    private abstract static class InternalNodeSpliterator<T,
                                                          S extends Spliterator<T>,
                                                          N extends Node<T>>
            implements Spliterator<T> {
        // Node we are pointing to
        // null if full traversal has occurred
        N curNode;

        // next child of curNode to consume
        int curChildIndex;

        // The spliterator of the curNode if that node is last and has no children.
        // This spliterator will be delegated to for splitting and traversing.
        // null if curNode has children
        S lastNodeSpliterator;

        // spliterator used while traversing with tryAdvance
        // null if no partial traversal has occurred
        S tryAdvanceSpliterator;

        // node stack used when traversing to search and find leaf nodes
        // null if no partial traversal has occurred
        Deque<N> tryAdvanceStack;

        InternalNodeSpliterator(N curNode) {
            this.curNode = curNode;
        }

        /**
         * Initiate a stack containing, in left-to-right order, the child nodes
         * covered by this spliterator
         */
        @SuppressWarnings("unchecked")
		protected final Deque<N> initStack() {
            // Bias size to the case where leaf nodes are close to this node
            // 8 is the minimum initial capacity for the ArrayDeque implementation
            Deque<N> stack = new ArrayDeque<>(8);
            for (int i = curNode.getChildCount() - 1; i >= curChildIndex; i--)
                stack.addFirst((N) curNode.getChild(i));
            return stack;
        }

        /**
         * Depth first search, in left-to-right order, of the node tree, using
         * an explicit stack, to find the next non-empty leaf node.
         */
        @SuppressWarnings("unchecked")
		protected final N findNextLeafNode(Deque<N> stack) {
            N n = null;
            while ((n = stack.pollFirst()) != null) {
                if (n.getChildCount() == 0) {
                    if (n.count() > 0)
                        return n;
                } else {
                    for (int i = n.getChildCount() - 1; i >= 0; i--)
                        stack.addFirst((N) n.getChild(i));
                }
            }

            return null;
        }

        @SuppressWarnings("unchecked")
		protected final boolean initTryAdvance() {
            if (curNode == null)
                return false;

            if (tryAdvanceSpliterator == null) {
                if (lastNodeSpliterator == null) {
                    // Initiate the node stack
                    tryAdvanceStack = initStack();
                    N leaf = findNextLeafNode(tryAdvanceStack);
                    if (leaf != null)
                        tryAdvanceSpliterator = (S) leaf.spliterator();
                    else {
                        // A non-empty leaf node was not found
                        // No elements to traverse
                        curNode = null;
                        return false;
                    }
                }
                else
                    tryAdvanceSpliterator = lastNodeSpliterator;
            }
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final S trySplit() {
            if (curNode == null || tryAdvanceSpliterator != null)
                return null; // Cannot split if fully or partially traversed
            else if (lastNodeSpliterator != null)
                return (S) lastNodeSpliterator.trySplit();
            else if (curChildIndex < curNode.getChildCount() - 1)
                return (S) curNode.getChild(curChildIndex++).spliterator();
            else {
                curNode = (N) curNode.getChild(curChildIndex);
                if (curNode.getChildCount() == 0) {
                    lastNodeSpliterator = (S) curNode.spliterator();
                    return (S) lastNodeSpliterator.trySplit();
                }
                else {
                    curChildIndex = 0;
                    return (S) curNode.getChild(curChildIndex++).spliterator();
                }
            }
        }

        @Override
        public final long estimateSize() {
            if (curNode == null)
                return 0;

            // Will not reflect the effects of partial traversal.
            // This is compliant with the specification
            if (lastNodeSpliterator != null)
                return lastNodeSpliterator.estimateSize();
            else {
                long size = 0;
                for (int i = curChildIndex; i < curNode.getChildCount(); i++)
                    size += curNode.getChild(i).count();
                return size;
            }
        }

        @Override
        public final int characteristics() {
            return Spliterator.SIZED;
        }

        private static final class OfRef<T>
                extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {

            OfRef(Node<T> curNode) {
                super(curNode);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (!initTryAdvance())
                    return false;

                boolean hasNext = tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        // Advance to the spliterator of the next non-empty leaf node
                        Node<T> leaf = findNextLeafNode(tryAdvanceStack);
                        if (leaf != null) {
                            tryAdvanceSpliterator = leaf.spliterator();
                            // Since the node is not-empty the spliterator can be advanced
                            return tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                    // No more elements to traverse
                    curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                if (curNode == null)
                    return;

                if (tryAdvanceSpliterator == null) {
                    if (lastNodeSpliterator == null) {
                        Deque<Node<T>> stack = initStack();
                        Node<T> leaf;
                        while ((leaf = findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        curNode = null;
                    }
                    else
                        lastNodeSpliterator.forEachRemaining(consumer);
                }
                else
                    while(tryAdvance(consumer)) { }
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
            public Comparator<? super T> getComparator() {
                return Spliterators.getComparator(this);
            }
        }

        private abstract static class OfPrimitive<T, T_CONS, T_ARR,
                                                  T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                                                  N extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, N>>
                extends InternalNodeSpliterator<T, T_SPLITR, N>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {

            OfPrimitive(N cur) {
                super(cur);
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                if (!initTryAdvance())
                    return false;

                boolean hasNext = tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        // Advance to the spliterator of the next non-empty leaf node
                        N leaf = findNextLeafNode(tryAdvanceStack);
                        if (leaf != null) {
                            tryAdvanceSpliterator = leaf.spliterator();
                            // Since the node is not-empty the spliterator can be advanced
                            return tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                    // No more elements to traverse
                    curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                if (curNode == null)
                    return;

                if (tryAdvanceSpliterator == null) {
                    if (lastNodeSpliterator == null) {
                        Deque<N> stack = initStack();
                        N leaf;
                        while ((leaf = findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        curNode = null;
                    }
                    else
                        lastNodeSpliterator.forEachRemaining(consumer);
                }
                else
                    while(tryAdvance(consumer)) { }
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
            public Comparator<? super T> getComparator() {
                return Spliterators.getComparator(this);
            }
        }

        private static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Spliterator.OfInt {

            OfInt(Node.OfInt cur) {
                super(cur);
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
                extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Spliterator.OfLong {

            OfLong(Node.OfLong cur) {
                super(cur);
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
                extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Spliterator.OfDouble {

            OfDouble(Node.OfDouble cur) {
                super(cur);
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

    /**
     * Fixed-sized builder class for reference nodes
     */
    private static final class FixedNodeBuilder<T>
            extends ArrayNode<T>
            implements Node.Builder<T> {

        FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
        }

        @Override
        public Node<T> build() {
            if (curSize < array.length)
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                                                              curSize, array.length));
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length)
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                                                              size, array.length));
            curSize = 0;
        }

        @Override
        public void accept(T t) {
            if (curSize < array.length) {
                array[curSize++] = t;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                                                              array.length));
            }
        }

        @Override
        public void end() {
            if (curSize < array.length)
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d",
                                                              curSize, array.length));
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public String toString() {
            return String.format("FixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    /**
     * Variable-sized builder class for reference nodes
     */
    private static final class SpinedNodeBuilder<T>
            extends SpinedBuffer<T>
            implements Node<T>, Node.Builder<T> {

        SpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public Spliterator<T> spliterator() {
            return super.spliterator();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            super.forEach(consumer);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.getShape();
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public Node<T> getChild(int i) {
            return Nodes.getChild();
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            return Nodes.truncate(this, from, to, generator);
        }

        @Override
        public void begin(long size) {
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(T t) {
            super.accept(t);
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public void end() {
            // @@@ check begin(size) and size
        }

        @Override
        public void copyInto(T[] array, int offset) {
            super.copyInto(array, offset);
        }

        @Override
        public T[] asArray(IntFunction<T[]> arrayFactory) {
            return super.asArray(arrayFactory);
        }

        @Override
        public Node<T> build() {
            return this;
        }
    }

    //

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    private static class IntArrayNode implements Node.OfInt {
        final int[] array;
        int curSize;

        IntArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new int[(int) size];
            this.curSize = 0;
        }

        IntArrayNode(int[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        // Node

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public void forEach(Consumer<? super Integer> consumer) {
            Nodes.OfInt.forEach(this, consumer);
        }

        @Override
        public OfInt getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }

        @Override
        public Spliterator.OfInt spliterator() {
            return java8.util.J8Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public OfInt truncate(long from, long to,
                IntFunction<Integer[]> generator) {
            return Nodes.OfInt.truncate(this, from, to, generator);
        }

        @Override
        public void copyInto(Integer[] boxed, int offset) {
            Nodes.OfInt.copyInto(this, boxed, offset);
        }

        @Override
        public int[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public Integer[] asArray(IntFunction<Integer[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public int[] newArray(int count) {
            return Nodes.OfInt.newArray(this, count);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfInt.getShape();
        }

        @Override
        public void copyInto(int[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(IntConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("IntArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static class LongArrayNode implements Node.OfLong {
        final long[] array;
        int curSize;

        LongArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new long[(int) size];
            this.curSize = 0;
        }

        LongArrayNode(long[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public void forEach(Consumer<? super Long> consumer) {
            Nodes.OfLong.forEach(this, consumer);
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public java8.util.stream.Node.OfLong getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }

        @Override
        public void copyInto(Long[] boxed, int offset) {
            Nodes.OfLong.copyInto(this, boxed, offset);
        }

        @Override
        public java8.util.stream.Node.OfLong truncate(long from, long to,
                IntFunction<Long[]> generator) {
            return Nodes.OfLong.truncate(this, from, to, generator);
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return java8.util.J8Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public long[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public Long[] asArray(IntFunction<Long[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public long[] newArray(int count) {
            return Nodes.OfLong.newArray(this, count);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfLong.getShape();
        }

        @Override
        public void copyInto(long[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(LongConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("LongArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static class DoubleArrayNode implements Node.OfDouble {
        final double[] array;
        int curSize;

        DoubleArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new double[(int) size];
            this.curSize = 0;
        }

        DoubleArrayNode(double[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public void forEach(Consumer<? super Double> consumer) {
            Nodes.OfDouble.forEach(this, consumer);
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public OfDouble getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }

        @Override
        public OfDouble truncate(long from, long to,
                IntFunction<Double[]> generator) {
            return Nodes.OfDouble.truncate(this, from, to, generator);
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            return java8.util.J8Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public double[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public Double[] asArray(IntFunction<Double[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public double[] newArray(int count) {
            return Nodes.OfDouble.newArray(this, count);
        }

        @Override
        public void copyInto(Double[] boxed, int offset) {
            Nodes.OfDouble.copyInto(this, boxed, offset);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfDouble.getShape();
        }

        @Override
        public void copyInto(double[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(DoubleConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("DoubleArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class IntFixedNodeBuilder
            extends IntArrayNode
            implements Node.Builder.OfInt {

        IntFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfInt build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(int i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                                                              array.length));
            }
        }

        @Override
        public void accept(Integer t) {
            SinkDefaults.OfInt.accept(this, t);
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public String toString() {
            return String.format("IntFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class LongFixedNodeBuilder
            extends LongArrayNode
            implements Node.Builder.OfLong {

        LongFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfLong build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(long i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                                                              array.length));
            }
        }

        @Override
        public void accept(Long t) {
            SinkDefaults.OfLong.accept(this, t);
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public String toString() {
            return String.format("LongFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class DoubleFixedNodeBuilder
            extends DoubleArrayNode
            implements Node.Builder.OfDouble {

        DoubleFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfDouble build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(double i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                                                              array.length));
            }
        }

        @Override
        public void accept(Double i) {
            SinkDefaults.OfDouble.accept(this, i);
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public String toString() {
            return String.format("DoubleFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class IntSpinedNodeBuilder
            extends SpinedBuffer.OfInt
            implements Node.OfInt, Node.Builder.OfInt {

        IntSpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public Spliterator.OfInt spliterator() {
            return super.spliterator();
        }

        @Override
        public void forEach(IntConsumer consumer) {
            super.forEach(consumer);
        }

        //
        @Override
        public void begin(long size) {
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(int i) {
            super.accept(i);
        }

        @Override
        public void accept(Integer t) {
            SinkDefaults.OfInt.accept(this, t);
        }

        @Override
        public void end() {
            // @@@ check begin(size) and size
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public void copyInto(int[] array, int offset) throws IndexOutOfBoundsException {
            super.copyInto(array, offset);
        }

        @Override
        public void copyInto(Integer[] boxed, int offset) {
            Nodes.OfInt.copyInto(this, boxed, offset);
        }

        @Override
        public Node.OfInt truncate(long from, long to,
                IntFunction<Integer[]> generator) {
            return Nodes.OfInt.truncate(this, from, to, generator);
        }

        @Override
        public int[] asPrimitiveArray() {
            return super.asPrimitiveArray();
        }

        @Override
        public Integer[] asArray(IntFunction<Integer[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfInt.getShape();
        }

        @Override
        public Node.OfInt build() {
            return this;
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public java8.util.stream.Node.OfInt getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }
    }

    private static final class LongSpinedNodeBuilder
            extends SpinedBuffer.OfLong
            implements Node.OfLong, Node.Builder.OfLong {

        LongSpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public java8.util.stream.Node.OfLong truncate(long from, long to,
                IntFunction<Long[]> generator) {
            return Nodes.OfLong.truncate(this, from, to, generator);
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return super.spliterator();
        }

        @Override
        public void forEach(LongConsumer consumer) {
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(long i) {
            super.accept(i);
        }

        @Override
        public void accept(Long t) {
            SinkDefaults.OfLong.accept(this, t);
        }

        @Override
        public void end() {
            // @@@ check begin(size) and size
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public void copyInto(long[] array, int offset) {
            super.copyInto(array, offset);
        }

        @Override
        public void copyInto(Long[] boxed, int offset) {
            Nodes.OfLong.copyInto(this, boxed, offset);
        }

        @Override
        public long[] asPrimitiveArray() {
            return super.asPrimitiveArray();
        }

        @Override
        public Long[] asArray(IntFunction<Long[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfLong.getShape();
        }

        @Override
        public Node.OfLong build() {
            return this;
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public java8.util.stream.Node.OfLong getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }
    }

    private static final class DoubleSpinedNodeBuilder
            extends SpinedBuffer.OfDouble
            implements Node.OfDouble, Node.Builder.OfDouble {

        DoubleSpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public Node.OfDouble truncate(long from, long to,
                IntFunction<Double[]> generator) {
            return Nodes.OfDouble.truncate(this, from, to, generator);
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            return super.spliterator();
        }

        @Override
        public void forEach(DoubleConsumer consumer) {
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(double i) {
            super.accept(i);
        }

        @Override
        public void accept(Double i) {
            SinkDefaults.OfDouble.accept(this, i);
        }

        @Override
        public void end() {
            // @@@ check begin(size) and size
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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
        public void copyInto(double[] array, int offset) {
            super.copyInto(array, offset);
        }

        @Override
        public void copyInto(Double[] boxed, int offset) {
            Nodes.OfDouble.copyInto(this, boxed, offset);
        }

        @Override
        public double[] asPrimitiveArray() {
            return super.asPrimitiveArray();
        }

        @Override
        public Double[] asArray(IntFunction<Double[]> generator) {
            return Nodes.OfPrimitive.asArray(this, generator);
        }

        @Override
        public Node.OfDouble build() {
            return this;
        }

        @Override
        public int getChildCount() {
            return Nodes.getChildCount();
        }

        @Override
        public java8.util.stream.Node.OfDouble getChild(int i) {
            return Nodes.OfPrimitive.getChild();
        }

        @Override
        public StreamShape getShape() {
            return Nodes.OfDouble.getShape();
        }
    }

    /*
     * This and subclasses are not intended to be serializable
     */
    @SuppressWarnings("serial")
    private abstract static class SizedCollectorTask<P_IN, P_OUT, T_SINK extends Sink<P_OUT>,
                                                     K extends SizedCollectorTask<P_IN, P_OUT, T_SINK, K>>
            extends CountedCompleter<Void>
            implements Sink<P_OUT> {
        protected final Spliterator<P_IN> spliterator;
        protected final PipelineHelper<P_OUT> helper;
        protected final long targetSize;
        protected long offset;
        protected long length;
        // For Sink implementation
        protected int index, fence;

        SizedCollectorTask(Spliterator<P_IN> spliterator,
                           PipelineHelper<P_OUT> helper,
                           int arrayLength) {

            this.spliterator = spliterator;
            this.helper = helper;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            this.offset = 0;
            this.length = arrayLength;
        }

        SizedCollectorTask(K parent, Spliterator<P_IN> spliterator,
                           long offset, long length, int arrayLength) {
            super(parent);

            this.spliterator = spliterator;
            this.helper = parent.helper;
            this.targetSize = parent.targetSize;
            this.offset = offset;
            this.length = length;

            if (offset < 0 || length < 0 || (offset + length - 1 >= arrayLength)) {
                throw new IllegalArgumentException(
                        String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)",
                                      offset, offset, length, arrayLength));
            }

        }

        @Override
        public void compute() {
            SizedCollectorTask<P_IN, P_OUT, T_SINK, K> task = this;
            Spliterator<P_IN> rightSplit = spliterator, leftSplit;
            while (rightSplit.estimateSize() > task.targetSize &&
                   (leftSplit = rightSplit.trySplit()) != null) {
                task.setPendingCount(1);
                long leftSplitSize = leftSplit.estimateSize();
                task.makeChild(leftSplit, task.offset, leftSplitSize).fork();
                task = task.makeChild(rightSplit, task.offset + leftSplitSize,
                                      task.length - leftSplitSize);
            }


            @SuppressWarnings("unchecked")
			T_SINK sink = (T_SINK) task;
            task.helper.wrapAndCopyInto(sink, rightSplit);
            task.propagateCompletion();
        }

        abstract K makeChild(Spliterator<P_IN> spliterator, long offset, long size);

        @Override
        public void begin(long size) {
            if (size > length)
                throw new IllegalStateException("size passed to Sink.begin exceeds array length");
            // Casts to int are safe since absolute size is verified to be within
            // bounds when the root concrete SizedCollectorTask is constructed
            // with the shared array
            index = (int) offset;
            fence = index + (int) length;
        }

        @Override
        public void end() {
        }

        @Override
        public boolean cancellationRequested() {
            return false;
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

        static final class OfRef<P_IN, P_OUT>
                extends SizedCollectorTask<P_IN, P_OUT, Sink<P_OUT>, OfRef<P_IN, P_OUT>>
                implements Sink<P_OUT> {
            private final P_OUT[] array;

            OfRef(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, P_OUT[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfRef(OfRef<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator,
                  long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfRef<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator,
                                         long offset, long size) {
                return new OfRef<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(P_OUT value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }
        }

        static final class OfInt<P_IN>
                extends SizedCollectorTask<P_IN, Integer, Sink.OfInt, OfInt<P_IN>>
                implements Sink.OfInt {
            private final int[] array;

            OfInt(Spliterator<P_IN> spliterator, PipelineHelper<Integer> helper, int[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfInt(SizedCollectorTask.OfInt<P_IN> parent, Spliterator<P_IN> spliterator,
                  long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfInt<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                     long offset, long size) {
                return new SizedCollectorTask.OfInt<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(int value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }

            @Override
            public void accept(Integer i) {
                SinkDefaults.OfInt.accept(this, i);
            }
        }

        static final class OfLong<P_IN>
                extends SizedCollectorTask<P_IN, Long, Sink.OfLong, OfLong<P_IN>>
                implements Sink.OfLong {
            private final long[] array;

            OfLong(Spliterator<P_IN> spliterator, PipelineHelper<Long> helper, long[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfLong(SizedCollectorTask.OfLong<P_IN> parent, Spliterator<P_IN> spliterator,
                   long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfLong<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                      long offset, long size) {
                return new SizedCollectorTask.OfLong<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(long value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }

            @Override
            public void accept(Long i) {
                SinkDefaults.OfLong.accept(this, i);
            }
        }

        static final class OfDouble<P_IN>
                extends SizedCollectorTask<P_IN, Double, Sink.OfDouble, OfDouble<P_IN>>
                implements Sink.OfDouble {
            private final double[] array;

            OfDouble(Spliterator<P_IN> spliterator, PipelineHelper<Double> helper, double[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfDouble(SizedCollectorTask.OfDouble<P_IN> parent, Spliterator<P_IN> spliterator,
                     long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfDouble<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                        long offset, long size) {
                return new SizedCollectorTask.OfDouble<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(double value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }

            @Override
            public void accept(Double i) {
                SinkDefaults.OfDouble.accept(this, i);
            }
        }
    }

    @SuppressWarnings("serial")
    private abstract static class ToArrayTask<T, T_NODE extends Node<T>,
                                              K extends ToArrayTask<T, T_NODE, K>>
            extends CountedCompleter<Void> {
        protected final T_NODE node;
        protected final int offset;

        ToArrayTask(T_NODE node, int offset) {
            this.node = node;
            this.offset = offset;
        }

        ToArrayTask(K parent, T_NODE node, int offset) {
            super(parent);
            this.node = node;
            this.offset = offset;
        }

        abstract void copyNodeToArray();

        abstract K makeChild(int childIndex, int offset);

        @Override
        public void compute() {
            ToArrayTask<T, T_NODE, K> task = this;
            while (true) {
                if (task.node.getChildCount() == 0) {
                    task.copyNodeToArray();
                    task.propagateCompletion();
                    return;
                }
                else {
                    task.setPendingCount(task.node.getChildCount() - 1);

                    int size = 0;
                    int i = 0;
                    for (;i < task.node.getChildCount() - 1; i++) {
                        K leftTask = task.makeChild(i, task.offset + size);
                        size += leftTask.node.count();
                        leftTask.fork();
                    }
                    task = task.makeChild(i, task.offset + size);
                }
            }
        }

        private static final class OfRef<T>
                extends ToArrayTask<T, Node<T>, OfRef<T>> {
            private final T[] array;

            private OfRef(Node<T> node, T[] array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfRef(OfRef<T> parent, Node<T> node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfRef<T> makeChild(int childIndex, int offset) {
                return new OfRef<>(this, node.getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                node.copyInto(array, offset);
            }
        }

        private static class OfPrimitive<T, T_CONS, T_ARR,
                                         T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                                         T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>>
                extends ToArrayTask<T, T_NODE, OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> {
            private final T_ARR array;

            private OfPrimitive(T_NODE node, T_ARR array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfPrimitive(OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> parent, T_NODE node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> makeChild(int childIndex, int offset) {
                return new OfPrimitive<>(this, node.getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                node.copyInto(array, offset);
            }
        }

        private static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> {
            private OfInt(Node.OfInt node, int[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfLong
                extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> {
            private OfLong(Node.OfLong node, long[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> {
            private OfDouble(Node.OfDouble node, double[] array, int offset) {
                super(node, array, offset);
            }
        }
    }

    @SuppressWarnings("serial")
    private static class CollectorTask<P_IN, P_OUT, T_NODE extends Node<P_OUT>, T_BUILDER extends Node.Builder<P_OUT>>
            extends AbstractTask<P_IN, P_OUT, T_NODE, CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>> {
        protected final PipelineHelper<P_OUT> helper;
        protected final LongFunction<T_BUILDER> builderFactory;
        protected final BinaryOperator<T_NODE> concFactory;

        CollectorTask(PipelineHelper<P_OUT> helper,
                      Spliterator<P_IN> spliterator,
                      LongFunction<T_BUILDER> builderFactory,
                      BinaryOperator<T_NODE> concFactory) {
            super(helper, spliterator);
            this.helper = helper;
            this.builderFactory = builderFactory;
            this.concFactory = concFactory;
        }

        CollectorTask(CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> parent,
                      Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            helper = parent.helper;
            builderFactory = parent.builderFactory;
            concFactory = parent.concFactory;
        }

        @Override
        protected CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> makeChild(Spliterator<P_IN> spliterator) {
            return new CollectorTask<>(this, spliterator);
        }

		@Override
        @SuppressWarnings("unchecked")
        protected T_NODE doLeaf() {
            T_BUILDER builder = builderFactory.apply(helper.exactOutputSizeIfKnown(spliterator));
            return (T_NODE) helper.wrapAndCopyInto(builder, spliterator).build();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf())
                setLocalResult(concFactory.apply(leftChild.getLocalResult(), rightChild.getLocalResult()));
            super.onCompletion(caller);
        }

        private static final class OfRef<P_IN, P_OUT>
                extends CollectorTask<P_IN, P_OUT, Node<P_OUT>, Node.Builder<P_OUT>> {
            OfRef(PipelineHelper<P_OUT> helper,
                  IntFunction<P_OUT[]> generator,
                  Spliterator<P_IN> spliterator) {
                super(helper, spliterator, s -> builder(s, generator), ConcNode::new);
            }
        }

        private static final class OfInt<P_IN>
                extends CollectorTask<P_IN, Integer, Node.OfInt, Node.Builder.OfInt> {
            OfInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::intBuilder, ConcNode.OfInt::new);
            }
        }

        private static final class OfLong<P_IN>
                extends CollectorTask<P_IN, Long, Node.OfLong, Node.Builder.OfLong> {
            OfLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::longBuilder, ConcNode.OfLong::new);
            }
        }

        private static final class OfDouble<P_IN>
                extends CollectorTask<P_IN, Double, Node.OfDouble, Node.Builder.OfDouble> {
            OfDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::doubleBuilder, ConcNode.OfDouble::new);
            }
        }
    }
}
