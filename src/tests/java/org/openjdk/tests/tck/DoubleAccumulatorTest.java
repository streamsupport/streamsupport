/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java8.lang.Doubles;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java8.util.concurrent.Phaser;
import java8.util.concurrent.ThreadLocalRandom;
import java8.util.concurrent.atomic.DoubleAccumulator;
import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class DoubleAccumulatorTest extends JSR166TestCase {
// CVS rev. 1.8

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return new TestSuite(DoubleAccumulatorTest.class);
    }

    /**
     * new instance initialized to supplied identity
     */
    public void testConstructor() {
        for (double identity : new double[] {
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.MIN_VALUE,
                Double.MAX_VALUE,
                0.0,
            })
           assertEquals(identity,
                        new DoubleAccumulator(Doubles::max, identity).get());
    }

    /**
     * accumulate accumulates given value to current, and get returns current value
     */
    public void testAccumulateAndGet() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        acc.accumulate(2.0);
        assertEquals(2.0, acc.get());
        acc.accumulate(-4.0);
        assertEquals(2.0, acc.get());
        acc.accumulate(4.0);
        assertEquals(4.0, acc.get());
    }

    /**
     * reset() causes subsequent get() to return zero
     */
    public void testReset() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        acc.accumulate(2.0);
        assertEquals(2.0, acc.get());
        acc.reset();
        assertEquals(0.0, acc.get());
    }

    /**
     * getThenReset() returns current value; subsequent get() returns zero
     */
    public void testGetThenReset() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        acc.accumulate(2.0);
        assertEquals(2.0, acc.get());
        assertEquals(2.0, acc.getThenReset());
        assertEquals(0.0, acc.get());
    }

    /**
     * toString returns current value.
     */
    public void testToString() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        assertEquals("0.0", acc.toString());
        acc.accumulate(1.0);
        assertEquals(Double.toString(1.0), acc.toString());
    }

    /**
     * intValue returns current value.
     */
    public void testIntValue() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        assertEquals(0, acc.intValue());
        acc.accumulate(1.0);
        assertEquals(1, acc.intValue());
    }

    /**
     * longValue returns current value.
     */
    public void testLongValue() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        assertEquals(0, acc.longValue());
        acc.accumulate(1.0);
        assertEquals(1, acc.longValue());
    }

    /**
     * floatValue returns current value.
     */
    public void testFloatValue() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        assertEquals(0.0f, acc.floatValue());
        acc.accumulate(1.0);
        assertEquals(1.0f, acc.floatValue());
    }

    /**
     * doubleValue returns current value.
     */
    public void testDoubleValue() {
        DoubleAccumulator acc = new DoubleAccumulator(Doubles::max, 0.0);
        assertEquals(0.0, acc.doubleValue());
        acc.accumulate(1.0);
        assertEquals(1.0, acc.doubleValue());
    }

    /**
     * accumulates by multiple threads produce correct result
     */
    public void testAccumulateAndGetMT() {
        final DoubleAccumulator acc
            = new DoubleAccumulator((x, y) -> x + y, 0.0);
        final int nThreads = ThreadLocalRandom.current().nextInt(1, 5);
        final Phaser phaser = new Phaser(nThreads + 1);
        final int incs = 1_000_000;
        final double total = nThreads * incs/2.0 * (incs - 1); // Gauss
        final Runnable task = () -> {
            phaser.arriveAndAwaitAdvance();
            for (int i = 0; i < incs; i++) {
                acc.accumulate((double) i);
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
