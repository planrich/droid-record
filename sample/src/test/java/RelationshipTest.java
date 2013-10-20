import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import at.pasra.record.sample.generate.Gallery;
import at.pasra.record.sample.generate.LocalSession;
import at.pasra.record.sample.generate.RecordMigrator;
import at.pasra.record.sample.generate.User;

/**
 * Created by rich on 10/1/13.
 */
public class RelationshipTest  extends AndroidTestCase {

    private SQLiteDatabase mDB;
    private LocalSession mSession;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDB = getContext().openOrCreateDatabase("test", Context.MODE_PRIVATE, null);
        new RecordMigrator(mDB).migrate();
        mSession = new LocalSession(mDB);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase("test");
    }

    public void testHasOne() {
        User user = new User();
        user.setFirstName("rich");
        user.setLastName("plan");
        mSession.saveUser(user);

        Gallery gallery = Gallery.of(user);
        gallery.setName("pixelart");
        mSession.saveGallery(gallery);
        long id = gallery.getId();

        gallery = user.loadGallery(mSession);
        assertEquals(gallery.getId(), new Long(id));
    }
}
