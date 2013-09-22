import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.AndroidTestCase;

import com.pasra.android.record.sample.generate.LocalSession;
import com.pasra.android.record.sample.generate.RecordMigrator;
import com.pasra.android.record.sample.generate.User;

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

    public void testRenameKeepsData() {
        LocalSession session = new LocalSession(mDB);
        RecordMigrator migrator = new RecordMigrator(mDB);
        long current = 0L;
        long target = 20130922092812L;
        migrator.migrate(current, target);
        assertEquals(migrator.getCurrentMigrationLevel(), target);
        current = target;

        ContentValues vals = new ContentValues();
        vals.put("first_name", "Chuck");
        vals.put("last_name", "Norris");
        vals.put("age", "2");
        long id = mDB.insert("usrs", null, vals);

        target = 20130922093131L;
        migrator.migrate(current, target);
        current = target;

        Cursor c = mDB.rawQuery("select * from users;", null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();
        assertEquals(c.getString(c.getColumnIndex("first_name")), "Chuck");
        assertEquals(c.getString(c.getColumnIndex("last_name")), "Norris");
        assertEquals(c.getLong(c.getColumnIndex("age")), 2L);

        target = RecordMigrator.MIGRATION_LEVEL;
        migrator.migrate(current, target);
        current = target;

        User chuck = session.findUser(id);
        assertNotNull(chuck);
        assertEquals(chuck.getFirstName(), "Chuck");
        assertEquals(chuck.getLastName(), "Norris");
    }

    public void testMigrate() {
        RecordMigrator migrator = new RecordMigrator(mDB);
        migrator.migrate();
        migrator.migrate();

        Cursor c = mDB.rawQuery("select * from android_record_configs where key = 'version';", null);
        assertEquals(c.getCount(), 1);

        try {
            mDB.execSQL("insert into android_record_configs (key,value) values ('version', 0);");
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
