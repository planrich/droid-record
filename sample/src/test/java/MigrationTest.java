import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.AndroidTestCase;

import com.pasra.android.record.sample.generate.RecordMigrator;

/**
 * Created by rich on 9/15/13.
 */
public class MigrationTest extends AndroidTestCase {

    private SQLiteDatabase mDB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDB = getContext().openOrCreateDatabase("test", Context.MODE_PRIVATE, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase("test");
    }

    public void testMigrate() {
        RecordMigrator migrator = new RecordMigrator(mDB);
        migrator.migrate();
        migrator.migrate();

        Cursor c = mDB.rawQuery("select * from android_record_config where key = 'version';", null);
        assertEquals(c.getCount(), 1);

        try {
            mDB.execSQL("insert into android_record_config (key,value) values ('version', 0);");
            fail("key column is not unique!");
        } catch (SQLiteException e) {
            // all good
        }
    }

    public void testDoubleMigrationFails() {
        RecordMigrator migrator = new RecordMigrator(mDB);
        migrator.migrate(0, migrator.getLatestMigrationLevel());
        try {
            migrator.migrate(0, migrator.getLatestMigrationLevel());
            fail("should not be able to migrate 2 times");
        } catch (SQLiteException e) {

        } catch (Exception e) {
            fail("no sqlite exception");
        }
    }

}
