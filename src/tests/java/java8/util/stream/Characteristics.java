package java8.util.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import java8.util.Spliterator;
import java8.util.Spliterators;

public final class Characteristics {

    // returns the characteristics of the associated Java 8 Spliterator
    public static <T> int get(Collection<T> c) {
        String cn = c.getClass().getName();
        if (c instanceof ArrayList<?>) {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if ("java.util.Arrays$ArrayList".equals(cn)) {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if (c instanceof LinkedList<?>) {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if (c instanceof LinkedHashSet<?>) {
            return Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if (c instanceof TreeSet<?>) {
            return Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        }
        if (c instanceof HashSet<?>) {
            return Spliterator.DISTINCT;
        }
        if ("java.util.ArrayList$SubList".equals(cn)) { // JRE7 and above (ArrayList.subList())
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if ("java.util.RandomAccessSubList".equals(cn)) { // JRE6 (ArrayList.subList())
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        if ("java.util.Collections$EmptyList".equals(cn)) {
            return Spliterators.emptySpliterator().characteristics();
        }
        // Android
        if ("java.util.AbstractList$SubAbstractListRandomAccess".equals(cn)) {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        System.err.println(" ==>  " + cn);
        throw new AssertionError(" ==>  " + cn);
    }

    private Characteristics() {
    }
}
