package com.turbomanage.storm.types;

public class PrimitiveConverter {

	public static String toString(boolean value) {
		return value ? "1" : "0";
	}

	public static String toString(byte value) {
		return Byte.toString(value);
	}

	public static String toString(char value) {
		return Integer.toString((int) value);
	}

	public static String toString(double value) {
		return Long.toString(Double.doubleToLongBits(value), 16);
	}

	public static String toString(float value) {
		return Float.toString(value);
	}

	public static String toString(int value) {
		return Integer.toString(value);
	}

	public static String toString(long value) {
		return Long.toString(value);
	}

	public static String toString(short value) {
		return Short.toString(value);
	}

}
