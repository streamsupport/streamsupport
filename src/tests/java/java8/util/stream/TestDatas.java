package java8.util.stream;

import java.util.Collection;
import java.util.Iterator;

import java8.util.Spliterator;
import java8.util.Spliterators;

public final class TestDatas {

    public static int size() {
        throw new UnsupportedOperationException();
    }

    public static <T, S extends BaseStream<T, S>> Iterator<T> iterator(TestData<T, S> this_) {
        return Spliterators.iterator(this_.getSpliterator());
    }

    public static <T, S extends BaseStream<T, S>> boolean isOrdered(TestData<T, S> this_) {
    	return Spliterators.hasCharacteristics(Spliterators.spliteratorUnknownSize(this_.iterator(), 0), Spliterator.ORDERED);
    }

    public static <A extends Collection<? super T>, T, S extends BaseStream<T, S>> A into(TestData<T, S> this_, A target) {
    	Spliterators.spliteratorUnknownSize(this_.iterator(), 0).forEachRemaining(target::add);
        return target;
    }

	private TestDatas() {
	}
}
