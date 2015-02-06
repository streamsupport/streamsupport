package java8.util.stream;

public final class IntermediateTestOps {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static<T> AbstractPipeline chain(AbstractPipeline upstream,
                                            IntermediateTestOp<?, T> op) {
        if (op instanceof StatelessTestOp)
            return StatelessTestOps.chain(upstream, (StatelessTestOp) op);

        if (op instanceof StatefulTestOp)
            return StatefulTestOps.chain(upstream, (StatefulTestOp) op);

        throw new IllegalStateException("Unknown test op type: " + op.getClass().getName());
    }

	private IntermediateTestOps() {
	}
}
