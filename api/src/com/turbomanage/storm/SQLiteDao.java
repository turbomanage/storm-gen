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
package com.turbomanage.storm;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.turbomanage.storm.api.Persistable;
import com.turbomanage.storm.exception.TooManyResultsException;
import com.turbomanage.storm.query.FilterBuilder;

public abstract class SQLiteDao<T extends Persistable> {

	private static final String TAG = SQLiteDao.class.getName();

	private final Context mContext;
	protected final TableHelper<T> th;

	@SuppressWarnings("unchecked")
	public SQLiteDao(Context ctx) {
		this.mContext = ctx;
		this.th = getTableHelper();
	}

	/**
	 * Generated subclasses implement to point to the correct
	 * {@link SQLiteOpenHelper} for the entity.
	 *
	 * @param Context ctx
	 * @return DatabaseHelper
	 */
	public abstract DatabaseHelper getDbHelper(Context ctx);

	@SuppressWarnings("rawtypes")
	public abstract TableHelper getTableHelper();

	public int delete(Long id) {
		if (id != null) {
			return getWritableDb().delete(th.getTableName(), th.getIdCol() + "=?", new String[]{id.toString()});
		}
		return 0;
	}

	public int deleteAll() {
		return getWritableDb().delete(th.getTableName(), null, null);
	}

	/**
	 * Return an object to construct a filter with AND conditions.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public FilterBuilder<T> filter() {
		return new FilterBuilder<T>((SQLiteDao<T>) this);
	}

	public T get(Long id) {
		return filter().eq(th.getIdCol(), id).get();
	}

	public T getByExample(T exampleObj) {
		return asObject(queryByExample(exampleObj));
	}

	public List<T> listAll() {
		return asList(queryAll());
	}

	public List<T> listByExample(T exampleObj) {
		return asList(queryByExample(exampleObj));
	}

	/**
	 * Insert a row in the database. If the object's id is the
	 * default long (0), the db will generate an id.
	 *
	 * @param obj
	 * @return row ID of newly inserted or -1 if err
	 */
	public long insert(T obj) {
		ContentValues cv = th.getEditableValues(obj);
		if (obj.getId() == 0) {
			// the default, remove from ContentValues to allow autoincrement
			cv.remove(th.getIdCol().toString());
		}
		long id = getWritableDb().insertOrThrow(th.getTableName(), null, cv);
		obj.setId(id);
		return id;
	}

	/**
	 * Efficiently insert a collection of objects using {@link InsertHelper}.
	 *
	 * @param many Collection of objects
	 * @return count of inserted objects or -1 immediately if any errors
	 */
	public long insertMany(Iterable<T> many) {
		long numInserted = 0;
		InsertHelper insertHelper = new DatabaseUtils.InsertHelper(getWritableDb(), th.getTableName());
		getWritableDb().beginTransaction();
		try {
			for (T obj : many) {
				ContentValues cv = th.getEditableValues(obj);
				if (obj.getId() == 0) {
					// the default, remove from ContentValues to allow autoincrement
					cv.remove(th.getIdCol().toString());
				}
				long id = insertHelper.insert(cv);
				if (id == -1)
					return -1;
				numInserted++;
			}
			getWritableDb().setTransactionSuccessful();
		} finally {
			getWritableDb().endTransaction();
		}
		return numInserted;
	}

	/**
	 * Insert or update, depending on whether the ID column is set to
	 * a non-default value.
	 *
	 * @param obj
	 * @return
	 */
	public long update(T obj) {
		ContentValues cv = th.getEditableValues(obj);
		Long id = obj.getId();
		int numRowsUpdated = getWritableDb().update(th.getTableName(), cv, th.getIdCol()
				+ "=?", new String[] { id.toString() });
		return numRowsUpdated;
	}

	// TODO beware leaky abstractions--who owns the cursor?
	public Cursor query(String where, String[] params) {
		return getReadableDb().query(th.getTableName(), null, where, params, null, null, null);
	}

	/**
	 * Execute a query which returns all rows in the table.
	 * Calling method MUST close the {@link Cursor}.
	 *
	 * @return Cursor
	 */
	protected Cursor queryAll() {
		return query(null, null);
	}

	/**
	 * Execute a query which returns all rows in the table.
	 * Calling method MUST close the {@link Cursor}.
	 *
	 * @return Cursor
	 */
	protected Cursor queryByExample(T obj) {
		return th.buildFilter(this.filter(), obj).exec();
	}

	/**
	 * Converts all rows in a {@link Cursor} to a List of objects.
	 *
	 * @param c Cursor
	 * @return
	 */
	public List<T> asList(Cursor c) {
		// TODO consider returning Iterable<T> instead
		try {
			ArrayList<T> resultList = new ArrayList<T>();
			for (boolean hasItem = c.moveToFirst(); hasItem; hasItem = c.moveToNext()) {
				T obj = th.newInstance(c);
				resultList.add(obj);
			}
			return resultList;
		} finally {
			c.close();
		}
	}

	/**
	 * Converts a {@link Cursor} to an object. Throws an exception if there
	 * was more than one row in the cursor.
	 *
	 * @param c Cursor
	 * @return Object
	 */
	public T asObject(Cursor c) {
		try {
			if (c.getCount() == 1) {
				c.moveToFirst();
				return th.newInstance(c);
			} else if (c.getCount() > 1) {
				throw new TooManyResultsException("Cursor returned " + c.getCount() + " rows");
			}
			return null;
		} finally {
			c.close();
		}
	}

	protected SQLiteDatabase getWritableDb() {
		return getDbHelper(mContext).getWritableDatabase();
	}

	protected SQLiteDatabase getReadableDb() {
		return getDbHelper(mContext).getReadableDatabase();
	}

}