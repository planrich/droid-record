import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.pasra.android.record.sample.generate.Gallery;
import com.pasra.android.record.sample.generate.LocalSession;
import com.pasra.android.record.sample.generate.RecordMigrator;

/**
 * Created by rich on 9/15/13.
 */
public class CRUDTest extends AndroidTestCase {

    private SQLiteDatabase mDB;
    private LocalSession mSession;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDB = getContext().openOrCreateDatabase("test", Context.MODE_PRIVATE, null);
        new RecordMigrator().migrate(mDB, 0, RecordMigrator.MIGRATION_LEVEL);
        mSession = new LocalSession(mDB);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase("test");
    }

    public void testGallery() {
        Gallery gallery = new Gallery(null);
        gallery.setName("sample");

        mSession.insertGallery(gallery);
        assertNotNull(gallery.getId());

        Long id = gallery.getId();

        gallery = mSession.loadGallery(id);
        assertEquals(gallery.getId(), id);
        assertEquals(gallery.getName(), "sample");

        //RemoteSession mRemote = new RemoteSession("https", "example.com", new RouteBuilder());
        //mRemote.hookup(mSession);
        //mLocal.load(id);
    }
}
