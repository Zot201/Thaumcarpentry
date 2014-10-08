package zotmc.thaumcarpentry.util;

import static java.lang.reflect.Modifier.FINAL;

import java.lang.reflect.Field;

import com.google.common.base.Throwables;

public class Fields {
	
	public static Field definalize(Field field) {
		try {
			MODIFIERS.setInt(field, field.getModifiers() & ~FINAL);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		return field;
	}
	private static final Field MODIFIERS;
	static {
		Field f = null;
		try {
			f = Field.class.getDeclaredField("modifiers");
			f.setAccessible(true);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		MODIFIERS = f;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj, Field field) {
		try {
			return (T) field.get(obj);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	public static <T> void set(Object obj, Field field, T value) {
		try {
			field.set(obj, value);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

}
