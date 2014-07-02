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

import com.turbomanage.storm.TableHelper;
import com.turbomanage.storm.TableHelper.Column;

/**
 * Contains methods used to dump a table to a CSV file. Each
 * instance is associated with one table.
 *
 * @author David M. Chandler
 */
public class CsvTableWriter extends CsvTableReader {

	/**
	 * Constructor requires a corresponding {@link TableHelper} class.
	 * @param tableHelper
	 */
	@SuppressWarnings("rawtypes")
	public CsvTableWriter(TableHelper tableHelper) {
		super(tableHelper);
	}

	private String buildCsvRow(Cursor c) {
		StringBuilder sb = new StringBuilder();
		String[] values = th.getRowValues(c);
		for (String val : values) {
			sb.append(',');
			sb.append(CsvUtils.escapeCsv(val));
		}
		return sb.toString().substring(1);
	}

	private String buildHeaderRow() {
		// Write column names in first row
		StringBuilder sb = new StringBuilder();
        // TODO write old #, name, type from saved schema
		Column[] cols = th.getColumns();
		for (Column col : cols) {
			sb.append(',');
			sb.append(col.toString());
		}
		return sb.toString().substring(1);
	}

	/**
	 * Dumps a database table to a CSV file in the default location.
	 * Returns the number of rows written to the file.
	 * 
	 * @param ctx
	 * @param db
	 * @param suffix 
	 *
	 * @return count of rows in the exported file
	 */
	public int dumpToCsv(Context ctx, SQLiteDatabase db, String suffix) throws FileNotFoundException {
		int numRowsWritten = 0;
		Cursor c;
		String filename = getCsvFilename(db.getPath(), db.getVersion(), suffix);
		c = db.query(th.getTableName(), null, null, null, null, null, null);
		FileOutputStream fos;
		fos = ctx.openFileOutput(filename, 0);
		PrintWriter printWriter = new PrintWriter(fos);
		String headerRow = buildHeaderRow();
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
	}

}