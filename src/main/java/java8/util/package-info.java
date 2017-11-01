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

/**
 * Provides some of the new {@code java.util} classes and implementations of
 * static and default interface methods added in Java 8 and, in addition, the
 * <a href="http://openjdk.java.net/jeps/269">JEP 269: Convenience Factory
 * Methods for Collections</a> that were introduced in Java 9.
 * 
 * <p>
 * <a id="unmodifiable"><b>Unmodifiable collections</b></a> An <i>unmodifiable
 * collection</i> is a collection, all of whose mutator methods are specified to
 * throw {@code UnsupportedOperationException}. Such a collection thus cannot be
 * modified by calling any methods on it. For a collection to be properly
 * unmodifiable, any view collections derived from it must also be unmodifiable.
 * For example, if a List is unmodifiable, the List returned by
 * {@link java.util.List#subList List.subList} is also unmodifiable.
 *
 * <p>
 * An unmodifiable collection is not necessarily immutable. If the contained
 * elements are mutable, the entire collection is clearly mutable, even though
 * it might be unmodifiable. For example, consider two unmodifiable lists
 * containing mutable elements. The result of calling
 * {@code list1.equals(list2)} might differ from one call to the next if the
 * elements had been mutated, even though both lists are unmodifiable. However,
 * if an unmodifiable collection contains all immutable elements, it can be
 * considered effectively immutable.
 * 
 * @since 1.8
 */
package java8.util;
