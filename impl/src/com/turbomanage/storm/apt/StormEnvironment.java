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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.turbomanage.storm.apt.converter.ConverterModel;
import com.turbomanage.storm.apt.database.DatabaseModel;
import com.turbomanage.storm.exception.TypeNotSupportedException;
import com.turbomanage.storm.types.BlobConverter;
import com.turbomanage.storm.types.BooleanConverter;
import com.turbomanage.storm.types.ByteConverter;
import com.turbomanage.storm.types.CharConverter;
import com.turbomanage.storm.types.DateConverter;
import com.turbomanage.storm.types.DoubleConverter;
import com.turbomanage.storm.types.EnumConverter;
import com.turbomanage.storm.types.FloatConverter;
import com.turbomanage.storm.types.IntegerConverter;
import com.turbomanage.storm.types.LongConverter;
import com.turbomanage.storm.types.ShortConverter;
import com.turbomanage.storm.types.StringConverter;
import com.turbomanage.storm.types.TypeConverter;

/**
 * Top-level container for the source code models populated by
 * {@link MainProcessor} and its subsidiaries.
 *
 * @author David M. Chandler
 */
public class StormEnvironment {

	public static final String BEGIN_DATABASE = ":DB_START";
	public static final String END_DATABASE = ":DB_END";
	public static final String BEGIN_CONVERTERS = ":CONV_START";
	public static final String END_CONVERTERS = ":CONV_END";
	private static final String ENV_FILE = "stormEnv";
	private ProcessorLogger logger;
	private List<DatabaseModel> dbModels = new ArrayList<DatabaseModel>();
	private List<ConverterModel> converters = new ArrayList<ConverterModel>();
	private Map<String, ConverterModel> typeMap = new HashMap<String, ConverterModel>();

	StormEnvironment(ProcessorLogger logger) {
		this.logger = logger;
		addBuiltInConverters();
	}

	private void addBuiltInConverters() {
		this.addConverter(new ConverterModel(new BlobConverter()));
		this.addConverter(new ConverterModel(new BooleanConverter()));
		this.addConverter(new ConverterModel(new ByteConverter()));
		this.addConverter(new ConverterModel(new CharConverter()));
		this.addConverter(new ConverterModel(new DateConverter()));
		this.addConverter(new ConverterModel(new DoubleConverter()));
		this.addConverter(new ConverterModel(new EnumConverter()));
		this.addConverter(new ConverterModel(new FloatConverter()));
		this.addConverter(new ConverterModel(new IntegerConverter()));
		this.addConverter(new ConverterModel(new LongConverter()));
		this.addConverter(new ConverterModel(new ShortConverter()));
		this.addConverter(new ConverterModel(new StringConverter()));
	}

	public ProcessorLogger getLogger() {
		return this.logger;
	}

	void addDatabase(DatabaseModel dbModel) {
		dbModels.add(dbModel);
	}

	List<DatabaseModel> getDbModels() {
		return dbModels;
	}

	public DatabaseModel getDbByName(String helperClass) {
		// iterate over all dbs and find matching
		for (DatabaseModel dbModel : dbModels) {
			if (dbModel.getDbHelperClass().equals(helperClass)) {
				return dbModel;
			}
		}
		return null;
	}

	public DatabaseModel getDefaultDb() {
		if (dbModels.size() > 0) {
			return dbModels.get(0);
		}
		return null;
	}

	/**
	 * Read the current model state from a file in support of incremental
	 * compilation. This is necessary because the annotation processor has
	 * access to only classes which have been annotated (and any resulting
	 * generated classes on subsequent rounds), but DatabaseHelper classes
	 * aren't available when doing incremental compilation on a new @Entity.
	 *
	 * @see http
	 *      ://stackoverflow.com/questions/10585665/how-can-i-examine-the-whole
	 *      -source-tree-with-an-annotation-processor
	 *      https://github.com/sentinelt/evo-classindex
	 *
	 * @param Filer
	 *            used by the annotation processor
	 */
	void readIndex(Filer filer) {
		StandardLocation location = StandardLocation.SOURCE_OUTPUT;
		FileObject indexFile;
		try {
			indexFile = filer.getResource(location, "com.turbomanage.storm",
					ENV_FILE);
			logger.info("Reading index " + indexFile.toUri());
			// indexFile.openReader() not implemented on all platforms
			Reader fileReader = new InputStreamReader(indexFile.openInputStream());
			BufferedReader reader = new BufferedReader(fileReader);
			String line = reader.readLine(); // BEGIN_CONVERTERS
			line = reader.readLine();
			while (line != null && !line.startsWith(END_CONVERTERS)) {
				ConverterModel converter = ConverterModel.readFromIndex(line, logger);
				this.addConverter(converter);
				line = reader.readLine();
			}
			line = reader.readLine();
			while (line != null && line.startsWith(BEGIN_DATABASE)) {
				DatabaseModel dbModel = DatabaseModel.readFromIndex(reader, logger);
				this.addDatabase(dbModel);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			// gulp--only way to catch not yet existing file on first run
		}
	}

	/**
	 * Write the current model state to a file in support of incremental
	 * compilation.
	 *
	 * @see StormEnvironment#readIndex(Filer)
	 *
	 * @param Filer
	 *            used by the annotation processor
	 */
	void writeIndex(Filer filer) {
		StandardLocation location = StandardLocation.SOURCE_OUTPUT;
		FileObject indexFile;
		try {
			indexFile = filer.createResource(location, "com.turbomanage.storm",
					ENV_FILE);
			OutputStream fos = indexFile.openOutputStream();
			PrintWriter out = new PrintWriter(fos);
			// Dump converters
			out.println(BEGIN_CONVERTERS);
			for (ConverterModel converter : converters) {
				converter.writeToIndex(out);
			}
			out.println(END_CONVERTERS);
			// Dump databases
			for (DatabaseModel dbModel : dbModels) {
				dbModel.writeToIndex(out);
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Register a custom {@link TypeConverter} for a given data (field) type.
	 * This method is called at compile time by the annotation processor. In
	 * order for the TypeConverter to be visible, it must be in a jar on the
	 * client project's annotation factory classpath.
	 *
	 * @param converter
	 * @return true if successful, false if converter was already registered for
	 *         type
	 */
	public boolean addConverter(ConverterModel converter) {
		if (converters.contains(converter))
			return true;
		for (String type : converter.getConvertibleTypes()) {
			// TODO what if already put the 1st type?
			if (typeMap.containsKey(type)
					&& !typeMap.get(type).equals(converter))
				return false;
			typeMap.put(type, converter);
		}
		converters.add(converter);
		logger.info("Added " + converter.getQualifiedClassName());
		return true;
	}

	public ConverterModel getConverterForType(String javaType) {
		ConverterModel converter = typeMap.get(javaType);
		if (converter != null)
			return converter;
		throw new TypeNotSupportedException(
				"Could not find converter for type " + javaType);
	}

	public List<ConverterModel> getConverters() {
		return converters;
	}

}