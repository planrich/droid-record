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
    public void deletePicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to delete a picture record with a null key?");
        }
        PictureRecord record = PictureRecord.instance();
        record.delete(mDB, id);
    }
    public void updatePicture(AbstractPicture obj){
        if (obj == null){
            throw new IllegalArgumentException("Argument picture is null");
        }
        PictureRecord record = PictureRecord.instance();
        record.update(mDB, obj);
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
    public void deleteGallery(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to delete a gallery record with a null key?");
        }
        GalleryRecord record = GalleryRecord.instance();
        record.delete(mDB, id);
    }
    public void updateGallery(AbstractGallery obj){
        if (obj == null){
            throw new IllegalArgumentException("Argument gallery is null");
        }
        GalleryRecord record = GalleryRecord.instance();
        record.update(mDB, obj);
    }
}
