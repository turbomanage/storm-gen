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

@Converter(forTypes = { double.class, Double.class }, bindType = BindType.DOUBLE, sqlType = SqlType.REAL)
public class DoubleConverter extends TypeConverter<Double,Double> {

	public static final DoubleConverter GET = new DoubleConverter();

	@Override
	public Double toSql(Double javaValue) {
		return javaValue;
	}

	@Override
	public Double fromSql(Double sqlValue) {
		return sqlValue;
	}

	@Override
	public Double fromString(String strValue) {
		// use long bits as hex to preserve exact value
		return Double.longBitsToDouble(Long.parseLong(strValue, 16));
	}

	@Override
	public String toString(Double sqlValue) {
		// use long bits as hex to preserve exact value
		// Don't use Long.toHexString! Long.parseLong doesn't understand 2's complement
		return (sqlValue == null) ? null : Long.toString(Double.doubleToLongBits(sqlValue), 16);
	}
}
