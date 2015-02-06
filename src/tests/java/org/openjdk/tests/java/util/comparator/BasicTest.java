/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test
 * @summary Comparator default method tests
 * @run testng BasicTest
 */
package org.openjdk.tests.java.util.comparator;

import java.util.Comparator;

import org.testng.annotations.Test;

import java8.util.Comparators;
import java8.util.function.Function;
import java8.util.function.ToIntFunction;
import java8.util.function.ToLongFunction;
import java8.util.function.ToDoubleFunction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit")
public class BasicTest {
    private static class Thing {
        public final int intField;
        public final long longField;
        public final double doubleField;
        public final String stringField;

        private Thing(int intField, long longField, double doubleField, String stringField) {
            this.intField = intField;
            this.longField = longField;
            this.doubleField = doubleField;
            this.stringField = stringField;
        }

        public int getIntField() {
            return intField;
        }

        public long getLongField() {
            return longField;
        }

        public double getDoubleField() {
            return doubleField;
        }

        public String getStringField() {
            return stringField;
        }
    }

    private final int[] intValues = { -2, -2, -1, -1, 0, 0, 1, 1, 2, 2 };
    private final long[] longValues = { -2, -2, -1, -1, 0, 0, 1, 1, 2, 2 };
    private final double[] doubleValues = { -2, -2, -1, -1, 0, 0, 1, 1, 2, 2 };
    private final String[] stringValues = { "a", "a", "b", "b", "c", "c", "d", "d", "e", "e" };
    private final int[] comparisons = { 0, -1, 0, -1, 0, -1, 0, -1, 0 };

    private <T> void assertComparisons(T[] things, Comparator<T> comp, int[] comparisons) {
        for (int i=0; i<comparisons.length; i++) {
            assertEquals(comparisons.length + 1, things.length);
            assertEquals(comparisons[i], comp.compare(things[i], things[i+1]));
            assertEquals(-comparisons[i], comp.compare(things[i+1], things[i]));
        }
    }

    public void testIntComparator() {
        Thing[] things = new Thing[intValues.length];
        for (int i=0; i<intValues.length; i++)
            things[i] = new Thing(intValues[i], 0L, 0.0, null);
        Comparator<Thing> comp = Comparators.comparingInt(new ToIntFunction<Thing>() {
            @Override
            public int applyAsInt(Thing thing) {
                return thing.getIntField();
            }
        });

        assertComparisons(things, comp, comparisons);
    }

    public void testLongComparator() {
        Thing[] things = new Thing[longValues.length];
        for (int i=0; i<longValues.length; i++)
            things[i] = new Thing(0, longValues[i], 0.0, null);
        Comparator<Thing> comp = Comparators.comparingLong(new ToLongFunction<Thing>() {
            @Override
            public long applyAsLong(Thing thing) {
                return thing.getLongField();
            }
        });

        assertComparisons(things, comp, comparisons);
    }

    public void testDoubleComparator() {
        Thing[] things = new Thing[doubleValues.length];
        for (int i=0; i<doubleValues.length; i++)
            things[i] = new Thing(0, 0L, doubleValues[i], null);
        Comparator<Thing> comp = Comparators.comparingDouble(new ToDoubleFunction<Thing>() {
            @Override
            public double applyAsDouble(Thing thing) {
                return thing.getDoubleField();
            }
        });

        assertComparisons(things, comp, comparisons);
    }

    public void testComparing() {
        Thing[] things = new Thing[doubleValues.length];
        for (int i=0; i<doubleValues.length; i++)
            things[i] = new Thing(0, 0L, 0.0, stringValues[i]);
        Comparator<Thing> comp = Comparators.comparing(new Function<Thing, String>() {
            @Override
            public String apply(Thing thing) {
                return thing.getStringField();
            }
        });

        assertComparisons(things, comp, comparisons);
    }

    public void testNaturalOrderComparator() {
        Comparator<String> comp = Comparators.naturalOrder();

        assertComparisons(stringValues, comp, comparisons);
    }

    public void testReverseComparator() {
        Comparator<String> cmpr = Comparators.reverseOrder();
        Comparator<String> cmp = Comparators.reversed(cmpr);

        // This test doesn't make sense because it depends on implementation details
//        assertEquals(Comparators.reversed(cmp), cmpr); // TODO: this can only work for JRE7 and above
        assertEquals(0, cmp.compare("a", "a"));
        assertEquals(0, cmpr.compare("a", "a"));
        assertTrue(cmp.compare("a", "b") < 0);
        assertTrue(cmpr.compare("a", "b") > 0);
        assertTrue(cmp.compare("b", "a") > 0);
        assertTrue(cmpr.compare("b", "a") < 0);
    }

