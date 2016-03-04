package org.openjdk.tests.java.util;

import java8.util.Spliterators;

final class JDK8148748SublistBugIndicator {

    static final boolean BUG_IS_PRESENT = hasJDK8148748SublistBug();

    /**
     * The Java 8/9 Spliterator for ArrayList.sublist() is currently not
     * late-binding (https://bugs.openjdk.java.net/browse/JDK-8148748). We'd get
     * test failures because of this when we run on Java 8/9 with Spliterator
     * delegation enabled.
     * 
     * @return {@code true} on Java 8/9 when Spliterator delegation hasn't been
     *         disabled, {@code false} otherwise.
     */
    private static boolean hasJDK8148748SublistBug() {
        // a) must have at least major version number 52 (Java 8 or higher)
        String ver = System.getProperty("java.class.version", "45");
        if (ver != null && ver.length() >= 2) {
            ver = ver.substring(0, 2);
            if ("52".compareTo(ver) > 0) {
                return false;
            }
        }
        // b) Spliterator delegation must not be disabled
        String s = System.getProperty(Spliterators.class.getName() + ".jre.delegation.enabled", Boolean.TRUE.toString());
        return (s == null) || s.trim().equalsIgnoreCase(Boolean.TRUE.toString());
    }

    private JDK8148748SublistBugIndicator() {
        throw new AssertionError();
    }
}
