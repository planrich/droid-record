package at.pasra.record.sample;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

/**
 * Created by rich on 9/13/13.
 */
public class MainActivity extends Activity {

    private SQLiteDatabase mDB;

    public SQLiteDatabase getDB() {
        return mDB;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDB = openOrCreateDatabase("test", MODE_PRIVATE, null);

        //RecordContext context  = new RecordContextImpl(db, new RecordMigrator());

        //LocalSession session = context.getSession();

        //session.insert(new Gallery(null));
        //List<Gallery> galleries = session.loadGalleries();
        //Gallery gallery = session.loadGallery(0L);

        //GalleryRecord.insert(session, new Gallery(null), null);
        //GalleryRecord.loadAll(session);
    }
}
