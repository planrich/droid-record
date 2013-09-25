// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!
package at.pasra.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import at.pasra.record.RecordBuilder;

public class LocalSession{
    private SQLiteDatabase mDB;
    public LocalSession(SQLiteDatabase database){
        this.mDB = database;
    }
    public void saveGallery(AbstractGallery obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of Gallery which was null. Cannot do that!");
        }
        GalleryRecord record = GalleryRecord.instance();
        record.save(mDB, obj);
    }
    public Gallery findGallery(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a gallery record with a null key?");
        }
        GalleryRecord record = GalleryRecord.instance();
        return record.load(mDB, id);
    }
    public void destroyGallery(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to delete a gallery record with a null key?");
        }
        GalleryRecord record = GalleryRecord.instance();
        record.delete(mDB, id);
    }
    public GalleryRecordBuilder queryGalleries(){
        return new GalleryRecordBuilder(mDB);
    }
    public void savePicture(AbstractPicture obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of Picture which was null. Cannot do that!");
        }
        PictureRecord record = PictureRecord.instance();
        record.save(mDB, obj);
    }
    public Picture findPicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a picture record with a null key?");
        }
        PictureRecord record = PictureRecord.instance();
        return record.load(mDB, id);
    }
    public void destroyPicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to delete a picture record with a null key?");
        }
        PictureRecord record = PictureRecord.instance();
        record.delete(mDB, id);
    }
    public PictureRecordBuilder queryPictures(){
        return new PictureRecordBuilder(mDB);
    }
    public void saveUser(AbstractUser obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of User which was null. Cannot do that!");
        }
        UserRecord record = UserRecord.instance();
        record.save(mDB, obj);
    }
    public User findUser(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a user record with a null key?");
        }
        UserRecord record = UserRecord.instance();
        return record.load(mDB, id);
    }
    public void destroyUser(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to delete a user record with a null key?");
        }
        UserRecord record = UserRecord.instance();
        record.delete(mDB, id);
    }
    public UserRecordBuilder queryUsers(){
        return new UserRecordBuilder(mDB);
    }
}
