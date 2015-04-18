/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util.stream;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * Replacement for Android's API 21 j.u.c.ConcurrentHashMap.
 * Equivalent to OpenJDK's 6u45 version of j.u.c.CHM.
 * 
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
final class CHM<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V> {

    /*
     * The basic strategy is to subdivide the table among Segments,
     * each of which itself is a concurrently readable hash table.
     */

    /* ---------------- Constants -------------- */

    /**
     * The default initial capacity for this table,
     * used when not otherwise specified in a constructor.
     */
    private static final int DFLT_INITIAL_CAP = 16;

    /**
     * The default load factor for this table, used when not
     * otherwise specified in a constructor.
     */
    private static final float DFLT_LOAD_FACTOR = 0.75f;

    /**
     * The default concurrency level for this table, used when not
     * otherwise specified in a constructor.
     */
    private static final int DFLT_CONC_LVL = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly
     * specified by either of the constructors with arguments.  MUST
     * be a power of two <= 1<<30 to ensure that entries are indexable
     * using ints.
     */
    private static final int MAX_CAP = 1 << 30;

    /**
     * The maximum number of segments to allow; used to bound
     * constructor arguments.
     */
    private static final int MAX_SEGS = 1 << 16; // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue
     * methods before resorting to locking. This is used to avoid
     * unbounded retries if tables undergo continuous modification
     * which would make it impossible to obtain an accurate result.
     */
    private static final int RETRIES_BEFORE_LOCK = 2;

    /* ---------------- Fields -------------- */

    /**
     * Mask value for indexing into segments. The upper bits of a
     * key's hash code are used to choose the segment.
     */
    private final int segMask;

    /**
     * Shift value for indexing within segments.
     */
    private final int segShift;

    /**
     * The segments, each of which is a specialized hash table
     */
    private final Seg<K, V>[] segs;

    private Set<K> keySet;
    private Set<Map.Entry<K, V>> entrySet;
    private Collection<V> values;

    /* ---------------- Small Utilities -------------- */

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because ConcurrentHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not
     * differ in lower or upper bits.
     */
    private static int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
     * Returns the segment that should be used for key with given hash
     * @param hash the hash code for the key
     * @return the segment
     */
    private Seg<K, V> segFor(int hash) {
        return segs[(hash >>> segShift) & segMask];
    }

    /* ---------------- Inner Classes -------------- */

    /**
     * ConcurrentHashMap list entry. Note that this is never exported
     * out as a user-visible Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt
     * the Java Memory Model for an unsynchronized reader to see null
     * instead of initial value when read via a data race.  Although a
     * reordering leading to this is not likely to ever actually
     * occur, the Segment.readValueUnderLock method is used as a
     * backup in case a null (pre-initialized) value is ever seen in
     * an unsynchronized access method.
     */
    static final class HE<K, V> { // HashEntry
        final K k; // key
        final int hash; // hash
        volatile V v; // value
        final HE<K, V> next; // next

        HE(K key, int hash, HE<K, V> next, V value) {
            this.k = key;
            this.hash = hash;
            this.next = next;
            this.v = value;
        }

        @SuppressWarnings("unchecked")
        static final <K, V> HE<K, V>[] newArray(int i) {
            return new HE[i];
        }
    }

    /**
     * Segments are specialized versions of hash tables.  This
     * subclasses from ReentrantLock opportunistically, just to
     * simplify some locking and avoid separate construction.
     */
    @SuppressWarnings("serial")
    static final class Seg<K, V> extends ReentrantLock {
        /**
         * The number of elements in this segment's region.
         */
        volatile int cnt;

        /**
         * Number of updates that alter the size of the table. This is
         * used during bulk-read methods to make sure they see a
         * consistent snapshot: If modCounts change during a traversal
         * of segments computing size or checking containsValue, then
         * we might have an inconsistent view of state so (usually)
         * must retry.
         */
        int modCnt;

        /**
         * The table is rehashed when its size exceeds this threshold.
         * (The value of this field is always <tt>(int)(capacity *
         * loadFactor)</tt>.)
         */
        int thresh;

        /**
         * The per-segment table.
         */
        volatile HE<K, V>[] table;

        /**
         * The load factor for the hash table.  Even though this value
         * is same for all segments, it is replicated to avoid needing
         * links to outer object.
         */
        final float loadFactor;

        Seg(int initialCapacity, float lf) {
            loadFactor = lf;
            setTable(HE.<K, V>newArray(initialCapacity));
        }

        @SuppressWarnings("unchecked")
        static final <K, V> Seg<K, V>[] newArray(int i) {
            return new Seg[i];
        }

