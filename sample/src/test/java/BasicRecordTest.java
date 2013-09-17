import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.pasra.android.record.sample.generate.Gallery;
import com.pasra.android.record.sample.generate.LocalSession;
import com.pasra.android.record.sample.generate.Picture;
import com.pasra.android.record.sample.generate.RecordMigrator;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by rich on 9/15/13.
 */
public class BasicRecordTest extends AndroidTestCase {

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

    public void testGalleryCRUD() {
        Gallery gallery = new Gallery();
        gallery.setName("sample");

        mSession.insertGallery(gallery);
        assertNotNull(gallery.getId());

        Long id = gallery.getId();

        gallery = mSession.loadGallery(id);
        assertEquals(gallery.getId(), id);
        assertEquals(gallery.getName(), "sample");

        gallery.setName("example");
        mSession.updateGallery(gallery);
        gallery = mSession.loadGallery(id);
        assertEquals(gallery.getName(), "example");

        mSession.deleteGallery(id);
        gallery = mSession.loadGallery(id);
        assertNull(gallery);
    }

    public void testRelationship() {
        Gallery gallery = new Gallery();
        gallery.setName("pixelart");
        mSession.insertGallery(gallery);

        Picture picture = Picture.of(gallery);
        picture.setName("truely-red.jpg");
        Calendar c = new GregorianCalendar();
        c.set(2013,8,1);
        picture.setDate(c.getTime());
        picture.setImage(ByteBuffer.wrap(new byte[] { (byte)0xff, (byte)0xff, 0x0, 0x0 })); // argb :)
        mSession.insertPicture(picture);

        assertNotNull(picture.getId());

        long id = picture.getId();
        picture = mSession.loadPicture(id);

        assertEquals(new Long(id), picture.getId());
        assertEquals(picture.getGalleryId(), gallery.getId());
        // picture = gallery.loadPictures(mSession);
        // gallery = picture.loadGallery(mSession);
        // GalleryRecord.contentProvider();
        // PictureRecord.contentProvider(where gallery_id = x);




    }

    //RemoteSession mRemote = new RemoteSession("https", "example.com", new RouteBuilder());
    //mRemote.hookup(mSession);
    //mLocal.load(id);
}
