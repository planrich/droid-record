package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import com.pasra.android.record.Migrator;

public class RecordMigrator implements Migrator{
    public static final long MIGRATION_LEVEL = 20130922093633L;
    
    private final SQLiteDatabase db;
    public RecordMigrator(SQLiteDatabase db){
        this.db = db;
        this.tryCreateVersioningTable();
    }
    private void tryCreateVersioningTable(){
        db.execSQL("create table if not exists android_record_config (_id integer primary key, key text unique not null, value text);");
    }
    public long getCurrentMigrationLevel(){
        Cursor c = db.rawQuery(
                    "select key, value from android_record_config where key = ? order by value desc limit 1;",
                    new String[] { "version" } );
        if (c.moveToNext()){
            long version = Long.parseLong(c.getString(1));
            c.close();
            return version;
        }
        return 0;
    }
    public long getLatestMigrationLevel(){
        return MIGRATION_LEVEL;
    }
    @Override
    public void migrate(){
        migrate(getCurrentMigrationLevel(), MIGRATION_LEVEL);
    }
    @Override
    public void migrate(long currentVersion, long targetVersion){
        db.execSQL("insert or replace into android_record_config (key,value) values ('generator_version','0.0.1')");
        if (currentVersion < targetVersion){
            db.execSQL("create table gallery (name text , _id integer primary key);");
        }
        if (currentVersion < targetVersion){
            db.execSQL("create table picture (name text , image blob , date text , gallery_id integer , _id integer primary key);");
        }
        if (currentVersion < targetVersion){
            db.execSQL("create table usr (first_name text , last_name text , age integer , _id integer primary key);");
        }
        if (currentVersion < targetVersion){
            db.execSQL("alter table gallery add column (usr_id integer );");
        }

        db.execSQL("insert or replace into android_record_config (key,value) values (?,?)", new Object[] { "version", new Long(targetVersion) });
    }
}
