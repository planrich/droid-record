package com.pasra.android.record;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rich on 9/8/13.
 */
public class RootDao {

    SQLiteDatabase mDatabase;

    public RootDao(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }
}
