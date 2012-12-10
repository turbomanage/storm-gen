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

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.turbomanage.storm.SQLiteDao;
import com.turbomanage.storm.api.Entity;
import com.turbomanage.storm.api.Persistable;
import com.turbomanage.storm.apt.ClassProcessor;
import com.turbomanage.storm.apt.StormEnvironment;
import com.turbomanage.storm.apt.converter.ConverterModel;
import com.turbomanage.storm.apt.database.DatabaseModel;
import com.turbomanage.storm.exception.TypeNotSupportedException;

public class EntityProcessor extends ClassProcessor {

	private static final String TAG = EntityProcessor.class.getName();
	private EntityModel entityModel;

	public EntityProcessor(Element el, StormEnvironment stormEnv) {
		super(el, stormEnv);
	}

	@Override
	public EntityModel getModel() {
		return this.entityModel;
	}

	@Override
	public void populateModel() {
		this.entityModel = new EntityModel();
		super.populateModel();
		this.entityModel.addImport(getQualifiedClassName());
		chooseBaseDao();
		chooseDatabase();
		readFields(typeElement);
	}

	protected void chooseBaseDao() {
		List<String> iNames = super.inspectInterfaces();
//		TODO Choose Dao base class based on interfaces
		if (iNames.contains(Persistable.class.getName())) {
			this.entityModel.setBaseDaoClass(SQLiteDao.class);
		} else {
			stormEnv.getLogger().error(TAG + ": Entities must implement Persistable", this.typeElement);
		}
	}

	protected void chooseDatabase() {
		Entity entity = this.typeElement.getAnnotation(Entity.class);
		DatabaseModel defaultDb = stormEnv.getDefaultDb();
		if (entity.dbName().length() > 0) {
			// Add db to entity model and vice versa
			String dbName = entity.dbName();
			DatabaseModel db = stormEnv.getDbByName(dbName);
			if (db != null) {
				this.entityModel.setDatabase(db);
			} else {
				stormEnv.getLogger().error(TAG + ": There is no @Database named " + dbName, this.typeElement);
			}
		} else if (defaultDb != null) {
			this.entityModel.setDatabase(defaultDb);
		} else {
			stormEnv.getLogger().error(TAG + ": You must define at least one @Database", this.typeElement);
		}
	}

	@Override
	protected void inspectField(VariableElement field) {
		Set<Modifier> modifiers = field.getModifiers();
		if (!modifiers.contains(Modifier.TRANSIENT)) {
			String javaType = getFieldType(field);
			if (TypeKind.DECLARED.equals(field.asType().getKind())) {
				DeclaredType type = (DeclaredType) field.asType();
				TypeElement typeElement = (TypeElement) type.asElement();
				TypeMirror superclass = typeElement.getSuperclass();
				if (ElementKind.ENUM.equals(typeElement.getKind())) {
					entityModel.addField(field.getSimpleName().toString(), javaType, true, stormEnv.getConverterForType("java.lang.Enum"));
					return;
				}
			}
			// Verify supported type
			try {
				ConverterModel converter = stormEnv.getConverterForType(javaType);
				entityModel.addField(field.getSimpleName().toString(), javaType, false, converter);
			} catch (TypeNotSupportedException e) {
				stormEnv.getLogger().error(TAG + "inspectField", e, field);
			} catch (Exception e) {
				stormEnv.getLogger().error(TAG, e, field);
			}
			// TODO verify getter + setter
		}
	}

}
