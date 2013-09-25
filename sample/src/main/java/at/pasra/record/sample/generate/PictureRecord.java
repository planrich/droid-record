package at.pasra.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import at.pasra.record.SQLiteConverter;

public class PictureRecord{
    private static PictureRecord mInstance;
    public static PictureRecord instance(){
        if (mInstance == null){
            mInstance = new PictureRecord();
        }
        return mInstance;
    }
    public void save(SQLiteDatabase db, AbstractPicture record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractPicture record){
        ContentValues values = new ContentValues(4);
        values.put("name", record.getName());
        values.put("image", record.getImage());
        values.put("date", SQLiteConverter.dateToString(record.getDate()));
        values.put("gallery_id", record.getGalleryId());
        long id = db.insert("pictures", null, values);
        record.setId(id);
    }
    public Picture load(SQLiteDatabase db, long id){
        Cursor c = db.rawQuery("select * from pictures where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Picture record = new Picture();
            record.setId(c.getLong(0));
            record.setName(c.getString(1));
            record.setImage(c.getBlob(2));
            record.setDate(SQLiteConverter.stringToDate(c.getString(3)));
            record.setGalleryId(c.getLong(4));
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from pictures where  _id = ?;", new String[] { Long.toString(id) });
    }
    public void update(SQLiteDatabase db, AbstractPicture record){
        ContentValues values = new ContentValues(4);
        values.put("name", record.getName());
        values.put("image", record.getImage());
        values.put("date", SQLiteConverter.dateToString(record.getDate()));
        values.put("gallery_id", record.getGalleryId());
        long id = record.getId();
        db.update("pictures", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
