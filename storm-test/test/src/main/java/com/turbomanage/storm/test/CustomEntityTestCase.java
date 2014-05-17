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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TableHelper;
import com.turbomanage.storm.TestDatabaseHelper;
import com.turbomanage.storm.TestDbFactory;
import com.turbomanage.storm.entity.CustomEntity;
import com.turbomanage.storm.entity.dao.CustomEntityDao;
import com.turbomanage.storm.entity.dao.OrderTable;

public class CustomEntityTestCase extends AndroidTestCase {
	private Context ctx;
	private CustomEntityDao dao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = getContext();
		openDatabase();
		dao = new CustomEntityDao(ctx);
	}
	
	public void testCustomIdCol() {
		CustomEntity e1 = new CustomEntity();
		e1.setId(1597);
		CustomEntity e2 = new CustomEntity();
		e2.setId(1597);
		long id1 = dao.insert(e1);
		long id2 = dao.insert(e2);
		assertEquals(1, id1);
		assertEquals(1, e1.getCustomId());
		assertEquals(2, id2);
		assertEquals(2, e2.getCustomId());
	}
	
	public void testCustomTableName() {
		TableHelper[] tableHelpers = TestDbFactory.getDatabaseHelper(ctx).getDbFactory().getTableHelpers();
		for (TableHelper th : tableHelpers) {
			if (th instanceof OrderTable) {
				return;
			}
		}
		fail("CustomNameTable not found in TableHelpers");
	}

	private void openDatabase() {
		DatabaseHelper dbHelper = TestDbFactory.getDatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		assertEquals(TestDatabaseHelper.DB_VERSION, db.getVersion());
		// wipe database
		dbHelper.onUpgrade(db, TestDatabaseHelper.DB_VERSION, TestDatabaseHelper.DB_VERSION);
	}

}
