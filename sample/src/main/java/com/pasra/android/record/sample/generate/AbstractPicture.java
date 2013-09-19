package com.pasra.android.record.sample.generate;
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!

import com.pasra.android.record.SQLiteConverter;

public class AbstractPicture{
    protected java.lang.String mName;
    protected byte[] mImage;
    protected java.util.Date mDate;
    protected java.lang.Long mGalleryId;
    protected java.lang.Long mId;
    
    public AbstractPicture(java.lang.Long id){
        this.mId = id;
        this.mName = "";
        this.mImage = new byte[0];
        this.mDate = new java.util.Date(0);
        this.mGalleryId = new Long(0L);
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public byte[] getImage() { return mImage; }
    public void setImage(byte[] value) { mImage = value; }
    public java.util.Date getDate() { return mDate; }
    public void setDate(java.util.Date value) { mDate = value; }
    public java.lang.Long getGalleryId() { return mGalleryId; }
    public void setGalleryId(java.lang.Long value) { mGalleryId = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public Gallery loadGallery(LocalSession session){
        return session.loadGallery(this.getId());
    }
    public static Picture of(Gallery obj0){
        Picture obj = new Picture();
        obj.setGalleryId(obj0.getId());
        return obj;
    }
    public static Picture fromCursor(android.database.Cursor cursor){
        Picture record = new Picture();
        record.setName(cursor.getString(0));
        record.setImage(cursor.getBlob(1));
        record.setDate(SQLiteConverter.stringToDate(cursor.getString(2)));
        record.setGalleryId(cursor.getLong(3));
        record.setId(cursor.getLong(4));
        return record;
    }
}
