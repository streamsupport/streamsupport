package org.openjdk.tests.java.util;

final class Android7PlusDetector {

    static final boolean IS_OPENJDK_ANDROID = isOpenJDKAndroid();
    static final boolean IS_HARMONY_ANDROID = isHarmonyAndroid();
    static final boolean IS_ANDROID_O = isAndroidO();
    static final boolean IS_ANDROID_7X = IS_OPENJDK_ANDROID && !IS_ANDROID_O; 

    private static final String DISPLAY_METRICS = "android.util.DisplayMetrics";
    private static final String GLES32 = "android.opengl.GLES32$DebugProc";
    private static final String TIME_API = "java.time.DateTimeException";

    /**
     * Are we running on Android ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isAndroid() {
        return isClassPresent(DISPLAY_METRICS);
    }

    /**
     * Are we running on Android 7+ ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isOpenJDKAndroid() {
        return isAndroid() && isClassPresent(GLES32);
    }

    /**
     * Are we running on an Apache Harmony based Android ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isHarmonyAndroid() {
        return isAndroid() && !isClassPresent(GLES32);
    }

    /**
     * Are we running on Android O or above?
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isAndroidO() {
        return isAndroid() && isClassPresent(TIME_API);
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
