package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;

// NOTE generated file! do not edit.

public class LocalSession{
    private SQLiteDatabase mDB;
    public LocalSession(SQLiteDatabase database){
        this.mDB = database;
    }
    public void insertPicture(AbstractPicture obj){
        PictureRecord record = PictureRecord.instance();
        record.insert(mDB, obj);
    }
    public Picture loadPicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a picture record with a null key?");
        }
        PictureRecord record = PictureRecord.instance();
        return record.load(mDB, id);
    }
    public void insertGallery(AbstractGallery obj){
        GalleryRecord record = GalleryRecord.instance();
        record.insert(mDB, obj);
    }
    public Gallery loadGallery(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a gallery record with a null key?");
        }
        GalleryRecord record = GalleryRecord.instance();
        return record.load(mDB, id);
    }
}
