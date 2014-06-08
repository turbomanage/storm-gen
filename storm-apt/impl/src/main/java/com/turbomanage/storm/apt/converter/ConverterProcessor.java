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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.apt.ClassProcessor;
import com.turbomanage.storm.apt.StormEnvironment;
import com.turbomanage.storm.types.TypeConverter.BindType;
import com.turbomanage.storm.types.TypeConverter.SqlType;

public class ConverterProcessor extends ClassProcessor {

	private ConverterModel cm;

	public ConverterProcessor(Element el, StormEnvironment stormEnv) {
		super(el, stormEnv);
	}

	@Override
	protected ConverterModel getModel() {
		return cm;
	}

	@Override
	// TODO verify presence of static GET field
	public void populateModel() {
		String converterClass = this.typeElement.getQualifiedName().toString();
		Converter annotation = this.typeElement.getAnnotation(Converter.class);
		BindType bindType = annotation.bindType();
		SqlType sqlType = annotation.sqlType();
		// Read forTypes={byte.class,Byte.class} into String[]
		// This is complicated because you can't directly reference class values in annotations
		List<? extends AnnotationMirror> annoMirrors = this.typeElement
				.getAnnotationMirrors();
		for (AnnotationMirror anno : annoMirrors) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> annoValues = anno
					.getElementValues();
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annoValues.entrySet()) {
				String key = e.getKey().getSimpleName().toString();
				if ("forTypes".equals(key)) {
					AnnotationValue val = e.getValue();
					String[] types = val.accept(
							new ConverterTypeAnnotationValuesVisitor(),
							stormEnv.getLogger());
					this.cm = new ConverterModel(converterClass, types, bindType, sqlType);
					if (!stormEnv.addConverter(this.cm))
						stormEnv.getLogger()
						.error("Converter already registered for type "
								, this.typeElement);
				}
			}
		}
	}

}
