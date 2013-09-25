package at.pasra.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import at.pasra.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class GalleryRecordBuilder extends RecordBuilder<Gallery>{
    public GalleryRecordBuilder(SQLiteDatabase db){
        super("galleries", new String[] { "_id", "name", "user_id" }, db);
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
    @Override
    public Gallery first(){
        android.database.Cursor c = cursor();
        if (c.moveToFirst()){
            Gallery record = Gallery.fromCursor(c);
            c.close();
            return record;
        }
        c.close();
        return null;
    }
}
