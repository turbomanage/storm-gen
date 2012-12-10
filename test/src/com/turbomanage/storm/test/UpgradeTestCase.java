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


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TestDatabaseHelper;
import com.turbomanage.storm.TestDbFactory;
import com.turbomanage.storm.csv.CsvTableReader;
import com.turbomanage.storm.entity.SimpleEntity;
import com.turbomanage.storm.entity.SimpleEntity.EnumType;
import com.turbomanage.storm.entity.dao.SimpleEntityDao;
import com.turbomanage.storm.entity.dao.SimpleEntityTable;

public class UpgradeTestCase extends AndroidTestCase {

	private Context ctx;
	private SimpleEntityDao dao;
	private DatabaseHelper dbHelper;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = getContext();
		openDatabase();
		dao = new SimpleEntityDao(ctx);
	}

	private void openDatabase() {
		dbHelper = TestDbFactory.getDatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		assertEquals(TestDatabaseHelper.DB_VERSION, db.getVersion());
		// wipe database
		dbHelper.dropAndCreate();
	}

	private void persistRandomEntities(int n) {
		for (int i = 0; i < n; i++) {
			SimpleEntity randomEntity = new SimpleEntity();
			randomEntity.setLongField(new Random().nextLong());
			long id = dao.insert(randomEntity);
			assertTrue(id > 0);
		}
	}

	public void testBackupAndRestore() throws IOException {
		persistRandomEntities(11);
		SimpleEntity e = newTestEntity();
		dao.insert(e);
		List<SimpleEntity> before = dao.listAll();
		dbHelper.backupAllTablesToCsv();
		dbHelper.dropAndCreate();
		assertEquals(0, dao.listAll().size());
		dbHelper.restoreAllTablesFromCsv();
		List<SimpleEntity> after = dao.listAll();
		for (int i = 0; i < before.size(); i++) {
			DaoTestCase.assertAllFieldsMatch(before.get(i), after.get(i));
		}
	}

	/**
	 * Verify that all columns are correctly escaped and converted to Strings.
	 *
	 * @throws IOException
	 */
	public void testWriteToCsv() throws IOException {
		dbHelper.dropAndCreate();
		SimpleEntity populatedEntity = newTestEntity();
		dao.insert(populatedEntity);
		dao.insert(new SimpleEntity()); // default
		dbHelper.backupAllTablesToCsv();
		FileInputStream ois = ctx.openFileInput("testDb.v2.SimpleEntity");
		InputStreamReader isr = new InputStreamReader(ois);
		BufferedReader reader = new BufferedReader(isr);
		reader.readLine(); // header row
		String row1 = reader.readLine();
		String expected = "Q0FGRUJBQkU=,1,0,122,3ff9e3779b97f4a8,VALUE1,1.618034,1,75025,12586269025,0,28657,1,89,88,18000000,-401c3910c8d016b0,-0.618034,1836311903,,86267571272,17711,\"Hello, world!\"";
		assertEquals(expected, row1);
		String row2 = reader.readLine();
		expected = ",0,0,0,0,,0.0,2,0,0,0,0,,,,,,,,,,,";
		assertEquals(expected, row2);
	}

	/**
	 * Read in a CSV file from the prior db version with columns added and dropped.
	 * Ensure that the unchanged columns are matched by name and that new columns
	 * have the expected default values.
	 */
	public void testReadFromCsv() {
		dbHelper.dropAndCreate();
		SimpleEntityTable th = new SimpleEntityTable();
		InputStream csvStream = this.getClass().getClassLoader().getResourceAsStream("assets/testDb.v1.SimpleEntity");
		new CsvTableReader(th).importFromCsv(dbHelper, csvStream);
		List<SimpleEntity> listAll = dao.listAll();
		assertEquals(2, listAll.size());
		SimpleEntity testEntity = newTestEntity();
		testEntity.setId(1);
		testEntity.setCharField((char) 0); // Expected default value
		DaoTestCase.assertAllFieldsMatch(testEntity,listAll.get(0));
		SimpleEntity newEntity = new SimpleEntity();
		newEntity.setId(2);
		DaoTestCase.assertAllFieldsMatch(newEntity, listAll.get(1));
	}

	public void testRestoreWithDefaultValuesForNewFields() {
		dbHelper.dropAndCreate();
		SimpleEntityTable th = new SimpleEntityTable();
		InputStream csvStream = this.getClass().getClassLoader().getResourceAsStream("assets/testDb.v0.SimpleEntity");
		new CsvTableReader(th).importFromCsv(dbHelper, csvStream);
		List<SimpleEntity> listAll = dao.listAll();
		assertEquals(1, listAll.size());
		SimpleEntity newEntity = new SimpleEntity();
		newEntity.setId(1);
		DaoTestCase.assertAllFieldsMatch(newEntity, listAll.get(0));
	}

	private SimpleEntity newTestEntity() {
		SimpleEntity e = new SimpleEntity();
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
		e.setwDateField(new Date("Jan 1, 1970 EST"));
		e.setEnumField(EnumType.VALUE1);
		e.setwDoubleField((1 - Math.sqrt(5)) / 2);
		e.setwFloatField((float) ((1 - Math.sqrt(5)) / 2));
		e.setwIntegerField(1836311903);
		e.setwLongField(86267571272L);
		e.setwShortField((short) 17711);
		e.setwStringField("Hello, world!");
		return e;
	}

}
