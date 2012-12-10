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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Base class that introspects an annotated class using the Mirror API and
 * populates a model for use by a code generation template.
 *
 * @author David M. Chandler
 */
public abstract class ClassProcessor {

	protected TypeElement typeElement;
	protected StormEnvironment stormEnv;

	protected abstract ClassModel getModel();

	/**
	 * Constructor intended to be overridden by subclasses.
	 *
	 * @param el
	 * @param logger
	 */
	protected ClassProcessor(Element el, StormEnvironment stormEnv) {
		this.typeElement = (TypeElement) el;
		this.stormEnv = stormEnv;
	}

	/**
	 * Subclasses override to populate the model.
	 * Invoked by main annotation processor.
	 */
	protected void populateModel() {
		inspectClass();
	}

	protected void readFields(TypeElement type) {
		// Read fields from superclass if any
		TypeMirror superClass = type.getSuperclass();
		if (TypeKind.DECLARED.equals(superClass.getKind())) {
			DeclaredType superType = (DeclaredType) superClass;
			readFields((TypeElement) superType.asElement());
		}
		for (Element child : type.getEnclosedElements()) {
			if (child.getKind() == ElementKind.FIELD) {
				VariableElement field = (VariableElement) child;
				inspectField(field);
			}
		}
	}

	protected String getFieldType(VariableElement field) {
		TypeMirror fieldType = field.asType();
		return fieldType.toString();
	}

	protected void inspectClass() {
		this.getModel().setPackageName(getPackageName());
		this.getModel().setClassName(getClassName());
	}

	/**
	 * Subclasses override to inspect each field and possibly add it
	 * to the model. Invoked by main annotation processor.
	 * Default impl does nothing, just serves to make implementation
	 * optional for subclasses.
	 *
	 * @param field VariableElement that represents a class field
	 */
	protected void inspectField(VariableElement field) {
	}

	public String getQualifiedClassName() {
		String pkg = getPackageName();
		if (pkg == null || pkg.length() < 1)
			throw new IllegalArgumentException("The default package is not allowed for type " + getClassName());
		return getPackageName() + "." + getClassName();
	}

	protected String getPackageName() {
		PackageElement pkgEl = (PackageElement) this.typeElement.getEnclosingElement();
		return pkgEl.getQualifiedName().toString();
	}

	protected String getClassName() {
		return this.typeElement.getSimpleName().toString();
	}

	protected List<String> inspectInterfaces() {
		// get list of interfaces
		List<String> iNames = new ArrayList<String>();
		List<? extends TypeMirror> interfaces = this.typeElement.getInterfaces();
		for (TypeMirror i : interfaces) {
			String iName = i.toString();
			iNames.add(iName);
		}
		return iNames;
	}

}
