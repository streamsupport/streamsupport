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
package java8.util.concurrent;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import java8.util.Objects;
import java8.util.function.BiConsumer;
import java8.util.function.BiFunction;
import java8.util.function.Function;

/**
 * A place for static default implementations of the new Java 8
 * default interface methods and static interface methods in the
 * {@link ConcurrentMap} interface. 
 */
public final class ConcurrentMaps {
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
     * <p>The remapping function itself should not modify the passed map during computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to performing the following
     * steps for the {@code map}:
     *
     * <pre> {@code
     * for (;;) {
     *   V oldValue = map.get(key);
     *   if (oldValue != null) {
     *     V newValue = remappingFunction.apply(oldValue, value);
     *     if (newValue != null) {
     *       if (map.replace(key, oldValue, newValue))
     *         return newValue;
     *     } else if (map.remove(key, oldValue)) {
     *       return null;
     *     }
     *   } else if (map.putIfAbsent(key, value) == null) {
     *     return value;
     *   }
     * }}</pre>
     * When multiple threads attempt updates, map operations and the
     * remapping function may be called multiple times.
     *
     * <p>This implementation assumes that the ConcurrentMap cannot contain null
     * values and {@code get()} returning null unambiguously means the key is
     * absent. Implementations which support null values <strong>must</strong>
     * override the default implementation.
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * remapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Concurrent implementations should override
     * this method and, on a best-effort basis, throw an {@code IllegalStateException}
     * if it is detected that the remapping function modifies the map during computation
     * and as a result computation would never complete.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code merge}
     * operation.
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value
     *        associated with the key or, if no existing value or a null value
     *        is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
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
    public static <K, V> V merge(ConcurrentMap<K, V> map, K key, V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        retry: for (;;) {
            V oldValue = map.get(key);
            // if putIfAbsent fails, opportunistically use its return value
            haveOldValue: for (;;) {
                if (oldValue != null) {
                    V newValue = remappingFunction.apply(oldValue, value);
                    if (newValue != null) {
                        if (map.replace(key, oldValue, newValue))
                            return newValue;
                    } else if (map.remove(key, oldValue)) {
                        return null;
                    }
                    continue retry;
                } else {
                    if ((oldValue = map.putIfAbsent(key, value)) == null)
                        return value;
                    continue haveOldValue;
                }
            }
        }
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
     * <p>The mapping function itself should not modify the passed map during computation.
     *
     * <p><b>Implementation Requirements:</b><br>
     * The default implementation is equivalent to the following steps for the
     * {@code map}:
     *
     * <pre> {@code
     * V oldValue, newValue;
     * return ((oldValue = map.get(key)) == null
     *         && (newValue = mappingFunction.apply(key)) != null
     *         && (oldValue = map.putIfAbsent(key, newValue)) == null)
     *   ? newValue
     *   : oldValue;}</pre>
     *
     * <p>This implementation assumes that the ConcurrentMap cannot contain null
     * values and {@code get()} returning null unambiguously means the key is
     * absent. Implementations which support null values <strong>must</strong>
     * override the default implementation.
     *
     * <p>The default implementation makes no guarantees about detecting if the
     * mapping function modifies the passed map during computation and, if
     * appropriate, reporting an error. Concurrent implementations should override
     * this method and, on a best-effort basis, throw an {@code IllegalStateException}
     * if it is detected that the mapping function modifies the map during computation
     * and as a result computation would never complete.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code computeIfAbsent}
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
    public static <K, V> V computeIfAbsent(ConcurrentMap<K, V> map, K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(mappingFunction);
        V oldValue, newValue;
        return ((oldValue = map.get(key)) == null
                && (newValue = mappingFunction.apply(key)) != null
                && (oldValue = map.putIfAbsent(key, newValue)) == null)
            ? newValue
            : oldValue;
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
     * steps for the {@code map}:
     *
     * <pre> {@code
     * for (V oldValue; (oldValue = map.get(key)) != null; ) {
     *   V newValue = remappingFunction.apply(key, oldValue);
     *   if ((newValue == null)
     *       ? map.remove(key, oldValue)
     *       : map.replace(key, oldValue, newValue))
     *     return newValue;
     * }
     * return null;}</pre>
     * When multiple threads attempt updates, map operations and the
     * remapping function may be called multiple times.
     *
     * <p>This implementation assumes that the ConcurrentMap cannot contain null
     * values and {@code get()} returning null unambiguously means the key is
     * absent. Implementations which support null values <strong>must</strong>
     * override this default implementation.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the
     * {@code computeIfPresent} operation.
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
    public static <K, V> V computeIfPresent(ConcurrentMap<K, V> map, K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(remappingFunction);
        for (V oldValue; (oldValue = map.get(key)) != null; ) {
            V newValue = remappingFunction.apply(key, oldValue);
            if ((newValue == null)
                ? map.remove(key, oldValue)
                : map.replace(key, oldValue, newValue))
                return newValue;
        }
        return null;
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
     * steps for the {@code map}:
     *
     * <pre> {@code
     * for (;;) {
     *   V oldValue = map.get(key);
     *   V newValue = remappingFunction.apply(key, oldValue);
     *   if (newValue != null) {
     *     if ((oldValue != null)
     *       ? map.replace(key, oldValue, newValue)
     *       : map.putIfAbsent(key, newValue) == null)
     *       return newValue;
     *   } else if (oldValue == null || map.remove(key, oldValue)) {
     *     return null;
     *   }
     * }}</pre>
     * When multiple threads attempt updates, map operations and the
     * remapping function may be called multiple times.
     *
     * <p>This implementation assumes that the ConcurrentMap cannot contain null
     * values and {@code get()} returning null unambiguously means the key is
     * absent. Implementations which support null values <strong>must</strong>
     * override this default implementation.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code compute}
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
    public static <K, V> V compute(ConcurrentMap<K, V> map, K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(map);
        retry: for (;;) {
            V oldValue = map.get(key);
            // if putIfAbsent fails, opportunistically use its return value
            haveOldValue: for (;;) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    if (oldValue != null) {
                        if (map.replace(key, oldValue, newValue))
                            return newValue;
                    }
                    else if ((oldValue = map.putIfAbsent(key, newValue)) == null)
                        return newValue;
                    else continue haveOldValue;
                } else if (oldValue == null || map.remove(key, oldValue)) {
                    return null;
                }
                continue retry;
            }
        }
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
     * for ((Map.Entry<K, V> entry : map.entrySet())
     *     do {
     *        K k = entry.getKey();
     *        V v = entry.getValue();
     *     } while(!replace(k, v, function.apply(k, v)));
     * }</pre>
     *
     * The default implementation may retry these steps when multiple
     * threads attempt updates including potentially calling the function
     * repeatedly for a given key.
     *
     * <p>This implementation assumes that the ConcurrentMap cannot contain null
     * values and {@code get()} returning null unambiguously means the key is
     * absent. Implementations which support null values <strong>must</strong>
     * override the default implementation.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code replaceAll}
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
    public static <K, V> void replaceAll(ConcurrentMap<K, V> map, BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(function);
        forEach(map, (k,v) -> {
            while (!map.replace(k, v, function.apply(k, v))) {
                // v changed or k is gone
                if ( (v = map.get(k)) == null) {
                    // k is no longer in the map.
                    break;
                }
            }
        });
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if the map contains no mapping for the key.
     *
     * <p><b>Implementation Note:</b><br> This implementation assumes that the
     * ConcurrentMap cannot contain null values and {@code get()} returning
     * null unambiguously means the key is absent. Implementations which
     * support null values <strong>must</strong> override this default implementation.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code getOrDefault}
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
    public static <K, V> V getOrDefault(ConcurrentMap<K, V> map, Object key, V defaultValue) {
        Objects.requireNonNull(map);
        V v;
        return ((v = map.get(key)) != null) ? v : defaultValue;
    }

    /**
     * Performs the given action for each entry in the map until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation
     * is equivalent to, for the
     * {@code map}:
     * <pre> {@code
     * for ((Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     *
     * <p><b>Implementation Note:</b><br> The default implementation assumes that
     * {@code IllegalStateException} thrown by {@code getKey()} or
     * {@code getValue()} indicates that the entry has been removed and cannot
     * be processed. Operation continues for subsequent entries.
     *
     * @param <K> the type of keys maintained by the passed map
     * @param <V> the type of mapped values in the passed map
     * @param map the {@code ConcurrentMap} on which to execute the {@code forEach}
     * operation.
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified map or action is null
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 1.8
     */
    public static <K, V> void forEach(ConcurrentMap<K, V> map, BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                continue;
            }
            action.accept(k, v);
        }
    }

    private ConcurrentMaps() {
    }
}
