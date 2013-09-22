package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.RecordBuilder;
import android.database.sqlite.SQLiteDatabase;

public class UserRecordBuilder extends RecordBuilder<User>{
    public UserRecordBuilder(SQLiteDatabase db){
        super("users", new String[] { "_id", "first_name", "last_name" }, db);
    }
    @Override
    public java.util.List<User> all(){
        java.util.List<User> list = new java.util.ArrayList<User>();
        android.database.Cursor c = cursor();
        while (c.moveToNext()){
            list.add(User.fromCursor(c));
        }
        return list;
    }
    @Override
    public User first(){
        android.database.Cursor c = cursor();
        if (c.moveToFirst()){
            User record = User.fromCursor(c);
            c.close();
            return record;
        }
        c.close();
        return null;
    }
}
