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

/**
 * Provides classes for some of the new static methods that were added in Java 8.
 *
 * <h2><a id="Value-based-Classes">Value-based Classes</a></h2>
 * <p>
 * Some classes, such as {@code java8.util.Optional}, are value-based. Instances
 * of a value-based class:
 *
 * <ul>
 * <li>are final and immutable (though may contain references to mutable objects);
 * <li>have implementations of equals, hashCode, and toString which are  computed
 * solely from the instance's state and not from its identity  or the state of
 * any other object or variable;
 * <li>make no use of identity-sensitive operations such as reference equality
 * (==) between instances, identity hash code of instances, or synchronization
 * on an instances's intrinsic lock;
 * <li>are considered equal solely based on equals(), not based on reference
 * equality (==);
 * <li>do not have accessible constructors, but are instead instantiated through
 * factory methods which make no committment as to the identity of returned
 * instances;
 * <li>are freely substitutable when equal, meaning that interchanging any two
 * instances x and y that are equal according to equals() in any computation or
 * method invocation should produce no visible change in behavior.
 * </ul>
 *
 * A program may produce unpredictable results if it attempts to distinguish two
 * references to equal values of a value-based class, whether directly via
 * reference equality or indirectly via an appeal to synchronization, identity
 * hashing, serialization, or any other identity-sensitive mechanism. Use of such
 * identity-sensitive operations on instances of value-based classes may have
 * unpredictable effects and should be avoided.
 * <p>
 * 
 * @since 1.8
 */
package java8.lang;

