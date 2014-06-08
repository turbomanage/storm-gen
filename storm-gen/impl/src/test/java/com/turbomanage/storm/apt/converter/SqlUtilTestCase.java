package com.turbomanage.storm.apt.converter;

import junit.framework.TestCase;

import com.turbomanage.storm.apt.SqlUtil;

public class SqlUtilTestCase extends TestCase {

	public void testValidSqlIdentifiers() {
		assertTrue(SqlUtil.isValidIdentifier("table1"));
		assertTrue(SqlUtil.isValidIdentifier("_123table"));
		assertFalse(SqlUtil.isValidIdentifier("order"));
		assertTrue(SqlUtil.isValidIdentifier("[order]"));
		assertTrue(SqlUtil.isValidIdentifier("orders"));
		assertFalse(SqlUtil.isValidIdentifier("4you"));
		assertTrue(SqlUtil.isValidIdentifier("[a_valid_java_identifier]"));
	}
}
