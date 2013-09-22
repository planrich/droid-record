package com.pasra.android.record;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rich on 9/22/13.
 */
public interface DataMigrator {
    /**
     * This custom migrator can be used when the normal migrations cannot handle the conversion.
     * Do _NOT_ drop or alter tables. Android record cannot track these changes and this will lead
     * to undefined behaviour.
     * @param db
     * @param currentVersion
     * @param targetVersion
     */
    void migrate(SQLiteDatabase db, long currentVersion, long targetVersion);
}
