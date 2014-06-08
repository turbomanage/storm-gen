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


@Converter(forTypes = { boolean.class, Boolean.class }, bindType = BindType.INT, sqlType = SqlType.INTEGER)
public class BooleanConverter extends TypeConverter<Boolean,Integer> {

	public static final BooleanConverter GET = new BooleanConverter();

	@Override
	public Integer toSql(Boolean javaValue) {
		if (javaValue == null)
			return null;
		return (javaValue==Boolean.TRUE) ? 1 : 0;
	}

	@Override
	public Boolean fromSql(Integer sqlValue) {
		if (sqlValue == null)
			return null;
		return sqlValue==0 ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public Integer fromString(String strValue) {
		if (strValue == null)
			return null;
		return Integer.valueOf(strValue);
	}

}
