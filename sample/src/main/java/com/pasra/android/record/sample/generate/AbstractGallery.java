package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.SQLiteConverter;
import com.pasra.android.record.RecordBuilder;

public class AbstractGallery{
    protected java.lang.String mName;
    protected java.lang.Long mId;
    protected java.lang.Integer mUsrId;
    
    public AbstractGallery(java.lang.Long id){
        this.mId = id;
        this.mName = "";
        this.mUsrId = new Integer(0);
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public java.lang.Integer getUsrId() { return mUsrId; }
    public void setUsrId(java.lang.Integer value) { mUsrId = value; }
    public RecordBuilder<Picture> loadPictures(LocalSession session){
        return session.queryPictures().where("gallery_id = ?", Long.toString(mId) );
    }
    public static Gallery fromCursor(android.database.Cursor cursor){
        Gallery record = new Gallery();
        record.setId(cursor.getLong(0));
        record.setName(cursor.getString(1));
        record.setUsrId(cursor.getInt(2));
        return record;
    }
}
