package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import com.pasra.android.record.SQLiteConverter;
import java.nio.ByteBuffer;

public class PictureRecord{
    private static PictureRecord mInstance;
    public static PictureRecord instance(){
        if (mInstance == null){
            mInstance = new PictureRecord();
        }
        return mInstance;
    }
    public void insert(SQLiteDatabase db, AbstractPicture record){
        ContentValues values = new ContentValues(4);
        values.put("name", record.getName());
        values.put("image", record.getImage().array());
        values.put("date", SQLiteConverter.dateToString(record.getDate()));
        values.put("gallery_id", record.getGalleryId());
        long id = db.insert("picture", null, values);
        record.setId(id);
    }
    public Picture load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from picture where id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Picture record = new Picture(null);
            record.setName(c.getString(0));
            record.setImage(ByteBuffer.wrap(c.getBlob(1)));
            record.setDate(SQLiteConverter.stringToDate(c.getString(2)));
            record.setGalleryId(c.getInt(3));
            record.setId(c.getLong(4));
            
        }
        return null;
    }
}
