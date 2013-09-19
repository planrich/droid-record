package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class GalleryRecordBuilder extends RecordBuilder<Gallery>{
    public GalleryRecordBuilder(SQLiteDatabase db){
        super("gallery", new String[] { "name", "id" }, db);
    }
    @Override
    public java.util.List<Gallery> all(){
        java.util.List<Gallery> list = new java.util.ArrayList<Gallery>();
        android.database.Cursor c = cursor();
        while (c.moveToNext()){
            list.add(Gallery.fromCursor(c));
        }
        return list;
    }
}
