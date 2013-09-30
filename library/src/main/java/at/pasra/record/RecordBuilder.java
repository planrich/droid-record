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

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
