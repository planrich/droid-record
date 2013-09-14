package com.pasra.android.record.sample;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.pasra.android.record.Migrator;
import com.pasra.android.record.sample.generate.Gallery;
import com.pasra.android.record.sample.generate.GalleryRecord;
import com.pasra.android.record.sample.generate.RecordMigrator;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by rich on 9/13/13.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase db = openOrCreateDatabase("test", MODE_PRIVATE, null);
        //RecordContext context  = new RecordContextImpl(db, new RecordMigrator());

        //Session session = context.getSession();

        //session.insert(new Gallery(null));
        //List<Gallery> galleries = session.loadGalleries();
        //Gallery gallery = session.loadGallery(0L);

        //GalleryRecord.insert(session, new Gallery(null), null);
        //GalleryRecord.loadAll(session);
    }
}
