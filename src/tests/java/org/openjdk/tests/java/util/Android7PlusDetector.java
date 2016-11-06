package org.openjdk.tests.java.util;

import java.lang.reflect.Field;

final class Android7PlusDetector {

    static final boolean IS_OPENJDK_ANDROID = isOpenJDKAndroid();
    static final boolean IS_ANDROID_API24 = isAndroidAPI24();
    static final boolean IS_HARMONY_ANDROID = isHarmonyAndroid();

    private static final String DISPLAY_METRICS = "android.util.DisplayMetrics";
    private static final String GLES32 = "android.opengl.GLES32$DebugProc";

    /**
     * Are we running on Android 7+ ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isOpenJDKAndroid() {
        return isClassPresent(DISPLAY_METRICS) && isClassPresent(GLES32);
    }

    /**
     * Are we running on an Apache Harmony based Android ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isHarmonyAndroid() {
        return isClassPresent(DISPLAY_METRICS) && !isClassPresent(GLES32);
    }

    /**
     * Are we running on Android API level 24 ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isAndroidAPI24() {
        if (IS_OPENJDK_ANDROID) {
            Field field = null;
            try {
                Class<?> clazz = Class.forName(DISPLAY_METRICS);
                // DENSITY_340 has been added in API level 25
                try {
                    field = clazz.getDeclaredField("DENSITY_340");
                } catch (NoSuchFieldException e) {
                    return true;
                }
            } catch (Throwable ignore) {
            }
            return field == null;
        }
        return false;
    }

    private static boolean isClassPresent(String name) {
        Class<?> clazz = null;
        try {
            // avoid <clinit> which triggers a lot of JNI code in the case
            // of android.util.DisplayMetrics
            clazz = Class.forName(name, false,
                    Android7PlusDetector.class.getClassLoader());
        } catch (Throwable notPresent) {
            // ignore
        }
        return clazz != null;
    }

    private Android7PlusDetector() {
    }
}
