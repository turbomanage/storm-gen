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
import com.turbomanage.storm.api.Persistable;
import com.turbomanage.storm.types.BooleanConverter;
import com.turbomanage.storm.types.ByteConverter;
import com.turbomanage.storm.types.CharConverter;
import com.turbomanage.storm.types.EnumConverter;
import com.turbomanage.storm.types.IntegerConverter;
import com.turbomanage.storm.types.LongConverter;
import com.turbomanage.storm.types.ShortConverter;

public class FilterBuilder<T extends Persistable> {

	private static final String TAG = FilterBuilder.class.getName();
	private SQLiteDao<T> dao;
	private List<Predicate> where = new ArrayList<Predicate>();

	public FilterBuilder(SQLiteDao<T> dao) {
		this.dao = dao;
	}

	/*
	 * Convenience methods for comparing equality of each wrapper type
	 */

	public FilterBuilder<T> eq(Column colName, Boolean param) {
		Integer sqlValue = BooleanConverter.GET.toSql(param);
		where.add(new Equality(colName, BooleanConverter.GET.toString(sqlValue)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, Byte param) {
		Short sqlValue = ByteConverter.GET.toSql(param);
		where.add(new Equality(colName, ByteConverter.GET.toString(sqlValue)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, byte[] param) {
		throw new IllegalArgumentException("Exact match on type byte[] is not supported");
	}

	public FilterBuilder<T> eq(Column colName, Character param) {
		Integer sqlValue = CharConverter.GET.toSql(param);
		where.add(new Equality(colName, CharConverter.GET.toString(sqlValue)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, Double param) {
		throw new IllegalArgumentException("Exact match on type double is not supported");
	}

	public FilterBuilder<T> eq(Column colName, Enum param) {
		String sqlValue = EnumConverter.GET.toSql(param);
		where.add(new Equality(colName, sqlValue));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, Float param) {
		throw new IllegalArgumentException("Exact match on type float is not supported");
	}

	public FilterBuilder<T> eq(Column colName, Integer param) {
		where.add(new Equality(colName, IntegerConverter.GET.toString(param)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, Long param) {
		where.add(new Equality(colName, LongConverter.GET.toString(param)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, Short param) {
		where.add(new Equality(colName, ShortConverter.GET.toString(param)));
		return this;
	}

	public FilterBuilder<T> eq(Column colName, String param) {
		where.add(new Equality(colName, param));
		return this;
	}

	/**
	 * Execute the query using the attached DAO
	 *
	 * @return Cursor result
	 */
	public Cursor exec() {
		return dao.query(where(), params());
	}

	public T get() {
		return dao.asObject(this.exec());
	}

	public List<T> list() {
		return dao.asList(this.exec());
	}

	/**
	 * Convert the params in each predicate to String[]
	 * used by the query methods
	 *
	 * @return String[] parameters
	 */
	private String[] params() {
		String[] params = new String[this.where.size()];
		for (int i = 0; i < params.length; i++) {
			Predicate p = this.where.get(i);
			params[i] = p.getParam();
		}
		return params;
	}

	/**
	 * Convert the SQL conditions in each predicate by ANDing together
	 * into a single SQL WHERE clause with ? for each parameter
	 *
	 * @return String SQL WHERE clause
	 */
	private String where() {
		StringBuilder sqlWhere = new StringBuilder();
		for (Predicate p : where) {
			sqlWhere.append(" AND ");
			sqlWhere.append(p.getSqlOp() + "?");
		}
		return sqlWhere.toString().substring(5);
	}

}
