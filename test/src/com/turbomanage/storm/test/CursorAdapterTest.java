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

import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TestDatabaseHelper;
import com.turbomanage.storm.TestDbFactory;
import com.turbomanage.storm.entity.SimpleEntity;
import com.turbomanage.storm.entity.dao.SimpleEntityDao;

/**
 * Verify that the {@link Cursor} returned by a query can be used
 * in a {@link CursorAdapter}. 
 *  
 * @author David M. Chandler
 */
@SuppressLint("NewApi")
public class CursorAdapterTest extends AndroidTestCase {
	private Context ctx;
	private SimpleEntityDao dao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = getContext();
		openDatabase();
		dao = new SimpleEntityDao(ctx);
	}
	
	/**
	 * This will fail if there is no column named "id"
	 */
	public void testCursorAdapter() {
		insertRandomEntities(100);
		Cursor c = dao.query(null, null);
		CursorAdapter ca = new CursorAdapter(ctx, c, 0) {
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return null;
			}
			
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
			}
			
		};
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

}
