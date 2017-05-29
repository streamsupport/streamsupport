/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java.util.ArrayDeque;
import java.util.Collections;

import java8.util.Spliterator;
import java8.util.Spliterators;

import junit.framework.Test;

@org.testng.annotations.Test
public class ArrayDeque8Test extends JSR166TestCase {
// CVS rev. 1.3

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return newTestSuite(ArrayDeque8Test.class);
    }

    /**
     * Spliterator.getComparator always throws IllegalStateException
     */
    public void testSpliterator_getComparator() {
        assertThrows(IllegalStateException.class,
                     () -> Spliterators.spliterator(new ArrayDeque<>()).getComparator());
    }

    /**
     * Spliterator characteristics are as advertised
     */
    public void testSpliterator_characteristics() {
        ArrayDeque<?> q = new ArrayDeque<>();
        Spliterator<?> s = Spliterators.spliterator(q);
        int characteristics = s.characteristics();
        int required = Spliterator.NONNULL
            | Spliterator.ORDERED
            | Spliterator.SIZED
            | Spliterator.SUBSIZED;
        assertEquals(required, characteristics & required);
        assertTrue(s.hasCharacteristics(required));
        assertEquals(0, characteristics
                     & (Spliterator.CONCURRENT
                        | Spliterator.DISTINCT
                        | Spliterator.IMMUTABLE
                        | Spliterator.SORTED));
    }

    /**
     * Handle capacities near Integer.MAX_VALUE.
     * ant -Dvmoptions='-Xms28g -Xmx28g' -Djsr166.expensiveTests=true -Djsr166.tckTestClass=ArrayDeque8Test -Djsr166.methodFilter=testHugeCapacity tck
     */
    public void testHugeCapacity() {
        if (! (testImplementationDetails
               && expensiveTests
               && Runtime.getRuntime().maxMemory() > 24L * (1 << 30)))
            return;

        final Integer e = 42;
        final int maxArraySize = Integer.MAX_VALUE - 8;

        assertThrows(OutOfMemoryError.class,
                     () -> new ArrayDeque<>(Integer.MAX_VALUE));

        {
            ArrayDeque<?> q = new ArrayDeque<>(maxArraySize - 1);
            assertEquals(0, q.size());
            assertTrue(q.isEmpty());
            q = null;
        }

        {
            ArrayDeque<Integer> q = new ArrayDeque<>();
            assertTrue(q.addAll(Collections.nCopies(maxArraySize - 3, e)));
            assertEquals(e, q.peekFirst());
            assertEquals(e, q.peekLast());
            assertEquals(maxArraySize - 3, q.size());
            q.addFirst((Integer) 0);
            q.addLast((Integer) 1);
            assertEquals((Integer) 0, q.peekFirst());
            assertEquals((Integer) 1, q.peekLast());
            assertEquals(maxArraySize - 1, q.size());

            ArrayDeque<Integer> qq = q;
            ArrayDeque<Integer> smallish = new ArrayDeque<>(
                Collections.nCopies(Integer.MAX_VALUE - q.size() + 1, e));
            assertThrows(
                IllegalStateException.class,
                () -> qq.addAll(qq),
                () -> qq.addAll(smallish),
                () -> smallish.addAll(qq));
        }
    }
}
