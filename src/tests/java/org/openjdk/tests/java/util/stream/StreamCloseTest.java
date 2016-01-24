/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.tests.java.util.stream;

import java.util.Arrays;

import java8.util.stream.OpTestCase;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;

import org.testng.annotations.Test;

import static java8.util.stream.LambdaTestHelpers.countTo;
import static java8.util.stream.ThrowableHelper.checkNPE;
import static java8.util.stream.ThrowableHelper.checkISE;

import java8.util.stream.StreamSupport;

/**
 * StreamCloseTest
 *
 * @author Brian Goetz
 */
@Test(groups = { "serialization-hostile" })
public class StreamCloseTest extends OpTestCase {
    public void testNullCloseHandler() {
        checkNPE(() -> RefStreams.of(1).onClose(null));
    }

    public void testEmptyCloseHandler() {
        Stream<Integer> ints = null;
        try {
            ints = StreamSupport.stream(countTo(100));
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }
        }
    }

    public void testOneCloseHandler() {
        final boolean[] holder = new boolean[1];
        Runnable closer = () -> { holder[0] = true; };

        Stream<Integer> ints = null;
        try {
            ints = StreamSupport.stream(countTo(100));
            ints.onClose(closer);
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }
        }
        assertTrue(holder[0]);

        Arrays.fill(holder, false);
        ints = StreamSupport.stream(countTo(100)).onClose(closer);
        try {
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0]);

        Arrays.fill(holder, false);
        ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(closer);
        try {
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0]);

        Arrays.fill(holder, false);
        ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(closer).filter(e -> true);
        try {
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0]);
    }

    public void testTwoCloseHandlers() {
        final boolean[] holder = new boolean[2];
        Runnable close1 = () -> { holder[0] = true; };
        Runnable close2 = () -> { holder[1] = true; };

        Stream<Integer> ints = null;
        try {
            ints = StreamSupport.stream(countTo(100));
            ints.onClose(close1).onClose(close2);
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0] && holder[1]);

        Arrays.fill(holder, false);
        try {
            ints = StreamSupport.stream(countTo(100)).onClose(close1).onClose(close2);
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0] && holder[1]);

        Arrays.fill(holder, false);
        try {
            ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(close1).onClose(close2);
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }            
        }
        assertTrue(holder[0] && holder[1]);

        Arrays.fill(holder, false);
        try {
            ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(close1).onClose(close2).filter(e -> true);
            ints.forEach(i -> {});
        } finally {
            if (ints != null) {
                ints.close();
            }
        }
        assertTrue(holder[0] && holder[1]);
    }

    public void testCascadedExceptions() {
        final boolean[] holder = new boolean[3];
        boolean caught = false;
        Runnable close1 = () -> { holder[0] = true; throw new RuntimeException("1"); };
        Runnable close2 = () -> { holder[1] = true; throw new RuntimeException("2"); };
        Runnable close3 = () -> { holder[2] = true; throw new RuntimeException("3"); };

        Stream<Integer> ints = null;
        try {
            ints = StreamSupport.stream(countTo(100));
            ints.onClose(close1).onClose(close2).onClose(close3);
            ints.forEach(i -> {});

            ints.close();
        }
        catch (RuntimeException e) {
            assertCascaded(e, 3);
            assertTrue(holder[0] && holder[1] && holder[2]);
            caught = true;
        } finally {
            //
        }
        assertTrue(caught);

        Arrays.fill(holder, false);
        caught = false;
        try {
            ints = StreamSupport.stream(countTo(100)).onClose(close1).onClose(close2).onClose(close3);
            ints.forEach(i -> {});

            ints.close();
        }
        catch (RuntimeException e) {
            assertCascaded(e, 3);
            assertTrue(holder[0] && holder[1] && holder[2]);
            caught = true;
        } finally {
            //
        }
        assertTrue(caught);

        caught = false;
        Arrays.fill(holder, false);
        try {
            ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(close1).onClose(close2).onClose(close3);
            ints.forEach(i -> {});

            ints.close();
        }
        catch (RuntimeException e) {
            assertCascaded(e, 3);
            assertTrue(holder[0] && holder[1] && holder[2]);
            caught = true;
        } finally {
            //
        }
        assertTrue(caught);

        caught = false;
        Arrays.fill(holder, false);
        try {
            ints = StreamSupport.stream(countTo(100)).filter(e -> true).onClose(close1).onClose(close2).filter(e -> true).onClose(close3);
            ints.forEach(i -> {});

            ints.close();
        }
        catch (RuntimeException e) {
            assertCascaded(e, 3);
            assertTrue(holder[0] && holder[1] && holder[2]);
            caught = true;
        } finally {
            //
        }
        assertTrue(caught);
    }

    private void assertCascaded(RuntimeException e, int n) {
        assertTrue(e.getMessage().equals("1"));
        /*
        assertTrue(e.getSuppressed().length == n - 1);
        for (int i = 0; i < n-1; i++) {
            assertTrue(e.getSuppressed()[i].getMessage().equals(String.valueOf(i + 2)));
        }
        */
    }

    public void testConsumed() {
        Stream<Integer> s1 = StreamSupport.stream(countTo(100));
        try {
            s1.forEach(i -> {});
            checkISE(() -> s1.onClose(() -> fail("s1")));
        } finally {
            if (s1 != null) {
                s1.close();
            }
        }

        Stream<Integer> s2 = StreamSupport.stream(countTo(100));
        try {
            s2.map(x -> x).forEach(i -> {});
            checkISE(() -> s2.onClose(() -> fail("s2")));
        } finally {
            if (s2 != null) {
                s2.close();
            }
        }

        Stream<Integer> s3 = StreamSupport.stream(countTo(100));
        try {
            s3.close();
            checkISE(() -> s3.onClose(() -> fail("s3")));
        } finally {
            if (s3 != null) {
                s3.close();
            }
        }
    }
}
