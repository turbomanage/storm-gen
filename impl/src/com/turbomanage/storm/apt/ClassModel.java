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
package com.turbomanage.storm.apt;

import java.util.ArrayList;
import java.util.List;

import com.turbomanage.storm.apt.converter.ConverterModel;
import com.turbomanage.storm.apt.entity.FieldModel;

public abstract class ClassModel {

	private String className;
	private String packageName;
	protected List<String> imports = new ArrayList<String>();
	protected List<FieldModel> fields = new ArrayList<FieldModel>();

	public List<String> getImports() {
		return imports;
	}

	public List<FieldModel> getFields() {
		return fields;
	}

	public void addField(String fieldName, String javaType, boolean isEnum, ConverterModel converter) {
		FieldModel field = new FieldModel(fieldName, javaType, isEnum, converter);
		fields.add(field);
		if (isEnum) {
			return;
		}
		// add import for converter if needed
		String converterType = field.getQualifiedConverterClass();
		if (!imports.contains(converterType)) {
			imports.add(converterType);
		}
	}

	public void addImport(String importPath) {
		imports.add(importPath);
	}

	/**
	 * @return Name of the generated class
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Package name of the generated class
	 */
	public String getPackage() {
		return this.packageName;
	}

	/**
	 * Returns the fully qualified class name of the generated class.
	 *
	 * @return Name of generated class with package prepended
	 */
	public String getQualifiedClassName() {
		return this.getPackage() + "." + this.getClassName();
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	protected String capFirst(String anyName) {
		String capFirstLetter = anyName.substring(0, 1).toUpperCase();
		return capFirstLetter + anyName.substring(1);
	}

	protected void parseQualifiedClassName(String helperClass) {
		int lastDot = helperClass.lastIndexOf('.');
		if (lastDot < 1) {
			throw new IllegalArgumentException("The default package is not allowed for type " + helperClass);
		}
		String pkg = helperClass.substring(0, lastDot);
		this.setPackageName(pkg);
		this.setClassName(helperClass.substring(lastDot + 1));
	}

}
