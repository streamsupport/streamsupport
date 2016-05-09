/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Utility methods to check if state or arguments are correct.
 */
final class Preconditions {

    static final int CHECK_INDEX = 1;
    static final int CHECK_FROM_TO_INDEX = 2;
    static final int CHECK_FROM_INDEX_SIZE = 3;

    private static IndexOutOfBoundsException outOfBounds(int checkKind,
            int arg0, int arg1, int arg2) {
        return new IndexOutOfBoundsException(outOfBoundsMessage(checkKind, arg0, arg1, arg2));
    }

    static String outOfBoundsMessage(int checkKind, int arg0, int arg1, int arg2) {
        if (checkKind == CHECK_INDEX) { // "checkIndex" (1)
            return String.format("Index %d out-of-bounds for length %d",
                    arg0, arg1);
        }
        if (checkKind == CHECK_FROM_TO_INDEX) { // "checkFromToIndex" (2)
            return String.format("Range [%d, %d) out-of-bounds for length %d",
                    arg0, arg1, arg2);
        }
        if (checkKind == CHECK_FROM_INDEX_SIZE) { // "checkFromIndexSize" (3)
            return String.format("Range [%d, %<d + %d) out-of-bounds for length %d",
                    arg0, arg1, arg2);
        }
        throw new IllegalStateException();
    }

    /**
     * Checks if the {@code index} is within the bounds of the range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The {@code index} is defined to be out-of-bounds if any of the
     * following inequalities is true:
     * <ul>
     *  <li>{@code index < 0}</li>
     *  <li>{@code index >= length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the {@code index} is out-of-bounds, then a runtime exception is
     * thrown that is the result of applying the following arguments to the
     * exception formatter: the index of this method, {@code checkIndex};
     * and 2 integers whose values are, in order, the out-of-bounds arguments
     * {@code index} and {@code length}.
     *
     * @param index the index
     * @param length the upper-bound (exclusive) of the range
     * @return {@code index} if it is within bounds of the range
     * @throws IndexOutOfBoundsException if the {@code index} is out-of-bounds
     * @since 9
     */
    static int checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw outOfBounds(CHECK_INDEX, index, length, -1); // "checkIndex" (1)
        }
        return index;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive) is within the bounds of range from {@code 0}
     * (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out-of-bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code fromIndex > toIndex}</li>
     *  <li>{@code toIndex > length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the sub-range  is out-of-bounds, then a runtime exception is
     * thrown that is the result of applying the following arguments to the
     * exception formatter: the index of this method, {@code checkFromToIndex};
     * and 3 integers whose values are, in order, the out-of-bounds arguments
     * {@code fromIndex}, {@code toIndex}, and {@code length}.
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-range
     * @param toIndex the upper-bound (exclusive) of the sub-range
     * @param length the upper-bound (exclusive) the range
     * @return {@code fromIndex} if the sub-range is within bounds of the range
     * @throws IndexOutOfBoundsException if the sub-range is out-of-bounds
     * @since 9
     */
    static int checkFromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
            throw outOfBounds(CHECK_FROM_TO_INDEX, fromIndex, toIndex, length); // "checkFromToIndex" (2)
        }
        return fromIndex;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code fromIndex + size} (exclusive) is within the bounds of range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out-of-bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code size < 0}</li>
     *  <li>{@code fromIndex + size > length}, taking into account integer overflow</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the sub-range  is out-of-bounds, then a runtime exception is
     * thrown that is the result of applying the following arguments to the
     * exception formatter: the index of this method, {@code checkFromIndexSize};
     * and 3 integers whose values are, in order, the out-of-bounds arguments
     * {@code fromIndex}, {@code size}, and {@code length}.
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size the size of the sub-range
     * @param length the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range is within bounds of the range
     * @throws IndexOutOfBoundsException if the sub-range is out-of-bounds
     * @since 9
     */
    static int checkFromIndexSize(int fromIndex, int size, int length) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            throw outOfBounds(CHECK_FROM_INDEX_SIZE, fromIndex, size, length); // "checkFromIndexSize" (3)
        }
        return fromIndex;
    }

    private Preconditions() {
    }
}
