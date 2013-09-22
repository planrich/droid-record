package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import com.pasra.android.record.SQLiteConverter;

public class UserRecord{
    private static UserRecord mInstance;
    public static UserRecord instance(){
        if (mInstance == null){
            mInstance = new UserRecord();
        }
        return mInstance;
    }
    public void save(SQLiteDatabase db, AbstractUser record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractUser record){
        ContentValues values = new ContentValues(2);
        values.put("first_name", record.getFirstName());
        values.put("last_name", record.getLastName());
        long id = db.insert("user", null, values);
        record.setId(id);
    }
    public User load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from user where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            User record = new User();
            record.setId(c.getLong(0));
            record.setFirstName(c.getString(1));
            record.setLastName(c.getString(2));
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from user where  _id = ?;", new String[] { Long.toString(id) });
    }
    public void update(SQLiteDatabase db, AbstractUser record){
        ContentValues values = new ContentValues(2);
        values.put("first_name", record.getFirstName());
        values.put("last_name", record.getLastName());
        long id = record.getId();
        db.update("user", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
