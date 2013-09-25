package at.pasra.record;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rich on 9/22/13.
 */
public interface DataMigrator {
    /**
     * This custom migrator can be used when the normal migrations cannot handle the conversion.
     * Do _NOT_ drop or alter tables. Android record cannot track these changes and this will lead
     * to undefined behaviour.
     *
     * It is not advisable to use any generated class here. As this class might disappear in
     * the app development process.
     * @param db
     * @param currentVersion
     * @param targetVersion
     */
    void migrate(SQLiteDatabase db, long currentVersion, long targetVersion);
}
