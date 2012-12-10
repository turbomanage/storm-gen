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

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Base class that converts a Java type to one of the four valid
 * SQLite representations. To write a custom converter, extend
 * this class and annotate it with @Convert. Besides the abstract
 * methods, each concrete TypeConverter class must also have a
 * static field named GET that is used by generated code to obtain
 * an instance of the converter. See {@link BooleanConverter} for
 * an example.
 * 
 * @author David M. Chandler
 *
 * @param <J> native Java type
 * @param <S> SQL type (Integer, Double, String, or byte[])
 */
public abstract class TypeConverter<J extends Object, S extends Object> {
	
	public enum SqlType {INTEGER, REAL, BLOB, TEXT};
	
	/**
	 * Enum representing the types for which there are corresponding methods
	 * {@link Cursor} and bind methods. Never called at runtime.
	 * 
	 * @author David M. Chandler
	 */
	public enum BindType {BLOB, DOUBLE, FLOAT, INT, LONG, SHORT, STRING}
	
	/**
	 * Convert a Java value to a representation that can
	 * be put into a {@link ContentValues} map.
	 *  
	 * @param javaValue
	 * @return
	 */
	public abstract S toSql(J javaValue);
	
	/**
	 * Convert a value obtained from the {@link Cursor} getS method
	 * to its Java type.
	 * 
	 * @param sqlValue
	 * @return
	 */
	public abstract J fromSql(S sqlValue);

	/**
	 * Convert a value from a String to its {@link SqlType}. 
	 * This method is used by the CSV importer.
	 * 
	 * @param strValue
	 * @return 
	 */
	public abstract S fromString(String strValue);

	/**
	 * Convert a value from its SQL type to a String. This
	 * method is used by the CSV exporter.
	 * 
	 * @param sqlValue
	 * @return
	 */
	public String toString(S sqlValue) {
		return (sqlValue == null) ? null : sqlValue.toString();
	}
	
}
