package com.pasra.android.record.sample;

import android.database.sqlite.SQLiteDatabase;
import com.pasra.android.record.Migrator;

public class RecordMigrator implements Migrator {

    public static final long MIGRATION_LEVEL = 20130913154915L;
    
    public long getLatestMigrationLevel() {

        return MIGRATION_LEVEL;    }
    public void migrate(SQLiteDatabase db, long currentVersion) {


    }
}
