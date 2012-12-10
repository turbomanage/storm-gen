package ${daoPackage};

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import com.turbomanage.storm.query.FilterBuilder;
import com.turbomanage.storm.TableHelper;
import java.util.Map;
import java.util.HashMap;
<#list imports as import>
import ${import};
</#list>

/**
 * GENERATED CODE
 *
 * This class contains the SQL DDL for the named entity / table.
 * These methods are not included in the EntityDao class because
 * they must be executed before the Dao can be instantiated, and
 * they are instance methods vs. static so that they can be
 * overridden in a typesafe manner.
 *
 * @author David M. Chandler
 */
public class ${tableHelperName} extends TableHelper<${entityName}> {

	public enum Columns implements TableHelper.Column {
		<#list fields as field>
		${field.colName}<#if field_has_next>,</#if>
		</#list>
	}

	@Override
	public String getTableName() {
		return "${tableName}";
	}

	@Override
	public Column[] getColumns() {
		return Columns.values();
	}

	@Override
	public Column getIdCol() {
		return Columns._ID;
	}

	@Override
	public String createSql() {
		return
			"CREATE TABLE IF NOT EXISTS ${tableName}(" +
				<#list fields as field>
				"${field.colName} ${field.sqlType}<#if !field.nullable> NOT NULL</#if><#if field_has_next>,</#if>" +
				</#list>
			")";
	}

	@Override
	public String dropSql() {
		return "DROP TABLE IF EXISTS ${tableName}";
	}

	@Override
	public String upgradeSql(int oldVersion, int newVersion) {
		return null;
	}

	@Override
	public String[] getRowValues(Cursor c) {
		String[] values = new String[c.getColumnCount()];
		<#list fields as field>
		<#if field.enum>
		values[${field_index}] = c.isNull(${field_index}) ? null : c.getString(${field_index});
		<#else>
		values[${field_index}] = ${field.converterName}.GET.toString(get${field.bindType}OrNull(c, ${field_index}));
		</#if>
		</#list>
		return values;
	}

	@Override
	public void bindRowValues(InsertHelper insHelper, String[] rowValues) {
		<#list fields as field>
		<#if field.enum>
		if (rowValues[${field_index}] == null) insHelper.bindNull(${field_index+1}); else insHelper.bind(${field_index+1}, rowValues[${field_index}]);
		<#else>
		if (rowValues[${field_index}] == null) insHelper.bindNull(${field_index+1}); else insHelper.bind(${field_index+1}, ${field.converterName}.GET.fromString(rowValues[${field_index}]));
		</#if>
		</#list>
	}

	@Override
	public String[] getDefaultValues() {
		String[] values = new String[getColumns().length];
		${entityName} defaultObj = new ${entityName}();
		<#list fields as field>
		<#if field.enum>
		values[${field_index}] = (defaultObj.${field.getter}() == null) ? null : defaultObj.${field.getter}().name();
		<#else>
		values[${field_index}] = ${field.converterName}.GET.toString(${field.converterName}.GET.toSql(defaultObj.${field.getter}()));
		</#if>
		</#list>
		return values;
	}

	@Override
	public ${entityName} newInstance(Cursor c) {
		${entityName} obj = new ${entityName}();
		<#list fields as field>
		<#if field.javaType == "byte[]">
		obj.${field.setter}(c.getBlob(${field_index}));
		<#elseif field.javaType == "boolean">
		obj.${field.setter}(c.getInt(${field_index}) == 1 ? true : false);
		<#elseif field.javaType == "byte">
		obj.${field.setter}((byte) c.getShort(${field_index}));
		<#elseif field.javaType == "char">
		obj.${field.setter}((char) c.getInt(${field_index}));
		<#elseif field.javaType == "double">
		obj.${field.setter}(c.getDouble(${field_index}));
		<#elseif field.javaType == "float">
		obj.${field.setter}(c.getFloat(${field_index}));
		<#elseif field.javaType == "int">
		obj.${field.setter}(c.getInt(${field_index}));
		<#elseif field.javaType == "long">
		obj.${field.setter}(c.getLong(${field_index}));
		<#elseif field.javaType == "short">
		obj.${field.setter}(c.getShort(${field_index}));
		<#elseif field.javaType == "java.lang.String">
		obj.${field.setter}(c.getString(${field_index}));
		<#elseif field.enum>
		obj.${field.setter}(c.isNull(${field_index}) ? null : ${field.javaType}.valueOf(c.getString(${field_index})));
		<#else>
		obj.${field.setter}(${field.converterName}.GET.fromSql(get${field.bindType}OrNull(c, ${field_index})));
		</#if>
		</#list>
		return obj;
	}

	@Override
	public ContentValues getEditableValues(${entityName} obj) {
		ContentValues cv = new ContentValues();
		<#list fields as field>
		<#if field.javaType == "byte[]">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "boolean">
		cv.put("${field.colName}", obj.${field.getter}() ? 1 : 0);
		<#elseif field.javaType == "byte">
		cv.put("${field.colName}", (short) obj.${field.getter}());
		<#elseif field.javaType == "char">
		cv.put("${field.colName}", (int) obj.${field.getter}());
		<#elseif field.javaType == "double">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "float">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "int">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "long">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "short">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.javaType == "java.lang.String">
		cv.put("${field.colName}", obj.${field.getter}());
		<#elseif field.enum>
		cv.put("${field.colName}", obj.${field.getter}() == null ? null : obj.${field.getter}().name());
		<#else>
		cv.put("${field.colName}", ${field.converterName}.GET.toSql(obj.${field.getter}()));
		</#if>
		</#list>
		return cv;
	}

	@Override
	public FilterBuilder buildFilter(FilterBuilder filter, ${entityName} obj) {
		${entityName} defaultObj = new ${entityName}();
		// Include fields in query if they differ from the default object
		<#list fields as field>
		if (obj.${field.getter}() != defaultObj.${field.getter}())
		<#if field.enum>
			filter = filter.eq(Columns.${field.colName}, obj.${field.getter}() == null ? null : obj.${field.getter}().name());
		<#else>
			filter = filter.eq(Columns.${field.colName}, ${field.converterName}.GET.toSql(obj.${field.getter}()));
		</#if>
		</#list>
		return filter;
	}

}