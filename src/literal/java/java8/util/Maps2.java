/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;
import java.util.Map.Entry;

/**
 * A place for the new Java 9 <a href="http://openjdk.java.net/jeps/269">JEP
 * 269</a> {@code "Immutable Map Static Factory Methods"} in the {@link Map}
 * interface.
 * 
 * <h2><a name="immutable">Immutable Map Static Factory Methods</a></h2>
 * <p>
 * The {@link Maps2#of() Maps.of()} and {@link Maps2#ofEntries(Map.Entry...)
 * Maps.ofEntries()} static factory methods provide a convenient way to create
 * immutable maps. The {@code Map} instances created by these methods have the
 * following characteristics:
 *
 * <ul>
 * <li>They are <em>structurally immutable</em>. Keys and values cannot be
 * added, removed, or updated. Calling any mutator method will always cause
 * {@code UnsupportedOperationException} to be thrown.
 * However, if the contained keys or values are themselves mutable, this may cause
 * the Map to behave inconsistently or its contents to appear to change.
 * <li>They disallow {@code null} keys and values. Attempts to create them with
 * {@code null} keys or values result in {@code NullPointerException}.
 * <li>They are serializable if all keys and values are serializable.
 * <li>They reject duplicate keys at creation time. Duplicate keys passed to a
 * static factory method result in {@code IllegalArgumentException}.
 * <li>The iteration order of mappings is unspecified and is subject to change.
 * <li>They are <a
 * href="package-summary.html#Value-based-Classes">value-based</a>. Callers
 * should make no assumptions about the identity of the returned instances.
 * Factories are free to create new instances or reuse existing ones. Therefore,
 * identity-sensitive operations on these instances (reference equality (
 * {@code ==}), identity hash code, and synchronization) are unreliable and
 * should be avoided.
 * </ul>
 */
public final class Maps2 {
    /**
     * Returns an immutable map containing zero mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return an empty {@code Map}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of() {
        return ImmutableCollections.Map0.instance();
    }

    /**
     * Returns an immutable map containing a single mapping.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the mapping's key
     * @param v1 the mapping's value
     * @return a {@code Map} containing the specified mapping
     * @throws NullPointerException if the key or the value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1) {
        return new ImmutableCollections.Map1<K, V>(k1, v1);
    }

    /**
     * Returns an immutable map containing two mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the keys are duplicates
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2);
    }

    /**
     * Returns an immutable map containing three mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3);
    }

    /**
     * Returns an immutable map containing four mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Returns an immutable map containing five mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Returns an immutable map containing six mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5,
                                                   k6, v6);
    }

    /**
     * Returns an immutable map containing seven mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5,
                                                   k6, v6, k7, v7);
    }

    /**
     * Returns an immutable map containing eight mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5,
                                                   k6, v6, k7, v7, k8, v8);
    }

    /**
     * Returns an immutable map containing nine mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @param k9 the ninth mapping's key
     * @param v9 the ninth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5,
                                                   k6, v6, k7, v7, k8, v8, k9, v9);
    }

    /**
     * Returns an immutable map containing ten mappings.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @param k9 the ninth mapping's key
     * @param v9 the ninth mapping's value
     * @param k10 the tenth mapping's key
     * @param v10 the tenth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is {@code null}
     *
     * @since 9
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return new ImmutableCollections.MapN<K, V>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5,
                                                   k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    /**
     * Returns an immutable map containing keys and values extracted from the given entries.
     * The entries themselves are not stored in the map.
     * See <a href="#immutable">Immutable Map Static Factory Methods</a> for details.
     *
     * <p><b>API Note:</b><br>
     * It is convenient to create the map entries using the {@link Maps2#entry Maps.entry()} method.
     * For example,
     *
     * <pre>
     * {@code
     *     import static java.util.Maps.entry;
     *
     *     Map<Integer,String> map = Maps.ofEntries(
     *         entry(1, "a"),
     *         entry(2, "b"),
     *         entry(3, "c"),
     *         ...
     *         entry(26, "z"));
     * }</pre>
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param entries {@code Map.Entry}s containing the keys and values from which the map is populated
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any entry, key, or value is {@code null}, or if
     *         the {@code entries} array is {@code null}
     *
     * @see Maps2#entry Maps.entry()
     * @since 9
     */
    public static <K, V> Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        if (entries.length == 0) { // implicit null check of entries
            return ImmutableCollections.Map0.instance();
        } else if (entries.length == 1) {
            return new ImmutableCollections.Map1<K, V>(entries[0].getKey(),
                                                       entries[0].getValue());
        } else {
            Object[] kva = new Object[entries.length << 1];
            int a = 0;
            for (Map.Entry<? extends K, ? extends V> entry : entries) {
                kva[a++] = entry.getKey();
                kva[a++] = entry.getValue();
            }
            return new ImmutableCollections.MapN<K, V>(kva);
        }
    }

    /**
     * Returns an immutable {@link Entry} containing the given key and value.
     * These entries are suitable for populating {@code Map} instances using the
     * {@link Maps2#ofEntries Maps.ofEntries()} method.
     * The {@code Entry} instances created by this method have the following characteristics:
     *
     * <ul>
     * <li>They disallow {@code null} keys and values. Attempts to create them using a {@code null}
     * key or value result in {@code NullPointerException}.
     * <li>They are immutable. Calls to {@link Map.Entry#setValue Entry.setValue()}
     * on a returned {@code Entry} result in {@code UnsupportedOperationException}.
     * <li>They are not serializable.
     * <li>They are <a href="../lang/package-summary.html#Value-based-Classes">value-based</a>.
     * Callers should make no assumptions about the identity of the returned instances.
     * This method is free to create new instances or reuse existing ones. Therefore,
     * identity-sensitive operations on these instances (reference equality ({@code ==}),
     * identity hash code, and synchronization) are unreliable and should be avoided.
     * </ul>
     *
     * <p><b>API Note:</b><br>
     * For a serializable {@code Entry}, see {@link java.util.AbstractMap.SimpleEntry} or
     * {@link java.util.AbstractMap.SimpleImmutableEntry}.
     *
     * @param <K> the key's type
     * @param <V> the value's type
     * @param k the key
     * @param v the value
     * @return an {@code Entry} containing the specified key and value
     * @throws NullPointerException if the key or value is {@code null}
     *
     * @see Maps2#ofEntries Maps.ofEntries()
     * @since 9
     */
    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        // KeyValueHolder checks for nulls
        return new KeyValueHolder<K, V>(k, v);
    }

    private Maps2() {
    }
}
