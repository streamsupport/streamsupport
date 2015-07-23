/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.other.tests.chm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import java8.util.concurrent.CompletableFuture;
import java8.util.concurrent.CompletionException;
import java8.util.concurrent.ThreadLocalRandom;
import java8.util.function.BiConsumer;
import java8.util.function.Function;
import java8.util.function.IntFunction;
import java8.util.function.Supplier;
import java8.util.stream.IntStreams;
import java8.util.stream.Stream;

import org.testng.annotations.Test;

/**
 * @test
 * @bug 8028564
 * @run testng ConcurrentAssociateTest
 * @summary Test that association operations, such as put and putIfAbsent,
 * place entries in the map
 */
public class ConcurrentAssociateTest {

    // The number of entries for each thread to place in a map
    private static final int N = Integer.getInteger("n", 512);
    // The number of iterations of the test
    private static final int I = Integer.getInteger("i", 256);
    // Bound concurrency to avoid degenerate performance (JDK-8081734)
    private static final int availableProcessors = Math.min(Runtime.getRuntime().availableProcessors(), 32);

    public static void main(String[] args) throws Exception {
        System.out.println("availableProcessors: " + availableProcessors);

        ConcurrentAssociateTest tester = new ConcurrentAssociateTest();

        // put
        try {
            tester.testPut();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // putIfAbsent
        try {
            tester.testPutIfAbsent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // putAll
        try {
            tester.testPutAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    // Object to be placed in the concurrent map
    static class X {
        // Limit the hash code to trigger collisions
        int hc = ThreadLocalRandom.current().nextInt(1, 9);

        public int hashCode() { return hc; }
    }

    @SuppressWarnings("serial")
    static class AssociationFailure extends RuntimeException {
        AssociationFailure(String message) {
            super(message);
        }
    }

    @Test
    public void testPut() {
        test("ConcurrentHashMap.put",
                new BiConsumer<ConcurrentMap<Object, Object>, Object>() {
                    @Override
                    public void accept(ConcurrentMap<Object, Object> m, Object o) {
                        m.put(o, o);
                    }
                });
    }

    @Test
    public void testPutIfAbsent() {
        test("ConcurrentHashMap.putIfAbsent",
                new BiConsumer<ConcurrentMap<Object, Object>, Object>() {
                    @Override
                    public void accept(ConcurrentMap<Object, Object> m, Object o) {
                        m.putIfAbsent(o, o);
                    }
                });
    }

    @Test
    public void testPutAll() {
        test("ConcurrentHashMap.putAll",
                new BiConsumer<ConcurrentMap<Object, Object>, Object>() {
                    @Override
                    public void accept(ConcurrentMap<Object, Object> m, Object o) {
                        Map<Object, Object> hm = new HashMap<Object, Object>();
                        hm.put(o, o);
                        m.putAll(hm);
                    }
                });
    }

    private static void test(String desc, BiConsumer<ConcurrentMap<Object, Object>, Object> associator) {
        for (int i = 0; i < I; i++) {
            testOnce(desc, associator);
        }
    }

    private static void testOnce(final String desc, final BiConsumer<ConcurrentMap<Object, Object>, Object> associator) {
        final ConcurrentHashMap<Object, Object> m = new ConcurrentHashMap<Object, Object>();
        final CountDownLatch s = new CountDownLatch(1);

        final Supplier<Runnable> putter = new Supplier<Runnable>() {
            @Override
            public Runnable get() {
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            s.await();
                        } catch (InterruptedException e) {
                        }

                        for (int i = 0; i < N; i++) {
                            Object o = new X();
                            associator.accept(m, o);
                            if (!m.containsKey(o)) {
                                throw new AssociationFailure(desc + " failed: entry does not exist");
                            }
                        }
                    }
                };
            }
        };

        Stream<CompletableFuture<Void>> putters = IntStreams.range(0, availableProcessors)
                .mapToObj(new IntFunction<Runnable>() {
                    public Runnable apply(int i) {
                        return putter.get();
                    }})
                .map(new Function<Runnable, CompletableFuture<Void>>() {
                    @Override
                    public CompletableFuture<Void> apply(Runnable runnable) {
                        return CompletableFuture.runAsync(runnable);
                    }
                });

        CompletableFuture<Void> all = CompletableFuture.allOf(
                putters.toArray(new IntFunction<CompletableFuture<Void>[]>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public CompletableFuture<Void>[] apply(int size) {
                        return (CompletableFuture<Void>[]) new CompletableFuture[size];
                    }
                }));

        // Trigger the runners to start
        s.countDown();
        try {
            all.join();
        } catch (CompletionException e) {
            Throwable t = e.getCause();
            if (t instanceof AssociationFailure) {
                throw (AssociationFailure) t;
            } else {
                throw e;
            }
        }
    }
}
