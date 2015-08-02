/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util.stream;

import java8.util.function.Consumer;

interface SinkConsumer<T> extends Consumer<T> {
    // compiler kludge
}
