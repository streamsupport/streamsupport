/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import java8.util.Objects;
import java8.util.concurrent.ConcurrentMaps;
import java8.util.function.BiConsumer;
import java8.util.function.BiFunction;
import java8.util.function.Function;

/**
 * A place for static default implementations of the new Java 8 / Java 9
 * default interface methods and static interface methods in the
 * {@link Map} interface.
 * 
 * <h2><a name="immutable">Immutable Map Static Factory Methods</a></h2>
 * <p>The {@link Maps#of() Maps.of()} and
 * {@link Maps#ofEntries(Map.Entry...) Maps.ofEntries()}
 * static factory methods provide a convenient way to create immutable maps.
 * The {@code Map}
 * instances created by these methods have the following characteristics:
 *
 * <ul>
 * <li>They are <em>structurally immutable</em>. Keys and values cannot be added,
 * removed, or updated. Attempts to do so result in {@code UnsupportedOperationException}.
 * However, if the contained keys or values are themselves mutable, this may cause the
 * Map to behave inconsistently or its contents to appear to change.
 * <li>They disallow {@code null} keys and values. Attempts to create them with
 * {@code null} keys or values result in {@code NullPointerException}.
 * <li>They are serializable if all keys and values are serializable.
 * <li>They reject duplicate keys at creation time. Duplicate keys
 * passed to a static factory method result in {@code IllegalArgumentException}.
 * <li>The iteration order of mappings is unspecified and is subject to change.
 * <li>They are <a href="../lang/package-summary.html#Value-based-Classes">value-based</a>.
 * Callers should make no assumptions about the identity of the returned instances.
 * Factories are free to create new instances or reuse existing ones. Therefore,
 * identity-sensitive operations on these instances (reference equality ({@code ==}),
 * identity hash code, and synchronization) are unreliable and should be avoided.
 * </ul>
 */
