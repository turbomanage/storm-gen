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
package com.turbomanage.storm.apt.database;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import com.turbomanage.storm.api.Database;
import com.turbomanage.storm.apt.ClassProcessor;
import com.turbomanage.storm.apt.StormEnvironment;

public class DatabaseProcessor extends ClassProcessor {

	private DatabaseModel databaseModel;

	public DatabaseProcessor(Element el, StormEnvironment stormEnv) {
		super(el, stormEnv);
	}

	@Override
	public DatabaseModel getModel() {
		return this.databaseModel;
	}

	@Override
	public void populateModel() {
		Database dba = this.typeElement.getAnnotation(Database.class);
		databaseModel = new DatabaseModel(dba.name(), dba.version(), getQualifiedClassName());
		super.populateModel();
	}

	@Override
	protected void inspectField(VariableElement field) {
		// None to inspect
	}

}
