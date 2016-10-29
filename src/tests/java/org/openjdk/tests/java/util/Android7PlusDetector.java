package org.openjdk.tests.java.util;

final class Android7PlusDetector {

    static final boolean IS_OPENJDK_ANDROID = isOpenJDKAndroid();

    /**
     * Are we running on Android 7+ ?
     * 
     * @return {@code true} if yes, otherwise {@code false}.
     */
    static boolean isOpenJDKAndroid() {
        return isClassPresent("android.util.DisplayMetrics")
                && isClassPresent("android.opengl.GLES32$DebugProc");
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
