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
import java.util.concurrent.CopyOnWriteArraySet;

final class CopyOnWriteArraySetSpliterator {

    private CopyOnWriteArraySetSpliterator() {
    }

    static <T> Spliterator<T> spliterator(CopyOnWriteArraySet<T> set) {
        Object[] array = CopyOnWriteArrayListSpliterator
                .getArray(getCowArrayList(set));
        return Spliterators.spliterator(array, Spliterator.IMMUTABLE
                | Spliterator.DISTINCT);
    }

    private static <T> CopyOnWriteArrayList<T> getCowArrayList(
            CopyOnWriteArraySet<T> set) {
        return (CopyOnWriteArrayList<T>) U.getObject(set, COW_ARRAY_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long COW_ARRAY_OFF;
    static {
        try {
            COW_ARRAY_OFF = U.objectFieldOffset(CopyOnWriteArraySet.class
                    .getDeclaredField("al"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
