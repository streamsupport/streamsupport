package org.openjdk.tests.java.util;

import java8.util.Spliterators;

final class DelegationActive {

    static final boolean IS_SPLITERATOR_DELEGATION_ENABLED = isSpliteratorDelegationEnabled();

    static boolean isSpliteratorDelegationEnabled() {
        String s = System.getProperty(Spliterators.class.getName()
                + ".jre.delegation.enabled", Boolean.TRUE.toString());
        return (s == null)
                || s.trim().equalsIgnoreCase(Boolean.TRUE.toString());
    }

    private DelegationActive() {
    }
}
