/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java.util.concurrent.LinkedBlockingDeque;
import java8.util.Spliterator;
import java8.util.Spliterators;

import junit.framework.Test;

@org.testng.annotations.Test
public class LinkedBlockingDeque8Test extends JSR166TestCase {
// CVS rev. 1.2

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return newTestSuite(LinkedBlockingDeque8Test.class);
    }

    /**
     * Spliterator.getComparator always throws IllegalStateException
     */
    public void testSpliterator_getComparator() {
        assertThrows(IllegalStateException.class,
                     () -> Spliterators.spliterator(new LinkedBlockingDeque<Object>()).getComparator());
    }

    /**
     * Spliterator characteristics are as advertised
     */
    public void testSpliterator_characteristics() {
        if (!NATIVE_SPECIALIZATION && !IS_ANDROID) {
            // Iterator-based Spliterator doesn't have all characteristics
            return;
        }
        LinkedBlockingDeque<?> q = new LinkedBlockingDeque<Object>();
        Spliterator<?> s = Spliterators.spliterator(q);
        int characteristics = s.characteristics();
        int required = Spliterator.CONCURRENT
            | Spliterator.NONNULL
            | Spliterator.ORDERED;
        assertEquals(required, characteristics & required);
        assertTrue(s.hasCharacteristics(required));
        assertEquals(0, characteristics
                     & (Spliterator.DISTINCT
                        | Spliterator.IMMUTABLE
                        | Spliterator.SORTED));
    }
}
