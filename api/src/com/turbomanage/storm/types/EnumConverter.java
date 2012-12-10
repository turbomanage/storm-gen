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
package com.turbomanage.storm.types;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.query.FilterBuilder;
import com.turbomanage.storm.types.TypeConverter.BindType;
import com.turbomanage.storm.types.TypeConverter.SqlType;

/**
 * The annotations in this converter are used to generate
 * inline enum conversion code in the TableHelper. The toSql() method
 * is used by {@link FilterBuilder}. Otherwise, the methods in this
 * class are not actually used.
 *
 * @author David M. Chandler
 */
@Converter(forTypes = { Enum.class }, bindType = BindType.STRING, sqlType = SqlType.TEXT)
public class EnumConverter extends TypeConverter<Enum, String> {

	public static final EnumConverter GET = new EnumConverter();

	@Override
	public String toSql(Enum javaValue) {
		return javaValue.name();
	}

	@Override
	public Enum fromSql(String sqlValue) {
		return null;
	}

	@Override
	public String fromString(String strValue) {
		return strValue;
	}

}