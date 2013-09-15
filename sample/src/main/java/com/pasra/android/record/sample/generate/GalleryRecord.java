package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import com.pasra.android.record.SQLiteConverter;

public class GalleryRecord{
    private static GalleryRecord mInstance;
    public static GalleryRecord instance(){
        if (mInstance == null){
            mInstance = new GalleryRecord();
        }
        return mInstance;
    }
    public void insert(SQLiteDatabase db, AbstractGallery record){
        ContentValues values = new ContentValues(1);
        values.put("name", record.getName());
        long id = db.insert("gallery", null, values);
        record.setId(id);
    }
    public Gallery load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from gallery where id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Gallery record = new Gallery(null);
            record.setName(c.getString(0));
            record.setId(c.getLong(1));
            
        }
        return null;
    }
}
