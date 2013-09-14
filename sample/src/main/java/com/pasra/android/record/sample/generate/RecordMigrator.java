package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import com.pasra.android.record.Migrator;

public class RecordMigrator implements Migrator{
    public static final long MIGRATION_LEVEL = 20130913154915L;
    
    public long getLatestMigrationLevel(){
        return MIGRATION_LEVEL;    }
    public void migrate(SQLiteDatabase db, long currentVersion, long targetVersion){
        if (currentVersion <= targetVersion){
            db.execSQL("create table picture (name text , image blob , date text , gallery_id integer , id integer primary key);");
        }
        if (currentVersion <= targetVersion){
            db.execSQL("create table gallery (name text , id integer primary key);");
        }

    }
}
