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

import com.turbomanage.storm.TableHelper.Column;

/**
 * A condition in a SQL WHERE clause
 * 
 * @author David M. Chandler
 */
public abstract class Predicate {

	protected Column colName;
	protected String param;

	public Predicate(Column colName, String param) {
		this.colName = colName;
		this.param = (String) param;
	}
	
	/**
	 * Left side of SQL condition. May wrap colName with a function.
	 * Example:
	 * "hex(blobCol) ="
	 * 
	 * @return String SQL
	 */
	public abstract String getSqlOp();
	
	/**
	 * @return String parameter
	 */
	String getParam() {
		return param;
	}
	
}
