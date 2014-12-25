package java8.util.concurrent.atomic;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

class UnsafeAccess {

	static final Unsafe unsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private UnsafeAccess() {
		throw new AssertionError();
	}
}
