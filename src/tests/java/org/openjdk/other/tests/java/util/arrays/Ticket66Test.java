package org.openjdk.other.tests.java.util.arrays;

import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java8.util.J8Arrays;

import org.testng.annotations.Test;

public class Ticket66Test {

    public static void main(String[] args) {
        int[] a = new int[10000];

        int period = 67; // java.util.DualPivotQuicksort.MAX_RUN_COUNT

        for (int m = period - 2; m <= period + 2; m++) {
            System.out.println("m: " + m);
            descending(a, m);

            int[] test = toIntArray(a);
            J8Arrays.parallelSort(test);

            checkSorted(test);
            System.out.println(Arrays.toString(Arrays.copyOf(test, 100)));
        }
    }

    @Test
    public static void test() {
        Exception ex = null;
        try {
            main(new String[]{"-shortrun"});
        } catch (Exception t) {
            ex = t;
        }
        String msg = "";
        if (ex != null) {
            msg = ex.getMessage();
        }
        assertNull(ex, "Test FAILED with " + msg);
    }

    private static void descending(int[] a, int m) {
        int period = a.length / m;
        int v = -1;
        int i = 0;

        for (int k = 0; k < m; k++) {
            v = -1;
            for (int p = 0; p < period; p++) {
                a[i++] = v--;
            }
        }
        for (int j = i; j < a.length - 1; j++) {
            a[j] = v--;
        }
        a[a.length - 1] = 0;
    }

    private static void checkSorted(long[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                failedSort(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private static void checkSorted(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                failedSort(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private static long[] toLongArray(int[] a) {
        long[] b = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = (long) a[i];
        }
        return b;    
    }

    private static int[] toIntArray(int[] a) {
        return a.clone();
    }

    private static void failedSort(int index, String value1, String value2) {
        throw new RuntimeException("TEST FAILED - Array is not sorted at " + index + "-th position: " +
                value1 + " and " + value2);
    }
}
