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
package com.turbomanage.storm.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;

import com.turbomanage.storm.TableHelper;

/**
 * Contains methods used to restore a table from a CSV file. Each
 * instance is associated with one table.
 *
 * @author David M. Chandler
 */
public class CsvTableReader {

	@SuppressWarnings("rawtypes")
	protected TableHelper th;
	private InsertHelper insertHelper;

	/**
	 * Constructor requires a corresponding {@link TableHelper} class.
	 *
	 * @param tableHelper
	 */
	@SuppressWarnings("rawtypes")
	public CsvTableReader(TableHelper tableHelper) {
		this.th = tableHelper;
	}

	protected String getCsvFilename(String dbPath, int dbVersion, String suffix) {
		String[] path = dbPath.split("/");
		String dbName = path[path.length-1];
		String tableName = th.getTableName();
		return String.format("%s.v%d.%s%s", dbName, dbVersion, tableName, suffix);
	}

	/**
	 * Attempts to import a database table from a CSV file in the default
	 * location.
	 * 
	 * @param ctx
	 * @param db
	 * @param suffix
	 * 
	 * @return count of rows imported or -1 if error
	 */
	public int importFromCsv(Context ctx, SQLiteDatabase db, String suffix) {
		String filename = getCsvFilename(db.getPath(), db.getVersion(), suffix);
		FileInputStream fileInputStream;
		try {
			fileInputStream = ctx.openFileInput(filename);
			return importFromCsv(db, fileInputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Attempts to import a database table from an {@link InputStream}
	 * formatted as a CSV file. Does all inserts in a single transaction
	 * for efficiency and so that all inserts will succeed or fail
	 * together.
	 *
	 * @param db
	 * @param is
	 * @return count of rows imported or -1 if error
	 */
	public int importFromCsv(SQLiteDatabase db, InputStream is) {
		int numInserts = 0;
		db.beginTransaction();
		insertHelper = new DatabaseUtils.InsertHelper(db,
				th.getTableName());
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String headerRow = reader.readLine();
			String csvRow = reader.readLine();
			while (csvRow != null) {
				long rowId = parseAndInsertRow(csvRow);
				if (rowId == -1L) {
					throw new RuntimeException("Error after row " + numInserts);
				}
				numInserts++;
				csvRow = reader.readLine();
			}
			db.setTransactionSuccessful();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return numInserts;
	}

	/**
	 * Parse the values in a CSV row, map them to the table column
	 * order, and insert. Uses {@link InsertHelper} so it may be
	 * called repeatedly within a transaction for max performance.
	 *
	 * @param csvRow
	 * @return row ID of the newly inserted row or -1
	 */
	private long parseAndInsertRow(String csvRow) {
		List<String> textValues = CsvUtils.getValues(csvRow);
		insertHelper.prepareForInsert();
        String[] rowValues = textValues.toArray(new String[textValues.size()]);
		th.bindRowValues(insertHelper, rowValues);
		return insertHelper.execute();
	}

}
