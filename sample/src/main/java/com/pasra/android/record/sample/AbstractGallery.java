package com.pasra.android.record.sample;

public class AbstractGallery{
    protected java.lang.String mName;
    protected java.lang.Long mId;
    
    public AbstractGallery(java.lang.Long id){
        this.mId = id;
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
}
