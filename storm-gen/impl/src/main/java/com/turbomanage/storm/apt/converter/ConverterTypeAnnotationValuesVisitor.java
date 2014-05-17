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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.apt.ProcessorLogger;

/**
 * Obtains the list of all types declared in a {@link Converter#forTypes()}
 * annotation.
 * 
 * @author David M. Chandler
 */
public class ConverterTypeAnnotationValuesVisitor extends SimpleAnnotationValueVisitor6<String[], ProcessorLogger> {

	@Override
	public String[] visitArray(List<? extends AnnotationValue> vals, ProcessorLogger logger) {
		List<String> types = new ArrayList<String>();
		for (AnnotationValue val : vals) {
			types.add(val.accept(new ConverterTypeAnnotationValueVisitor(), logger));
		}
		return types.toArray(new String[]{});
	}

}