public final class Maps {
    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the {@code
     * map}:
     *
     * <pre> {@code
     * V v = map.get(key);
     * if (v == null)
     *     v = map.put(key, value);
     *
     * return v;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code putIfAbsent}
     * operation.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for the map (optional)
     * @throws NullPointerException if the specified key or value is null,
     *         and the map does not permit null keys or values (optional)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in the map (optional)
     * @since 1.8
     */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        Objects.requireNonNull(map);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ((ConcurrentMap<K, V>) map).putIfAbsent(key, value);
        }

        V v = map.get(key);
        if (v == null) {
            v = map.put(key, value);
        }
        return v;
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if the map contains no mapping for the key.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code getOrDefault}
     * operation.
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if the map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     * the map (optional)
     * @throws NullPointerException if the specified key is null and the map
     * does not permit null keys (optional)
     * @since 1.8
     */
    public static <K, V> V getOrDefault(Map<K, V> map, Object key, V defaultValue) {
        Objects.requireNonNull(map);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ConcurrentMaps.getOrDefault((ConcurrentMap<K, V>) map, key, defaultValue);
        }

        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
            ? v
            : defaultValue;
    }

    /**
     * Performs the given action for each entry in the map until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the {@code map}:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     *
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code forEach}
     * operation.
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified map or action is null
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 1.8
     */
    public static <K, V> void forEach(Map<K, V> map, BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(action);
        // safety measure
        if (map instanceof ConcurrentMap) {
            ConcurrentMaps.forEach((ConcurrentMap<K, V>) map, action);
        } else {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K k;
                V v;
                try {
                    k = entry.getKey();
                    v = entry.getValue();
                } catch (IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    ConcurrentModificationException cme = new ConcurrentModificationException();
                    cme.initCause(ise);
                    throw cme;
                }
                action.accept(k, v);
            }
        }
    }

    /**
     * If the specified key is not already associated with a value or is
     * associated with null, associates it with the given non-null value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}. This
     * method may be of use when combining multiple mapped values for a key.
     * For example, to either create or append a {@code String msg} to a
     * value mapping:
     *
     * <pre> {@code
     * map.merge(key, msg, String::concat)
     * }</pre>
     *
     * <p>If the remapping function returns {@code null} the mapping is removed.
     * If the remapping function itself throws an (unchecked) exception, the
     * exception is rethrown, and the current mapping is left unchanged.
     *
     * <p>The remapping function itself should not modify the passed map during
     * computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to performing the following
     * steps for the {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * remapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Non-concurrent implementations should
     * override this method and, on a best-effort basis, throw a
     * {@code ConcurrentModificationException} if it is detected that the
     * remapping function modifies the map during computation. Concurrent
     * implementations should override this method and, on a best-effort basis,
     * throw an {@code IllegalStateException} if it is detected that the
     * remapping function modifies the map during computation and as a result
     * computation would never complete.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the remapping function is applied once atomically only if the
     * value is not present.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code merge} operation.
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value
     *        associated with the key or, if no existing value or a null value
     *        is associated with the key, to be associated with the key
     * @param remappingFunction the remapping function to recompute a value if present
     * @return the new value associated with the specified key, or null if no
     *         value is associated with the key
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in the map (optional)
     * @throws NullPointerException if the specified key is null and the map
     *         does not support null keys or the value or remappingFunction is
     *         null
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in the map (optional)
     * @since 1.8
     */
    public static <K, V> V merge(Map<K, V> map, K key, V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ConcurrentMaps.merge((ConcurrentMap<K, V>) map, key, value, remappingFunction);
        }

        V oldValue = map.get(key);
        V newValue = (oldValue == null) ? value :
                   remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            map.remove(key);
        } else {
            map.put(key, newValue);
        }
        return newValue;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into the passed {@code map} unless {@code null}.
     *
     * <p>If the mapping function returns {@code null} no mapping is recorded.
     * If the mapping function itself throws an (unchecked) exception, the
     * exception is rethrown, and no mapping is recorded.  The most
     * common usage is to construct a new object serving as an initial
     * mapped value or memoized result, as in:
     *
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new Value(f(k)));
     * }</pre>
     *
     * <p>Or to implement a multi-value map, {@code Map<K,Collection<V>>},
     * supporting multiple values per key:
     *
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
     * }</pre>
     *
     * <p>The mapping function itself should not modify the passed map during
     * computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to the following steps for the
     * {@code map}, then returning the current value or {@code null} if now
     * absent:
     *
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         map.put(key, newValue);
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * mapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Non-concurrent implementations should
     * override this method and, on a best-effort basis, throw a
     * {@code ConcurrentModificationException} if it is detected that the
     * mapping function modifies the map during computation. Concurrent
     * implementations should override this method and, on a best-effort basis,
     * throw an {@code IllegalStateException} if it is detected that the
     * mapping function modifies the map during computation and as a result
     * computation would never complete.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the mapping function is applied once atomically only if the value
     * is not present.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code computeIfAbsent}
     * operation.
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the mapping function to compute a value
     * @return the current (existing or computed) value associated with
     *         the specified key, or null if the computed value is null
     * @throws NullPointerException if the specified key is null and
     *         the map does not support null keys, or the mappingFunction
     *         is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in the map (optional)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in the map (optional)
     * @since 1.8
     */
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(mappingFunction);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ConcurrentMaps.computeIfAbsent((ConcurrentMap<K, V>) map, key, mappingFunction);
        }

        V v;
        if ((v = map.get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                map.put(key, newValue);
                return newValue;
            }
        }
        return v;
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * The default implementation does not throw NullPointerException
     * for maps that do not support null values if oldValue is null unless
     * newValue is also null.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code replace} operation.
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of a specified key or value
     *         prevents it from being stored in the map
     * @throws NullPointerException if a specified key or newValue is null,
     *         and the map does not permit null keys or values
     * @throws NullPointerException if oldValue is null and the map does not
     *         permit null values (optional)
     * @throws IllegalArgumentException if some property of a specified key
     *         or value prevents it from being stored in the map
     * @since 1.8
     */
    public static <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
        Objects.requireNonNull(map);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ((ConcurrentMap<K, V>) map).replace(key, oldValue, newValue);
        }

        Object curValue = map.get(key);
        if (!Objects.equals(curValue, oldValue) ||
            (curValue == null && !map.containsKey(key))) {
            return false;
        }
        map.put(key, newValue);
        return true;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key)) {
     *     return map.put(key, value);
     * } else
     *     return null;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     * 
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code replace} operation.
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in the map (optional)
     * @throws NullPointerException if the specified key or value is null,
     *         and the map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in the map
     * @since 1.8
     */
    public static <K, V> V replace(Map<K, V> map, K key, V value) {
        Objects.requireNonNull(map);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ((ConcurrentMap<K, V>) map).replace(key, value);
        }

        V curValue;
        if (((curValue = map.get(key)) != null) || map.containsKey(key)) {
            curValue = map.put(key, value);
        }
        return curValue;
    }

    /**
     * Replaces each entry's value with the result of invoking the given
     * function on that entry until all entries have been processed or the
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br>
     * <p>The default implementation is equivalent to, for the {@code map}:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     entry.setValue(function.apply(entry.getKey(), entry.getValue()));
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code replaceAll}
     * operation.
     * @param function the function to apply to each entry
     * @throws UnsupportedOperationException if the {@code set} operation
     * is not supported by the map's entry set iterator.
     * @throws ClassCastException if the class of a replacement value
     * prevents it from being stored in the map
     * @throws NullPointerException if the specified function is null, or the
     * specified replacement value is null, and the map does not permit null
     * values
     * @throws ClassCastException if a replacement value is of an inappropriate
     *         type for the map (optional)
     * @throws NullPointerException if function or a replacement value is null,
     *         and the map does not permit null keys or values (optional)
     * @throws IllegalArgumentException if some property of a replacement value
     *         prevents it from being stored in the map (optional)
     * @throws ConcurrentModificationException if an entry is found to be
     *         removed during iteration
     * @since 1.8
     */
    public static <K, V> void replaceAll(Map<K, V> map, BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(function);
        // safety measure
        if (map instanceof ConcurrentMap) {
            ConcurrentMaps.replaceAll((ConcurrentMap<K, V>) map, function);
        } else {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K k;
                V v;
                try {
                    k = entry.getKey();
                    v = entry.getValue();
                } catch (IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    ConcurrentModificationException cmex = new ConcurrentModificationException();
                    cmex.initCause(ise);
                    throw cmex;
                }
    
                // ise thrown from function is not a cme.
                v = function.apply(k, v);
    
                try {
                    entry.setValue(v);
                } catch (IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    ConcurrentModificationException cmex = new ConcurrentModificationException();
                    cmex.initCause(ise);
                    throw cmex;
                }
            }
        }
    }

    /**
     * Attempts to compute a mapping for the specified key and its current
     * mapped value (or {@code null} if there is no current mapping). For
     * example, to either create or append a {@code String} msg to a value
     * mapping:
     *
     * <pre> {@code
     * map.compute(key, (k, v) -> (v == null) ? msg : v.concat(msg))}</pre>
     * (Method {@link #merge merge()} is often simpler to use for such purposes.)
     *
     * <p>If the remapping function returns {@code null}, the mapping is removed
     * (or remains absent if initially absent).  If the remapping function itself
     * throws an (unchecked) exception, the exception is rethrown, and the current
     * mapping is left unchanged.
     *
     * <p>The remapping function itself should not modify the passed map during
     * computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to performing the following
     * steps for the {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       map.remove(key);
     * } else {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       return null;
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * remapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Non-concurrent implementations should
     * override this method and, on a best-effort basis, throw a
     * {@code ConcurrentModificationException} if it is detected that the
     * remapping function modifies the map during computation. Concurrent
     * implementations should override this method and, on a best-effort basis,
     * throw an {@code IllegalStateException} if it is detected that the
     * remapping function modifies the map during computation and as a result
     * computation would never complete.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the remapping function is applied once atomically only if the
     * value is not present.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code compute} operation.
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the remapping function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null and
     *         the map does not support null keys, or the
     *         remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in the map (optional)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map (optional)
     * @since 1.8
     */
    public static <K, V> V compute(Map<K, V> map, K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(remappingFunction);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ConcurrentMaps.compute((ConcurrentMap<K, V>) map, key, remappingFunction);
        }

        V oldValue = map.get(key);

        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            // delete mapping
            if (oldValue != null || map.containsKey(key)) {
                // something to remove
                map.remove(key);
                return null;
            } else {
                // nothing to do. Leave things as they were.
                return null;
            }
        } else {
            // add or replace old mapping
            map.put(key, newValue);
            return newValue;
        }
    }

    /**
     * If the value for the specified key is present and non-null, attempts to
     * compute a new mapping given the key and its current mapped value.
     *
     * <p>If the remapping function returns {@code null}, the mapping is removed.
     * If the remapping function itself throws an (unchecked) exception, the
     * exception is rethrown, and the current mapping is left unchanged.
     *
     * <p>The remapping function itself should not modify the passed map during
     * computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to performing the following
     * steps for the {@code map}, then returning the current value or
     * {@code null} if now absent:
     *
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.put(key, newValue);
     *     else
     *         map.remove(key);
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * remapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Non-concurrent implementations should
     * override this method and, on a best-effort basis, throw a
     * {@code ConcurrentModificationException} if it is detected that the
     * remapping function modifies the map during computation. Concurrent
     * implementations should override this method and, on a best-effort basis,
     * throw an {@code IllegalStateException} if it is detected that the
     * remapping function modifies the map during computation and as a result
     * computation would never complete.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the remapping function is applied once atomically only if the
     * value is not present.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code computeIfPresent}
     * operation.
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the remapping function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null and
     *         the map does not support null keys, or the
     *         remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in the map (optional)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map (optional)
     * @since 1.8
     */
    public static <K, V> V computeIfPresent(Map<K, V> map, K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(remappingFunction);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ConcurrentMaps.computeIfPresent((ConcurrentMap<K, V>) map, key, remappingFunction);
        }

        V oldValue;
        if ((oldValue = map.get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                map.put(key, newValue);
                return newValue;
            } else {
                map.remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to, for the {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.remove(key);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code Map} on which to execute the {@code remove} operation.
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation
     *         is not supported by the map (optional)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for the map (optional)
     * @throws NullPointerException if the specified key or value is null,
     *         and the map does not permit null keys or values (optional)
     * @since 1.8
     */
    public static <K, V> boolean remove(Map<K, V> map, Object key, Object value) {
        Objects.requireNonNull(map);
        // safety measure
        if (map instanceof ConcurrentMap) {
            return ((ConcurrentMap<K, V>) map).remove(key, value);
        }

        Object curValue = map.get(key);
        if (!Objects.equals(curValue, value) ||
            (curValue == null && !map.containsKey(key))) {
            return false;
        }
        map.remove(key);
        return true;
    }

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
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> of() {
        return (Map<K, V>) ImmutableCollections.Map0.EMPTY_MAP;
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
     * It is convenient to create the map entries using the {@link Maps#entry Maps.entry()} method.
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
     * @see Maps#entry Maps.entry()
     * @since 9
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries);
        if (entries.length == 0) {
            return (Map<K, V>) ImmutableCollections.Map0.EMPTY_MAP;
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
     * {@link Maps#ofEntries Maps.ofEntries()} method.
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
     * @see Maps#ofEntries Maps.ofEntries()
     * @since 9
     */
    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        // KeyValueHolder checks for nulls
        return new KeyValueHolder<K, V>(k, v);
    }

    /**
     * A place for the static interface methods of the Java 8 {@link Map.Entry}
     * interface.
     */
    public static final class Entry {
        /**
         * Returns a comparator that compares {@link Map.Entry} in natural order
         * on key.
         *
         * <p>
         * The returned comparator is serializable and throws
         * {@link NullPointerException} when comparing an entry with a null key.
         *
         * @param <K>
         *            the {@link Comparable} type of then map keys
         * @param <V>
         *            the type of the map values
         * @return a comparator that compares {@link Map.Entry} in natural order
         *         on key.
         * @see Comparable
         * @since 1.8
         */
        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> comparingByKey() {
            return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> c1
                    .getKey().compareTo(c2.getKey());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} in natural order
         * on value.
         *
         * <p>
         * The returned comparator is serializable and throws
         * {@link NullPointerException} when comparing an entry with null
         * values.
         *
         * @param <K>
         *            the type of the map keys
         * @param <V>
         *            the {@link Comparable} type of the map values
         * @return a comparator that compares {@link Map.Entry} in natural order
         *         on value.
         * @see Comparable
         * @since 1.8
         */
        public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValue() {
            return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> c1
                    .getValue().compareTo(c2.getValue());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} by key using the
         * given {@link Comparator}.
         *
         * <p>
         * The returned comparator is serializable if the specified comparator
         * is also serializable.
         *
         * @param <K>
         *            the type of the map keys
         * @param <V>
         *            the type of the map values
         * @param cmp
         *            the key {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the key.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(
                Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> cmp
                    .compare(c1.getKey(), c2.getKey());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} by value using
         * the given {@link Comparator}.
         *
         * <p>
         * The returned comparator is serializable if the specified comparator
         * is also serializable.
         *
         * @param <K>
         *            the type of the map keys
         * @param <V>
         *            the type of the map values
         * @param cmp
         *            the value {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the value.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByValue(
                Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> cmp
                    .compare(c1.getValue(), c2.getValue());
        }

        private Entry() {
        }
    }

    private Maps() {
    }
}
