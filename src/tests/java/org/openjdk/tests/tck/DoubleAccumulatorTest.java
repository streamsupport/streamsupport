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
import java8.util.concurrent.atomic.DoubleAccumulator;
import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class DoubleAccumulatorTest extends JSR166TestCase {
// CVS rev. 1.7

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
        final int incs = 1000000;
        final int nthreads = 4;
        final ExecutorService pool = Executors.newCachedThreadPool();
        DoubleAccumulator a = new DoubleAccumulator(Doubles::max, 0.0);
        Phaser phaser = new Phaser(nthreads + 1);
        for (int i = 0; i < nthreads; ++i)
            pool.execute(new AccTask(a, phaser, incs));
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        double expected = incs - 1;
        double result = a.get();
        assertEquals(expected, result);
        pool.shutdown();
    }

    static final class AccTask implements Runnable {
        final DoubleAccumulator acc;
        final Phaser phaser;
        final int incs;
        volatile double result;
        AccTask(DoubleAccumulator acc, Phaser phaser, int incs) {
            this.acc = acc;
            this.phaser = phaser;
            this.incs = incs;
        }

        public void run() {
            phaser.arriveAndAwaitAdvance();
            DoubleAccumulator a = acc;
            for (int i = 0; i < incs; ++i)
                a.accumulate(i);
            result = a.get();
            phaser.arrive();
        }
    }
}
