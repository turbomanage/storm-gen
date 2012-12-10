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
package com.turbomanage.storm.apt.entity;

import com.turbomanage.storm.SQLiteDao;
import com.turbomanage.storm.apt.ClassModel;
import com.turbomanage.storm.apt.database.DatabaseModel;


public class EntityModel extends ClassModel {

	private static final String TABLE_SUFFIX = "Table";
	private Class<SQLiteDao> baseDaoClass;
	private DatabaseModel dbModel;

	public String getEntityName() {
		return this.getClassName();
	}
	
	public String getDaoName() {
		return this.getEntityName() + "Dao";
	}
	
	public String getDaoPackage() {
		return this.getPackage() + ".dao";
	}

	public String getDbFactory() {
		return dbModel.getFactoryClass();
	}
	
	/**
	 * Provides the simple name of the base DAO class to templates.
	 * 
	 * @return String Simple name of the base DAO
	 */
	public String getBaseDaoName() {
		return this.baseDaoClass.getSimpleName();
	}

	protected Class<SQLiteDao> getBaseDaoClass() {
		return baseDaoClass;
	}

	protected void setBaseDaoClass(Class<SQLiteDao> daoClass) {
		this.baseDaoClass = daoClass;
		// add corresponding import
		this.addImport(daoClass.getCanonicalName());
	}

	void setDatabase(DatabaseModel dbModel) {
		this.dbModel = dbModel;
		dbModel.addEntity(this);
	}
	
	public String getTableHelperClass() {
		return getDaoPackage() + "." + getTableHelperName();
	}
	
	public String getTableHelperName() {
		return getTableName() + TABLE_SUFFIX;
	}

	public String getTableName() {
		// TODO Make configurable in @Entity
		return getEntityName();
	}

}
