package java8.util.stream;

import java8.util.function.Function;

public final class BaseTerminalTestScenarios {

    public static <U, R, S_OUT extends BaseStream<U, S_OUT>> R run(Function<S_OUT, R> terminalF, S_OUT source, StreamShape shape) {
        return terminalF.apply(source);
    }

	private BaseTerminalTestScenarios() {
	}
}
