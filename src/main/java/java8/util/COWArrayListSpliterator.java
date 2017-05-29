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
package java8.util;

import java.util.concurrent.CopyOnWriteArrayList;

// Spliterator for java.util.concurrent.CopyOnWriteArrayList
final class COWArrayListSpliterator {
// CVS rev. 1.143
    private COWArrayListSpliterator() {
    }

    static <T> Spliterator<T> spliterator(CopyOnWriteArrayList<T> list) {
        return Spliterators.spliterator(getArray(list), Spliterator.IMMUTABLE
                | Spliterator.ORDERED);
    }

    static <T> Object[] getArray(CopyOnWriteArrayList<T> list) {
        return (Object[]) U.getObject(list, ARRAY_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long ARRAY_OFF;
    static {
        ARRAY_OFF = fieldOffset(Spliterators.IS_ANDROID ? "elements" : "array",
                true);
    }

    static long fieldOffset(String arrayFieldName, boolean recursive) {
        try {
            return U.objectFieldOffset(CopyOnWriteArrayList.class
                    .getDeclaredField(arrayFieldName));
        } catch (Exception e) {
            if (recursive
                    && e instanceof NoSuchFieldException
                    && (Spliterators.IS_ANDROID && !Spliterators.IS_HARMONY_ANDROID)) {
                // https://android.googlesource.com/platform/libcore/+/29957558cf0db700bfaae360a80c42dc3871d0e5
                return fieldOffset("array", false);
            }
            throw new Error(e);
        }
    }
}
