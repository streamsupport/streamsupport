package org.testng695;

/**
 * This interface facilitates the use of {@link Assert#expectThrows} from Java 8. It allows
 * method references to both void and non-void methods to be passed directly into
 * expectThrows without wrapping, even if they declare checked exceptions.
 * <p/>
 * This interface is not meant to be implemented directly.
 */
public interface ThrowingRunnable {
    void run() throws Throwable;
}
