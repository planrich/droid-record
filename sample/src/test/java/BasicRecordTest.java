import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import at.pasra.record.sample.generate.Gallery;
import at.pasra.record.sample.generate.LocalSession;
import at.pasra.record.sample.generate.Picture;
import at.pasra.record.sample.generate.RecordMigrator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
        new RecordMigrator(mDB).migrate();
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

        mSession.saveGallery(gallery);
        assertNotNull(gallery.getId());

        Long id = gallery.getId();

        gallery = mSession.findGallery(id);
        assertEquals(gallery.getId(), id);
        assertEquals(gallery.getName(), "sample");

        gallery.setName("example");
        mSession.saveGallery(gallery);
        gallery = mSession.findGallery(id);
        assertEquals(gallery.getName(), "example");

        mSession.destroyGallery(gallery);
        gallery = mSession.findGallery(id);
        assertNull(gallery);
    }

    public void testRelationship() {
        byte[] bytes = new byte[] { (byte)0xff, (byte)0xff, 0x0, 0x0 };  // argb :)
        Gallery gallery = new Gallery();
        gallery.setName("pixelart");
        mSession.saveGallery(gallery);

        Picture picture = Picture.of(gallery);
        picture.setName("truely-red.jpg");
        Calendar c = new GregorianCalendar();
        c.set(2013,8,1);
        picture.setDate(c.getTime());
        picture.setImage(bytes);
        mSession.savePicture(picture);

        assertNotNull(picture.getId());

        long id = picture.getId();
        picture = mSession.findPicture(id);

        assertEquals(new Long(id), picture.getId());
        assertEquals(picture.getGalleryId(), gallery.getId());

        List<Picture> pictures = gallery.loadPictures(mSession).all();
        assertEquals(pictures.size(), 1);
        assertEquals(pictures.get(0).getId(), picture.getId());
        assertEquals(pictures.get(0).getName(), picture.getName());

        Picture pic2 = Picture.of(gallery);
        pic2.setName("somerandom.jpg");
        mSession.savePicture(pic2);

        pictures = gallery.loadPictures(mSession).all();
        assertEquals(pictures.size(), 2);
        assertEquals(pictures.get(0).getDate(), picture.getDate());

        id = gallery.getId();
        gallery = picture.loadGallery(mSession);
        assertEquals(gallery.getId(), new Long(id));

        List<Gallery> galleries = mSession.queryGalleries().all();
        assertEquals(galleries.size(), 1);
        mSession.saveGallery(new Gallery());
        assertEquals(galleries.size(), 1);
        galleries = mSession.queryGalleries().all();
        assertEquals(galleries.size(), 2);
    }

    public void testCustomCursorAdapter() {
        Gallery gallery = new Gallery();
        gallery.setName("pixelart");
        mSession.saveGallery(gallery);
        final boolean[] called = {false};
        Cursor c = mSession.queryGalleries().where("name = ? or name = ?", "pixelart", "randomart").cursor();
        CursorAdapter a = new CursorAdapter(getContext(), c, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                Gallery g = Gallery.fromCursor(cursor);
                assertEquals(g.getName(), "pixelart");
                called[0] = true;
                return null;
            }
            @Override
            public void bindView(View view, Context context, Cursor cursor) { }
        };
        a.getView(0, null, null);
        if (!called[0]) {
            fail("did not call newView of cursor adapter");
        }
    }

    public void testRemoteSession() {
        //RemoteSession mRemote = new RemoteSession("https", "example.com", new RouteBuilder());
        //mRemote.hookup(mSession);
        //mLocal.load(id);
    }
}
