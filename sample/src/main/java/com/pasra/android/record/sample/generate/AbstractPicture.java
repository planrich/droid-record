package com.pasra.android.record.sample.generate;


// NOTE generated file! do not edit.

public class AbstractPicture{
    protected java.lang.String mName;
    protected java.nio.ByteBuffer mImage;
    protected java.util.Date mDate;
    protected java.lang.Integer mGalleryId;
    protected java.lang.Long mId;
    
    private Object[] _fieldArray = { mName, mImage, mDate, mGalleryId, mId };
    
    public AbstractPicture(java.lang.Long id){
        this.mId = id;
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public java.nio.ByteBuffer getImage() { return mImage; }
    public void setImage(java.nio.ByteBuffer value) { mImage = value; }
    public java.util.Date getDate() { return mDate; }
    public void setDate(java.util.Date value) { mDate = value; }
    public java.lang.Integer getGalleryId() { return mGalleryId; }
    public void setGalleryId(java.lang.Integer value) { mGalleryId = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public Object[] dataFields(){
        return _fieldArray;
    }
}
