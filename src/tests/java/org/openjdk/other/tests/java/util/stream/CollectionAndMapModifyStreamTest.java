/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.other.tests.java.util.stream;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java8.util.*;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import java8.util.function.Supplier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests laziness of stream operations -- mutations to the source after the stream() but prior to terminal operations
 * are reflected in the stream contents.
 */
@Test
public class CollectionAndMapModifyStreamTest {

    @DataProvider(name = "maps")
    public Object[][] createMaps() {
        final Map<Integer, Integer> content = new HashMap<Integer, Integer>();
        for (int i = 0; i < 10; i++) {
            content.put(i, i);
        }

        Map<String, Supplier<Map<Integer, Integer>>> maps = new HashMap<String, Supplier<Map<Integer, Integer>>>();

        maps.put(HashMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new HashMap<Integer, Integer>(content);
            }
        });
        maps.put(HashMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new LinkedHashMap<Integer, Integer>(content);
            }
        });
        maps.put(IdentityHashMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new IdentityHashMap<Integer, Integer>(content);
            }
        });
        maps.put(WeakHashMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new WeakHashMap<Integer, Integer>(content);
            }
        });

        maps.put(TreeMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new TreeMap<Integer, Integer>(content);
            }
        });
        maps.put(TreeMap.class.getName() + ".descendingMap()", new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new TreeMap<Integer, Integer>(content).descendingMap();
            }
        });

        // The following are not lazy
//        maps.put(TreeMap.class.getName() + ".descendingMap().descendingMap()", () -> new TreeMap<>(content).descendingMap().descendingMap());
//        maps.put(TreeMap.class.getName() + ".headMap()", () -> new TreeMap<>(content).headMap(content.size() - 1));
//        maps.put(TreeMap.class.getName() + ".descendingMap().headMap()", () -> new TreeMap<>(content).descendingMap().tailMap(content.size() - 1, false));

        // Concurrent collections

        maps.put(ConcurrentHashMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new ConcurrentHashMap<Integer, Integer>(content);
            }
        });
        maps.put(ConcurrentSkipListMap.class.getName(), new Supplier<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> get() {
                return new ConcurrentSkipListMap<Integer, Integer>(content);
            }
        });

        Object[][] params = new Object[maps.size()][];
        int i = 0;
        for (Map.Entry<String, Supplier<Map<Integer, Integer>>> e : maps.entrySet()) {
            params[i++] = new Object[]{e.getKey(), e.getValue()};

        }

        return params;
    }

    @Test(dataProvider = "maps")
    public void testMapEntriesSizeRemove(String name, Supplier<Map<Integer, Integer>> c) {
        testEntrySetSizeRemove(name + " entry set", c.get().entrySet());
    }

    @Test(dataProvider = "maps")
    public void testMapEntriesSizeRemove2(String name, Supplier<Map<Integer, Integer>> c) {
        testEntrySetSizeRemove2(name + " entry set", c.get().entrySet());
    }

    private void testEntrySetSizeRemove(String name, final Set<Map.Entry<Integer, Integer>> c) {
    	Iterator<Map.Entry<Integer, Integer>> testIter = c.iterator();
        Map.Entry<Integer, Integer> first = testIter.next();
        assertTrue(c.remove(first));

        Supplier<Spliterator<Map.Entry<Integer, Integer>>> late = new Supplier<Spliterator<Map.Entry<Integer, Integer>>>() {
            @Override
            public Spliterator<Map.Entry<Integer, Integer>> get() {
                return Spliterators.spliterator(c, 0);
            }
        };

        Stream<Map.Entry<Integer, Integer>> stream = StreamSupport.stream(late, 0, false);

        Map.Entry<Integer, Integer> second = c.iterator().next();
        assertTrue(c.remove(second));
        Object[] result = stream.toArray();
        assertEquals(result.length, c.size());
    }

    private void testEntrySetSizeRemove2(String name, Set<Map.Entry<Integer, Integer>> c) {
        Map.Entry<Integer, Integer> first = c.iterator().next();
        assertTrue(c.remove(first));
        
        Stream<Map.Entry<Integer, Integer>> stream = StreamSupport.stream(c, 0, false);

        Map.Entry<Integer, Integer> second = c.iterator().next();
        assertTrue(c.remove(second));
        Object[] result = stream.toArray();
        assertEquals(result.length, c.size());
    }
}
