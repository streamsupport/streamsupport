/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package org.openjdk.other.tests.objects;

/**
 * @test
 * @summary Objects.checkIndex/jdk.internal.util.Preconditions.checkIndex tests
 * @run testng CheckIndex
 * @bug 8135248 8142493 8155794
 * @modules java.base/jdk.internal.util
 */

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java8.util.Objects;
import java8.util.function.BiConsumer;
import java8.util.function.BiFunction;
import java8.util.function.Function;
import java8.util.function.IntSupplier;
import java8.util.stream.StreamSupport;
import static org.testng.Assert.*;

public class CheckIndex {

//    @SuppressWarnings("serial")
//    static class AssertingOutOfBoundsException extends RuntimeException {
//        public AssertingOutOfBoundsException(String message) {
//            super(message);
//        }
//    }

//    static BiFunction<String, List<Integer>, AssertingOutOfBoundsException> assertingOutOfBounds(
//            String message, String expCheckKind, Integer... expArgs) {
//        return (checkKind, args) -> {
//            assertEquals(checkKind, expCheckKind);
//            assertEquals(args, Arrays.asList(expArgs));
//            try {
//                args.clear();
//                fail("Out of bounds List<Integer> argument should be unmodifiable");
//            } catch (Exception e)  {
//            }
//            return new AssertingOutOfBoundsException(message);
//        };
//    }

//    static BiFunction<String, List<Integer>, AssertingOutOfBoundsException> assertingOutOfBoundsReturnNull(
//            String expCheckKind, Integer... expArgs) {
//        return (checkKind, args) -> {
//            assertEquals(checkKind, expCheckKind);
//            assertEquals(args, Arrays.asList(expArgs));
//            return null;
//        };
//    }

    private static final int CHECK_INDEX = 1;
    private static final int CHECK_FROM_TO_INDEX = 2;
    private static final int CHECK_FROM_INDEX_SIZE = 3;

    static <EX extends RuntimeException>
    BiFunction<Integer, List<Integer>, EX> outOfBoundsExceptionFormatter(Function<String, EX> f) {
        return new BiFunction<Integer, List<Integer>, EX>() {
            @Override
            public EX apply(Integer checkKind, List<Integer> args) {
                int arg0 = args.get(0);
                int arg1 = args.get(1);
                int arg2 = args.size() == 3 ? args.get(2) : -1; 
                return f.apply(outOfBoundsMessage(checkKind, arg0, arg1, arg2));
            }
        };
    }

