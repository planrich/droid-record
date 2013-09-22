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
        if (currentVersion < targetVersion){
            db.execSQL("create table user (first_name text , last_name text , age integer , _id integer primary key);");
            {
                Cursor c = db.rawQuery("select * from usr", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    String s0 = c.getString(c.getColumnIndex("_id"));
                    String s1 = c.getString(c.getColumnIndex("first_name"));
                    String s2 = c.getString(c.getColumnIndex("last_name"));
                    String s3 = c.getString(c.getColumnIndex("age"));
                    db.rawQuery("insert into user (_id, first_name, last_name, age) values (?, ?, ?);", new String[] {s0, s1, s2, s3});
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table usr");
        }
        if (currentVersion < targetVersion){
            db.execSQL("create table gallery_mig_temp_table (name text , _id integer primary key, user_id integer );");
            {
                Cursor c = db.rawQuery("select * from gallery", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    String s0 = c.getString(c.getColumnIndex("_id"));
                    String s1 = c.getString(c.getColumnIndex("name"));
                    String s2 = c.getString(c.getColumnIndex("user_id"));
                    db.rawQuery("insert into gallery_mig_temp_table (_id, name, user_id) values (?, ?);", new String[] {s0, s1, s2});
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table gallery");
            db.execSQL("create table gallery (name text , _id integer primary key, user_id integer );");
            {
                Cursor c = db.rawQuery("select * from gallery_mig_temp_table", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    String s0 = c.getString(c.getColumnIndex("_id"));
                    String s1 = c.getString(c.getColumnIndex("name"));
                    String s2 = c.getString(c.getColumnIndex("user_id"));
                    db.rawQuery("insert into gallery (_id, name, user_id) values (?, ?);", new String[] {s0, s1, s2});
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table gallery_mig_temp_table");
        }
        if (currentVersion < targetVersion){
            db.execSQL("create table user_mig_temp_table (first_name text , last_name text , _id integer primary key);");
            {
                Cursor c = db.rawQuery("select * from user", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    String s0 = c.getString(c.getColumnIndex("_id"));
                    String s1 = c.getString(c.getColumnIndex("first_name"));
                    String s2 = c.getString(c.getColumnIndex("last_name"));
                    db.rawQuery("insert into user_mig_temp_table (_id, first_name, last_name) values (?, ?);", new String[] {s0, s1, s2});
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table user");
            db.execSQL("create table user (first_name text , last_name text , _id integer primary key);");
            {
                Cursor c = db.rawQuery("select * from user_mig_temp_table", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    String s0 = c.getString(c.getColumnIndex("_id"));
                    String s1 = c.getString(c.getColumnIndex("first_name"));
                    String s2 = c.getString(c.getColumnIndex("last_name"));
                    db.rawQuery("insert into user (_id, first_name, last_name) values (?, ?);", new String[] {s0, s1, s2});
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table user_mig_temp_table");
        }

        db.execSQL("insert or replace into android_record_config (key,value) values (?,?)", new Object[] { "version", new Long(targetVersion) });
    }
}
