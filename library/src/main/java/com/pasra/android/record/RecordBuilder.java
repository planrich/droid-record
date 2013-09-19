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
    private Selection selection = null;
    private String order = null;
    private boolean distinct = false;
    private String[] columns;

    public RecordBuilder(String tableName, String[] columns, SQLiteDatabase db) {
        this.tableName = tableName;
        this.db = db;
        this.columns = columns;
    }

    public RecordBuilder where(String selection, String ... args) {
        this.selection = new Selection(selection, args);
        return this;
    }

    public RecordBuilder orderBy(String order) {
        this.order = order;

        return this;
    }

    public Cursor cursor() {
        return db.query(distinct, tableName, columns, selection.toString(), selection.getBindings(), null, null, order, null);
    }

    public abstract List<E> all();

    private static class Selection {
        StringBuilder mBuilder = new StringBuilder();
        String[] mBindings;

        private Selection(String selection, String[] mBindings) {
            mBuilder.append(selection);
            this.mBindings = mBindings;
        }

        public StringBuilder getBuilder() {
            return mBuilder;
        }

        public String[] getBindings() {
            return mBindings;
        }
    }
}
