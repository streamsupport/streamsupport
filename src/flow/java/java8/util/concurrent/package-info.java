/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Backport of the new Java 9 (JEP 266) reactive-streams Flow and
 * SubmissionPublisher API for Java 6 to 8 and Android developers.
 * 
 * See <a href="http://openjdk.java.net/jeps/266">JEP 266: More Concurrency Updates</a>.
 * 
 * <h2 id="MemoryVisibility">Memory Consistency Properties</h2>
 *
 * <a href=
 * "https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.5">
 * Chapter 17 of <cite>The Java&trade; Language Specification</cite></a> defines
 * the <i>happens-before</i> relation on memory operations such as reads and
 * writes of shared variables. The results of a write by one thread are
 * guaranteed to be visible to a read by another thread only if the write
 * operation <i>happens-before</i> the read operation. The {@code synchronized}
 * and {@code volatile} constructs, as well as the {@code Thread.start()} and
 * {@code Thread.join()} methods, can form <i>happens-before</i> relationships.
 * In particular:
 *
 * <ul>
 * <li>Each action in a thread <i>happens-before</i> every action in that thread
 * that comes later in the program's order.
 *
 * <li>An unlock ({@code synchronized} block or method exit) of a monitor
 * <i>happens-before</i> every subsequent lock ({@code synchronized} block or
 * method entry) of that same monitor. And because the <i>happens-before</i>
 * relation is transitive, all actions of a thread prior to unlocking
 * <i>happen-before</i> all actions subsequent to any thread locking that
 * monitor.
 *
 * <li>A write to a {@code volatile} field <i>happens-before</i> every
 * subsequent read of that same field. Writes and reads of {@code volatile}
 * fields have similar memory consistency effects as entering and exiting
 * monitors, but do <em>not</em> entail mutual exclusion locking.
 *
 * <li>A call to {@code start} on a thread <i>happens-before</i> any action in
 * the started thread.
 *
 * <li>All actions in a thread <i>happen-before</i> any other thread
 * successfully returns from a {@code join} on that thread.
 *
 * </ul>
 * 
 * @since 9
 */
package java8.util.concurrent;