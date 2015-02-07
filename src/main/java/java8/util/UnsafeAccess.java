package java8.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

class UnsafeAccess {

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

	private UnsafeAccess() {
		throw new AssertionError();
	}
}
