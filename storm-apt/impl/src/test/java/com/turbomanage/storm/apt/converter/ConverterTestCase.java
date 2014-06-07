package com.turbomanage.storm.apt.converter;

import junit.framework.TestCase;

import com.turbomanage.storm.types.BooleanConverter;
import com.turbomanage.storm.types.DoubleConverter;

public class ConverterTestCase extends TestCase {

	public void testBooleanConversions() {
		BooleanConverter conv = new BooleanConverter();
		assertEquals(0, conv.toSql(false).intValue());
		assertEquals(1, conv.toSql(true).intValue());
		assertFalse(conv.fromSql(0));
		assertTrue(conv.fromSql(1));
	}

	public void testDoubleConversions() {
		DoubleConverter dc = new DoubleConverter();
		double d1 = (1 - Math.sqrt(5))/2.;
		String s1 = dc.toString(d1);
		double d2 = dc.fromString(s1);
		assertEquals(d1, d2, 0);
	}

}
