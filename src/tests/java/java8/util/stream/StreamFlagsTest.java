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
package java8.util.stream;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import org.testng.annotations.Test;

import java8.util.stream.Stream;
import java8.util.stream.StreamOpFlag;

import static java8.util.stream.StreamOpFlag.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * StreamFlagsTest
 *
 * @author Brian Goetz
 */
@Test
public class StreamFlagsTest {
    Stream<String> arrayList = StreamSupport.stream(new ArrayList<String>());
    Stream<String> linkedList = StreamSupport.stream(new LinkedList<String>());
    Stream<String> hashSet = StreamSupport.stream(new HashSet<String>());
    Stream<String> treeSet = StreamSupport.stream(new TreeSet<String>());
    Stream<String> linkedHashSet = StreamSupport.stream(new LinkedHashSet<String>());
    Stream<String> repeat = RefStreams.generate(() -> "");

    Stream<?>[] streams = { arrayList, linkedList, hashSet, treeSet, linkedHashSet, repeat };

    private void assertFlags(int value, EnumSet<StreamOpFlag> setFlags, EnumSet<StreamOpFlag> clearFlags) {
        for (StreamOpFlag flag : setFlags)
            assertTrue(flag.isKnown(value));
        for (StreamOpFlag flag : clearFlags)
            assertTrue(!flag.isKnown(value));
    }

    public void testStreamSupportGenerate() {

        Stream<String> repeat = RefStreams.generate(() -> "");

        assertFlags(OpTestCase.getStreamFlags(repeat),
                    EnumSet.noneOf(StreamOpFlag.class),
                    EnumSet.of(DISTINCT, SORTED, SHORT_CIRCUIT));
    }

    public void testFilter() {
        for (Stream<?> s : streams) {
            int baseFlags = OpTestCase.getStreamFlags(s);
            int filteredFlags = OpTestCase.getStreamFlags(s.filter((Object e) -> true));
            int expectedFlags = baseFlags & ~SIZED.set();

            assertEquals(filteredFlags, expectedFlags);
        }
    }
}
