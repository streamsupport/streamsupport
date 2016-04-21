package java8.util.stream;

final class AndroidNDetector {
    // is this Android? (defaults to false)
    private static final boolean IS_ANDROID = isClassPresent("android.util.DisplayMetrics");

    // is this Android N developer preview? (defaults to false)
    private static final boolean IS_ANDROID_N = IS_ANDROID && isClassPresent("java.util.function.Function");

    // see https://sourceforge.net/p/streamsupport/tickets/149/?limit=25&page=1#145a
    // and https://sourceforge.net/p/streamsupport/tickets/191/#6ff8
    static final int MAX_SIZE = 725;

    /**
     * The current Android N developer previews (1 & 2) have
     * a bug that can elicit StackOverflowErrors in a few tests.
     * We workaround this issue by reducing the test size when
     * we are on Android N.
     * @return {@code true} if we are running on Android N.
     */
    static boolean hasStackOverflowErrorProblem() {
        return IS_ANDROID_N;
    }

    /**
     * Used to detect the presence or absence of android.util.DisplayMetrics
     * and other classes. Gets employed when we need to establish whether we
     * are running on Android and, if yes, whether the version of Android is
     * based on Apache Harmony or on OpenJDK.
     * 
     * @param name
     *            fully qualified class name
     * @return {@code true} if class is present, otherwise {@code false}.
     */
    private static boolean isClassPresent(String name) {
        Class<?> clazz = null;
        try {
            // avoid <clinit> which triggers a lot of JNI code in the case
            // of android.util.DisplayMetrics
            clazz = Class.forName(name, false, AndroidNDetector.class.getClassLoader());
        } catch (Throwable notPresent) {
            // ignore
        }
        return clazz != null;
    }

    private AndroidNDetector() {
    }
}
