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

import android.util.Base64;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.types.TypeConverter.BindType;
import com.turbomanage.storm.types.TypeConverter.SqlType;

@Converter(forTypes = { byte[].class }, bindType = BindType.BLOB, sqlType = SqlType.BLOB)
public class BlobConverter extends TypeConverter<byte[], byte[]> {

	public static final BlobConverter GET = new BlobConverter();

	@Override
	public byte[] toSql(byte[] javaValue) {
		return javaValue;
	}

	@Override
	public byte[] fromSql(byte[] sqlValue) {
		return sqlValue;
	}

	@Override
	public byte[] fromString(String strValue) {
		return Base64.decode(strValue, Base64.DEFAULT);
	}

	@Override
	public String toString(byte[] sqlValue) {
		return (sqlValue == null) ? null : Base64.encodeToString(sqlValue, Base64.NO_WRAP);
	}

}
