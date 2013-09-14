package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import java.util.concurrent.Future;

public class GalleryRecord{
    private static GalleryRecord mInstance;
    public static GalleryRecord instance(){
        if (mInstance == null){
            mInstance = new GalleryRecord();
        }
        return mInstance;
    }
    public void insert(SQLiteDatabase db, AbstractGallery record){
        String sql = "insert into gallery (com.pasra.android.record.database.Field@44162fb2) values (?)";
        String[] args = new String[1];
        if (record.getName() != null){
            args[0] = record.getName().toString();
        }
        db.execSQL(sql, args);
    }
}
