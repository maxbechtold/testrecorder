package net.amygdalum.testrecorder.util;

import java.util.Arrays;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class PrimitiveArrayMatcher<T> extends TypeSafeMatcher<T> {

	private T array;

	private PrimitiveArrayMatcher(T array) {
		this.array = array;
	}

	public static Matcher<boolean[]> booleanArrayContaining(boolean... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<char[]> charArrayContaining(char... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<byte[]> byteArrayContaining(byte... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<short[]> shortArrayContaining(short... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<int[]> intArrayContaining(int... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<float[]> floatArrayContaining(float... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<long[]> longArrayContaining(long... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	public static Matcher<double[]> doubleArrayContaining(double... items) {
		return new PrimitiveArrayMatcher<>(items);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("an array containing values of type ")
			.appendValue(array.getClass().getComponentType())
			.appendText(": ")
			.appendValue(array);
	}

	@Override
	protected void describeMismatchSafely(T item, Description mismatchDescription) {
		if (!item.getClass().isArray()) {
			mismatchDescription.appendText("not an array");
		} else if (!item.getClass().getComponentType().isPrimitive()) {
			mismatchDescription.appendText("not a primitive array");
		} else if (item.getClass() != array.getClass()) {
			mismatchDescription.appendText("of type ").appendValue(item.getClass().getComponentType());
		} else {
			mismatchDescription.appendText("with items ").appendValue(item);
		}
	}

	@Override
	protected boolean matchesSafely(T item) {
		if (!item.getClass().isArray()) {
			return false;
		}
		if (!item.getClass().getComponentType().isPrimitive()) {
			return false;
		}
		if (item.getClass() != array.getClass()) {
			return false;
		}
		Class<?> type = array.getClass().getComponentType();
		if (type == boolean.class) {
			return Arrays.equals((boolean[]) array, (boolean[]) item);
		} else if (type == char.class) {
			return Arrays.equals((char[]) array, (char[]) item);
		} else if (type == byte.class) {
			return Arrays.equals((byte[]) array, (byte[]) item);
		} else if (type == short.class) {
			return Arrays.equals((short[]) array, (short[]) item);
		} else if (type == int.class) {
			return Arrays.equals((int[]) array, (int[]) item);
		} else if (type == float.class) {
			return Arrays.equals((float[]) array, (float[]) item);
		} else if (type == long.class) {
			return Arrays.equals((long[]) array, (long[]) item);
		} else if (type == double.class) {
			return Arrays.equals((double[]) array, (double[]) item);
		} else {
			return false;
		}
	}

}
