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
import java8.util.concurrent.atomic.LongAccumulator;
import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class LongAccumulatorTest extends JSR166TestCase {
// CVS rev. 1.8

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
        final int incs = 1000000;
        final int nthreads = 4;
        final ExecutorService pool = Executors.newCachedThreadPool();
        LongAccumulator a = new LongAccumulator(Longs::max, 0L);
        Phaser phaser = new Phaser(nthreads + 1);
        for (int i = 0; i < nthreads; ++i)
            pool.execute(new AccTask(a, phaser, incs));
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        long expected = incs - 1;
        long result = a.get();
        assertEquals(expected, result);
        pool.shutdown();
    }

    static final class AccTask implements Runnable {
        final LongAccumulator acc;
        final Phaser phaser;
        final int incs;
        volatile long result;
        AccTask(LongAccumulator acc, Phaser phaser, int incs) {
            this.acc = acc;
            this.phaser = phaser;
            this.incs = incs;
        }

        public void run() {
            phaser.arriveAndAwaitAdvance();
            LongAccumulator a = acc;
            for (int i = 0; i < incs; ++i)
                a.accumulate(i);
            result = a.get();
            phaser.arrive();
        }
    }
}
