package com.pasra.android.record;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rich on 9/13/13.
 */
public interface Migrator {

    /**
     * @return the migration level of the database
     */
    long getLatestMigrationLevel();

    /**
     * Migrates the database to the specified target version
     * @param db
     * @param currentVersion
     * @param versionTarget
     */
    void migrate(SQLiteDatabase db, long currentVersion, long versionTarget);
}
