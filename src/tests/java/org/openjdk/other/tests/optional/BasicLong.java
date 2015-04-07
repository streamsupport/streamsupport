package org.openjdk.other.tests.optional;
/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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

/* @test
 * @summary Basic functional test of OptionalLong
 * @author Mike Duigou
 * @run testng BasicLong
 */

import java.util.NoSuchElementException;

import java8.lang.Longs;
import java8.util.OptionalLong;
import java8.util.function.LongConsumer;
import java8.util.function.LongSupplier;
import java8.util.function.Supplier;
import java8.util.stream.LongStream;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class BasicLong {

    @Test(groups = "unit")
    public void testEmpty() {
        OptionalLong empty = OptionalLong.empty();
        OptionalLong present = OptionalLong.of(1);

        // empty
        assertTrue(empty.equals(empty));
        assertTrue(empty.equals(OptionalLong.empty()));
        assertTrue(!empty.equals(present));
        assertTrue(0 == empty.hashCode());
        assertTrue(!empty.toString().isEmpty());
        assertTrue(!empty.isPresent());
        empty.ifPresent(new LongConsumer() {
            @Override
            public void accept(long v) {
                fail();
            }
        });
        assertEquals(2, empty.orElse(2));
        assertEquals(2, empty.orElseGet(new LongSupplier() {
            @Override
            public long getAsLong() {
                return 2;
            }
        }));
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void testEmptyGet() {
        OptionalLong empty = OptionalLong.empty();

        long got = empty.getAsLong();
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseGetNull() {
        OptionalLong empty = OptionalLong.empty();

        long got = empty.orElseGet(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseThrowNull() throws Throwable {
        OptionalLong empty = OptionalLong.empty();

        long got = empty.orElseThrow(null);
    }

    @Test(expectedExceptions=ObscureException.class)
    public void testEmptyOrElseThrow() throws Exception {
        OptionalLong empty = OptionalLong.empty();
        long got = empty.orElseThrow(new Supplier<ObscureException>() {
            @Override
            public ObscureException get() {
                return new ObscureException();
            }
        });
    }

    @Test(groups = "unit")
    public void testPresent() {
        OptionalLong empty = OptionalLong.empty();
        OptionalLong present = OptionalLong.of(1L);

        // present
        assertTrue(present.equals(present));
        assertFalse(present.equals(OptionalLong.of(0L)));
        assertTrue(present.equals(OptionalLong.of(1L)));
        assertFalse(present.equals(empty));
        assertTrue(Longs.hashCode(1) == present.hashCode());
        assertFalse(present.toString().isEmpty());
        assertTrue(-1 != present.toString().indexOf(Long.toString(present.getAsLong()).toString()));
        assertEquals(1L, present.getAsLong());
        try {
            present.ifPresent(new LongConsumer() {
                @Override
                public void accept(long v) {
                    throw new ObscureException();
                }
            });
            fail();
        } catch(ObscureException expected) {

        }
        assertEquals(1, present.orElse(2));
        assertEquals(1, present.orElseGet(null));
        assertEquals(1, present.orElseGet(new LongSupplier() {
            @Override
            public long getAsLong() {
                return 2;
            }
        }));
        assertEquals(1, present.orElseGet(new LongSupplier() {
            @Override
            public long getAsLong() {
                return 3;
            }
        }));
        assertEquals(1, present.<RuntimeException>orElseThrow(null));
        assertEquals(1, present.<RuntimeException>orElseThrow(new Supplier<RuntimeException>() {
            @Override
            public RuntimeException get() {
                return new ObscureException();
            }
        }));
    }

    @Test(groups = "unit")
    public void testStream() {
        {
            LongStream s = OptionalLong.empty().stream();

            long[] es = s.toArray();
            assertEquals(es.length, 0);
        }

        {
            LongStream s = OptionalLong.of(42L).stream();

            long[] es = s.toArray();
            assertEquals(es.length, 1);
            assertEquals(es[0], 42L);
        }
    }

    private static class ObscureException extends RuntimeException {

    }
}
