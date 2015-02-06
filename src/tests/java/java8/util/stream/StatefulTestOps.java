package java8.util.stream;

import java8.util.Spliterator;
import java8.util.function.IntFunction;

public final class StatefulTestOps {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static<T> AbstractPipeline chain(AbstractPipeline upstream,
                                            StatefulTestOp op) {
    	if (op.outputShape() == StreamShape.REFERENCE) {
            return new ReferencePipeline.StatefulOp<Object, T>(upstream, op.inputShape(), op.opGetFlags()) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }

                @Override
                <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper,
                                                             Spliterator<P_IN> spliterator) {
                    return op.opEvaluateParallelLazy(helper, spliterator);
                }

                @Override
                <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                                  Spliterator<P_IN> spliterator,
                                                  IntFunction<T[]> generator) {
                    return op.opEvaluateParallel(helper, spliterator, generator);
                }
            };
    	}
    	if (op.outputShape() == StreamShape.INT_VALUE) {
            return new IntPipeline.StatefulOp<Object>(upstream, op.inputShape(), op.opGetFlags()) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }

                @Override
                <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper,
                                                             Spliterator<P_IN> spliterator) {
                    return op.opEvaluateParallelLazy(helper, spliterator);
                }

                @Override
                <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                        Spliterator<P_IN> spliterator,
                                                        IntFunction<Integer[]> generator) {
                    return (Node<Integer>) op.opEvaluateParallel(helper, spliterator, generator);
                }
            };
    	}
    	if (op.outputShape() == StreamShape.LONG_VALUE) {
            return new LongPipeline.StatefulOp<Object>(upstream, op.inputShape(), op.opGetFlags()) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }

                @Override
                <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper,
                                                             Spliterator<P_IN> spliterator) {
                    return op.opEvaluateParallelLazy(helper, spliterator);
                }

                @Override
                <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     IntFunction<Long[]> generator) {
                    return (Node<Long>) op.opEvaluateParallel(helper, spliterator, generator);
                }
            };	
    	}
    	if (op.outputShape() == StreamShape.DOUBLE_VALUE) {
            return new DoublePipeline.StatefulOp<Object>(upstream, op.inputShape(), op.opGetFlags()) {
                @Override
                Sink opWrapSink(int flags, Sink sink) {
                    return op.opWrapSink(flags, isParallel(), sink);
                }

                @Override
                <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper,
                                                                Spliterator<P_IN> spliterator) {
                    return op.opEvaluateParallelLazy(helper, spliterator);
                }

                @Override
                <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                       Spliterator<P_IN> spliterator,
                                                       IntFunction<Double[]> generator) {
                    return (Node<Double>) op.opEvaluateParallel(helper, spliterator, generator);
                }
            };
    	}
    	throw new IllegalStateException(op.outputShape().toString());
    }

    public static StreamShape inputShape() { return StreamShape.REFERENCE; }

    public static StreamShape outputShape() { return StreamShape.REFERENCE; }

    public static int opGetFlags() { return 0; }

    @SuppressWarnings("unchecked")
    public static <E, P_IN> Spliterator<E> opEvaluateParallelLazy(StatefulTestOp<E> this_, PipelineHelper<E> helper,
                                                         Spliterator<P_IN> spliterator) {
        return this_.opEvaluateParallel(helper, spliterator, i -> (E[]) new Object[i]).spliterator();
    }

	private StatefulTestOps() {
	}
}