    public void testReverseComparator2() {
        Comparator<String> cmp = (s1, s2) -> s1.length() - s2.length();
        Comparator<String> cmpr = Comparators.reversed(cmp);

        // This test doesn't make sense because it depends on implementation details
//        assertEquals(Comparators.reversed(cmpr), cmp); // TODO: this can only work for JRE7 and above
        assertEquals(0, cmp.compare("abc", "def"));
        assertEquals(0, cmpr.compare("abc", "def"));
        assertTrue(cmp.compare("abcd", "def") > 0);
        assertTrue(cmpr.compare("abcd", "def") < 0);
        assertTrue(cmp.compare("abc", "defg") < 0);
        assertTrue(cmpr.compare("abc", "defg") > 0);
    }

    private <T> void assertComparison(Comparator<T> cmp, T less, T greater) {
        assertTrue(cmp.compare(less, greater) < 0, "less");
        assertTrue(cmp.compare(less, less) == 0, "equal");
        assertTrue(cmp.compare(greater, greater) == 0, "equal");
        assertTrue(cmp.compare(greater, less) > 0, "greater");
    }

    private static class People {
        final String firstName;
        final String lastName;
        final int age;

        People(String first, String last, int age) {
            firstName = first;
            lastName = last;
            this.age = age;
        }

        String getFirstName() { return firstName; }
        String getLastName() { return lastName; }
        int getAge() { return age; }
        long getAgeAsLong() { return (long) age; };
        double getAgeAsDouble() { return (double) age; };
    }

    private final People people[] = {
        new People("John", "Doe", 34),
        new People("Mary", "Doe", 30),
        new People("Maria", "Doe", 14),
        new People("Jonah", "Doe", 10),
        new People("John", "Cook", 54),
        new People("Mary", "Cook", 50),
        new People("Mary", null, 25),
        new People("John", null, 27)
    };

    public void testComparatorDefaultMethods() {
        Comparator<People> cmp = Comparators.comparing(People::getFirstName);
        Comparator<People> cmp2 = Comparators.comparing(People::getLastName);
        // reverseOrder
        assertComparison(Comparators.reversed(cmp), people[1], people[0]);
        // thenComparing(Comparator)
        assertComparison(Comparators.thenComparing(cmp, cmp2), people[0], people[1]);
        assertComparison(Comparators.thenComparing(cmp, cmp2), people[4], people[0]);
        // thenComparing(Function)
        assertComparison(Comparators.thenComparing(cmp, People::getLastName), people[0], people[1]);
        assertComparison(Comparators.thenComparing(cmp, People::getLastName), people[4], people[0]);
        // thenComparing(ToIntFunction)
        assertComparison(Comparators.thenComparingInt(cmp, People::getAge), people[0], people[1]);
        assertComparison(Comparators.thenComparingInt(cmp, People::getAge), people[1], people[5]);
        // thenComparing(ToLongFunction)
        assertComparison(Comparators.thenComparingLong(cmp, People::getAgeAsLong), people[0], people[1]);
        assertComparison(Comparators.thenComparingLong(cmp, People::getAgeAsLong), people[1], people[5]);
        // thenComparing(ToDoubleFunction)
        assertComparison(Comparators.thenComparingDouble(cmp, People::getAgeAsDouble), people[0], people[1]);
        assertComparison(Comparators.thenComparingDouble(cmp, People::getAgeAsDouble), people[1], people[5]);
    }

    public void testNullsFirst() {
        Comparator<String> strcmp = Comparators.nullsFirst(Comparators.naturalOrder());
        Comparator<People> cmp = Comparators.thenComparing(Comparators.comparing(People::getLastName, strcmp),
                People::getFirstName, strcmp);
        // Mary.null vs Mary.Cook - solve by last name
        assertComparison(cmp, people[6], people[5]);
        // John.null vs Mary.null - solve by first name
        assertComparison(cmp, people[7], people[6]);

        // More than one thenComparing
        strcmp = Comparators.nullsFirst(Comparators.thenComparing(Comparators.comparingInt(String::length),
                String.CASE_INSENSITIVE_ORDER));
        assertComparison(strcmp, null, "abc");
        assertComparison(strcmp, "ab", "abc");
        assertComparison(strcmp, "abc", "def");
        assertEquals(0, strcmp.compare("abc", "ABC"));

        // Ensure reverse still handle null properly
        Comparator<String> strcmp2 = Comparators.thenComparing(Comparators.reversed(strcmp), Comparators.naturalOrder());
        assertComparison(strcmp2, "abc", null);
        assertComparison(strcmp2, "abc", "ab");
        assertComparison(strcmp2, "def", "abc");
        assertComparison(strcmp2, "ABC", "abc");

        // Considering non-null values to be equal
        Comparator<String> blind = Comparators.nullsFirst(null);
        assertComparison(blind, null, "abc");
        assertEquals(0, blind.compare("abc", "def"));
        // reverse still consider non-null values to be equal
        strcmp = Comparators.reversed(blind);
        assertComparison(strcmp, "abc", null);
        assertEquals(0, strcmp.compare("abc", "def"));
        // chain with another comparator to compare non-nulls
        strcmp = Comparators.thenComparing(blind, Comparators.naturalOrder());
        assertComparison(strcmp, null, "abc");
        assertComparison(strcmp, "abc", "def");
    }

