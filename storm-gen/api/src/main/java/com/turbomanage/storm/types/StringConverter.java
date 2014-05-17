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
import com.turbomanage.storm.types.TypeConverter.BindType;
import com.turbomanage.storm.types.TypeConverter.SqlType;

@Converter(forTypes = { String.class }, bindType = BindType.STRING, sqlType = SqlType.TEXT)
public class StringConverter extends TypeConverter<String,String> {

	public static final StringConverter GET = new StringConverter();

	@Override
	public String toSql(String javaValue) {
		return javaValue;
	}

	@Override
	public String fromSql(String sqlValue) {
		return sqlValue;
	}

	@Override
	public String fromString(String strValue) {
		return strValue;
	}

}
