package java8.util.stream;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

class UnsafeHelper {

	static final Unsafe unsafe;

	static {
		try {
			Field field = null;
			try {
				field = Unsafe.class.getDeclaredField("theUnsafe");
			} catch (NoSuchFieldException oldAndroid) {
				field = Unsafe.class.getDeclaredField("THE_ONE");
			}
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private UnsafeHelper() {
		throw new AssertionError();
	}
}
