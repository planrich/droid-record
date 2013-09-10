package com.pasra.android.record.sample;

import android.database.sqlite.SQLiteDatabase;

public class RecordMigrator {

    private long mLastMigration = 12345;
    
    public void migrate(SQLiteDatabase db, long version) {

        if (mLastMigration <= version) {


        }
    }
}
