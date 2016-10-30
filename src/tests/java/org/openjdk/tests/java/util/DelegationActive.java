package org.openjdk.tests.java.util;

import java.lang.reflect.Method;

import java8.util.Spliterator;
import java8.util.Spliterators;

final class DelegationActive {

    static final boolean IS_SPLITERATOR_DELEGATION_ENABLED = isSpliteratorDelegationEnabled();

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
    static boolean hasAndroidAPI24Bug(Spliterator<?> spliterator) {
        if (Android7PlusDetector.IS_ANDROID_API24
                && IS_SPLITERATOR_DELEGATION_ENABLED && spliterator != null) {
            if (spliterator.hasCharacteristics(Spliterator.ORDERED)) {
                String impl = getDelegateeImplementationName(spliterator);
                if ("java.util.HashMap$ValueSpliterator".equals(impl)
                        || "java.util.HashMap$KeySpliterator".equals(impl)
                        || "java.util.HashMap$EntrySpliterator".equals(impl)) {
                    return true;
                }
            }
        }
        return false;
    }

    static String getDelegateeImplementationName(Spliterator<?> spliterator) {
        try {
            if (spliterator == null
                    || !"java8.util.DelegatingSpliterator".equals(spliterator
                            .getClass().getName())) {
                return null;
            }
            return (String) GET_IMPL_NAME.invoke(spliterator);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private DelegationActive() {
    }

    private static final Method GET_IMPL_NAME;
    static {
        try {
            Class<?> clazz = Class.forName("java8.util.DelegatingSpliterator");
            Method getImplName = clazz
                    .getDeclaredMethod("getDelegateeImplementationName");
            getImplName.setAccessible(true);
            GET_IMPL_NAME = getImplName;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
