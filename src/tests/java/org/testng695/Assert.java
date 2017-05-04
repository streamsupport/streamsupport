package org.testng695;

/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order.
 * The order is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public final class Assert {

    private Assert() {
        throw new AssertionError();
    }

    /**
     * Asserts that {@code runnable} throws an exception when invoked. If it
     * does not, an {@link AssertionError} is thrown.
     *
     * @param runnable
     *            A function that is expected to throw an exception when invoked
     * @since 6.9.5
     */
    public static void assertThrows(ThrowingRunnable runnable) {
        assertThrows(Throwable.class, runnable);
    }

    /**
     * Asserts that {@code runnable} throws an exception of type
     * {@code throwableClass} when executed. If it does not throw an exception,
     * an {@link AssertionError} is thrown. If it throws the wrong type of
     * exception, an {@code AssertionError} is thrown describing the mismatch;
     * the exception that was actually thrown can be obtained by calling
     * {@link AssertionError#getCause}.
     *
     * @param throwableClass
     *            the expected type of the exception
     * @param runnable
     *            A function that is expected to throw an exception when invoked
     * @since 6.9.5
     */
    public static <T extends Throwable> void assertThrows(
            Class<T> throwableClass, ThrowingRunnable runnable) {
        expectThrows(throwableClass, runnable);
    }

    private static <T extends Throwable> void assertThrows(
            Class<T> throwableClass, ThrowingRunnable runnable, String message) {
        try {
            assertThrows(throwableClass, runnable);
        } catch (AssertionError e) {
            AssertionError err = new AssertionError(String.format("%s%n%s",
                    ((null != message) ? message : ""), e.getMessage()));
            err.initCause(e);
            throw err;
        }
    }

    public static void assertThrowsNPE(ThrowingRunnable r, String message) {
        assertThrows(NullPointerException.class, r, message);
    }

    public static void assertThrowsNPE(ThrowingRunnable r) {
        assertThrows(NullPointerException.class, r);
    }

    /**
     * Asserts that {@code runnable} throws an exception of type
     * {@code throwableClass} when executed and returns the exception. If
     * {@code runnable} does not throw an exception, an {@link AssertionError}
     * is thrown. If it throws the wrong type of exception, an
     * {@code AssertionError} is thrown describing the mismatch; the exception
     * that was actually thrown can be obtained by calling
     * {@link AssertionError#getCause}.
     *
     * @param throwableClass
     *            the expected type of the exception
     * @param runnable
     *            A function that is expected to throw an exception when invoked
     * @return The exception thrown by {@code runnable}
     * @since 6.9.5
     */
    public static <T extends Throwable> T expectThrows(Class<T> throwableClass,
            ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (throwableClass.isInstance(t)) {
                return throwableClass.cast(t);
            } else {
                String mismatchMessage = String.format(
                        "Expected %s to be thrown, but %s was thrown",
                        throwableClass.getSimpleName(), t.getClass()
                                .getSimpleName());

                AssertionError e = new AssertionError(mismatchMessage);
                e.initCause(t);
                throw e;
            }
        }
        String message = String.format(
                "Expected %s to be thrown, but nothing was thrown",
                throwableClass.getSimpleName());
        throw new AssertionError(message);
    }
}
