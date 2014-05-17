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

import com.turbomanage.storm.apt.ClassModel;
import com.turbomanage.storm.apt.ClassTemplate;

public class EntityDaoTemplate extends ClassTemplate {

	public EntityDaoTemplate(ClassModel model) {
		super(model);
	}

	@Override
	public String getTemplatePath() {
		return "EntityDao.ftl";
	}

	@Override
	public String getPackage() {
		return ((EntityModel) model).getDaoPackage();
	}

	@Override
	public String getGeneratedClass() {
		return getPackage() + "." + ((EntityModel) model).getDaoName();
	}

}
