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
 * {@link Consumer} interface. 
 */
public final class Consumers {
    /**
     * Returns a composed {@code Consumer} that performs, in sequence, the {@code this_}
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing the {@code this_} operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param <T> the type of the input to the operations
     * @param this_ the operation to perform before the {@code after} operation
     * @param after the operation to perform after the {@code this_} operation
     * @return a composed {@code Consumer} that performs in sequence the {@code this_}
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if {@code after} is null
     */
    public static <T> Consumer<T> andThen(final Consumer<? super T> this_, final Consumer<? super T> after) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(after);
        return (T t) -> { this_.accept(t); after.accept(t); };
    }

    private Consumers() {
        throw new AssertionError();
    }
}
