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
package com.turbomanage.storm.query;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.turbomanage.storm.SQLiteDao;
import com.turbomanage.storm.TableHelper.Column;
import com.turbomanage.storm.types.BooleanConverter;
import com.turbomanage.storm.types.ByteConverter;
import com.turbomanage.storm.types.CharConverter;
import com.turbomanage.storm.types.EnumConverter;
import com.turbomanage.storm.types.IntegerConverter;
import com.turbomanage.storm.types.LongConverter;
import com.turbomanage.storm.types.ShortConverter;

/**
 * Builds a SQL query constructed by ANDing together all conditions.
 *
 * @author David M. Chandler
 *
 * @param <T>
 */
public class Query<T> {

	private static final String TAG = Query.class.getName();
	private SQLiteDao<T> dao;
	private StringBuilder where = new StringBuilder();
	protected List<String> params = new ArrayList<String>();
	protected String orderBy;

	/**
	 * Constructor requires the {@link SQLiteDao} that will be
	 * used to execute the completed query.
	 *
	 * @param dao
	 */
	public Query(SQLiteDao<T> dao) {
		this.dao = dao;
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
	public Query<T> byExample(T obj) {
		if (obj != null) {
			return dao.getTableHelper().buildFilter(this, obj);
		}
		return this;
	}

	/*
	 * Convenience methods for comparing equality of each wrapper type
	 */

	public Query<T> eq(Column colName, Boolean param) {
		Integer sqlValue = BooleanConverter.GET.toSql(param);
		where.append(" AND " + colName + "=?");
		params.add(BooleanConverter.GET.toString(sqlValue));
		return this;
	}

	public Query<T> eq(Column colName, Byte param) {
		Short sqlValue = ByteConverter.GET.toSql(param);
		where.append(" AND " + colName + "=?");
		params.add(ByteConverter.GET.toString(sqlValue));
		return this;
	}

	public Query<T> eq(Column colName, byte[] param) {
		throw new IllegalArgumentException("Exact match on type byte[] is not supported");
	}

	public Query<T> eq(Column colName, Character param) {
		Integer sqlValue = CharConverter.GET.toSql(param);
		where.append(" AND " + colName + "=?");
		params.add(CharConverter.GET.toString(sqlValue));
		return this;
	}

	public Query<T> eq(Column colName, Double param) {
		throw new IllegalArgumentException("Exact match on type double is not supported");
	}

	public Query<T> eq(Column colName, Enum param) {
		String sqlValue = EnumConverter.GET.toSql(param);
		where.append(" AND " + colName + "=?");
		params.add(sqlValue);
		return this;
	}

	public Query<T> eq(Column colName, Float param) {
		throw new IllegalArgumentException("Exact match on type float is not supported");
	}

	public Query<T> eq(Column colName, Integer param) {
		where.append(" AND " + colName + "=?");
		params.add(IntegerConverter.GET.toString(param));
		return this;
	}

	public Query<T> eq(Column colName, Long param) {
		where.append(" AND " + colName + "=?");
		params.add(LongConverter.GET.toString(param));
		return this;
	}

	public Query<T> eq(Column colName, Short param) {
		where.append(" AND " + colName + "=?");
		params.add(ShortConverter.GET.toString(param));
		return this;
	}

	public Query<T> eq(Column colName, String param) {
		where.append(" AND " + colName + "=?");
		params.add(param);
		return this;
	}

	/**
	 * Executes the query using the attached DAO.
	 * Calling method MUST close the Cursor!
	 *
	 * @return Cursor result
	 */
	public Cursor exec() {
		return dao.query(where(), params(), orderBy);
	}

	/**
	 * Executes the query and returns the result as an
	 * object.
	 *
	 * @see SQLiteDao#asObject(Cursor)
	 * @return The matching entity or null
	 */
	public T get() {
		return dao.asObject(this.exec());
	}

	public Query<T> in(Column colName, String... values) {
		// WHERE colName IN (?,?,?)
		if (values.length > 0) {
			String valueList = "(?";
			params.add(values[0]);
			for (int i = 1; i < values.length; i++) {
				valueList += ",?";
				params.add(values[i]);
			}
			valueList += ")";
			where.append(" AND " + colName + " IN " + valueList);
		}
		return this;
	}

	/**
	 * Executes the query and returns the result as a {@link java.util.List}.
	 *
	 * @see SQLiteDao#asList(Cursor)
	 * @return A List of matching entities or null
	 */
	public List<T> list() {
		return dao.asList(this.exec());
	}

	public Query<T> order(String...columns) {
		if (columns.length < 1) {
			throw new IllegalArgumentException();
		}
		StringBuilder orderBy = new StringBuilder(columns[0]);
		for (int i = 1; i < columns.length; i++) {
			String col = columns[i];
			orderBy.append(", " + col);
		}
		this.orderBy = orderBy.toString();
		return this;
	}
	
	/**
	 * Convert the params in each predicate to String[]
	 * used by the query methods
	 *
	 * @return String[] parameters
	 */
	String[] params() {
		return params.toArray(new String[params.size()]);
	}

	/**
	 * Convert the SQL conditions in each predicate by ANDing together
	 * into a single SQL WHERE clause with ? for each parameter
	 *
	 * @return String SQL WHERE clause
	 */
	String where() {
		if (where.length() > 0) {
			return where.substring(5).toString();
		}
		return null;
	}

}
