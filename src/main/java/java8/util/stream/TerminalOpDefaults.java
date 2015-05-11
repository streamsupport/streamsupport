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

import java8.util.Spliterator;

final class TerminalOpDefaults {

    /**
     * Gets the shape of the input type of this operation.
     *
     * <p><b>Implementation Requirements:</b><br> The default returns {@code StreamShape.REFERENCE}.
     *
     * @return StreamShape of the input type of this operation
     */
    static StreamShape inputShape() { return StreamShape.REFERENCE; }

    /**
     * Gets the stream flags of the operation.  Terminal operations may set a
     * limited subset of the stream flags defined in {@link StreamOpFlag}, and
     * these flags are combined with the previously combined stream and
     * intermediate operation flags for the pipeline.
     *
     * <p><b>Implementation Requirements:</b><br> The default implementation returns zero.
     *
     * @return the stream flags for this operation
     * @see StreamOpFlag
     */
    static int getOpFlags() { return 0; }

    /**
     * Performs a parallel evaluation of the operation using the specified
     * {@code PipelineHelper}, which describes the upstream intermediate
     * operations.
     *
     * <p><b>Implementation Requirements:</b><br> The default performs a sequential evaluation of the operation
     * using the specified {@code PipelineHelper}.
     *
     * @param helper the pipeline helper
     * @param spliterator the source spliterator
     * @return the result of the evaluation
     */
    static <E_IN, P_IN, R> R evaluateParallel(TerminalOp<E_IN, R> this_, PipelineHelper<E_IN> helper,
                                      Spliterator<P_IN> spliterator) {

        return this_.evaluateSequential(helper, spliterator);
    }

    private TerminalOpDefaults() {
    }
}
