package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import com.pasra.android.record.SQLiteConverter;

public class UsrRecord{
    private static UsrRecord mInstance;
    public static UsrRecord instance(){
        if (mInstance == null){
            mInstance = new UsrRecord();
        }
        return mInstance;
    }
    public void save(SQLiteDatabase db, AbstractUsr record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractUsr record){
        ContentValues values = new ContentValues(3);
        values.put("first_name", record.getFirstName());
        values.put("last_name", record.getLastName());
        values.put("age", record.getAge());
        long id = db.insert("usr", null, values);
        record.setId(id);
    }
    public Usr load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from usr where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Usr record = new Usr();
            record.setId(c.getLong(0));
            record.setFirstName(c.getString(1));
            record.setLastName(c.getString(2));
            record.setAge(c.getInt(3));
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from usr where  _id = ?;", new String[] { Long.toString(id) });
    }
    public void update(SQLiteDatabase db, AbstractUsr record){
        ContentValues values = new ContentValues(3);
        values.put("first_name", record.getFirstName());
        values.put("last_name", record.getLastName());
        values.put("age", record.getAge());
        long id = record.getId();
        db.update("usr", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
