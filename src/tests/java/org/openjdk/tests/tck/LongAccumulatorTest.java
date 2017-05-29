/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java8.lang.Longs;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java8.util.concurrent.Phaser;
import java8.util.concurrent.ThreadLocalRandom;
import java8.util.concurrent.atomic.LongAccumulator;
import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class LongAccumulatorTest extends JSR166TestCase {
// CVS rev. 1.9

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return new TestSuite(LongAccumulatorTest.class);
    }

    /**
     * new instance initialized to supplied identity
     */
    public void testConstructor() {
        for (long identity : new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE })
            assertEquals(identity,
                         new LongAccumulator(Longs::max, identity).get());
    }

    /**
     * accumulate accumulates given value to current, and get returns current value
     */
    public void testAccumulateAndGet() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        acc.accumulate(2);
        assertEquals(2, acc.get());
        acc.accumulate(-4);
        assertEquals(2, acc.get());
        acc.accumulate(4);
        assertEquals(4, acc.get());
    }

    /**
     * reset() causes subsequent get() to return zero
     */
    public void testReset() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        acc.accumulate(2);
        assertEquals(2, acc.get());
        acc.reset();
        assertEquals(0, acc.get());
    }

    /**
     * getThenReset() returns current value; subsequent get() returns zero
     */
    public void testGetThenReset() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        acc.accumulate(2);
        assertEquals(2, acc.get());
        assertEquals(2, acc.getThenReset());
        assertEquals(0, acc.get());
    }

    /**
     * toString returns current value.
     */
    public void testToString() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        assertEquals("0", acc.toString());
        acc.accumulate(1);
        assertEquals(Long.toString(1), acc.toString());
    }

    /**
     * intValue returns current value.
     */
    public void testIntValue() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        assertEquals(0, acc.intValue());
        acc.accumulate(1);
        assertEquals(1, acc.intValue());
    }

    /**
     * longValue returns current value.
     */
    public void testLongValue() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        assertEquals(0, acc.longValue());
        acc.accumulate(1);
        assertEquals(1, acc.longValue());
    }

    /**
     * floatValue returns current value.
     */
    public void testFloatValue() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        assertEquals(0.0f, acc.floatValue());
        acc.accumulate(1);
        assertEquals(1.0f, acc.floatValue());
    }

    /**
     * doubleValue returns current value.
     */
    public void testDoubleValue() {
        LongAccumulator acc = new LongAccumulator(Longs::max, 0L);
        assertEquals(0.0, acc.doubleValue());
        acc.accumulate(1);
        assertEquals(1.0, acc.doubleValue());
    }

    /**
     * accumulates by multiple threads produce correct result
     */
    public void testAccumulateAndGetMT() {
        final LongAccumulator acc
            = new LongAccumulator((x, y) -> x + y, 0L);
        final int nThreads = ThreadLocalRandom.current().nextInt(1, 5);
        final Phaser phaser = new Phaser(nThreads + 1);
        final int incs = 1_000_000;
        final long total = nThreads * incs/2L * (incs - 1); // Gauss
        final Runnable task = () -> {
            phaser.arriveAndAwaitAdvance();
            for (int i = 0; i < incs; i++) {
                acc.accumulate((long) i);
                assertTrue(acc.get() <= total);
            }
            phaser.arrive();
        };
        final ExecutorService p = Executors.newCachedThreadPool();
        PoolCleaner cleaner = null;
        try {
            cleaner = cleaner(p);
            for (int i = nThreads; i-- > 0; /**/)
                p.execute(task);
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
            assertEquals(total, acc.get());
        } finally {
            if (cleaner != null) {
                cleaner.close();
            }
        }
    }
}