    public void testNullsLast() {
        Comparator<String> strcmp = Comparators.nullsLast(Comparators.naturalOrder());
        Comparator<People> cmp = Comparators.thenComparing(Comparators.comparing(People::getLastName, strcmp),
                                           Comparators.comparing(People::getFirstName, strcmp));
        // Mary.null vs Mary.Cook - solve by last name
        assertComparison(cmp, people[5], people[6]);
        // John.null vs Mary.null - solve by first name
        assertComparison(cmp, people[7], people[6]);

        // More than one thenComparing
        strcmp = Comparators.nullsLast(Comparators.thenComparing(Comparators.comparingInt(String::length),
                                                String.CASE_INSENSITIVE_ORDER));
        assertComparison(strcmp, "abc", null);
        assertComparison(strcmp, "ab", "abc");
        assertComparison(strcmp, "abc", "def");

        // Ensure reverse still handle null properly
        Comparator<String> strcmp2 = Comparators.thenComparing(Comparators.reversed(strcmp), Comparators.naturalOrder());
        assertComparison(strcmp2, null, "abc");
        assertComparison(strcmp2, "abc", "ab");
        assertComparison(strcmp2, "def", "abc");
        assertComparison(strcmp2, "ABC", "abc");

        // Considering non-null values to be equal
        Comparator<String> blind = Comparators.nullsLast(null);
        assertComparison(blind, "abc", null);
        assertEquals(0, blind.compare("abc", "def"));
        // reverse still consider non-null values to be equal
        strcmp = Comparators.reversed(blind);
        assertComparison(strcmp, null, "abc");
        assertEquals(0, strcmp.compare("abc", "def"));
        // chain with another comparator to compare non-nulls
        strcmp = Comparators.thenComparing(blind, Comparators.naturalOrder());
        assertComparison(strcmp, "abc", null);
        assertComparison(strcmp, "abc", "def");
    }

    public void testComposeComparator() {
        // Longer string in front
        Comparator<String> first = (s1, s2) -> s2.length() - s1.length();
        Comparator<String> second = Comparators.naturalOrder();
        Comparator<String> composed = Comparators.thenComparing(first, second);

        assertTrue(composed.compare("abcdefg", "abcdef") < 0);
        assertTrue(composed.compare("abcdef", "abcdefg") > 0);
        assertTrue(composed.compare("abcdef", "abcdef") == 0);
        assertTrue(composed.compare("abcdef", "ghijkl") < 0);
        assertTrue(composed.compare("ghijkl", "abcdefg") > 0);
    }

    public void testNulls() {
        try {
            Comparators.<String>naturalOrder().compare("abc", (String) null);
            fail("expected NPE with naturalOrder");
        } catch (NullPointerException npe) {}
        try {
            Comparators.<String>naturalOrder().compare((String) null, "abc");
            fail("expected NPE with naturalOrder");
        } catch (NullPointerException npe) {}

        try {
            Comparators.<String>reverseOrder().compare("abc", (String) null);
            fail("expected NPE with naturalOrder");
        } catch (NullPointerException npe) {}
        try {
            Comparators.<String>reverseOrder().compare((String) null, "abc");
            fail("expected NPE with naturalOrder");
        } catch (NullPointerException npe) {}

        try {
            Comparator<People> cmp = Comparators.comparing(null, Comparators.<String>naturalOrder());
            fail("comparing(null, cmp) should throw NPE");
        } catch (NullPointerException npe) {}
        try {
            Comparator<People> cmp = Comparators.comparing(People::getFirstName, null);
            fail("comparing(f, null) should throw NPE");
        } catch (NullPointerException npe) {}

        try {
            Comparator<People> cmp = Comparators.comparing(null);
            fail("comparing(null) should throw NPE");
        } catch (NullPointerException npe) {}
        try {
            Comparator<People> cmp = Comparators.comparingInt(null);
            fail("comparing(null) should throw NPE");
        } catch (NullPointerException npe) {}
        try {
            Comparator<People> cmp = Comparators.comparingLong(null);
            fail("comparing(null) should throw NPE");
        } catch (NullPointerException npe) {}
        try {
            Comparator<People> cmp = Comparators.comparingDouble(null);
            fail("comparing(null) should throw NPE");
        } catch (NullPointerException npe) {}
    }
}