    private static String outOfBoundsMessage(int checkKind, int arg0, int arg1, int arg2) {
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

    static final int[] VALUES = {0, 1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, -1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE};

    @DataProvider
    static Object[][] checkIndexProvider() {
        List<Object[]> l = new ArrayList<>();
        for (int index : VALUES) {
            for (int length : VALUES) {
                boolean withinBounds = index >= 0 &&
                                       length >= 0 &&
                                       index < length;
                l.add(new Object[]{index, length, withinBounds});
            }
        }
        return l.toArray(new Object[0][0]);
    }

//    interface X {
//        int apply(int a, int b, int c);
//    }

    @Test(dataProvider = "checkIndexProvider")
    public void testCheckIndex(int index, int length, boolean withinBounds) {
        String expectedMessage = withinBounds
                                 ? null
                                 : outOfBoundsExceptionFormatter(IndexOutOfBoundsException::new).
                apply(CHECK_INDEX /*"checkIndex"*/, Arrays.asList(index, length)).getMessage();

        BiConsumer<Class<? extends RuntimeException>, IntSupplier> checker = (ec, s) -> {
            try {
                int rIndex = s.getAsInt();
                if (!withinBounds)
                    fail(String.format(
                            "Index %d is out of bounds of [0, %d), but was reported to be within bounds", index, length));
                assertEquals(rIndex, index);
            }
            catch (RuntimeException e) {
                assertTrue(ec.isInstance(e));
                if (withinBounds)
                    fail(String.format(
                            "Index %d is within bounds of [0, %d), but was reported to be out of bounds", index, length));
                else
                    assertEquals(e.getMessage(), expectedMessage);
            }
        };

//        checker.accept(AssertingOutOfBoundsException.class,
//                     () -> Preconditions.checkIndex(index, length,
//                                              assertingOutOfBounds(expectedMessage, "checkIndex", index, length)));
//        checker.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkIndex(index, length,
//                                              assertingOutOfBoundsReturnNull("checkIndex", index, length)));
//        checker.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkIndex(index, length, null));
        checker.accept(IndexOutOfBoundsException.class,
                     () -> Objects.checkIndex(index, length));
//        checker.accept(ArrayIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkIndex(index, length,
//                                              Preconditions.outOfBoundsExceptionFormatter(ArrayIndexOutOfBoundsException::new)));
//        checker.accept(StringIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkIndex(index, length,
//                                              Preconditions.outOfBoundsExceptionFormatter(StringIndexOutOfBoundsException::new)));
    }


    @DataProvider
    static Object[][] checkFromToIndexProvider() {
        List<Object[]> l = new ArrayList<>();
        for (int fromIndex : VALUES) {
            for (int toIndex : VALUES) {
                for (int length : VALUES) {
                    boolean withinBounds = fromIndex >= 0 &&
                                           toIndex >= 0 &&
                                           length >= 0 &&
                                           fromIndex <= toIndex &&
                                           toIndex <= length;
                    l.add(new Object[]{fromIndex, toIndex, length, withinBounds});
                }
            }
        }
        return l.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "checkFromToIndexProvider")
    public void testCheckFromToIndex(int fromIndex, int toIndex, int length, boolean withinBounds) {
        String expectedMessage = withinBounds
                                 ? null
                                 : outOfBoundsExceptionFormatter(IndexOutOfBoundsException::new).
                apply(CHECK_FROM_TO_INDEX /*"checkFromToIndex"*/, Arrays.asList(fromIndex, toIndex, length)).getMessage();

        BiConsumer<Class<? extends RuntimeException>, IntSupplier> check = (ec, s) -> {
            try {
                int rIndex = s.getAsInt();
                if (!withinBounds)
                    fail(String.format(
                            "Range [%d, %d) is out of bounds of [0, %d), but was reported to be within bounds", fromIndex, toIndex, length));
                assertEquals(rIndex, fromIndex);
            }
            catch (RuntimeException e) {
                assertTrue(ec.isInstance(e));
                if (withinBounds)
                    fail(String.format(
                            "Range [%d, %d) is within bounds of [0, %d), but was reported to be out of bounds", fromIndex, toIndex, length));
                else
                    assertEquals(e.getMessage(), expectedMessage);
            }
        };

//        check.accept(AssertingOutOfBoundsException.class,
//                     () -> Preconditions.checkFromToIndex(fromIndex, toIndex, length,
//                                                    assertingOutOfBounds(expectedMessage, "checkFromToIndex", fromIndex, toIndex, length)));
//        check.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromToIndex(fromIndex, toIndex, length,
//                                                    assertingOutOfBoundsReturnNull("checkFromToIndex", fromIndex, toIndex, length)));
//        check.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromToIndex(fromIndex, toIndex, length, null));
        check.accept(IndexOutOfBoundsException.class,
                     () -> Objects.checkFromToIndex(fromIndex, toIndex, length));
//        check.accept(ArrayIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromToIndex(fromIndex, toIndex, length,
//                                              Preconditions.outOfBoundsExceptionFormatter(ArrayIndexOutOfBoundsException::new)));
//        check.accept(StringIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromToIndex(fromIndex, toIndex, length,
//                                              Preconditions.outOfBoundsExceptionFormatter(StringIndexOutOfBoundsException::new)));
    }


    @DataProvider
    static Object[][] checkFromIndexSizeProvider() {
        List<Object[]> l = new ArrayList<>();
        for (int fromIndex : VALUES) {
            for (int size : VALUES) {
                for (int length : VALUES) {
                    // Explicitly convert to long
                    long lFromIndex = fromIndex;
                    long lSize = size;
                    long lLength = length;
                    // Avoid overflow
                    long lToIndex = lFromIndex + lSize;

                    boolean withinBounds = lFromIndex >= 0L &&
                                           lSize >= 0L &&
                                           lLength >= 0L &&
                                           lFromIndex <= lToIndex &&
                                           lToIndex <= lLength;
                    l.add(new Object[]{fromIndex, size, length, withinBounds});
                }
            }
        }
        return l.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "checkFromIndexSizeProvider")
    public void testCheckFromIndexSize(int fromIndex, int size, int length, boolean withinBounds) {
        String expectedMessage = withinBounds
                                 ? null
                                 : outOfBoundsExceptionFormatter(IndexOutOfBoundsException::new).
                apply(CHECK_FROM_INDEX_SIZE /*"checkFromIndexSize"*/, Arrays.asList(fromIndex, size, length)).getMessage();

        BiConsumer<Class<? extends RuntimeException>, IntSupplier> check = (ec, s) -> {
            try {
                int rIndex = s.getAsInt();
                if (!withinBounds)
                    fail(String.format(
                            "Range [%d, %d + %d) is out of bounds of [0, %d), but was reported to be within bounds", fromIndex, fromIndex, size, length));
                assertEquals(rIndex, fromIndex);
            }
            catch (RuntimeException e) {
                assertTrue(ec.isInstance(e));
                if (withinBounds)
                    fail(String.format(
                            "Range [%d, %d + %d) is within bounds of [0, %d), but was reported to be out of bounds", fromIndex, fromIndex, size, length));
                else
                    assertEquals(e.getMessage(), expectedMessage);
            }
        };

//        check.accept(AssertingOutOfBoundsException.class,
//                     () -> Preconditions.checkFromIndexSize(fromIndex, size, length,
//                                                      assertingOutOfBounds(expectedMessage, "checkFromIndexSize", fromIndex, size, length)));
//        check.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromIndexSize(fromIndex, size, length,
//                                                      assertingOutOfBoundsReturnNull("checkFromIndexSize", fromIndex, size, length)));
//        check.accept(IndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromIndexSize(fromIndex, size, length, null));
        check.accept(IndexOutOfBoundsException.class,
                     () -> Objects.checkFromIndexSize(fromIndex, size, length));
//        check.accept(ArrayIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromIndexSize(fromIndex, size, length,
//                                                    Preconditions.outOfBoundsExceptionFormatter(ArrayIndexOutOfBoundsException::new)));
//        check.accept(StringIndexOutOfBoundsException.class,
//                     () -> Preconditions.checkFromIndexSize(fromIndex, size, length,
//                                                    Preconditions.outOfBoundsExceptionFormatter(StringIndexOutOfBoundsException::new)));
    }

    @Test
    public void uniqueMessagesForCheckKinds() {
        BiFunction<Integer, List<Integer>, IndexOutOfBoundsException> f =
                outOfBoundsExceptionFormatter(IndexOutOfBoundsException::new);

        List<String> messages = new ArrayList<>();
        // Exact arguments
        messages.add(f.apply(CHECK_INDEX /*"checkIndex"*/, Arrays.asList(-1, 0)).getMessage());
        messages.add(f.apply(CHECK_FROM_TO_INDEX /*"checkFromToIndex"*/, Arrays.asList(-1, 0, 0)).getMessage());
        messages.add(f.apply(CHECK_FROM_INDEX_SIZE /*"checkFromIndexSize"*/, Arrays.asList(-1, 0, 0)).getMessage());
//        // Unknown check kind
//        messages.add(f.apply("checkUnknown", Arrays.asList(-1, 0, 0)).getMessage());
//        // Known check kind with more arguments
//        messages.add(f.apply("checkIndex", Arrays.asList(-1, 0, 0)).getMessage());
//        messages.add(f.apply("checkFromToIndex", Arrays.asList(-1, 0, 0, 0)).getMessage());
//        messages.add(f.apply("checkFromIndexSize", Arrays.asList(-1, 0, 0, 0)).getMessage());
//        // Known check kind with fewer arguments
//        messages.add(f.apply("checkIndex", Arrays.asList(-1)).getMessage());
//        messages.add(f.apply("checkFromToIndex", Arrays.asList(-1, 0)).getMessage());
//        messages.add(f.apply("checkFromIndexSize", Arrays.asList(-1, 0)).getMessage());
//        // Null arguments
//        messages.add(f.apply(null, null).getMessage());
//        messages.add(f.apply("checkNullArguments", null).getMessage());
//        messages.add(f.apply(null, Arrays.asList(-1)).getMessage());

        assertEquals(messages.size(), StreamSupport.stream(messages).distinct().count());
    }
}
