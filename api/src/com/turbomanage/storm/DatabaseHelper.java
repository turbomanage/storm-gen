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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.turbomanage.storm.api.Database;
import com.turbomanage.storm.api.DatabaseFactory;

/**
 * Default implementation of the SQLiteOpenHelper. Projects should extend this
 * class and annotate it with {@link Database}. Subclasses may override the
 * default onCreate and onUpgrade methods.
 *
 * @author David M. Chandler
 */
public abstract class DatabaseHelper extends SQLiteOpenHelper {

	public enum UpgradeStrategy {
		/**
		 * Drop and recreate all tables without preserving existing data.
		 */
		DROP_CREATE,
		/**
		 * Drop and recreate all tables with attempt to preserve existing data
		 * by first backing up to CSV and restoring from the same.
		 */
		BACKUP_RESTORE,
		/**
		 * Custom upgrade strategy implemented by overriding
		 * {@link DatabaseHelper#getUpgradeStrategy()}.
		 */
		UPGRADE
	}

	private DatabaseFactory dbFactory;

	protected Context mContext;

	/**
	 * The constructor that should be overridden. Simply invoke
	 * the super constructor.
	 *
	 * @param ctx
	 * @param dbFactory
	 */
	public DatabaseHelper(Context ctx, DatabaseFactory dbFactory) {
		this(ctx, dbFactory.getName(), null, dbFactory.getVersion());
		this.mContext = ctx;
		this.setDbFactory(dbFactory);
	}

	/**
	 * Don't extend this one. You need dbFactory from the other constructor.
	 *
	 * @param ctx
	 * @param dbName
	 * @param cursorFactory
	 * @param dbVersion
	 */
	private DatabaseHelper(Context ctx, String dbName,
			CursorFactory cursorFactory, int dbVersion) {
		super(ctx, dbName, null, dbVersion);
	}

	/**
	 * Subclasses must implement to select what to do
	 * when you upgrade the app's database version. To implement
	 * your own upgrade strategy, return {@link UpgradeStrategy.UPGRADE}
	 * and override the upgrade() method in this class.
	 *
	 * @return UpgradeStrategy
	 */
	public abstract UpgradeStrategy getUpgradeStrategy();

	/**
	 * Provides a reference to the {@link Context} which was used
	 * to initialize this {@link SQLiteOpenHelper}.
	 *
	 * @return Context
	 */
	public Context getContext() {
		return this.mContext;
	}

	/**
	 * Hook to replace any of the generated TableHelpers with your own.
	 *
	 * @return
	 */
	protected TableHelper[] getTableHelpers() {
		return getDbFactory().getTableHelpers();
	}

	/**
	 * Calls {@link TableHelper#onCreate(SQLiteDatabase)} for each TableHelper.
	 *
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		for (TableHelper th : getTableHelpers()) {
			th.onCreate(db);
		}
	}

	/**
	 * Calls a method on each TableHelper depending on the {@link UpgradeStrategy}.
	 * In order to prevent recursive calls to getDatabase(), this method must pass
	 * the db parameter through to any other methods that need it.
	 *
	 * @see https://code.google.com/p/storm-gen/issues/detail?id=11
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (getUpgradeStrategy()) {
		case DROP_CREATE:
			this.dropAndCreate(db);
			break;
		case BACKUP_RESTORE:
			this.backupAndRestore(db, this.getContext());
			break;
		case UPGRADE:
			this.upgrade(db, oldVersion, newVersion);
			break;
		}
	}

	/**
	 * Calls {@link TableHelper#onUpgrade(SQLiteDatabase, int, int)} for
	 * each TableHelper. Override this method to implement your own
	 * upgrade strategy.
	 *
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (TableHelper th : getTableHelpers()) {
			th.onUpgrade(db, oldVersion, newVersion);
		}
	}

	/**
	 * Backs up all tables to CSV, drops and recreates them, then restores
	 * them from CSV.
	 * @param db 
	 * @param ctx 
	 */
	public void backupAndRestore(SQLiteDatabase db, Context ctx) {
		for (TableHelper th : getTableHelpers()) {
			th.backupAndRestore(db, ctx);
		}
	}

	/**
	 * Drops and recreates all tables.
	 * @param db 
	 */
	public void dropAndCreate(SQLiteDatabase db) {
		for (TableHelper th : getTableHelpers()) {
			th.dropAndCreate(db);
		}
	}

	/**
	 * Convenience method for testing or app backups
	 * 
	 * @param db
	 * @param ctx
	 */
	public void backupAllTablesToCsv(SQLiteDatabase db, Context ctx) {
		for (TableHelper table : getTableHelpers()) {
			table.backup(db, ctx);
		}
	}

	/**
	 * Convenience method for testing or app restores
	 * 
	 * @param db
	 * @param ctx
	 */
	public void restoreAllTablesFromCsv(SQLiteDatabase db, Context ctx) {
		for (TableHelper table : getTableHelpers()) {
			table.restore(db, ctx);
		}
	}

	/**
	 * Returns the {@link DatabaseFactory} from which this instance
	 * was obtained.
	 *
	 * @return DatabaseFactory
	 */
	public DatabaseFactory getDbFactory() {
		return dbFactory;
	}

	private void setDbFactory(DatabaseFactory dbFactory) {
		this.dbFactory = dbFactory;
	}

}
