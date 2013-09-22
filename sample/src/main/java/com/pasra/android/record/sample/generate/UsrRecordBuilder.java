package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class UsrRecordBuilder extends RecordBuilder<Usr>{
    public UsrRecordBuilder(SQLiteDatabase db){
        super("usr", new String[] { "_id", "first_name", "last_name", "age" }, db);
    }
    @Override
    public java.util.List<Usr> all(){
        java.util.List<Usr> list = new java.util.ArrayList<Usr>();
        android.database.Cursor c = cursor();
        while (c.moveToNext()){
            list.add(Usr.fromCursor(c));
        }
        return list;
    }
    @Override
    public Usr first(){
        android.database.Cursor c = cursor();
        if (c.moveToFirst()){
            Usr record = Usr.fromCursor(c);
            c.close();
            return record;
        }
        c.close();
        return null;
    }
}
