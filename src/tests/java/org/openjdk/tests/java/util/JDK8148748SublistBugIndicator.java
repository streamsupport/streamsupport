package org.openjdk.tests.java.util;

import java8.util.Spliterators;

final class JDK8148748SublistBugIndicator {

    static final boolean BUG_IS_PRESENT = hasJDK8148748SublistBug();

    /**
     * The Java 8 Spliterator for ArrayList.sublist() is currently not
     * late-binding (https://bugs.openjdk.java.net/browse/JDK-8148748). We'd get
     * test failures because of this when we run on Java 8 with Spliterator
     * delegation enabled. This bug has been fixed in Java 9 ea build 112, so we
     * require this as the minimum version for test runs on Java 9 (otherwise
     * we'd also get failures on Java 9 since we do not test for class.version
     * 53.0 here).
     * 
     * @return {@code true} on Java 8 when Spliterator delegation hasn't been
     *         disabled, {@code false} otherwise.
     */
    private static boolean hasJDK8148748SublistBug() {
        // a) must have exactly major version number 52 (Java 8)
        String ver = System.getProperty("java.class.version", "45");
        if (ver != null && ver.length() >= 2) {
            ver = ver.substring(0, 2);
            if ("52".equals(ver)) {
                // b) Spliterator delegation must not be disabled
                return isSpliteratorDelegationEnabled();
            }
        }
        return isOpenJDKAndroid() && isSpliteratorDelegationEnabled();
    }

    private static boolean isSpliteratorDelegationEnabled() {
        String s = System.getProperty(Spliterators.class.getName()
                + ".jre.delegation.enabled", Boolean.TRUE.toString());
        return (s == null)
                || s.trim().equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * Are we running on Android 7+ ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    private static boolean isOpenJDKAndroid() {
        return isClassPresent("android.util.DisplayMetrics")
                && isClassPresent("android.opengl.GLES32$DebugProc");
    }

    private static boolean isClassPresent(String name) {
        Class<?> clazz = null;
        try {
            // avoid <clinit> which triggers a lot of JNI code in the case
            // of android.util.DisplayMetrics
            clazz = Class.forName(name, false,
                    JDK8148748SublistBugIndicator.class.getClassLoader());
        } catch (Throwable notPresent) {
            // ignore
        }
        return clazz != null;
    }

    private JDK8148748SublistBugIndicator() {
        throw new AssertionError();
    }
}
