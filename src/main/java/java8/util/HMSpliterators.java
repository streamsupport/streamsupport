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

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import java8.util.function.Consumer;

final class HMSpliterators {

    private HMSpliterators() {
        throw new AssertionError();
    }

    static <K> Spliterator<K> getKeySetSpliterator(Set<K> keySet) {
        return new KeySpliterator<K, Object>(getHashMapFromKeySet(keySet), 0,
                -1, 0, 0);
    }

    static <K, V> Spliterator<Map.Entry<K, V>> getEntrySetSpliterator(
            Set<Map.Entry<K, V>> entrySet) {
        return new EntrySpliterator<K, V>(getHashMapFromEntrySet(entrySet), 0,
                -1, 0, 0);
    }

    static <V> Spliterator<V> getValuesSpliterator(Collection<V> values) {
        return new ValueSpliterator<Object, V>(getHashMapFromValues(values), 0,
                -1, 0, 0);
    }

    static <E> Spliterator<E> getHashSetSpliterator(HashSet<E> hashSet) {
        return new KeySpliterator<E, Object>(getHashMapFromHashSet(hashSet), 0,
                -1, 0, 0);
    }

    private static final class KeySpliterator<K, V> extends
            HashMapSpliterator<K, V> implements Spliterator<K> {

        private KeySpliterator(HashMap<K, V> m, int origin, int fence, int est,
                int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        @Override
        public KeySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null
                    : new KeySpliterator<K, V>(map, lo, index = mid,
                            est >>>= 1, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Objects.requireNonNull(action);
            int i, hi, mc;
            HashMap<K, V> m = map;
            Object[] tab = getTable(m);
            if ((hi = fence) < 0) {
                mc = expectedModCount = getModCount(m);
                hi = fence = (tab == null) ? 0 : tab.length;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                Object p = current;
                current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                    } else {
                        action.accept(HashMapSpliterator.<K> getNodeKey(p));
                        p = getNextNode(p);
                    }
                } while (p != null || i < hi);
                if (mc != getModCount(m)) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            Objects.requireNonNull(action);
            int hi;
            Object[] tab = getTable(map);
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        K k = getNodeKey(current);
                        current = getNextNode(current);
                        action.accept(k);
                        if (expectedModCount != getModCount(map)) {
                            throw new ConcurrentModificationException();
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size() ? Spliterator.SIZED : 0)
                    | Spliterator.DISTINCT;
        }

        @Override
        public Comparator<? super K> getComparator() {
            return Spliterators.getComparator(this);
        }
    }

    private static final class ValueSpliterator<K, V> extends
            HashMapSpliterator<K, V> implements Spliterator<V> {

        private ValueSpliterator(HashMap<K, V> m, int origin, int fence,
                int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        @Override
        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null
                    : new ValueSpliterator<K, V>(map, lo, index = mid,
                            est >>>= 1, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            int i, hi, mc;
            HashMap<K, V> m = map;
            Object[] tab = getTable(m);
            if ((hi = fence) < 0) {
                mc = expectedModCount = getModCount(m);
                hi = fence = (tab == null) ? 0 : tab.length;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                Object p = current;
                current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                    } else {
                        action.accept(HashMapSpliterator.<V> getNodeValue(p));
                        p = getNextNode(p);
                    }
                } while (p != null || i < hi);
                if (mc != getModCount(m)) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            int hi;
            Object[] tab = getTable(map);
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        V v = getNodeValue(current);
                        current = getNextNode(current);
                        action.accept(v);
                        if (expectedModCount != getModCount(map)) {
                            throw new ConcurrentModificationException();
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size() ? Spliterator.SIZED : 0);
        }

        @Override
        public Comparator<? super V> getComparator() {
            return Spliterators.getComparator(this);
        }
    }

    private static final class EntrySpliterator<K, V> extends
            HashMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {

        private EntrySpliterator(HashMap<K, V> m, int origin, int fence,
                int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        @Override
        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null
                    : new EntrySpliterator<K, V>(map, lo, index = mid,
                            est >>>= 1, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            int i, hi, mc;
            HashMap<K, V> m = map;
            Object[] tab = getTable(m);
            if ((hi = fence) < 0) {
                mc = expectedModCount = getModCount(m);
                hi = fence = (tab == null) ? 0 : tab.length;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                Object p = current;
                current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                    } else {
                        action.accept((Map.Entry<K, V>) p);
                        p = getNextNode(p);
                    }
                } while (p != null || i < hi);
                if (mc != getModCount(m)) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            int hi;
            Object[] tab = getTable(map);
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        Map.Entry<K, V> e = (Map.Entry<K, V>) current;
                        current = getNextNode(current);
                        action.accept(e);
                        if (expectedModCount != getModCount(map)) {
                            throw new ConcurrentModificationException();
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size() ? Spliterator.SIZED : 0)
                    | Spliterator.DISTINCT;
        }

        @Override
        public Comparator<? super Entry<K, V>> getComparator() {
            return Spliterators.getComparator(this);
        }
    }

