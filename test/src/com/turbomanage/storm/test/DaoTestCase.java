/*******************************************************************************
 * Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.turbomanage.storm.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TestDatabaseHelper;
import com.turbomanage.storm.TestDbFactory;
import com.turbomanage.storm.entity.SimpleEntity;
import com.turbomanage.storm.entity.dao.SimpleEntityDao;
import com.turbomanage.storm.exception.TooManyResultsException;

public class DaoTestCase extends AndroidTestCase {
	private Context ctx;
	private SimpleEntityDao dao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = getContext();
		openDatabase();
		dao = new SimpleEntityDao(ctx);
		long id = dao.insert(new SimpleEntity());
		// Verify that we started clean
		assertEquals(1, id);
	}

	public void testDelete() {
		long id = dao.insert(new SimpleEntity());
		int numRowsDeleted = dao.delete(id);
		assertEquals(1, numRowsDeleted);
		assertNull(dao.get(id));
	}

	public void testDeleteAll() {
		insertRandomEntities(5);
		int numRowsDeleted = dao.deleteAll();
		assertTrue(numRowsDeleted > 5);
		List<SimpleEntity> listAll = dao.listAll();
		assertEquals(0, listAll.size());
	}

	public void testGetByExample() {
		SimpleEntity newEntity = new SimpleEntity();
		newEntity.setIntField(21);
		long id = dao.insert(newEntity);
		SimpleEntity exampleObj = new SimpleEntity();
		exampleObj.setIntField(21);
		SimpleEntity resultEntity = dao.getByExample(exampleObj);
		assertEquals(id, resultEntity.getId());
	}

	public void testGetByExampleWithTooManyResults() {
		String testName = "testGetByExampleWithTooManyResults";
		insertTwoEntitiesHavingAnIdenticalStringField(testName);
		SimpleEntity exampleObj = new SimpleEntity();
		exampleObj.setwStringField(testName);
		try {
			SimpleEntity resultObj = dao.getByExample(exampleObj);
			fail();
		} catch (TooManyResultsException e) {
			// passed
		}
	}

	public void testInsert() {
		SimpleEntity newEntity = new SimpleEntity();
		long id = dao.insert(newEntity);
		assertTrue(id > 0);
		assertEquals(id, newEntity.getId());
		SimpleEntity retrievedEntity = dao.get(id);
		assertAllFieldsMatch(newEntity, retrievedEntity);
	}

	public void testInsertMany() {
		dao.deleteAll();
		List<SimpleEntity> testEntities = new ArrayList<SimpleEntity>();
		for (int i = 0; i < 100; i++) {
			SimpleEntity randomEntity = new SimpleEntity();
			randomEntity.setLongField(new Random().nextLong());
			testEntities.add(randomEntity);
		}
		long numInserted = dao.insertMany(testEntities);
		assertEquals(100, numInserted);
		assertEquals(100, dao.listAll().size());
	}

	public void testInsertWithId() {
		for (int i = 5000; i < 5010; i++) {
			SimpleEntity newEntity = new SimpleEntity();
			newEntity.setId(i);
			newEntity.setwStringField("testInsertWithId");
			long id = dao.insert(newEntity);
			SimpleEntity resultEntity = dao.get(id);
			assertEquals(i, id);
			assertEquals(id, resultEntity.getId());
		}
		SimpleEntity ex = new SimpleEntity();
		ex.setwStringField("testInsertWithId");
		List<SimpleEntity> listAll = dao.listByExample(ex);
		assertEquals(10, listAll.size());
	}

	public void testInsertWithNonDefaultValues() {
		SimpleEntity newEntity = new SimpleEntity();
		populateTestEntity(newEntity);
		long id = dao.insert(newEntity);
		assertTrue(id > 0);
		SimpleEntity retrievedEntity = dao.get(id);
		assertAllFieldsMatch(newEntity, retrievedEntity);
	}

	public void testUpdate() {
		SimpleEntity newEntity = new SimpleEntity();
		long id = dao.insert(newEntity);
		populateTestEntity(newEntity);
		long numRowsUpdated = dao.update(newEntity);
		assertEquals(1, numRowsUpdated);
		SimpleEntity retrievedEntity = dao.get(id);
		assertAllFieldsMatch(newEntity, retrievedEntity);
	}

	public void testListAll() {
		List<SimpleEntity> before = dao.listAll();
		insertRandomEntities(5);
		List<SimpleEntity> after = dao.listAll();
		assertEquals(5, after.size() - before.size());
	}

	public void testListByExample() {
		String testName = "testListByExample";
		insertTwoEntitiesHavingAnIdenticalStringField(testName);
		SimpleEntity exampleObj = new SimpleEntity();
		exampleObj.setwStringField(testName);
		List<SimpleEntity> resultList = dao.listByExample(exampleObj);
		assertEquals(2, resultList.size());
	}

	static void assertAllFieldsMatch(SimpleEntity a, SimpleEntity b) {
		assertEquals(a.getId(), b.getId());
		assertEquals(a.getByteField(), b.getByteField());
		assertTrue(Arrays.equals(a.getBlobField(), b.getBlobField()));
		assertEquals(a.getCharField(), b.getCharField());
		assertEquals(a.getDoubleField(), b.getDoubleField());
		assertEquals(a.getEnumField(), b.getEnumField());
		assertEquals(a.getFloatField(), b.getFloatField());
		assertEquals(a.getIntField(), b.getIntField());
		assertEquals(a.getLongField(), b.getLongField());
		assertEquals(a.getShortField(), b.getShortField());
		assertEquals(a.getwStringField(), b.getwStringField());
		assertEquals(a.getwBooleanField(), b.getwBooleanField());
		assertEquals(a.getwByteField(), b.getwByteField());
		assertEquals(a.getwCharacterField(), b.getwCharacterField());
		assertEquals(a.getwDateField(), b.getwDateField());
		assertEquals(a.getwDoubleField(), b.getwDoubleField());
		assertEquals(a.getwFloatField(), b.getwFloatField());
		assertEquals(a.getwIntegerField(), b.getwIntegerField());
		assertEquals(a.getwLongField(), b.getwLongField());
		assertEquals(a.getwShortField(), b.getwShortField());
	}

	private void openDatabase() {
		DatabaseHelper dbHelper = TestDbFactory.getDatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		assertEquals(TestDatabaseHelper.DB_VERSION, db.getVersion());
		// wipe database
		dbHelper.onUpgrade(db, TestDatabaseHelper.DB_VERSION, TestDatabaseHelper.DB_VERSION);
	}

	private void insertRandomEntities(int n) {
		for (int i = 0; i < n; i++) {
			SimpleEntity randomEntity = new SimpleEntity();
			randomEntity.setLongField(new Random().nextLong());
			dao.insert(randomEntity);
		}
	}

	private void insertTwoEntitiesHavingAnIdenticalStringField(String strInCommon) {
		SimpleEntity e1 = new SimpleEntity();
		e1.setwStringField(strInCommon);
		dao.insert(e1);
		SimpleEntity e2 = new SimpleEntity();
		e2.setwStringField(strInCommon);
		dao.insert(e2);
	}

	private void populateTestEntity(SimpleEntity e) {
		e.setBlobField("CAFEBABE".getBytes());
		e.setBooleanField(true);
		e.setCharField('z');
		e.setDoubleField((1 + Math.sqrt(5)) / 2);
		e.setFloatField((float) ((1 + Math.sqrt(5)) / 2));
		e.setIntField(75025);
		e.setLongField(12586269025L);
		e.setShortField((short) 28657);
		e.setwBooleanField(Boolean.TRUE);
		e.setwByteField(new Byte((byte) 89));
		e.setwCharacterField('X');
		e.setwDateField(new Date());
		e.setwDoubleField((1 - Math.sqrt(5)) / 2);
		e.setwFloatField((float) ((1 - Math.sqrt(5)) / 2));
		e.setwIntegerField(1836311903);
		e.setwLongField(86267571272L);
		e.setwShortField((short) 17711);
		e.setwStringField("Hello, world!");
	}

}
