package com.pasra.android.record.sample;

public class AbstractPicture{
    protected java.lang.String mName;
    protected com.pasra.android.record.Blob mImage;
    protected java.util.Date mDate;
    protected java.lang.Integer mGallery_Id;
    protected java.lang.Long mId;
    
    public AbstractPicture(java.lang.Long id){
        this.mId = id;
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public com.pasra.android.record.Blob getImage() { return mImage; }
    public void setImage(com.pasra.android.record.Blob value) { mImage = value; }
    public java.util.Date getDate() { return mDate; }
    public void setDate(java.util.Date value) { mDate = value; }
    public java.lang.Integer getGallery_Id() { return mGallery_Id; }
    public void setGallery_Id(java.lang.Integer value) { mGallery_Id = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
}
