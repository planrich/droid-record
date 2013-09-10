package com.pasra.android.record;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rich on 9/10/13.
 */
public interface DatabaseMigration {

    public void migrate(SQLiteDatabase database, int version, boolean upwards);
}
