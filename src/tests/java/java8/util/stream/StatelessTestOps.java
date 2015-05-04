package java8.util.stream;

public final class StatelessTestOps {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static<T> AbstractPipeline chain(AbstractPipeline upstream,
                                            StatelessTestOp<?, T> op) {
        int flags = op.opGetFlags();
        if (op.outputShape() == StreamShape.REFERENCE) {
            return new ReferencePipeline.StatelessOp<Object, T>(upstream, op.inputShape(), flags) {
                public Sink opWrapSink(int flags, Sink<T> sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }
            };
        }
        if (op.outputShape() == StreamShape.INT_VALUE) {
            return new IntPipeline.StatelessOp<Object>(upstream, op.inputShape(), flags) {
                public Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }
            };
        }
        if (op.outputShape() == StreamShape.LONG_VALUE) {
            return new LongPipeline.StatelessOp<Object>(upstream, op.inputShape(), flags) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }
            };
        }
        if (op.outputShape() == StreamShape.DOUBLE_VALUE) {
            return new DoublePipeline.StatelessOp<Object>(upstream, op.inputShape(), flags) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }
            };
        }
        throw new IllegalStateException(op.outputShape().toString());
    }

    public static StreamShape inputShape() { return StreamShape.REFERENCE; }

    public static StreamShape outputShape() { return StreamShape.REFERENCE; }

    public static int opGetFlags() { return 0; }

    private StatelessTestOps() {
    }
}
