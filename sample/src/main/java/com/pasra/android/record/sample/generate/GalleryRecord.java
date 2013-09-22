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
    public void save(SQLiteDatabase db, AbstractGallery record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractGallery record){
        ContentValues values = new ContentValues(2);
        values.put("name", record.getName());
        values.put("user_id", record.getUserId());
        long id = db.insert("galleries", null, values);
        record.setId(id);
    }
    public Gallery load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from galleries where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Gallery record = new Gallery();
            record.setId(c.getLong(0));
            record.setName(c.getString(1));
            record.setUserId(c.getInt(2));
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from galleries where  _id = ?;", new String[] { Long.toString(id) });
    }
    public void update(SQLiteDatabase db, AbstractGallery record){
        ContentValues values = new ContentValues(2);
        values.put("name", record.getName());
        values.put("user_id", record.getUserId());
        long id = record.getId();
        db.update("galleries", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
