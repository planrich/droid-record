package at.pasra.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import at.pasra.record.SQLiteConverter;
import at.pasra.record.RecordBuilder;

public class AbstractUser{
    protected java.lang.String mFirstName;
    protected java.lang.String mLastName;
    protected java.lang.Long mId;
    
    public AbstractUser(java.lang.Long id){
        this.mId = id;
        this.mFirstName = "";
        this.mLastName = "";
    }
    
    public java.lang.String getFirstName() { return mFirstName; }
    public void setFirstName(java.lang.String value) { mFirstName = value; }
    public java.lang.String getLastName() { return mLastName; }
    public void setLastName(java.lang.String value) { mLastName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public static User fromCursor(android.database.Cursor cursor){
        User record = new User();
        record.setId(cursor.getLong(0));
        record.setFirstName(cursor.getString(1));
        record.setLastName(cursor.getString(2));
        return record;
    }
}
