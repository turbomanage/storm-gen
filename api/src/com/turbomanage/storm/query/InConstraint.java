package com.turbomanage.storm.query;

import com.turbomanage.storm.TableHelper.Column;

public class InConstraint extends Predicate {

	public InConstraint(Column colName, String param) {
		super(colName, param);
	}

	@Override
	public String getSqlOp() {
		return colName + " IN ";
	}

}
