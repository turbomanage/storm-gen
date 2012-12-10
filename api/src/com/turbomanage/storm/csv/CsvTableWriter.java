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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TableHelper;

public class CsvTableWriter extends CsvTableReader {

	@SuppressWarnings("rawtypes")
	public CsvTableWriter(TableHelper tableHelper) {
		super(tableHelper);
	}

	private String buildCsvRow(Cursor c) {
		StringBuilder sb = new StringBuilder();
		String[] values = th.getRowValues(c);
		for (int i = 0; i < c.getColumnCount(); i++) {
			String value = values[i];
			sb.append(',');
			sb.append(CsvUtils.escapeCsv(value));
		}
		return sb.toString().substring(1);
	}

	private String buildHeaderRow(Cursor c) {
		// Write column names in first row
		StringBuilder sb = new StringBuilder();
		String[] cols = c.getColumnNames();
		for (String colName : cols) {
			sb.append(',');
			sb.append(colName);
		}
		return sb.toString().substring(1);
	}

	public int dumpToCsv(DatabaseHelper dbHelper) {
		int numRowsWritten = 0;
		String filename = getCsvFilename(dbHelper);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query(th.getTableName(), null, null, null, null, null, null);
		FileOutputStream fos;
		try {
			Context ctx = dbHelper.getContext();
			fos = ctx.openFileOutput(filename, 0);
			PrintWriter printWriter = new PrintWriter(fos);
			String headerRow = buildHeaderRow(c);
			printWriter.println(headerRow);
			for (boolean hasItem = c.moveToFirst(); hasItem; hasItem = c
					.moveToNext()) {
				String csv = buildCsvRow(c);
				printWriter.println(csv);
				numRowsWritten++;
			}
			printWriter.flush();
			printWriter.close();
			return numRowsWritten;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
