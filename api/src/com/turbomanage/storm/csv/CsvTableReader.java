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

import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TableHelper;


public class CsvTableReader {

	@SuppressWarnings("rawtypes")
	protected TableHelper th;
	private int[] colMap;
	private String[] defaultValues;
	private InsertHelper insertHelper;

	@SuppressWarnings("rawtypes")
	public CsvTableReader(TableHelper tableHelper) {
		this.th = tableHelper;
	}
	
	protected String getCsvFilename(DatabaseHelper dbHelper) {
		String dbName = dbHelper.getDbFactory().getName();
		String tableName = th.getTableName();
		int version = dbHelper.getDbFactory().getVersion();
		return String.format("%s.v%d.%s", dbName, version, tableName);
	}

	public int importFromCsv(DatabaseHelper dbHelper) {
		String filename = getCsvFilename(dbHelper);
		FileInputStream fileInputStream;
		try {
			fileInputStream = dbHelper.getContext().openFileInput(filename);
			return importFromCsv(dbHelper, fileInputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int importFromCsv(DatabaseHelper dbHelper, InputStream is) {
		int numInserts = 0;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		insertHelper = new DatabaseUtils.InsertHelper(db,
				th.getTableName());
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String headerRow = reader.readLine();
			colMap = parseCsvHeader(headerRow);
			defaultValues = th.getDefaultValues();
			String csvRow = reader.readLine();
			while (csvRow != null) {
				long rowId = parseAndInsertRow(csvRow);
				if (rowId < 0) {
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
	 * Populate an array containing the value for each column
	 * from a CSV row where the columns may be in different order.
	 * 
	 * @param textValues One row from the CSV file
	 * @return
	 */
	private String[] mapValuesToTable(List<String> textValues) {
		String[] rowValues = defaultValues.clone();
		for (int i=0; i < rowValues.length; i++) {
			int csvPos = colMap[i];
			if (csvPos >= 0 ) {
				rowValues[i] = textValues.get(colMap[i]);
			}
		}
		return rowValues;
	}

	/**
	 * Map each column in the CSV to a column in the table.
	 * Assumes that {@link TableHelper#getColumns()} are the same 
	 * order as {@link InsertHelper#getColumnIndex(String)}.
	 * 
	 * @param headerRow
	 * @return An array containing the CSV col # for each table column
	 *         or -1 for missing columns
	 */
	private int[] parseCsvHeader(String headerRow) {
		List<String> csvCols = Arrays.asList(headerRow.split(","));
		int numCols = th.getColumns().length;
		colMap = new int[numCols];
		// Iterate over table columns and assign index of each csv column
		for (int i = 0; i < numCols; i++) {
			String colName = th.getColumns()[i].toString();
			int csvPos = csvCols.indexOf(colName);
			colMap[i] = csvPos;
		}
		return colMap;
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
		String[] rowValues = mapValuesToTable(textValues);
		th.bindRowValues(insertHelper, rowValues);
		return insertHelper.execute();
	}

}
