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
package com.turbomanage.storm;

import android.content.Context;

import com.turbomanage.storm.api.Database;
import com.turbomanage.storm.api.DatabaseFactory;

@Database(name = TestDatabaseHelper.DB_NAME, version = TestDatabaseHelper.DB_VERSION)
public class TestDatabaseHelper extends DatabaseHelper {

	public TestDatabaseHelper(Context ctx, DatabaseFactory dbFactory) {
		super(ctx, dbFactory);
	}

	public static final String DB_NAME = "testDb";
	public static final int DB_VERSION = 2;

	@Override
	protected UpgradeStrategy getUpgradeStrategy() {
		return UpgradeStrategy.DROP_CREATE;
	}

}
