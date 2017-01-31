/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java8.util.concurrent.CompletableFuture;

import junit.framework.Test;
import junit.framework.TestSuite;

@org.testng.annotations.Test
public class ForkJoinPool9Test extends JSR166TestCase {
// CVS rev. 1.2

//    public static void main(String[] args) {
//        main(suite(), args);
//    }

    public static Test suite() {
        return new TestSuite(ForkJoinPool9Test.class);
    }

    /**
     * Check handling of common pool thread context class loader
     */
    public void testCommonPoolThreadContextClassLoader() throws Throwable {
        if (!testImplementationDetails || isOpenJDKAndroid()) return;
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        boolean haveSecurityManager = (System.getSecurityManager() != null);
        CompletableFuture.runAsync(
            () -> {
                assertSame(systemClassLoader,
                           Thread.currentThread().getContextClassLoader());
                assertSame(systemClassLoader,
                           getContextClassLoader(Thread.currentThread()));
                if (haveSecurityManager)
                    assertThrows(
                        SecurityException.class,
                        () -> System.getProperty("foo"),
                        () -> Thread.currentThread().setContextClassLoader(null));

                // TODO ?
//                 if (haveSecurityManager
//                     && Thread.currentThread().getClass().getSimpleName()
//                     .equals("InnocuousForkJoinWorkerThread"))
//                     assertThrows(SecurityException.class, /* ?? */);
            }).join();
    }

    static ClassLoader getContextClassLoader(Thread thread) {
        return (ClassLoader) U.getObject(thread, CCL_OFF);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long CCL_OFF;
    static {
        try {
            CCL_OFF = U.objectFieldOffset(Thread.class
                    .getDeclaredField("contextClassLoader"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
