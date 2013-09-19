package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class PictureRecordBuilder extends RecordBuilder<Picture>{
    public PictureRecordBuilder(SQLiteDatabase db){
        super("picture", new String[] { "name", "image", "date", "gallery_id", "id" }, db);
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
}
