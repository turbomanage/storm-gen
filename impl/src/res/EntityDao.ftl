package ${daoPackage};

import android.content.Context;
import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.TableHelper;
<#list imports as import>
import ${import};
</#list>

/**
 * GENERATED CODE
 *
 * @author David M. Chandler
 */
public class ${daoName} extends ${baseDaoName}<${entityName}>{

	public DatabaseHelper getDbHelper(Context ctx) {
		return ${dbFactory}.getDatabaseHelper(ctx);
	}

	@SuppressWarnings("rawtypes")
	public TableHelper getTableHelper() {
		return new ${tableHelperClass}();
	}

	public ${daoName}(Context ctx) {
		super(ctx);
	}

}