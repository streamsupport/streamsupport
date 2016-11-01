package org.openjdk.tests.java.util;

import java.lang.reflect.Method;

import java8.util.Spliterator;
import java8.util.Spliterators;

/**
 * Really ugly hack to relax the Spliterator tests for Android 7.0 LinkedHashMap
 * a little.
 * <p>
 * In Android 7.0, the Spliterators returned from LinkedHashMap's collection
 * views (entrySet(), keySet() and values()) report Spliterator.ORDERED (as they
 * should) but actually they are not ORDERED. If
 * {@link #hasAndroidAPI24LHMBug(Spliterator)} returns {@code true} we only test
 * for unordered equality of the Spliterator contents against the expected
 * contents.
 * <p>
 * Cf. https://sourceforge.net/p/streamsupport/tickets/240/
 */
final class DelegationActive {

    static final boolean IS_SPLITERATOR_DELEGATION_ENABLED = isSpliteratorDelegationEnabled();

    private static final String UNMODIFIABLE_ENTRYSET_SPLITERATOR_CLASSNAME = "java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntrySetSpliterator";

    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final Method GET_DELEGATEE_METHOD = getDelegateeAccessMethod();
    private static final long UESS_SPLITERATOR_OFF = getUnmodifiableEntrySetSpliteratorOffset();

    private static Method getDelegateeAccessMethod() {
        Method getDelegatee = null;
        try {
            Class<?> clazz = Class.forName("java8.util.DelegatingSpliterator");
            getDelegatee = clazz.getDeclaredMethod("getDelegatee");
            getDelegatee.setAccessible(true);
        } catch (Throwable ignore) {
        }
        return getDelegatee;
    }

    private static long getUnmodifiableEntrySetSpliteratorOffset() {
        long spliteratorFieldOffset = -1L;
        try {
            Class<?> uessClass = Class
                    .forName(UNMODIFIABLE_ENTRYSET_SPLITERATOR_CLASSNAME);
            spliteratorFieldOffset = U.objectFieldOffset(uessClass
                    .getDeclaredField("s"));
        } catch (Throwable ignore) {
        }
        return spliteratorFieldOffset;
    }

    static boolean isSpliteratorDelegationEnabled() {
        String s = System.getProperty(Spliterators.class.getName()
                + ".jre.delegation.enabled", Boolean.TRUE.toString());
        return (s == null)
                || s.trim().equalsIgnoreCase(Boolean.TRUE.toString());
    }

    // In Android 7.0, the Spliterators returned from LinkedHashMap's collection
    // views (entrySet(), keySet() and values()) report Spliterator.ORDERED (as
    // they should) but actually they are not ORDERED.
    // see https://sourceforge.net/p/streamsupport/tickets/240/
    static boolean hasAndroidAPI24LHMBug(Spliterator<?> spliterator) {
        if (Android7PlusDetector.IS_ANDROID_API24
                && IS_SPLITERATOR_DELEGATION_ENABLED && spliterator != null) {
            if (spliterator.hasCharacteristics(Spliterator.ORDERED)) {
                String implName = null;
                Object impl = getDelegatee(spliterator);
                if (impl != null) {
                    implName = impl.getClass().getName();
                }
                if (UNMODIFIABLE_ENTRYSET_SPLITERATOR_CLASSNAME
                        .equals(implName)) {
                    // This again might delegate to one of HashMap's collection
                    // view Spliterators. We check this by accessing
                    // its "s" member by reflection.
                    implName = getEmbeddedSpliteratorImplementationClassName(impl);
                    return isHashMapCollectionViewSpliterator(implName);
                }
                return isHashMapCollectionViewSpliterator(implName);
            }
        }
        return false;
    }

    private static boolean isHashMapCollectionViewSpliterator(String implName) {
        if (implName != null) {
            if ("java.util.HashMap$ValueSpliterator".equals(implName)
                    || "java.util.HashMap$KeySpliterator".equals(implName)
                    || "java.util.HashMap$EntrySpliterator".equals(implName)) {
                return true;
            }
        }
        return false;
    }

    private static Object getDelegatee(Spliterator<?> spliterator) {
        try {
            if (spliterator == null
                    || GET_DELEGATEE_METHOD == null
                    || !"java8.util.DelegatingSpliterator".equals(spliterator
                            .getClass().getName())) {
                return null;
            }
            return GET_DELEGATEE_METHOD.invoke(spliterator);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static String getEmbeddedSpliteratorImplementationClassName(
            Object spliterator) {
        String implName = null;
        if (UESS_SPLITERATOR_OFF >= 0L && spliterator != null) {
            try {
                Object embedded = U
                        .getObject(spliterator, UESS_SPLITERATOR_OFF);
                if (embedded != null) {
                    implName = embedded.getClass().getName();
                }
            } catch (Throwable ignore) {
            }
        }
        return implName;
    }

    private DelegationActive() {
    }
}