    private static abstract class HashMapSpliterator<K, V> {
        final HashMap<K, V> map;
        Object current; // current node
        int index; // current index, modified on advance/split
        int fence; // one past last index
        int est; // size estimate
        int expectedModCount; // for co-modification checks

        HashMapSpliterator(HashMap<K, V> m, int origin, int fence, int est,
                int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K, V> m = map;
                est = m.size();
                expectedModCount = getModCount(m);
                Object[] tab = getTable(m);
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public abstract int characteristics();

        public final long getExactSizeIfKnown() {
            return Spliterators.getExactSizeIfKnown((Spliterator<?>) this);
        }

        public final boolean hasCharacteristics(int characteristics) {
            return Spliterators.hasCharacteristics((Spliterator<?>) this,
                    characteristics);
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }

        static int getModCount(HashMap<?, ?> map) {
            return UNSAFE.getInt(map, MODCOUNT_OFF);
        }

        static Object[] getTable(HashMap<?, ?> map) {
            return (Object[]) UNSAFE.getObject(map, TABLE_OFF);
        }

        static <K> K getNodeKey(Object node) {
            return (K) UNSAFE.getObject(node, NODE_KEY_OFF);
        }

        static <T> T getNodeValue(Object node) {
            return (T) UNSAFE.getObject(node, NODE_VAL_OFF);
        }

        static Object getNextNode(Object node) {
            return UNSAFE.getObject(node, NODE_NXT_OFF);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long TABLE_OFF;
        private static final long MODCOUNT_OFF;
        private static final long NODE_KEY_OFF;
        private static final long NODE_VAL_OFF;
        private static final long NODE_NXT_OFF;
        static {
            try {
                UNSAFE = UnsafeAccess.unsafe;
                Class<?> hmc = HashMap.class;
                TABLE_OFF = UNSAFE.objectFieldOffset(hmc
                        .getDeclaredField("table"));
                MODCOUNT_OFF = UNSAFE.objectFieldOffset(hmc
                        .getDeclaredField("modCount"));
//				String ncName = Spliterators.IS_ANDROID ? "HashMapEntry"
//						: Spliterators.JRE_HAS_STREAMS ? "Node" : "Entry";
                String ncName = Spliterators.JRE_HAS_STREAMS ? "Node" : "Entry";
                ncName = "java.util.HashMap$" + ncName;
                Class<?> nc = Class.forName(ncName);
                NODE_KEY_OFF = UNSAFE.objectFieldOffset(nc
                        .getDeclaredField("key"));
                NODE_VAL_OFF = UNSAFE.objectFieldOffset(nc
                        .getDeclaredField("value"));
                NODE_NXT_OFF = UNSAFE.objectFieldOffset(nc
                        .getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    private static <K, V> HashMap<K, V> getHashMapFromKeySet(Set<K> keySet) {
        return (HashMap<K, V>) UNSAFE.getObject(keySet, KEYSET_$0_OFF);
    }

    private static <K, V> HashMap<K, V> getHashMapFromEntrySet(
            Set<Map.Entry<K, V>> entrySet) {
        return (HashMap<K, V>) UNSAFE.getObject(entrySet, ENTRYSET_$0_OFF);
    }

    private static <K, V> HashMap<K, V> getHashMapFromValues(
            Collection<V> values) {
        return (HashMap<K, V>) UNSAFE.getObject(values, VALUES_$0_OFF);
    }

    private static <K, V> HashMap<K, V> getHashMapFromHashSet(HashSet<K> hashSet) {
        return (HashMap<K, V>) UNSAFE.getObject(hashSet, HASHSET_MAP_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long VALUES_$0_OFF;
    private static final long KEYSET_$0_OFF;
    private static final long ENTRYSET_$0_OFF;
    private static final long HASHSET_MAP_OFF;
    static {
        try {
            UNSAFE = UnsafeAccess.unsafe;
            Class<?> hsc = HashSet.class;
            Class<?> vc = Class.forName("java.util.HashMap$Values");
            Class<?> ksc = Class.forName("java.util.HashMap$KeySet");
            Class<?> esc = Class.forName("java.util.HashMap$EntrySet");
//			String hsMapFieldName = Spliterators.IS_ANDROID ? "backingMap"
//					: "map";
            VALUES_$0_OFF = UNSAFE.objectFieldOffset(vc
                    .getDeclaredField("this$0"));
            KEYSET_$0_OFF = UNSAFE.objectFieldOffset(ksc
                    .getDeclaredField("this$0"));
            ENTRYSET_$0_OFF = UNSAFE.objectFieldOffset(esc
                    .getDeclaredField("this$0"));
            HASHSET_MAP_OFF = UNSAFE.objectFieldOffset(hsc
                    .getDeclaredField("map")); // hsMapFieldName
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
