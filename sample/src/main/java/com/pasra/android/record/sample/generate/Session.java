package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;

// NOTE generated file! do not edit.

public class Session{
    private SQLiteDatabase mDB;
    public Session(SQLiteDatabase database){
        this.mDB = database;
    }
    public void insertPicture(AbstractPicture obj){
        PictureRecord record = PictureRecord.instance();
        record.insert(mDB, obj);
    }
    public void insertGallery(AbstractGallery obj){
        GalleryRecord record = GalleryRecord.instance();
        record.insert(mDB, obj);
    }
}
