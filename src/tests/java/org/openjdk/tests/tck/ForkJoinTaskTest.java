/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import java8.util.concurrent.ForkJoinPool;
import java8.util.concurrent.ForkJoinTask;
import java8.util.concurrent.RecursiveAction;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.openjdk.other.tests.flow.JSR166TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class ForkJoinTaskTest extends JSR166TestCase {

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return new TestSuite(ForkJoinTaskTest.class);
    }

    // Runs with "mainPool" use > 1 thread. singletonPool tests use 1
    static final int mainPoolSize =
        Math.max(2, Runtime.getRuntime().availableProcessors());

    private static ForkJoinPool mainPool() {
        return new ForkJoinPool(mainPoolSize);
    }

    private static ForkJoinPool singletonPool() {
        return new ForkJoinPool(1);
    }

    private static ForkJoinPool asyncSingletonPool() {
        return new ForkJoinPool(1,
                                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                                null, true);
    }

    private void testInvokeOnPool(ForkJoinPool pool, RecursiveAction a) {
        PoolCleaner cleaner = null;
        try {
            cleaner = cleaner(pool);
            assertFalse(a.isDone());
            assertFalse(a.isCompletedNormally());
            assertFalse(a.isCompletedAbnormally());
            assertFalse(a.isCancelled());
            assertNull(a.getException());
            assertNull(a.getRawResult());

            assertNull(pool.invoke(a));

            assertTrue(a.isDone());
            assertTrue(a.isCompletedNormally());
            assertFalse(a.isCompletedAbnormally());
            assertFalse(a.isCancelled());
            assertNull(a.getException());
            assertNull(a.getRawResult());
        } finally {
            if (cleaner != null) {
                cleaner.close();
            }
        }
    }

    void checkNotDone(ForkJoinTask<?> a) {
        assertFalse(a.isDone());
        assertFalse(a.isCompletedNormally());
        assertFalse(a.isCompletedAbnormally());
        assertFalse(a.isCancelled());
        assertNull(a.getException());
        assertNull(a.getRawResult());

        try {
            a.get(0L, SECONDS);
            shouldThrow();
        } catch (TimeoutException success) {
        } catch (Throwable fail) { threadUnexpectedException(fail); }
    }

    <T> void checkCompletedNormally(ForkJoinTask<T> a) {
        checkCompletedNormally(a, null);
    }

    <T> void checkCompletedNormally(ForkJoinTask<T> a, T expected) {
        assertTrue(a.isDone());
        assertFalse(a.isCancelled());
        assertTrue(a.isCompletedNormally());
        assertFalse(a.isCompletedAbnormally());
        assertNull(a.getException());
        assertSame(expected, a.getRawResult());

        {
            Thread.currentThread().interrupt();
            long startTime = System.nanoTime();
            assertSame(expected, a.join());
            assertTrue(millisElapsedSince(startTime) < SMALL_DELAY_MS);
            Thread.interrupted();
        }

        {
            Thread.currentThread().interrupt();
            long startTime = System.nanoTime();
            a.quietlyJoin();        // should be no-op
            assertTrue(millisElapsedSince(startTime) < SMALL_DELAY_MS);
            Thread.interrupted();
        }

        assertFalse(a.cancel(false));
        assertFalse(a.cancel(true));
        try {
            assertSame(expected, a.get());
        } catch (Throwable fail) { threadUnexpectedException(fail); }
        try {
            assertSame(expected, a.get(5L, SECONDS));
        } catch (Throwable fail) { threadUnexpectedException(fail); }
    }

    void checkCancelled(ForkJoinTask<?> a) {
        assertTrue(a.isDone());
        assertTrue(a.isCancelled());
        assertFalse(a.isCompletedNormally());
        assertTrue(a.isCompletedAbnormally());
        assertTrue(a.getException() instanceof CancellationException);
        assertNull(a.getRawResult());
        assertTrue(a.cancel(false));
        assertTrue(a.cancel(true));

        try {
            Thread.currentThread().interrupt();
            a.join();
            shouldThrow();
        } catch (CancellationException success) {
        } catch (Throwable fail) { threadUnexpectedException(fail); }
        Thread.interrupted();

        {
            long startTime = System.nanoTime();
            a.quietlyJoin();        // should be no-op
            assertTrue(millisElapsedSince(startTime) < SMALL_DELAY_MS);
        }

        try {
            a.get();
            shouldThrow();
        } catch (CancellationException success) {
        } catch (Throwable fail) { threadUnexpectedException(fail); }

        try {
            a.get(5L, SECONDS);
            shouldThrow();
        } catch (CancellationException success) {
        } catch (Throwable fail) { threadUnexpectedException(fail); }
    }

    void checkCompletedAbnormally(ForkJoinTask<?> a, Throwable t) {
        assertTrue(a.isDone());
        assertFalse(a.isCancelled());
        assertFalse(a.isCompletedNormally());
        assertTrue(a.isCompletedAbnormally());
        assertSame(t.getClass(), a.getException().getClass());
        assertNull(a.getRawResult());
        assertFalse(a.cancel(false));
        assertFalse(a.cancel(true));

        try {
            Thread.currentThread().interrupt();
            a.join();
            shouldThrow();
        } catch (Throwable expected) {
            assertSame(t.getClass(), expected.getClass());
        }
        Thread.interrupted();

        {
            long startTime = System.nanoTime();
            a.quietlyJoin();        // should be no-op
            assertTrue(millisElapsedSince(startTime) < SMALL_DELAY_MS);
        }

        try {
            a.get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertSame(t.getClass(), success.getCause().getClass());
        } catch (Throwable fail) { threadUnexpectedException(fail); }

        try {
            a.get(5L, SECONDS);
            shouldThrow();
        } catch (ExecutionException success) {
            assertSame(t.getClass(), success.getCause().getClass());
        } catch (Throwable fail) { threadUnexpectedException(fail); }
    }

    /*
     * Testing coverage notes:
     *
     * To test extension methods and overrides, most tests use
     * BinaryAsyncAction extension class that processes joins
     * differently than supplied Recursive forms.
     */

    @SuppressWarnings("serial")
    public static final class FJException extends RuntimeException {
        FJException() { super(); }
    }

    abstract static class BinaryAsyncAction extends ForkJoinTask<Void> {
        private static final long serialVersionUID = 1L;

        private volatile int controlState;

        static final AtomicIntegerFieldUpdater<BinaryAsyncAction> controlStateUpdater =
            AtomicIntegerFieldUpdater.newUpdater(BinaryAsyncAction.class,
                                                 "controlState");

        private volatile BinaryAsyncAction parent;

        private volatile BinaryAsyncAction sibling;

        protected BinaryAsyncAction() {
        }

        public final Void getRawResult() { return null; }
        protected final void setRawResult(Void mustBeNull) { }

        public final void linkSubtasks(BinaryAsyncAction x, BinaryAsyncAction y) {
            x.parent = y.parent = this;
            x.sibling = y;
            y.sibling = x;
        }

        protected void onComplete(BinaryAsyncAction x, BinaryAsyncAction y) {
        }

        protected boolean onException() {
            return true;
        }

        public void linkAndForkSubtasks(BinaryAsyncAction x, BinaryAsyncAction y) {
            linkSubtasks(x, y);
            y.fork();
            x.fork();
        }

        private void completeThis() {
            super.complete(null);
        }

        private void completeThisExceptionally(Throwable ex) {
            super.completeExceptionally(ex);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            if (super.cancel(mayInterruptIfRunning)) {
                completeExceptionally(new FJException());
                return true;
            }
            return false;
        }

        public final void complete() {
            BinaryAsyncAction a = this;
            for (;;) {
                BinaryAsyncAction s = a.sibling;
                BinaryAsyncAction p = a.parent;
                a.sibling = null;
                a.parent = null;
                a.completeThis();
                if (p == null || p.compareAndSetControlState(0, 1))
                    break;
                try {
                    p.onComplete(a, s);
                } catch (Throwable rex) {
                    p.completeExceptionally(rex);
                    return;
                }
                a = p;
            }
        }

        public final void completeExceptionally(Throwable ex) {
            for (BinaryAsyncAction a = this;;) {
                a.completeThisExceptionally(ex);
                BinaryAsyncAction s = a.sibling;
                if (s != null && !s.isDone())
                    s.completeExceptionally(ex);
                if ((a = a.parent) == null)
                    break;
            }
        }

        public final BinaryAsyncAction getParent() {
            return parent;
        }

        public BinaryAsyncAction getSibling() {
            return sibling;
        }

        public void reinitialize() {
            parent = sibling = null;
            super.reinitialize();
        }

        protected final int getControlState() {
            return controlState;
        }

        protected final boolean compareAndSetControlState(int expect,
                                                          int update) {
            return controlStateUpdater.compareAndSet(this, expect, update);
        }

        protected final void setControlState(int value) {
            controlState = value;
        }

        protected final void incrementControlState() {
            controlStateUpdater.incrementAndGet(this);
        }

        protected final void decrementControlState() {
            controlStateUpdater.decrementAndGet(this);
        }

    }

    static final class AsyncFib extends BinaryAsyncAction {
        private static final long serialVersionUID = 1L;
        int number;
        public AsyncFib(int n) {
            this.number = n;
        }

        public final boolean exec() {
            AsyncFib f = this;
            int n = f.number;
            while (n > 1) {
                AsyncFib p = f;
                AsyncFib r = new AsyncFib(n - 2);
                f = new AsyncFib(--n);
                p.linkSubtasks(r, f);
                r.fork();
            }
            f.complete();
            return false;
        }

        protected void onComplete(BinaryAsyncAction x, BinaryAsyncAction y) {
            number = ((AsyncFib)x).number + ((AsyncFib)y).number;
        }
    }

    static final class FailingAsyncFib extends BinaryAsyncAction {
        private static final long serialVersionUID = 1L;
        int number;
        public FailingAsyncFib(int n) {
            this.number = n;
        }

        public final boolean exec() {
            FailingAsyncFib f = this;
            int n = f.number;
            while (n > 1) {
                FailingAsyncFib p = f;
                FailingAsyncFib r = new FailingAsyncFib(n - 2);
                f = new FailingAsyncFib(--n);
                p.linkSubtasks(r, f);
                r.fork();
            }
            f.complete();
            return false;
        }

        protected void onComplete(BinaryAsyncAction x, BinaryAsyncAction y) {
            completeExceptionally(new FJException());
        }
    }

    /**
     * invoke returns when task completes normally.
     * isCompletedAbnormally and isCancelled return false for normally
     * completed tasks; getRawResult returns null.
     */
    public void testInvoke() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertNull(f.invoke());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * quietlyInvoke task returns when task completes normally.
     * isCompletedAbnormally and isCancelled return false for normally
     * completed tasks
     */
    public void testQuietlyInvoke() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                f.quietlyInvoke();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * join of a forked task returns when task completes
     */
    public void testForkJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.join());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * get of a forked task returns when task completes
     */
    public void testForkGet() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.get());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * timed get of a forked task returns when task completes
     */
    public void testForkTimedGet() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.get(LONG_DELAY_MS, MILLISECONDS));
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * timed get with null time unit throws NPE
     */
    public void testForkTimedGetNPE() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get(5L, null);
                    shouldThrow();
                } catch (NullPointerException success) {}
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task completes
     */
    public void testForkQuietlyJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                f.quietlyJoin();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * helpQuiesce returns when tasks are complete.
     * getQueuedTaskCount returns 0 when quiescent
     */
    public void testForkHelpQuiesce() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                helpQuiesce();
                assertEquals(21, f.number);
                assertEquals(0, getQueuedTaskCount());
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invoke task throws exception when task completes abnormally
     */
    public void testAbnormalInvoke() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                try {
                    f.invoke();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * quietlyInvoke task returns when task completes abnormally
     */
    public void testAbnormalQuietlyInvoke() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                f.quietlyInvoke();
                assertTrue(f.getException() instanceof FJException);
                checkCompletedAbnormally(f, f.getException());
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * join of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.join();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * get of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkGet() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get();
                    shouldThrow();
                } catch (ExecutionException success) {
                    Throwable cause = success.getCause();
                    assertTrue(cause instanceof FJException);
                    checkCompletedAbnormally(f, cause);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * timed get of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkTimedGet() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (ExecutionException success) {
                    Throwable cause = success.getCause();
                    assertTrue(cause instanceof FJException);
                    checkCompletedAbnormally(f, cause);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task completes abnormally
     */
    public void testAbnormalForkQuietlyJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                f.quietlyJoin();
                assertTrue(f.getException() instanceof FJException);
                checkCompletedAbnormally(f, f.getException());
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invoke task throws exception when task cancelled
     */
    public void testCancelledInvoke() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                try {
                    f.invoke();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * join of a forked task throws exception when task cancelled
     */
    public void testCancelledForkJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.join();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * get of a forked task throws exception when task cancelled
     */
    public void testCancelledForkGet() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.get();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * timed get of a forked task throws exception when task cancelled
     */
    public void testCancelledForkTimedGet() throws Exception {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.get(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task cancelled
     */
    public void testCancelledForkQuietlyJoin() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                f.quietlyJoin();
                checkCancelled(f);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * getPool of executing task returns its pool
     */
    public void testGetPool() {
        final ForkJoinPool mainPool = mainPool();
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                assertSame(mainPool, getPool());
            }};
        testInvokeOnPool(mainPool, a);
    }

    /**
     * getPool of non-FJ task returns null
     */
    public void testGetPool2() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                assertNull(getPool());
            }};
        assertNull(a.invoke());
    }

    /**
     * inForkJoinPool of executing task returns true
     */
    public void testInForkJoinPool() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                assertTrue(inForkJoinPool());
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * inForkJoinPool of non-FJ task returns false
     */
    public void testInForkJoinPool2() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                assertFalse(inForkJoinPool());
            }};
        assertNull(a.invoke());
    }

    /**
     * setRawResult(null) succeeds
     */
    public void testSetRawResult() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                setRawResult(null);
                assertNull(getRawResult());
            }};
        assertNull(a.invoke());
    }

    /**
     * invoke task throws exception after invoking completeExceptionally
     */
    public void testCompleteExceptionally() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                f.completeExceptionally(new FJException());
                try {
                    f.invoke();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * completeExceptionally(null) surprisingly has the same effect as
     * completeExceptionally(new RuntimeException())
     */
    public void testCompleteExceptionally_null() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                f.completeExceptionally(null);
                try {
                    f.invoke();
                    shouldThrow();
                } catch (RuntimeException success) {
                    assertSame(success.getClass(), RuntimeException.class);
                    assertNull(success.getCause());
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(t1, t2) invokes all task arguments
     */
    public void testInvokeAll2() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                invokeAll(f, g);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(tasks) with 1 argument invokes task
     */
    public void testInvokeAll1() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                invokeAll(f);
                checkCompletedNormally(f);
                assertEquals(21, f.number);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(tasks) with > 2 argument invokes tasks
     */
    public void testInvokeAll3() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                invokeAll(f, g, h);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                assertEquals(13, h.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
                checkCompletedNormally(h);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(collection) invokes all tasks in the collection
     */
    public void testInvokeAllCollection() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                HashSet<AsyncFib> set = new HashSet<>();
                set.add(f);
                set.add(g);
                set.add(h);
                invokeAll(set);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                assertEquals(13, h.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
                checkCompletedNormally(h);
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(tasks) with any null task throws NPE
     */
    public void testInvokeAllNPE() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = null;
                try {
                    invokeAll(f, g, h);
                    shouldThrow();
                } catch (NullPointerException success) {}
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(t1, t2) throw exception if any task does
     */
    public void testAbnormalInvokeAll2() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                FailingAsyncFib g = new FailingAsyncFib(9);
                ForkJoinTask[] tasks = { f, g };
                Collections.shuffle(Arrays.asList(tasks));
                try {
                    invokeAll(tasks);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(tasks) with 1 argument throws exception if task does
     */
    public void testAbnormalInvokeAll1() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib g = new FailingAsyncFib(9);
                try {
                    invokeAll(g);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(tasks) with > 2 argument throws exception if any task does
     */
    public void testAbnormalInvokeAll3() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                FailingAsyncFib g = new FailingAsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                ForkJoinTask[] tasks = { f, g, h };
                Collections.shuffle(Arrays.asList(tasks));
                try {
                    invokeAll(tasks);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * invokeAll(collection) throws exception if any task does
     */
    public void testAbnormalInvokeAllCollection() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                ForkJoinTask[] tasks = { f, g, h };
                List<ForkJoinTask> taskList = Arrays.asList(tasks);
                Collections.shuffle(taskList);
                try {
                    invokeAll(taskList);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(mainPool(), a);
    }

    /**
     * tryUnfork returns true for most recent unexecuted task,
     * and suppresses execution
     */
    public void testTryUnfork() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertTrue(f.tryUnfork());
                helpQuiesce();
                checkNotDone(f);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * getSurplusQueuedTaskCount returns > 0 when
     * there are more tasks than threads
     */
    public void testGetSurplusQueuedTaskCount() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib h = new AsyncFib(7);
                assertSame(h, h.fork());
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertTrue(getSurplusQueuedTaskCount() > 0);
                helpQuiesce();
                assertEquals(0, getSurplusQueuedTaskCount());
                checkCompletedNormally(f);
                checkCompletedNormally(g);
                checkCompletedNormally(h);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * peekNextLocalTask returns most recent unexecuted task.
     */
    public void testPeekNextLocalTask() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(f, peekNextLocalTask());
                assertNull(f.join());
                checkCompletedNormally(f);
                helpQuiesce();
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * pollNextLocalTask returns most recent unexecuted task without
     * executing it
     */
    public void testPollNextLocalTask() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(f, pollNextLocalTask());
                helpQuiesce();
                checkNotDone(f);
                assertEquals(34, g.number);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * pollTask returns an unexecuted task without executing it
     */
    public void testPollTask() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(f, pollTask());
                helpQuiesce();
                checkNotDone(f);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * peekNextLocalTask returns least recent unexecuted task in async mode
     */
    public void testPeekNextLocalTaskAsync() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(g, peekNextLocalTask());
                assertNull(f.join());
                helpQuiesce();
                checkCompletedNormally(f);
                assertEquals(34, g.number);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(asyncSingletonPool(), a);
    }

    /**
     * pollNextLocalTask returns least recent unexecuted task without
     * executing it, in async mode
     */
    public void testPollNextLocalTaskAsync() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(g, pollNextLocalTask());
                helpQuiesce();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
                checkNotDone(g);
            }};
        testInvokeOnPool(asyncSingletonPool(), a);
    }

    /**
     * pollTask returns an unexecuted task without executing it, in
     * async mode
     */
    public void testPollTaskAsync() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib g = new AsyncFib(9);
                assertSame(g, g.fork());
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertSame(g, pollTask());
                helpQuiesce();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
                checkNotDone(g);
            }};
        testInvokeOnPool(asyncSingletonPool(), a);
    }

    // versions for singleton pools

    /**
     * invoke returns when task completes normally.
     * isCompletedAbnormally and isCancelled return false for normally
     * completed tasks; getRawResult returns null.
     */
    public void testInvokeSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertNull(f.invoke());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * quietlyInvoke task returns when task completes normally.
     * isCompletedAbnormally and isCancelled return false for normally
     * completed tasks
     */
    public void testQuietlyInvokeSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                f.quietlyInvoke();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * join of a forked task returns when task completes
     */
    public void testForkJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.join());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * get of a forked task returns when task completes
     */
    public void testForkGetSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.get());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * timed get of a forked task returns when task completes
     */
    public void testForkTimedGetSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                assertNull(f.get(LONG_DELAY_MS, MILLISECONDS));
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * timed get with null time unit throws NPE
     */
    public void testForkTimedGetNPESingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get(5L, null);
                    shouldThrow();
                } catch (NullPointerException success) {}
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task completes
     */
    public void testForkQuietlyJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                f.quietlyJoin();
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * helpQuiesce returns when tasks are complete.
     * getQueuedTaskCount returns 0 when quiescent
     */
    public void testForkHelpQuiesceSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertSame(f, f.fork());
                helpQuiesce();
                assertEquals(0, getQueuedTaskCount());
                assertEquals(21, f.number);
                checkCompletedNormally(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invoke task throws exception when task completes abnormally
     */
    public void testAbnormalInvokeSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                try {
                    f.invoke();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * quietlyInvoke task returns when task completes abnormally
     */
    public void testAbnormalQuietlyInvokeSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                f.quietlyInvoke();
                assertTrue(f.getException() instanceof FJException);
                checkCompletedAbnormally(f, f.getException());
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * join of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.join();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * get of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkGetSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get();
                    shouldThrow();
                } catch (ExecutionException success) {
                    Throwable cause = success.getCause();
                    assertTrue(cause instanceof FJException);
                    checkCompletedAbnormally(f, cause);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * timed get of a forked task throws exception when task completes abnormally
     */
    public void testAbnormalForkTimedGetSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                try {
                    f.get(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (ExecutionException success) {
                    Throwable cause = success.getCause();
                    assertTrue(cause instanceof FJException);
                    checkCompletedAbnormally(f, cause);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task completes abnormally
     */
    public void testAbnormalForkQuietlyJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                assertSame(f, f.fork());
                f.quietlyJoin();
                assertTrue(f.getException() instanceof FJException);
                checkCompletedAbnormally(f, f.getException());
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invoke task throws exception when task cancelled
     */
    public void testCancelledInvokeSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                try {
                    f.invoke();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * join of a forked task throws exception when task cancelled
     */
    public void testCancelledForkJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.join();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * get of a forked task throws exception when task cancelled
     */
    public void testCancelledForkGetSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.get();
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * timed get of a forked task throws exception when task cancelled
     */
    public void testCancelledForkTimedGetSingleton() throws Exception {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() throws Exception {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                try {
                    f.get(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (CancellationException success) {
                    checkCancelled(f);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * quietlyJoin of a forked task returns when task cancelled
     */
    public void testCancelledForkQuietlyJoinSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                assertTrue(f.cancel(true));
                assertSame(f, f.fork());
                f.quietlyJoin();
                checkCancelled(f);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invoke task throws exception after invoking completeExceptionally
     */
    public void testCompleteExceptionallySingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                f.completeExceptionally(new FJException());
                try {
                    f.invoke();
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(t1, t2) invokes all task arguments
     */
    public void testInvokeAll2Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                invokeAll(f, g);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(tasks) with 1 argument invokes task
     */
    public void testInvokeAll1Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                invokeAll(f);
                checkCompletedNormally(f);
                assertEquals(21, f.number);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(tasks) with > 2 argument invokes tasks
     */
    public void testInvokeAll3Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                invokeAll(f, g, h);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                assertEquals(13, h.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
                checkCompletedNormally(h);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(collection) invokes all tasks in the collection
     */
    public void testInvokeAllCollectionSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                HashSet<AsyncFib> set = new HashSet<>();
                set.add(f);
                set.add(g);
                set.add(h);
                invokeAll(set);
                assertEquals(21, f.number);
                assertEquals(34, g.number);
                assertEquals(13, h.number);
                checkCompletedNormally(f);
                checkCompletedNormally(g);
                checkCompletedNormally(h);
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(tasks) with any null task throws NPE
     */
    public void testInvokeAllNPESingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = null;
                try {
                    invokeAll(f, g, h);
                    shouldThrow();
                } catch (NullPointerException success) {}
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(t1, t2) throw exception if any task does
     */
    public void testAbnormalInvokeAll2Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                FailingAsyncFib g = new FailingAsyncFib(9);
                ForkJoinTask[] tasks = { f, g };
                Collections.shuffle(Arrays.asList(tasks));
                try {
                    invokeAll(tasks);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(tasks) with 1 argument throws exception if task does
     */
    public void testAbnormalInvokeAll1Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib g = new FailingAsyncFib(9);
                try {
                    invokeAll(g);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(tasks) with > 2 argument throws exception if any task does
     */
    public void testAbnormalInvokeAll3Singleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                AsyncFib f = new AsyncFib(8);
                FailingAsyncFib g = new FailingAsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                ForkJoinTask[] tasks = { f, g, h };
                Collections.shuffle(Arrays.asList(tasks));
                try {
                    invokeAll(tasks);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(g, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * invokeAll(collection) throws exception if any task does
     */
    public void testAbnormalInvokeAllCollectionSingleton() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
            protected void realCompute() {
                FailingAsyncFib f = new FailingAsyncFib(8);
                AsyncFib g = new AsyncFib(9);
                AsyncFib h = new AsyncFib(7);
                ForkJoinTask[] tasks = { f, g, h };
                List<ForkJoinTask> taskList = Arrays.asList(tasks);
                Collections.shuffle(taskList);
                try {
                    invokeAll(taskList);
                    shouldThrow();
                } catch (FJException success) {
                    checkCompletedAbnormally(f, success);
                }
            }};
        testInvokeOnPool(singletonPool(), a);
    }

    /**
     * ForkJoinTask.quietlyComplete returns when task completes
     * normally without setting a value. The most recent value
     * established by setRawResult(V) (or null by default) is returned
     * from invoke.
     */
    public void testQuietlyComplete() {
        @SuppressWarnings("serial")
        RecursiveAction a = new CheckedRecursiveAction() {
                protected void realCompute() {
                    AsyncFib f = new AsyncFib(8);
                    f.quietlyComplete();
                    assertEquals(8, f.number);
                    checkCompletedNormally(f);
                }};
        testInvokeOnPool(mainPool(), a);
    }
}
