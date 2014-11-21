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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.turbomanage.storm.api.DatabaseFactory;
import com.turbomanage.storm.exception.TooManyResultsException;
import com.turbomanage.storm.query.Query;

/**
 * Base DAO class for entity DAOs. Most of the runtime is implemented
 * in this class. It is safe to construct new instances anywhere, as
 * this class holds a reference to the {@link Context}, which it passes
 * to the generated {@link DatabaseFactory} to obtain the singleton
 * {@link SQLiteOpenHelper}.
 *
 * @author David M. Chandler
 *
 * @param <T> Entity type
 */
public abstract class SQLiteDao<T> {

	private static final String TAG = SQLiteDao.class.getName();

	private final Context mContext;
	protected final TableHelper<T> th;

	/**
	 * Constructor requires the {@link Context}, which you typically
	 * obtain by calling {@link Activity#getApplicationContext()} in
	 * your Activity. The DAO maintains no database state, but rather
	 * only the {@link Context} to initialize the database if needed.
	 * It is safe to construct multiple instances of the DAO since all
	 * database access occurs a singleton instance of {@link SQLiteOpenHelper}
	 * provided by your generated {@link DatabaseFactory}.
	 *
	 * @param ctx Context
	 */
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

	/**
	 * Generated subclasses implement to provide the entity's
	 * {@link TableHelper}.
	 *
	 * @return TableHelper
	 */
	@SuppressWarnings("rawtypes")
	public abstract TableHelper getTableHelper();

	/**
	 * Deletes a single row by ID. Returns the number of rows deleted
	 * or 0 if unsuccessful.
	 *
	 * @param id
	 * @return count of rows deleted
	 */
	public int delete(Long id) {
		if (id != null) {
			return getWritableDb().delete(th.getTableName(), th.getIdCol() + "=?", new String[]{id.toString()});
		}
		return 0;
	}

	/**
	 * Deletes all rows in the entity's table. Returns the number of
	 * rows deleted.
	 *
	 * @return count of rows deleted
	 */
	public int deleteAll() {
		return getWritableDb().delete(th.getTableName(), null, null);
	}

	/**
	 * Returns a {@link Query} for the entity type. The default
	 * FilterBuilder constructs a query by ANDing all conditions.
	 *
	 * @return FilterBuilder
	 */
	@SuppressWarnings("unchecked")
	public Query<T> load() {
		return new Query<T>((SQLiteDao<T>) this);
	}

	/**
	 * Returns a single object by ID or null if no match found.
	 * If more than one match is found, throws
	 * {@link TooManyResultsException}.
	 *
	 * @param id
	 * @return One entity
	 */
	public T get(Long id) {
		return load().eq(th.getIdCol(), id).get();
	}

	/**
	 * Constructs a query from an example object and returns
	 * the matching entity or null. If more than one match is found,
	 * throws {@link TooManyResultsException}. Uses in the comparison
	 * only those fields of the example object which are different than
	 * their default values.
	 *
	 * @param exampleObj
	 * @return
	 */
	public T getByExample(T exampleObj) {
		return asObject(queryByExample(exampleObj));
	}

	/**
	 * Returns all rows in the entity table as a {@link List}.
	 *
	 * @return List<T>
	 */
	public List<T> listAll() {
		return asList(queryAll());
	}

	/**
	 * Returns all entities matching an example object. Uses in the comparison
	 * only those fields of the example object which are different than
	 * their default values.
	 *
	 * @param exampleObj
	 * @return
	 */
	public List<T> listByExample(T exampleObj) {
		return asList(queryByExample(exampleObj));
	}
	
	/**
	 * Inserts a row for the provided entity. If the entity's id is the
	 * default long (0), the database generates an id and populates the
	 * entity's ID field. Returns the generated ID or -1 if error.
	 *
	 * @param obj An entity
	 * @return ID of newly inserted row or -1 if err
	 */
	public long insert(T obj) {
		ContentValues cv = th.getEditableValues(obj);
		if (th.getId(obj) == 0) {
			// the default, remove from ContentValues to allow autoincrement
			cv.remove(th.getIdCol().toString());
		}
		long id = getWritableDb().insertOrThrow(th.getTableName(), null, cv);
		th.setId(obj, id);
		return id;
	}

	/**
	 * Efficiently insert a collection of entities using {@link InsertHelper}.
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
				if (th.getId(obj) == 0) {
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

	public long save(T obj) {
		// TODO if 0, insert, else update
	}

	/**
	 * Update all columns for the row having the ID matching
	 * the provided entity's ID.
	 *
	 * @param obj An entity
	 * @return count of updated rows
	 */
	public long update(T obj) {
		ContentValues cv = th.getEditableValues(obj);
		Long id = th.getId(obj);
		int numRowsUpdated = getWritableDb().update(th.getTableName(), cv, th.getIdCol()
				+ "=?", new String[] { id.toString() });
		return numRowsUpdated;
	}

	// TODO beware leaky abstractions--who owns the cursor?
	/**
	 * Convenience method queries the entity table using the provided
	 * WHERE clause and parameters and returns a {@link Cursor}.
	 *
	 * The calling method MUST close the Cursor!
	 *
	 * @param where
	 * @param params
	 * @return Cursor
	 */
	public Cursor query(String where, String[] params) {
		return query(where, params, null);
	}

	/**
	 * Convenience method queries the entity table using the provided
	 * WHERE clause and parameters and returns a {@link Cursor}.
	 *
	 * The calling method MUST close the Cursor!
	 *
	 * @param where
	 * @param params
	 * @param orderBy
	 * @return
	 */
	public Cursor query(String where, String[] params, String orderBy) {
		return getReadableDb().query(th.getTableName(), null, where, params, null, null, orderBy);
	}

	/**
	 * Execute a query which returns all rows in the entity table.
	 *
	 * Calling method MUST close the {@link Cursor}!
	 *
	 * @return Cursor
	 */
	protected Cursor queryAll() {
		return query(null, null, null);
	}

	/**
	 * Executes a query which returns all rows in the entity table
	 * that match the fields of the example object having values other
	 * than the defaults.
	 *
	 * Calling method MUST close the {@link Cursor}!
	 *
	 * @return Cursor
	 */
	protected Cursor queryByExample(T obj) {
		return th.buildFilter(this.load(), obj).exec();
	}

	/**
	 * Converts all rows in a {@link Cursor} to a List of objects.
	 *
	 * @param c Cursor
	 * @return List<T>
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
	 * Converts a {@link Cursor} to an object. If there is more than one
	 * row in the Cursor, throws {@link TooManyResultsException}.
	 *
	 * @param c Cursor
	 * @return An entity
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