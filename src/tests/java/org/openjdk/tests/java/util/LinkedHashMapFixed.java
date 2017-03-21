package org.openjdk.tests.java.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import build.IgnoreJava8API;

/**
 * Workaround for Android 7.0 / 7.1 Spliterators returned from LinkedHashMap's
 * collection views (entrySet(), keySet() and values()). They report
 * Spliterator.ORDERED (as they should) but actually they are not ORDERED. See
 * https://sourceforge.net/p/streamsupport/tickets/240/
 * 
 * This is only used in SpliteratorTraversingAndSplittingTest.
 */
@SuppressWarnings("serial")
final class LinkedHashMapFixed<K, V> extends LinkedHashMap<K, V> {

    private Set<K> keySet;
    private Collection<V> values;
    private Set<Map.Entry<K, V>> entrySet;

    public LinkedHashMapFixed() {
    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KS<K>(super.keySet());
            keySet = ks;
        }
        return ks;
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Vals<V>(super.values());
            values = vs;
        }
        return vs;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        if (es == null) {
            es = new ES<K, V>(super.entrySet());
            entrySet = es;
        }
        return es;
    }

    private static final class KS<K> extends AbstractSet<K> {
        final Set<K> keySet;
        KS(Set<K> keySet) {
            this.keySet = keySet;
        }
        // will only get invoked when Spliterator delegation is enabled (i.e., on Java 8/9 and Android 7.x)
        @IgnoreJava8API
        @Override
        public Spliterator<K> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
        }
        @Override
        public Iterator<K> iterator() {
            return keySet.iterator();
        }
        @Override
        public int size() {
            return keySet.size();
        }
    }

    private static final class ES<K, V> extends AbstractSet<Map.Entry<K, V>> {
        final Set<Map.Entry<K, V>> entrySet;
        ES(Set<Map.Entry<K, V>> entrySet) {
            this.entrySet = entrySet;
        }
        // will only get invoked when Spliterator delegation is enabled (i.e., on Java 8/9 and Android 7.x)
        @IgnoreJava8API
        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
        }
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return entrySet.iterator();
        }
        @Override
        public int size() {
            return entrySet.size();
        }
    }

    private static final class Vals<V> extends AbstractCollection<V> {
        final Collection<V> values;
        Vals(Collection<V> values) {
            this.values = values;
        }
        // will only get invoked when Spliterator delegation is enabled (i.e., on Java 8/9 and Android 7.x)
        @IgnoreJava8API
        @Override
        public Spliterator<V> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED);
        }
        @Override
        public Iterator<V> iterator() {
            return values.iterator();
        }
        @Override
        public int size() {
            return values.size();
        }
    }
}
