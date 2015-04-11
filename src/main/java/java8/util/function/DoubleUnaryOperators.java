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
package java8.util.function;

import java8.util.Objects;

/**
 * A place for static default implementations of the new Java 8
 * default interface methods and static interface methods in the
 * {@link DoubleUnaryOperator} interface. 
 */
public final class DoubleUnaryOperators {
    /**
     * Returns a composed operator that first applies the {@code before}
     * operator to its input, and then applies the {@code this_} operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param this_ the operator to apply after the {@code before} operator is applied
     * @param before the operator to apply before the {@code this_} operator is applied
     * @return a composed operator that first applies the {@code before}
     * operator and then applies the {@code this_} operator
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if before is null
     *
     * @see #andThen(DoubleUnaryOperator, DoubleUnaryOperator)
     */
    public static DoubleUnaryOperator compose(final DoubleUnaryOperator this_, final DoubleUnaryOperator before) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(before);
        return (double v) -> this_.applyAsDouble(before.applyAsDouble(v));
    }

    /**
     * Returns a composed operator that first applies the {@code this_} operator to
     * its input, and then applies the {@code after} operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param this_ the operator to apply before the {@code after} operator is applied
     * @param after the operator to apply after the {@code this_} operator is applied
     * @return a composed operator that first applies the {@code this_} operator and then
     * applies the {@code after} operator
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if after is null
     *
     * @see #compose(DoubleUnaryOperator, DoubleUnaryOperator)
     */
    public static DoubleUnaryOperator andThen(final DoubleUnaryOperator this_, final DoubleUnaryOperator after) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(after);
        return (double t) -> after.applyAsDouble(this_.applyAsDouble(t));
    }

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @return a unary operator that always returns its input argument
     */
    public static DoubleUnaryOperator identity() {
        return t -> t;
    }

    private DoubleUnaryOperators() {
        throw new AssertionError();
    }
}
