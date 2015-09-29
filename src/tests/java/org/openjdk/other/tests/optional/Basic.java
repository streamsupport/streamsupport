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
 * @summary Basic functional test of Optional
 * @author Mike Duigou
 * @run testng Basic
 */

import java.util.NoSuchElementException;

import static org.testng.Assert.*;

import java8.util.Optional;
import java8.util.function.Consumer;
import java8.util.function.Function;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.stream.Stream;

import org.testng.annotations.Test;

public class Basic {

    @Test(groups = "unit")
    public void testEmpty() {
        Optional<Boolean> empty = Optional.empty();
        Optional<String> presentEmptyString = Optional.of("");
        Optional<Boolean> present = Optional.of(Boolean.TRUE);

        // empty
        assertTrue(empty.equals(empty));
        assertTrue(empty.equals(Optional.empty()));
        assertTrue(!empty.equals(present));
        assertTrue(0 == empty.hashCode());
        assertTrue(!empty.toString().isEmpty());
        assertTrue(!empty.toString().equals(presentEmptyString.toString()));
        assertTrue(!empty.isPresent());
        empty.ifPresent(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean v) {
                fail();
            }
        });
        assertSame(null, empty.orElse(null));
        RuntimeException orElse = new RuntimeException() { };
        assertSame(Boolean.FALSE, empty.orElse(Boolean.FALSE));
        assertSame(null, empty.orElseGet(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return null;
            }
        }));
        assertSame(Boolean.FALSE, empty.orElseGet(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return Boolean.FALSE;
            }
        }));
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void testEmptyGet() {
        Optional<Boolean> empty = Optional.empty();

        Boolean got = empty.get();
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseGetNull() {
        Optional<Boolean> empty = Optional.empty();

        Boolean got = empty.orElseGet(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseThrowNull() throws Throwable {
        Optional<Boolean> empty = Optional.empty();

        Boolean got = empty.orElseThrow(null);
    }

    @Test(expectedExceptions=ObscureException.class)
    public void testEmptyOrElseThrow() throws Exception {
        Optional<Boolean> empty = Optional.empty();

        Boolean got = empty.orElseThrow(new Supplier<ObscureException>() {
            @Override
            public ObscureException get() {
                return new ObscureException();
            }
        });
    }

    @Test(groups = "unit")
    public void testPresent() {
        Optional<Boolean> empty = Optional.empty();
        Optional<String> presentEmptyString = Optional.of("");
        Optional<Boolean> present = Optional.of(Boolean.TRUE);

        // present
        assertTrue(present.equals(present));
        assertTrue(present.equals(Optional.of(Boolean.TRUE)));
        assertTrue(!present.equals(empty));
        assertTrue(Boolean.TRUE.hashCode() == present.hashCode());
        assertTrue(!present.toString().isEmpty());
        assertTrue(!present.toString().equals(presentEmptyString.toString()));
        assertTrue(-1 != present.toString().indexOf(Boolean.TRUE.toString()));
        assertSame(Boolean.TRUE, present.get());
        try {
            present.ifPresent(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean v) {
                    throw new ObscureException();
                }
            });
            fail();
        } catch(ObscureException expected) {

        }
        assertSame(Boolean.TRUE, present.orElse(null));
        assertSame(Boolean.TRUE, present.orElse(Boolean.FALSE));
        assertSame(Boolean.TRUE, present.orElseGet(null));
        assertSame(Boolean.TRUE, present.orElseGet(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return null;
            }
        }));
        assertSame(Boolean.TRUE, present.orElseGet(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return Boolean.FALSE;
            }
        }));
        assertSame(Boolean.TRUE, present.<RuntimeException>orElseThrow( null));
        assertSame(Boolean.TRUE, present.<RuntimeException>orElseThrow(new Supplier<RuntimeException>() {
            @Override
            public RuntimeException get() {
                return new ObscureException();
            }
        }));
    }

    @Test(groups = "unit")
    public void testOfNullable() {
        Optional<String> instance = Optional.ofNullable(null);
        assertFalse(instance.isPresent());

        instance = Optional.ofNullable("Duke");
        assertTrue(instance.isPresent());
        assertEquals(instance.get(), "Duke");
    }

    @Test(groups = "unit")
    public void testFilter() {
        // Null mapper function
        Optional<String> empty = Optional.empty();
        Optional<String> duke = Optional.of("Duke");

        try {
            Optional<String> result = empty.filter(null);
            fail("Should throw NPE on null mapping function");
        } catch (NullPointerException npe) {
            // expected
        }

        Optional<String> result = empty.filter((Predicate<String>) new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.isEmpty();
            }
        });
        assertFalse(result.isPresent());

        result = duke.filter((Predicate<String>) new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.isEmpty();
            }
        });
        assertFalse(result.isPresent());
        result = duke.filter(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.startsWith("D");
            }
        });
        assertTrue(result.isPresent());
        assertEquals(result.get(), "Duke");

        Optional<String> emptyString = Optional.of("");
        result = emptyString.filter((Predicate<String>) new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.isEmpty();
            }
        });
        assertTrue(result.isPresent());
        assertEquals(result.get(), "");
    }

    @Test(groups = "unit")
    public void testMap() {
        Optional<String> empty = Optional.empty();
        Optional<String> duke = Optional.of("Duke");

        // Null mapper function
        try {
            Optional<Boolean> b = empty.map(null);
            fail("Should throw NPE on null mapping function");
        } catch (NullPointerException npe) {
            // expected
        }

        // Map an empty value
        Optional<Boolean> b;
        b = empty.map((Function<String, Boolean>) new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                return s.isEmpty();
            }
        });
        assertFalse(b.isPresent());

        // Map into null
        b = empty.map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String n) {
                return null;
            }
        });
        assertFalse(b.isPresent());
        b = duke.map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                return null;
            }
        });
        assertFalse(b.isPresent());

        // Map to value
        Optional<Integer> l = duke.map((Function<String, Integer>) new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();
            }
        });
        assertEquals(l.get().intValue(), 4);
    }

    @Test(groups = "unit")
    public void testFlatMap() {
        Optional<String> empty = Optional.empty();
        Optional<String> duke = Optional.of("Duke");

        // Null mapper function
        try {
            Optional<Boolean> b = empty.flatMap(null);
            fail("Should throw NPE on null mapping function");
        } catch (NullPointerException npe) {
            // expected
        }

        // Map into null
        try {
            Optional<Boolean> b = duke.flatMap(new Function<String, Optional<Boolean>>() {
                @Override
                public Optional<Boolean> apply(String s) {
                    return null;
                }
            });
            fail("Should throw NPE when mapper return null");
        } catch (NullPointerException npe) {
            // expected
        }

        // Empty won't invoke mapper function
        try {
            Optional<Boolean> b;
            b = empty.flatMap(new Function<String, Optional<Boolean>>() {
                @Override
                public Optional<Boolean> apply(String s) {
                    return null;
                }
            });
            assertFalse(b.isPresent());
        } catch (NullPointerException npe) {
            fail("Mapper function should not be invoked");
        }

        // Map an empty value
        Optional<Integer> l = empty.flatMap(new Function<String, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(String s) {
                return Optional.of(s.length());
            }
        });
        assertFalse(l.isPresent());

        // Map to value
        final Optional<Integer> fixture = Optional.of(Integer.MAX_VALUE);
        l = duke.flatMap(new Function<String, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(String s) {
                return Optional.of(s.length());
            }
        });
        assertTrue(l.isPresent());
        assertEquals(l.get().intValue(), 4);

        // Verify same instance
        l = duke.flatMap(new Function<String, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(String s) {
                return fixture;
            }
        });
        assertSame(l, fixture);
    }

    @Test(groups = "unit")
    public void testOr() {
        Optional<String> empty = Optional.empty();
        Optional<String> duke = Optional.of("Duke");

        // Null supplier
        try {
            Optional<String> b = empty.or(null);
            fail("Should throw NPE on null supplier");
        } catch (NullPointerException npe) {
            // expected
        }

        // Supply null
        try {
            Optional<String> b = empty.or(() -> null);
            fail("Should throw NPE when supplier returns null");
        } catch (NullPointerException npe) {
            // expected
        }

        // Non-empty won't invoke supplier
        try {
            Optional<String> b = duke.or(() -> null);
            assertTrue(b.isPresent());
        } catch (NullPointerException npe) {
            fail("Supplier should not be invoked");
        }

        // Supply for empty
        Optional<String> suppliedDuke = empty.or(() -> duke);
        assertTrue(suppliedDuke.isPresent());
        assertSame(suppliedDuke, duke);

        // Supply for non-empty
        Optional<String> actualDuke = duke.or(() -> Optional.of("Other Duke"));
        assertTrue(actualDuke.isPresent());
        assertSame(actualDuke, duke);
    }

    @Test(groups = "unit")
    public void testStream() {
        {
            Stream<String> s = Optional.<String>empty().stream();
            assertFalse(s.isParallel());

            Object[] es = s.toArray();
            assertEquals(es.length, 0);
        }

        {
            Stream<String> s = Optional.of("Duke").stream();
            assertFalse(s.isParallel());

            String[] es = s.toArray(String[]::new);
            assertEquals(es.length, 1);
            assertEquals(es[0], "Duke");
        }
    }

    private static class ObscureException extends RuntimeException {

    }
}
