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
            Gallery record = new Gallery();
            record.setName(c.getString(0));
            record.setId(c.getLong(1));
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from gallery where id = ?;", new String[] { Long.toString(id) });
    }
    public void update(SQLiteDatabase db, AbstractGallery record){
        ContentValues values = new ContentValues(1);
        values.put("name", record.getName());
        long id = record.getId();
        db.update("gallery", values, "id = ?", new String[] { Long.toString(id) });
    }
    public java.util.List<Picture> loadPicturesBlocking(SQLiteDatabase db, long GalleryId){
        java.util.List<Picture> list = new java.util.ArrayList();
        Cursor c = db.rawQuery("select * from picture where gallery_id = ?", new String[] { Long.toString(GalleryId) } );
        while (c.moveToNext()){
            Picture record = new Picture();
            record.setName(c.getString(0));
            record.setImage(c.getBlob(1));
            record.setDate(SQLiteConverter.stringToDate(c.getString(2)));
            record.setGalleryId(c.getLong(3));
            record.setId(c.getLong(4));
            list.add(record);
        }
        return list;
    }
}
