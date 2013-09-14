package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import java.util.concurrent.Future;

public class PictureRecord{
    private static PictureRecord mInstance;
    public static PictureRecord instance(){
        if (mInstance == null){
            mInstance = new PictureRecord();
        }
        return mInstance;
    }
    public void insert(SQLiteDatabase db, AbstractPicture record){
        String sql = "insert into picture (com.pasra.android.record.database.Field@1c817c06,com.pasra.android.record.database.Field@78f57c88,com.pasra.android.record.database.Field@3cc302c2,com.pasra.android.record.database.Field@26ea2db8) values (?,?,?,?)";
        String[] args = new String[4];
        if (record.getName() != null){
            args[0] = record.getName().toString();
        }
        if (record.getImage() != null){
            args[1] = record.getImage().toString();
        }
        if (record.getDate() != null){
            args[2] = record.getDate().toString();
        }
        if (record.getGalleryId() != null){
            args[3] = record.getGalleryId().toString();
        }
        db.execSQL(sql, args);
    }
}
