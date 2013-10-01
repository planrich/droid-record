package at.pasra.record;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rich on 9/19/13.
 */
public abstract class RecordBuilder<E> {
    protected String tableName;
    protected SQLiteDatabase db;
    protected String selection = null;
    protected String[] bindings = null;
    protected String order = null;
    protected boolean distinct = false;
    protected String[] columns;
    protected boolean modified = false;
    protected List<E> cachedAll;
    protected E cachedFirst = null;
    protected int offset = -1;
    protected int limit = -1;

    public RecordBuilder(String tableName, String[] columns, SQLiteDatabase db) {
        this.tableName = tableName;
        this.db = db;
        this.columns = columns;
    }

    public RecordBuilder<E> where(String selection, String ... args) {
        modified = true;
        cachedFirst = null;

        this.selection = selection;
        this.bindings = args;
        return this;
    }

    public RecordBuilder<E> orderBy(String order) {
        modified = true;
        cachedFirst = null;

        this.order = order;

        return this;
    }

    public RecordBuilder<E> distinct() {
        return distinct(true);
    }

    public RecordBuilder<E> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public RecordBuilder<E> limit(int count) {
        this.limit = count;
        return this;
    }

    public RecordBuilder<E> limit(int offset, int count) {
        this.offset = offset;
        this.limit = count;
        return this;
    }

    public Cursor cursor() {
        String lim = null;
        if (offset >= 0 && this.limit >= 0) {
            lim = offset + " , " + Integer.toString(limit);
        } else if (this.limit >= 0) {
            lim = Integer.toString(limit);
        }

        return db.query(distinct, tableName, columns, selection, bindings, null, null, order, lim);
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

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
