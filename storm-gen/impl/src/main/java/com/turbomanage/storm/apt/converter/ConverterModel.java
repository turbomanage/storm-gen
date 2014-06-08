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
package com.turbomanage.storm.apt.converter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.apt.ClassModel;
import com.turbomanage.storm.apt.ProcessorLogger;
import com.turbomanage.storm.csv.CsvUtils;
import com.turbomanage.storm.types.TypeConverter;
import com.turbomanage.storm.types.TypeConverter.BindType;
import com.turbomanage.storm.types.TypeConverter.SqlType;

public class ConverterModel extends ClassModel {

	private static final String TYPE_DELIMITER = ";";
	private String[] convertibleTypes;
	private BindType bindType;
	private SqlType sqlType;
	private boolean builtIn;

	/**
	 * Construct from attributes provided by the annotation processor
	 * or index reader.
	 *
	 * @param converterClass
	 * @param types
	 * @param bindType
	 * @param sqlType
	 */
	public ConverterModel(String converterClass,
			String[] types, BindType bindType, SqlType sqlType) {
		this.parseQualifiedClassName(converterClass);
		this.convertibleTypes = types;
		this.bindType = bindType;
		this.sqlType = sqlType;
	}

	/**
	 * Construct from a built-in converter on the classpath.
	 *
	 * @param converter
	 */
	public ConverterModel(TypeConverter converter) {
		this.builtIn = true;
		this.parseQualifiedClassName(converter.getClass().getCanonicalName());
		Converter annotation = converter.getClass().getAnnotation(Converter.class);
		this.bindType = annotation.bindType();
		this.sqlType = annotation.sqlType();
		this.convertibleTypes = new String[annotation.forTypes().length];
		int i = 0;
		for (Class convertibleType : annotation.forTypes()) {
			this.convertibleTypes[i++] = convertibleType.getCanonicalName();
		}
	}

	public String[] getConvertibleTypes() {
		return convertibleTypes;
	}

	public BindType getBindType() {
		return bindType;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public static ConverterModel readFromIndex(String convInfo, ProcessorLogger logger) throws IOException {
		Map<String, String> props = CsvUtils.getAsMap(convInfo);
		String converterClass = props.get("converterClass");
		BindType bindType = BindType.valueOf(props.get("bindType"));
		SqlType sqlType = SqlType.valueOf(props.get("sqlType"));
		String[] convertibleTypes = props.get("convertibleTypes").split(TYPE_DELIMITER);
		return new ConverterModel(converterClass, convertibleTypes, bindType, sqlType);
	}

	public void writeToIndex(PrintWriter out) {
		// Don't write the built-in converters, causes dups
		if (builtIn)
			return;
		String typeList = new String();
		for (String type : this.convertibleTypes) {
			typeList += TYPE_DELIMITER + type;
		}
		typeList = typeList.substring(1);
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("converterClass", this.getQualifiedClassName());
		map.put("bindType", this.bindType.name());
		map.put("sqlType", this.sqlType.name());
		map.put("convertibleTypes", typeList);
		out.println(CsvUtils.mapToCsv(map));
	}

	@Override
	public int hashCode() {
		return getQualifiedClassName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO compare all fields to ensure index up to date
		// but how to replace a prior version with different values?
		if (obj instanceof ConverterModel) {
			ConverterModel cm = (ConverterModel) obj;
			return getQualifiedClassName().equals(cm.getQualifiedClassName());
		}
		return false;
	}

}