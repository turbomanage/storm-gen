package com.turbomanage.storm.sample.database;

import android.content.Context;

import com.turbomanage.storm.DatabaseHelper;
import com.turbomanage.storm.api.Database;
import com.turbomanage.storm.api.DatabaseFactory;

/**
 * Created by galex on 11/06/14.
 */

@Database(name = SampleDatabaseHelper.DB_NAME, version = SampleDatabaseHelper.DB_VERSION)
public class SampleDatabaseHelper extends DatabaseHelper{

    public final static String DB_NAME = "samples";
    public final static int DB_VERSION = 1;

    public SampleDatabaseHelper(Context ctx, DatabaseFactory dbFactory) {
        super(ctx, dbFactory);
    }

    @Override
    public UpgradeStrategy getUpgradeStrategy() {
        return UpgradeStrategy.BACKUP_RESTORE;
    }
}
