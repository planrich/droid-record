package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.SQLiteConverter;

public class AbstractGallery{
    protected java.lang.String mName;
    protected java.lang.Long mId;
    
    public AbstractGallery(java.lang.Long id){
        this.mId = id;
        this.mName = "";
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public java.util.List<Picture> loadPictures(LocalSession session){
        return session.loadPicturesBlocking(this.getId());
    }
    public static Gallery fromCursor(android.database.Cursor cursor){
        Gallery record = new Gallery();
        record.setName(cursor.getString(0));
        record.setId(cursor.getLong(1));
        return record;
    }
}
