package com.pasra.android.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import com.pasra.android.record.Migrator;

public class RecordMigrator implements Migrator{
    public static final long MIGRATION_LEVEL = 20130922093633L;
    
    private final SQLiteDatabase db;
    public RecordMigrator(SQLiteDatabase db){
        this.db = db;
        this.tryCreateVersioningTable();
    }
    private void tryCreateVersioningTable(){
        db.execSQL("create table if not exists android_record_configs (_id integer primary key, key text unique not null, value text);");
    }
    public long getCurrentMigrationLevel(){
        Cursor c = db.rawQuery(
                    "select key, value from android_record_configs where key = ? order by value desc limit 1;",
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
        db.execSQL("insert or replace into android_record_configs (key,value) values ('generator_version','0.0.1')");
        if (currentVersion < targetVersion && currentVersion < 20130913154915L){
            db.execSQL("create table galleries (_id integer primary key, name text );");
            currentVersion = 20130913154915L;
        }
        if (currentVersion < targetVersion && currentVersion < 20130913165751L){
            db.execSQL("create table pictures (_id integer primary key, name text , image blob , date text , gallery_id integer );");
            currentVersion = 20130913165751L;
        }
        if (currentVersion < targetVersion && currentVersion < 20130922092812L){
            db.execSQL("create table usrs (_id integer primary key, first_name text , last_name text , age integer );");
            db.execSQL("alter table galleries add column usr_id integer ;");
            currentVersion = 20130922092812L;
        }
        if (currentVersion < targetVersion && currentVersion < 20130922093131L){
            db.execSQL("create table users (_id integer primary key, first_name text , last_name text , age integer );");
            {
                Cursor c = db.rawQuery("select * from usrs", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(c.getColumnIndex("_id")));
                    vals.put("first_name", c.getString(c.getColumnIndex("first_name")));
                    vals.put("last_name", c.getString(c.getColumnIndex("last_name")));
                    vals.put("age", c.getString(c.getColumnIndex("age")));
                    db.insert("users", null, vals);
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table usrs");
            db.execSQL("create table galleries_mig_temp_table (_id integer primary key, name text , user_id integer );");
            {
                Cursor c = db.rawQuery("select * from galleries", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(c.getColumnIndex("_id")));
                    vals.put("name", c.getString(c.getColumnIndex("name")));
                    vals.put("user_id", c.getString(c.getColumnIndex("user_id")));
                    db.insert("galleries_mig_temp_table", null, vals);
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table galleries");
            db.execSQL("create table galleries (_id integer primary key, name text , user_id integer );");
            {
                Cursor c = db.rawQuery("select * from galleries_mig_temp_table", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(c.getColumnIndex("_id")));
                    vals.put("name", c.getString(c.getColumnIndex("name")));
                    vals.put("user_id", c.getString(c.getColumnIndex("user_id")));
                    db.insert("galleries", null, vals);
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table galleries_mig_temp_table");
            currentVersion = 20130922093131L;
        }
        if (currentVersion < targetVersion && currentVersion < 20130922093633L){
            db.execSQL("create table users_mig_temp_table (_id integer primary key, first_name text , last_name text );");
            {
                Cursor c = db.rawQuery("select * from users", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(c.getColumnIndex("_id")));
                    vals.put("first_name", c.getString(c.getColumnIndex("first_name")));
                    vals.put("last_name", c.getString(c.getColumnIndex("last_name")));
                    db.insert("users_mig_temp_table", null, vals);
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table users");
            db.execSQL("create table users (_id integer primary key, first_name text , last_name text );");
            {
                Cursor c = db.rawQuery("select * from users_mig_temp_table", null);
                db.execSQL("begin");
                while (c.moveToNext()){
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(c.getColumnIndex("_id")));
                    vals.put("first_name", c.getString(c.getColumnIndex("first_name")));
                    vals.put("last_name", c.getString(c.getColumnIndex("last_name")));
                    db.insert("users", null, vals);
                }
                db.execSQL("commit");
            }
            db.execSQL("drop table users_mig_temp_table");
            new com.pasra.android.record.sample.NullMigrator().migrate(db, currentVersion, targetVersion);
            currentVersion = 20130922093633L;
        }

        db.execSQL("insert or replace into android_record_configs (key,value) values (?,?)", new Object[] { "version", new Long(currentVersion) });
    }
}
