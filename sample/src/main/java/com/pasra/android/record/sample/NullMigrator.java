package com.pasra.android.record.sample;

import android.database.sqlite.SQLiteDatabase;

import com.pasra.android.record.DataMigrator;

/**
 * Created by rich on 9/22/13.
 */
public class NullMigrator implements DataMigrator {

    @Override
    public void migrate(SQLiteDatabase database, long current, long target) {
        // do nothing!!!
    }

}
