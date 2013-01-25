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

    @Override
	public DatabaseHelper getDbHelper(Context ctx) {
		return ${dbFactory}.getDatabaseHelper(ctx);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public TableHelper getTableHelper() {
		return new ${tableHelperClass}();
	}

	/**
	 * @see SQLiteDao#SQLiteDao(Context)
	 */
	public ${daoName}(Context ctx) {
		super(ctx);
	}

}