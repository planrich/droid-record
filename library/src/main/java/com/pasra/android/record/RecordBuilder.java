package com.pasra.android.record;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by rich on 9/19/13.
 */
public abstract class RecordBuilder<E> {
    private String tableName;
    private SQLiteDatabase db;
    private String selection = null;
    private String[] bindings = null;
    private String order = null;
    private boolean distinct = false;
    private String[] columns;

    public RecordBuilder(String tableName, String[] columns, SQLiteDatabase db) {
        this.tableName = tableName;
        this.db = db;
        this.columns = columns;
    }

    public RecordBuilder<E> where(String selection, String ... args) {
        this.selection = selection;
        this.bindings = args;
        return this;
    }

    public RecordBuilder<E> orderBy(String order) {
        this.order = order;

        return this;
    }

    public Cursor cursor() {
        return db.query(distinct, tableName, columns, selection, bindings, null, null, order, null);
    }

    /**
     * Performs the actual fetching from the database
     * @return fetch all entries and insert it into a list
     */
    public abstract List<E> all();

    /**
     *
     * @return the first entry if it exists. null otherwise
     */
    public abstract E first();
}
