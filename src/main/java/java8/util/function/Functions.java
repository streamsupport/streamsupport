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
 * {@link Function} interface. 
 */
public final class Functions {
    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies the {@code this_} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <R> the type of the result of the {@code this_} function and of the composed function
     * @param <T> the type of the input to the {@code this_} and of the result of the {@code before} function
     * @param <V> the type of input to the {@code before} function, and to the
     *           composed function
     * @param this_ the function to apply after the {@code before} function is applied
     * @param before the function to apply before the {@code this_} function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies the {@code this_} function
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if before is null
     *
     * @see #andThen(Function, Function)
     */
    public static <R, T, V> Function<V, R> compose(final Function<? super T, ? extends R> this_, final Function<? super V, ? extends T> before) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(before);
        return (V v) -> this_.apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies the {@code this_} function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <R> the type of the result of the {@code this_} function and of the input to the {@code after} function.
     * @param <T> the type of the input to the {@code this_} function and to the composed function
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param this_ the function to apply before the {@code after} function is applied
     * @param after the function to apply after the {@code this_} function is applied
     * @return a composed function that first applies the {@code this_} function and then
     * applies the {@code after} function
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if after is null
     *
     * @see #compose(Function, Function)
     */
    public static <R, T, V> Function<T, V> andThen(final Function<? super T, ? extends R> this_, final Function<? super R, ? extends V> after) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(after);
        return (T t) -> after.apply(this_.apply(t));
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    public static <T> Function<T, T> identity() {
        return t -> t;
    }

    private Functions() {
        throw new AssertionError();
    }
}
