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
package org.openjdk.other.tests.java.util.stream;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class StreamParSeqTest {

    public void testParSeq() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Spliterator<Integer> spliter = Spliterators.spliteratorUnknownSize(list.iterator(), 0);
        Stream<Integer> stream = StreamSupport.stream(spliter, false);

//        Stream<Integer> s = Arrays.asList(1, 2, 3, 4).stream().parallel();
        Stream<Integer> s = stream.parallel();
        assertTrue(s.isParallel());

        s = s.sequential();
        assertFalse(s.isParallel());

        s = s.sequential();
        assertFalse(s.isParallel());

        s = s.parallel();
        assertTrue(s.isParallel());

        s = s.parallel();
        assertTrue(s.isParallel());
    }
}
