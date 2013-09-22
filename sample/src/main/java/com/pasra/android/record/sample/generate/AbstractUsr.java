package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.SQLiteConverter;
import com.pasra.android.record.RecordBuilder;

public class AbstractUsr{
    protected java.lang.String mFirstName;
    protected java.lang.String mLastName;
    protected java.lang.Integer mAge;
    protected java.lang.Long mId;
    
    public AbstractUsr(java.lang.Long id){
        this.mId = id;
        this.mFirstName = "";
        this.mLastName = "";
        this.mAge = new Integer(0);
    }
    
    public java.lang.String getFirstName() { return mFirstName; }
    public void setFirstName(java.lang.String value) { mFirstName = value; }
    public java.lang.String getLastName() { return mLastName; }
    public void setLastName(java.lang.String value) { mLastName = value; }
    public java.lang.Integer getAge() { return mAge; }
    public void setAge(java.lang.Integer value) { mAge = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public static Usr fromCursor(android.database.Cursor cursor){
        Usr record = new Usr();
        record.setId(cursor.getLong(0));
        record.setFirstName(cursor.getString(1));
        record.setLastName(cursor.getString(2));
        record.setAge(cursor.getInt(3));
        return record;
    }
}