        /**
         * Sets table to new HashEntry array.
         * Call only while holding lock or in constructor.
         */
        void setTable(HE<K, V>[] newTable) {
            thresh = (int) (newTable.length * loadFactor);
            table = newTable;
        }

        /**
         * Returns properly casted first entry of bin for given hash.
         */
        HE<K, V> getFirst(int hash) {
            HE<K, V>[] tab = table;
            return tab[hash & (tab.length - 1)];
        }

        /**
         * Reads value field of an entry under lock. Called if value
         * field ever appears to be null. This is possible only if a
         * compiler happens to reorder a HashEntry initialization with
         * its table assignment, which is legal under memory model
         * but is not known to ever occur.
         */
        V readValUnderLock(HE<K, V> e) {
            lock();
            try {
                return e.v;
            } finally {
                unlock();
            }
        }

        /* Specialized implementations of map methods */

        V get(Object key, int hash) {
            if (cnt != 0) { // read-volatile
                HE<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key.equals(e.k)) {
                        V v = e.v;
                        if (v != null)
                            return v;
                        return readValUnderLock(e); // recheck
                    }
                    e = e.next;
                }
            }
            return null;
        }

        boolean containsKey(Object key, int hash) {
            if (cnt != 0) { // read-volatile
                HE<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key.equals(e.k))
                        return true;
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue(Object value) {
            if (cnt != 0) { // read-volatile
                HE<K, V>[] tab = table;
                int len = tab.length;
                for (int i = 0 ; i < len; i++) {
                    for (HE<K, V> e = tab[i]; e != null; e = e.next) {
                        V v = e.v;
                        if (v == null) // recheck
                            v = readValUnderLock(e);
                        if (value.equals(v))
                            return true;
                    }
                }
            }
            return false;
        }

        boolean replace(K key, int hash, V oldValue, V newValue) {
            lock();
            try {
                HE<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || !key.equals(e.k)))
                    e = e.next;

                boolean replaced = false;
                if (e != null && oldValue.equals(e.v)) {
                    replaced = true;
                    e.v = newValue;
                }
                return replaced;
            } finally {
                unlock();
            }
        }

        V replace(K key, int hash, V newValue) {
            lock();
            try {
                HE<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || !key.equals(e.k)))
                    e = e.next;

                V oldValue = null;
                if (e != null) {
                    oldValue = e.v;
                    e.v = newValue;
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        V put(K key, int hash, V value, boolean onlyIfAbsent) {
            lock();
            try {
                int c = cnt;
                if (c++ > thresh) // ensure capacity
                    rehash();
                HE<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                HE<K, V> first = tab[index];
                HE<K, V> e = first;
                while (e != null && (e.hash != hash || !key.equals(e.k)))
                    e = e.next;

                V oldValue;
                if (e != null) {
                    oldValue = e.v;
                    if (!onlyIfAbsent)
                        e.v = value;
                } else {
                    oldValue = null;
                    ++modCnt;
                    tab[index] = new HE<K, V>(key, hash, first, value);
                    cnt = c; // write-volatile
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void rehash() {
            HE<K, V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAX_CAP)
                return;

            HE<K, V>[] newTable = HE.newArray(oldCapacity << 1);
            thresh = (int) (newTable.length * loadFactor);
            int sizeMask = newTable.length - 1;
            for (int i = 0; i < oldCapacity ; i++) {
                // We need to guarantee that any existing reads of old Map can
                //  proceed. So we cannot yet null out each bin.
                HE<K, V> e = oldTable[i];

                if (e != null) {
                    HE<K, V> next = e.next;
                    int idx = e.hash & sizeMask;

                    //  Single node on list
                    if (next == null) {
                        newTable[idx] = e;
                    } else {
                        // Reuse trailing consecutive sequence at same slot
                        HE<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HE<K, V> last = next;
                             last != null;
                             last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;

                        // Clone all remaining nodes
                        for (HE<K, V> p = e; p != lastRun; p = p.next) {
                            int k = p.hash & sizeMask;
                            HE<K, V> n = newTable[k];
                            newTable[k] = new HE<K, V>(p.k, p.hash,
                                                              n, p.v);
                        }
                    }
                }
            }
            table = newTable;
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        V remove(Object key, int hash, Object value) {
            lock();
            try {
                int c = cnt - 1;
                HE<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                HE<K, V> first = tab[index];
                HE<K, V> e = first;
                while (e != null && (e.hash != hash || !key.equals(e.k)))
                    e = e.next;

                V oldValue = null;
                if (e != null) {
                    V v = e.v;
                    if (value == null || value.equals(v)) {
                        oldValue = v;
                        // All entries following removed node can stay
                        // in list, but all preceding ones need to be
                        // cloned.
                        ++modCnt;
                        HE<K, V> newFirst = e.next;
                        for (HE<K, V> p = first; p != e; p = p.next)
                            newFirst = new HE<K, V>(p.k, p.hash,
                                                           newFirst, p.v);
                        tab[index] = newFirst;
                        cnt = c; // write-volatile
                    }
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void clear() {
            if (cnt != 0) {
                lock();
                try {
                    HE<K, V>[] tab = table;
                    for (int i = 0; i < tab.length ; i++)
                        tab[i] = null;
                    ++modCnt;
                    cnt = 0; // write-volatile
                } finally {
                    unlock();
                }
            }
        }
    }



    /* ---------------- Public operations -------------- */

    /**
     * Creates a new, empty map with the specified initial
     * capacity, load factor and concurrency level.
     *
     * @param initialCapacity the initial capacity. The implementation
     * performs internal sizing to accommodate this many elements.
     * @param loadFactor  the load factor threshold, used to control resizing.
     * Resizing may be performed when the average number of elements per
     * bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently
     * updating threads. The implementation performs internal sizing
     * to try to accommodate this many threads.
     * @throws IllegalArgumentException if the initial capacity is
     * negative or the load factor or concurrencyLevel are
     * nonpositive.
     */
    public CHM(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();

        if (concurrencyLevel > MAX_SEGS)
            concurrencyLevel = MAX_SEGS;

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segShift = 32 - sshift;
        segMask = ssize - 1;
        segs = Seg.newArray(ssize);

        if (initialCapacity > MAX_CAP)
            initialCapacity = MAX_CAP;
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity)
            ++c;
        int cap = 1;
        while (cap < c)
            cap <<= 1;

        for (int i = 0; i < segs.length; ++i)
            segs[i] = new Seg<K, V>(cap, loadFactor);
    }

    /**
     * Creates a new, empty map with the specified initial capacity
     * and load factor and with the default concurrencyLevel (16).
     *
     * @param initialCapacity The implementation performs internal
     * sizing to accommodate this many elements.
     * @param loadFactor  the load factor threshold, used to control resizing.
     * Resizing may be performed when the average number of elements per
     * bin exceeds this threshold.
     * @throws IllegalArgumentException if the initial capacity of
     * elements is negative or the load factor is nonpositive
     *
     * @since 1.6
     */
    public CHM(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DFLT_CONC_LVL);
    }

    /**
     * Creates a new, empty map with the specified initial capacity,
     * and with default load factor (0.75) and concurrencyLevel (16).
     *
     * @param initialCapacity the initial capacity. The implementation
     * performs internal sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of
     * elements is negative.
     */
    public CHM(int initialCapacity) {
        this(initialCapacity, DFLT_LOAD_FACTOR, DFLT_CONC_LVL);
    }

    /**
     * Creates a new, empty map with a default initial capacity (16),
     * load factor (0.75) and concurrencyLevel (16).
     */
    public CHM() {
        this(DFLT_INITIAL_CAP, DFLT_LOAD_FACTOR, DFLT_CONC_LVL);
    }

    /**
     * Creates a new map with the same mappings as the given map.
     * The map is created with a capacity of 1.5 times the number
     * of mappings in the given map or 16 (whichever is greater),
     * and a default load factor (0.75) and concurrencyLevel (16).
     *
     * @param m the map
     */
    public CHM(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DFLT_LOAD_FACTOR) + 1,
                      DFLT_INITIAL_CAP),
             DFLT_LOAD_FACTOR, DFLT_CONC_LVL);
        putAll(m);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        Seg<K, V>[] segs = this.segs;
        /*
         * We keep track of per-segment modCounts to avoid ABA
         * problems in which an element in one segment was added and
         * in another removed during traversal, in which case the
         * table was never actually empty at any point. Note the
         * similar use of modCounts in the size() and containsValue()
         * methods, which are the only other methods also susceptible
         * to ABA problems.
         */
        int[] mc = new int[segs.length];
        int mcsum = 0;
        for (int i = 0; i < segs.length; ++i) {
            if (segs[i].cnt != 0)
                return false;
            else
                mcsum += mc[i] = segs[i].modCnt;
        }
        // If mcsum happens to be zero, then we know we got a snapshot
        // before any modifications at all were made.  This is
        // probably common enough to bother tracking.
        if (mcsum != 0) {
            for (int i = 0; i < segs.length; ++i) {
                if (segs[i].cnt != 0 ||
                    mc[i] != segs[i].modCnt)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        Seg<K, V>[] segs = this.segs;
        long sum = 0;
        long check = 0;
        int[] mc = new int[segs.length];
        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segs.length; ++i) {
                sum += segs[i].cnt;
                mcsum += mc[i] = segs[i].modCnt;
            }
            if (mcsum != 0) {
                for (int i = 0; i < segs.length; ++i) {
                    check += segs[i].cnt;
                    if (mc[i] != segs[i].modCnt) {
                        check = -1; // force retry
                        break;
                    }
                }
            }
            if (check == sum)
                break;
        }
        if (check != sum) { // Resort to locking all segments
            sum = 0;
            for (int i = 0; i < segs.length; ++i)
                segs[i].lock();
            for (int i = 0; i < segs.length; ++i)
                sum += segs[i].cnt;
            for (int i = 0; i < segs.length; ++i)
                segs[i].unlock();
        }
        if (sum > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        else
            return (int) sum;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key.equals(k)},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @throws NullPointerException if the specified key is null
     */
    public V get(Object key) {
        int h = hash(key.hashCode());
        return segFor(h).get(key, h);
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param  key   possible key
     * @return <tt>true</tt> if and only if the specified object
     *         is a key in this table, as determined by the
     *         <tt>equals</tt> method; <tt>false</tt> otherwise.
     * @throws NullPointerException if the specified key is null
     */
    public boolean containsKey(Object key) {
        int h = hash(key.hashCode());
        return segFor(h).containsKey(key, h);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. Note: This method requires a full internal
     * traversal of the hash table, and so is much slower than
     * method <tt>containsKey</tt>.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     * @throws NullPointerException if the specified value is null
     */
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();

        // See explanation of modCount use above

        Seg<K, V>[] segs = this.segs;
        int[] mc = new int[segs.length];

        // Try a few times without locking
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            int mcsum = 0;
            for (int i = 0; i < segs.length; ++i) {
                mcsum += mc[i] = segs[i].modCnt;
                if (segs[i].containsValue(value)) {
                    return true;
                }
            }
            boolean cleanSweep = true;
            if (mcsum != 0) {
                for (int i = 0; i < segs.length; ++i) {
                    if (mc[i] != segs[i].modCnt) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep)
                return false;
        }
        // Resort to locking all segments
        for (int i = 0; i < segs.length; ++i) {
            segs[i].lock();
        }
        boolean found = false;
        try {
            for (int i = 0; i < segs.length; ++i) {
                if (segs[i].containsValue(value)) {
                    found = true;
                    break;
                }
            }
        } finally {
            for (int i = 0; i < segs.length; ++i) {
                segs[i].unlock();
            }
        }
        return found;
    }

    /**
     * Legacy method testing if some key maps into the specified value
     * in this table.  This method is identical in functionality to
     * {@link #containsValue}, and exists solely to ensure
     * full compatibility with class {@link java.util.Hashtable},
     * which supported this method prior to introduction of the
     * Java Collections framework.

     * @param  value a value to search for
     * @return <tt>true</tt> if and only if some key maps to the
     *         <tt>value</tt> argument in this table as
     *         determined by the <tt>equals</tt> method;
     *         <tt>false</tt> otherwise
     * @throws NullPointerException if the specified value is null
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * Maps the specified key to the specified value in this table.
     * Neither the key nor the value can be null.
     *
     * <p> The value can be retrieved by calling the <tt>get</tt> method
     * with a key that is equal to the original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException if the specified key or value is null
     */
    public V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int h = hash(key.hashCode());
        return segFor(h).put(key, h, value, false);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key,
     *         or <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException if the specified key or value is null
     */
    public V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int h = hash(key.hashCode());
        return segFor(h).put(key, h, value, true);
    }

    /**
     * Copies all of the mappings from the specified map to this one.
     * These mappings replace any mappings that this map had for any of the
     * keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * Removes the key (and its corresponding value) from this map.
     * This method does nothing if the key is not in the map.
     *
     * @param  key the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException if the specified key is null
     */
    public V remove(Object key) {
        int h = hash(key.hashCode());
        return segFor(h).remove(key, h, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if the specified key is null
     */
    public boolean remove(Object key, Object value) {
        int h = hash(key.hashCode());
        if (value == null)
            return false;
        return segFor(h).remove(key, h, value) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if any of the arguments are null
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        int h = hash(key.hashCode());
        return segFor(h).replace(key, h, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key,
     *         or <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException if the specified key or value is null
     */
    public V replace(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int h = hash(key.hashCode());
        return segFor(h).replace(key, h, value);
    }

    /**
     * Removes all of the mappings from this map.
     */
    public void clear() {
        for (int i = 0; i < segs.length; ++i)
            segs[i].clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from this map,
     * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KSet());
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  The collection
     * supports element removal, which removes the corresponding
     * mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Vs());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from the map,
     * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>The view's <tt>iterator</tt> is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new ESet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<K> keys() {
        return new KIt();
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements() {
        return new VsIt();
    }

    /* ---------------- Iterator Support -------------- */

    // HashIterator
    abstract class HIt {
        int nextSegIdx;
        int nextTabIdx;
        HE<K, V>[] currTab;
        HE<K, V> nextEntry;
        HE<K, V> lastReturned;

        HIt() {
            nextSegIdx = segs.length - 1;
            nextTabIdx = -1;
            advance();
        }

        public final boolean hasMoreElements() { return hasNext(); }

        final void advance() {
            if (nextEntry != null && (nextEntry = nextEntry.next) != null)
                return;

            while (nextTabIdx >= 0) {
                if ( (nextEntry = currTab[nextTabIdx--]) != null)
                    return;
            }

            while (nextSegIdx >= 0) {
                Seg<K, V> seg = segs[nextSegIdx--];
                if (seg.cnt != 0) {
                    currTab = seg.table;
                    for (int j = currTab.length - 1; j >= 0; --j) {
                        if ( (nextEntry = currTab[j]) != null) {
                            nextTabIdx = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        public final boolean hasNext() { return nextEntry != null; }

        final HE<K, V> nextEntry() {
            if (nextEntry == null)
                throw new NoSuchElementException();
            lastReturned = nextEntry;
            advance();
            return lastReturned;
        }

        public final void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            CHM.this.remove(lastReturned.k);
            lastReturned = null;
        }
    }

    // KeyIterator
    final class KIt extends HIt implements Iterator<K>, Enumeration<K> {
        public K next()        { return super.nextEntry().k; }
        public K nextElement() { return super.nextEntry().k; }
    }

    // ValueIterator
    final class VsIt extends HIt implements Iterator<V>, Enumeration<V> {
        public V next()        { return super.nextEntry().v; }
        public V nextElement() { return super.nextEntry().v; }
    }

    /**
     * Custom Entry class used by EntryIterator.next(), that relays
     * setValue changes to the underlying map.  "WT" is short for
     * "WriteThrough".
     */
    @SuppressWarnings("serial")
    final class WTE extends AbstractMap.SimpleEntry<K, V> { // WriteThroughEntry
        WTE(K k, V v) {
            super(k, v);
        }

        /**
         * Set our entry's value and write through to the map. The
         * value to return is somewhat arbitrary here. Since a
         * WriteThroughEntry does not necessarily track asynchronous
         * changes, the most recent "previous" value could be
         * different from what we return (or could even have been
         * removed in which case the put will re-establish). We do not
         * and cannot guarantee more.
         */
        public V setValue(V value) {
            if (value == null) throw new NullPointerException();
            V v = super.setValue(value);
            CHM.this.put(getKey(), value);
            return v;
        }
    }

    // EntryIterator
    final class EIt extends HIt implements Iterator<Entry<K, V>> {
        public Map.Entry<K, V> next() {
            HE<K, V> e = super.nextEntry();
            return new WTE(e.k, e.v);
        }
    }

    // KeySet
    final class KSet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KIt();
        }
        public int size() {
            return CHM.this.size();
        }
        public boolean contains(Object o) {
            return CHM.this.containsKey(o);
        }
        public boolean remove(Object o) {
            return CHM.this.remove(o) != null;
        }
        public void clear() {
            CHM.this.clear();
        }
    }

    // Values
    final class Vs extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new VsIt();
        }
        public int size() {
            return CHM.this.size();
        }
        public boolean contains(Object o) {
            return CHM.this.containsValue(o);
        }
        public void clear() {
            CHM.this.clear();
        }
    }

    // EntrySet
    final class ESet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EIt();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = CHM.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return CHM.this.remove(e.getKey(), e.getValue());
        }
        public int size() {
            return CHM.this.size();
        }
        public void clear() {
            CHM.this.clear();
        }
    }

    static {
        // Reduce the risk of rare disastrous classloading in first call to
        // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
        @SuppressWarnings("unused")
        Class<?> ensureLoaded = LockSupport.class;    	
    }
}
