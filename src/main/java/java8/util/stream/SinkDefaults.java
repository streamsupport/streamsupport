/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java8.util.stream;

final class SinkDefaults {

	static final class OfInt {

        static void accept(Sink.OfInt this_, Integer i) {
            this_.accept(i.intValue());
        }

		private OfInt() {
			throw new AssertionError();
		}
	}

	static final class OfLong {

        static void accept(Sink.OfLong this_, Long i) {
            this_.accept(i.longValue());
        }

		private OfLong() {
			throw new AssertionError();
		}
	}

	static final class OfDouble {

        static void accept(Sink.OfDouble this_, Double i) {
            this_.accept(i.doubleValue());
        }

		private OfDouble() {
			throw new AssertionError();
		}
	}

	/**
     * Resets the sink state to receive a fresh data set.  This must be called
     * before sending any data to the sink.  After calling {@link #end()},
     * you may call this method to reset the sink for another calculation.
     * @param size The exact size of the data to be pushed downstream, if
     * known or {@code -1} if unknown or infinite.
     *
     * <p>Prior to this call, the sink must be in the initial state, and after
     * this call it is in the active state.
     */
    static <T> void begin(Sink<T> this_, long size) {}

    /**
     * Indicates that all elements have been pushed.  If the {@code Sink} is
     * stateful, it should send any stored state downstream at this time, and
     * should clear any accumulated state (and associated resources).
     *
     * <p>Prior to this call, the sink must be in the active state, and after
     * this call it is returned to the initial state.
     */
    static <T> void end(Sink<T> this_) {}

    /**
     * Indicates that this {@code Sink} does not wish to receive any more data.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation always returns false.
     *
     * @return true if cancellation is requested
     */
    static <T> boolean cancellationRequested(Sink<T> this_) {
        return false;
    }

    /**
     * Accepts an int value.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation throws IllegalStateException.
     *
     * @throws IllegalStateException if this sink does not accept int values
     */
    static <T> void accept(Sink<T> this_, int value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /**
     * Accepts a long value.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation throws IllegalStateException.
     *
     * @throws IllegalStateException if this sink does not accept long values
     */
    static <T> void accept(Sink<T> this_, long value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /**
     * Accepts a double value.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation throws IllegalStateException.
     *
     * @throws IllegalStateException if this sink does not accept double values
     */
    static <T> void accept(Sink<T> this_, double value) {
        throw new IllegalStateException("called wrong accept method");
    }

	private SinkDefaults() {
		throw new AssertionError();
	}
}
