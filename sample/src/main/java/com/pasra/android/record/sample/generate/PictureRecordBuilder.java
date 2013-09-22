package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class PictureRecordBuilder extends RecordBuilder<Picture>{
    public PictureRecordBuilder(SQLiteDatabase db){
        super("pictures", new String[] { "_id", "name", "image", "date", "gallery_id" }, db);
    }
    @Override
    public java.util.List<Picture> all(){
        java.util.List<Picture> list = new java.util.ArrayList<Picture>();
        android.database.Cursor c = cursor();
        while (c.moveToNext()){
            list.add(Picture.fromCursor(c));
        }
        return list;
    }
    @Override
    public Picture first(){
        android.database.Cursor c = cursor();
        if (c.moveToFirst()){
            Picture record = Picture.fromCursor(c);
            c.close();
            return record;
        }
        c.close();
        return null;
    }
}
