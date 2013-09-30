import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Collection;
import java.util.List;

import at.pasra.record.sample.generate.Gallery;
import at.pasra.record.sample.generate.LocalSession;
import at.pasra.record.sample.generate.Picture;
import at.pasra.record.sample.generate.RecordMigrator;

/**
 * Created by rich on 9/30/13.
 */
public class CachingTest  extends AndroidTestCase {

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

    public void testCachingUsingPrimaryKeys() {
        Gallery gallery = new Gallery();
        gallery.setName("sample");
        mSession.saveGallery(gallery);

        long id = gallery.getId();

        Gallery eqiv_gallery = mSession.findGallery(id);
        // must be the same object!
        assertTrue(eqiv_gallery == gallery);

        mSession.clearCache();

        eqiv_gallery = mSession.findGallery(id);
        // must not be the same object -> cache was cleared
        assertFalse(eqiv_gallery == gallery);
    }

    public void testRelationCaching() {

        Gallery gallery = new Gallery();
        gallery.setName("sample");
        mSession.saveGallery(gallery);

        Picture pic = Picture.of(gallery);
        pic.setName("test");
        mSession.savePicture(pic);

        List<Picture> pictures = gallery.loadPictures(mSession).all();
        List<Picture> cachedPictures = gallery.loadPictures(mSession).all();
        assertEquals(pictures.size(), 1);
        assertEquals(cachedPictures.size(), 1);
        assertTrue(pictures == cachedPictures);

        pic = Picture.of(gallery);
        pic.setName("test");
        mSession.savePicture(pic);

        pictures  = gallery.loadPictures(mSession).all();
        assertTrue(pictures != cachedPictures);
        assertEquals(cachedPictures.size(), 1);
        assertEquals(pictures.size(), 2);


        Gallery cachedGallery = pic.loadGallery(mSession);
        assertTrue(cachedGallery == gallery);

    }
}
