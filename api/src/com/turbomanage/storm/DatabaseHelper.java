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
		DROP_CREATE, BACKUP_RESTORE, UPGRADE
	}

	private DatabaseFactory dbFactory;
	protected Context mContext;

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

	protected abstract UpgradeStrategy getUpgradeStrategy();

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

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (TableHelper th : getTableHelpers()) {
			th.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (getUpgradeStrategy()) {
		case DROP_CREATE:
			this.dropAndCreate();
			break;
		case BACKUP_RESTORE:
			this.backupAndRestore();
			break;
		case UPGRADE:
			this.upgrade(db, oldVersion, newVersion);
			break;
		}
	}

	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (TableHelper th : getTableHelpers()) {
			th.onUpgrade(db, oldVersion, newVersion);
		}
	}

	public void backupAndRestore() {
		for (TableHelper th : getTableHelpers()) {
			th.backupAndRestore(this);
		}
	}

	public void dropAndCreate() {
		for (TableHelper th : getTableHelpers()) {
			th.dropAndCreate(this);
		}
	}

	/**
	 * Convenience method for testing or app backups
	 */
	public void backupAllTablesToCsv() {
		for (TableHelper table : getTableHelpers()) {
			table.backup(this);
		}
	}

	/**
	 * Convenience method for testing or app restores
	 */
	public void restoreAllTablesFromCsv() {
		for (TableHelper table : getTableHelpers()) {
			table.restore(this);
		}
	}

	public DatabaseFactory getDbFactory() {
		return dbFactory;
	}

	private void setDbFactory(DatabaseFactory dbFactory) {
		this.dbFactory = dbFactory;
	}

}
