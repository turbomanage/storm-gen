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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import com.turbomanage.storm.api.Converter;
import com.turbomanage.storm.api.Database;
import com.turbomanage.storm.api.Entity;
import com.turbomanage.storm.apt.converter.ConverterProcessor;
import com.turbomanage.storm.apt.database.DatabaseFactoryTemplate;
import com.turbomanage.storm.apt.database.DatabaseModel;
import com.turbomanage.storm.apt.database.DatabaseProcessor;
import com.turbomanage.storm.apt.entity.EntityDaoTemplate;
import com.turbomanage.storm.apt.entity.EntityProcessor;
import com.turbomanage.storm.apt.entity.TableHelperTemplate;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

@SupportedAnnotationTypes({ "com.turbomanage.storm.api.Database","com.turbomanage.storm.api.Entity","com.turbomanage.storm.api.Converter" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MainProcessor extends AbstractProcessor {
	private ProcessorLogger logger;
	private Configuration cfg = new Configuration();
	private static String ERR_MSG = "Error processing stORM annotation";

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {

		cfg.setTemplateLoader(new ClassTemplateLoader(this.getClass(), "/res"));
		this.logger = new ProcessorLogger(processingEnv.getMessager());
		logger.info("Running MainProcessor...");

		// Exit early if no annotations in this round so we don't overwrite the
		// env file
		if (annotations.size() < 1) {
			return true;
		}

		for (TypeElement annotationType : annotations) {
			logger.info("Processing elements with @" + annotationType.getQualifiedName());
		}

		StormEnvironment stormEnv = new StormEnvironment(logger);
		// Read in previously processed classes to support incremental compilation
		stormEnv.readIndex(processingEnv.getFiler());

		for (Element element : roundEnv.getElementsAnnotatedWith(Converter.class)) {
			try {
				ConverterProcessor cproc = new ConverterProcessor(element, stormEnv);
				cproc.populateModel();
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter out = new PrintWriter(sw);
				e.printStackTrace(out);
				logger.error(ERR_MSG + sw.toString(), e, element);
				return true;
			}
		}

		// First pass on @Database annotations to get all db names
		for (Element element : roundEnv.getElementsAnnotatedWith(Database.class)) {
			try {
				DatabaseProcessor dbProc = new DatabaseProcessor(element, stormEnv);
				dbProc.populateModel();
				stormEnv.addDatabase(dbProc.getModel());
			} catch (Exception e) {
				logger.error(ERR_MSG, e, element);
				return true;
			}
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
			try {
				EntityProcessor eproc = new EntityProcessor(element, stormEnv);
				eproc.populateModel();
				// Generate EntityDao
				EntityDaoTemplate daoTemplate = new EntityDaoTemplate(eproc.getModel());
				processTemplate(processingEnv, cfg, daoTemplate);
				// Generate EntityTable
				TableHelperTemplate tableHelperTemplate = new TableHelperTemplate(eproc.getModel());
				processTemplate(processingEnv, cfg, tableHelperTemplate);
			} catch (Exception e) {
				logger.error(ERR_MSG, e, element);
				return true;
			}
		}

		// Second pass to generate DatabaseFactory templates now that
		// all entities have been associated with a db
		for (DatabaseModel dbModel : stormEnv.getDbModels()) {
			DatabaseFactoryTemplate dbFactoryTemplate = new DatabaseFactoryTemplate(dbModel);
			processTemplate(processingEnv, cfg, dbFactoryTemplate);
		}

		// Write all processed dbs to index to support incremental compilation
		stormEnv.writeIndex(processingEnv.getFiler());

		return true;
	}

	void processTemplate(ProcessingEnvironment processingEnv, Configuration cfg, ClassTemplate template) {
		JavaFileObject file;
		try {
			file = processingEnv.getFiler().createSourceFile(
					template.getGeneratedClass());
			Writer out = file.openWriter();
			Template t = cfg.getTemplate(template.getTemplatePath());
			logger.info("Generating " + file.getName() + " with " + t.getName());
			t.process(template.getModel(), out);
			out.flush();
			out.close();
		} catch (Exception e) {
			logger.error("Template error", e);
		}
	}

}
