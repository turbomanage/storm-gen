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

import java.io.FileNotFoundException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.turbomanage.storm.csv.CsvTableReader;
import com.turbomanage.storm.csv.CsvTableWriter;
import com.turbomanage.storm.query.Query;

/**
 * Abstract base class for generated TableHelpers.
 * Contains methods related to database creation and upgrades which
 * are executed before the database is fully ready for use by the DAOs. Its
 * methods are invoked by {@link DatabaseHelper#onCreate(SQLiteDatabase)}. To
 * customize the SQL for a particular table, create your own {@link TableHelper}
 * that overrides one or more of these methods and modify
 * {@link DatabaseHelper#getTableHelpers()} to return an instance of you custom
 * TableHelper.
 *
 * @author David M. Chandler
 */
public abstract class TableHelper<T> {

	/**
	 * Marker interface for column enums
	 *
	 * @author David M. Chandler
	 */
	public interface Column{
		public abstract String asc();
		public abstract String desc();
	};

	private static final String TAG = TableHelper.class.getName();
	
	/**
	 * @return array of column names in declared order
	 */
	public abstract Column[] getColumns();
	
	/**
	 * Obtains an entity's ID field. This abstraction supports
	 * the optional @Id annotation.
	 * 
	 * @param obj The entity
	 * @return long id
	 */
	public abstract long getId(T obj);
	
	/**
	 * Sets and entity's ID field. This abstraction supports
	 * the optional @Id annotation.
	 * 
	 * @param obj The entity
	 * @param id
	 */
	public abstract void setId(T obj, long id);

	/**
	 * @return SQL table name
	 */
	public abstract String getTableName();

	/**
	 * @return CREATE TABLE statement
	 */
	protected abstract String createSql();

	/**
	 * @return DROP TABLE statement
	 */
	protected abstract String dropSql();

	/**
	 * Return SQL statement to execute when upgrading the table,
	 * probably ALTER TABLE
	 *
	 * @param oldVersion
	 * @param newVersion
	 * @return String SQL statement
	 */
	protected abstract String upgradeSql(int oldVersion, int newVersion);

	/**
	 * Extract from a cursor an array of String values in order of the Columns
	 * enum. Note that column order is not guaranteed when adding or removing
	 * entity fields, so these values are not necessarily in cursor order. 
	 * This is used by the CsvWriter and is necessary mainly because
	 * Cursor.getString truncates doubles and blobs need to be Base64 encoded.
	 * Writes columns in order of the Columns enum, not their cursor position.
	 *
	 * @param c Cursor
	 * @return Map<String colName, String colValue>
	 */
	public abstract String[] getRowValues(Cursor c);

	/**
	 * Convert the String values for each column into their appropriate SQL
	 * types and bind to the insert statement using {@link InsertHelper}.
	 *
	 * @param insHelper
	 * @param rowValues
	 */
	public abstract void bindRowValues(InsertHelper insHelper, String[] rowValues);

	/**
	 * Populate an array with the entity's default values for each field obtained
	 * by creating a new instance of the entity. These values are used to fill in
	 * any missing columns when importing from CSV.
	 *
	 * @return
	 */
	public abstract String[] getDefaultValues();

	/**
	 * Populate a {@link ContentValues} object with the values of the supplied
	 * POJO by calling the appropriate getters and converters.
	 *
	 * @param obj
	 * @return ContentValues
	 */
	public abstract ContentValues getEditableValues(T obj);

	/**
	 * @return name of the ID column
	 */
	public abstract Column getIdCol();

	/**
	 * Create a new instance of a POJO by calling its setters with the values
	 * obtained from a {@link Cursor}.
	 *
	 * @param c
	 * @return a new instance
	 */
	public abstract T newInstance(Cursor c);

	/**
	 * Add conditions to a filter for each property of the example object
	 * which does not have its default value.
	 *
	 * @param builder
	 * @param exampleObj
	 * @return FilterBuilder ready to execute
	 */
	public abstract Query<T> buildFilter(Query<T> builder, T exampleObj);

	/**
	 * Creates the table that represents the associated entity.
	 *
	 * @param db
	 */
	protected void onCreate(SQLiteDatabase db) {
		db.execSQL(createSql());
	}

	/**
	 * Drops the table that represents the associated entity.
	 *
	 * @param db
	 */
	protected void onDrop(SQLiteDatabase db) {
		db.execSQL(dropSql());
	}

	/**
	 * Upgrades the table that represents the associated entity. This will
	 * typically be an ALTER TABLE statement.
	 *
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	protected void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		db.execSQL(upgradeSql(oldVersion, newVersion));
	}

	/**
	 * Backs up the current table to a CSV file.
	 *
	 * @param db
	 * @param ctx
	 * @param suffix 
	 * 
	 * @return true on success
	 */
	public boolean backup(SQLiteDatabase db, Context ctx, String suffix) {
		try {
			new CsvTableWriter(this).dumpToCsv(ctx, db, suffix);
		} catch (SQLException e) {
			if (e.getMessage().contains("no such table")) {
				Log.w(TAG, "Table " + this.getTableName() + " doesn't exist. This is expected if the table is new in this db version.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Restores a table from a text file.
	 *
	 * @param db
	 * @param ctx
	 * @param suffix
	 */
	public void restore(SQLiteDatabase db, Context ctx, String suffix) {
		new CsvTableReader(this).importFromCsv(ctx, db, suffix);
	}

	/**
	 * Drops a table and recreates it.
	 *
	 * @param db
	 */
	protected void dropAndCreate(SQLiteDatabase db) {
		this.onDrop(db);
		this.onCreate(db);
	}

	/*
	 * Cursor wrapper methods which bind to primitive type columns
	 * and return the corresponding wrapper type which may be null
	 */

	protected byte[] getBlobOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getBlob(col);
	}

	protected Double getDoubleOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getDouble(col);
	}

	protected Float getFloatOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getFloat(col);
	}

	protected Integer getIntOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getInt(col);
	}

	protected Long getLongOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getLong(col);
	}

	protected Short getShortOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getShort(col);
	}

	protected String getStringOrNull(Cursor c, int col) {
		return c.isNull(col) ? null : c.getString(col);
	}

}
