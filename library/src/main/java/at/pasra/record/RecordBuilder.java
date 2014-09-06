package at.pasra.record;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    /*!
     * @query_interface Query interface
     * -#after relations
     * -#position 100001
     *
     * %p
     *   The query interface allows you to easily select the data records that match the given
     *   rules. The following interface provides access to the records:
     *
     */
    public RecordBuilder(String tableName, String[] columns, SQLiteDatabase db) {
        this.tableName = tableName;
        this.db = db;
        this.columns = columns;
    }

    /*!
     * @query_interface|where Where
     * %span.filename
     *   where
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       public RecordBuilder&lt;E&gt; where(String selection, String ... args);
     *
     *       List&lt;Picture&gt; pictures =
     *           session.queryPictures().where("(title like ? and likes &gt; ?) or likes &lt; ?",
     *              "%rocky mountains%", "100", "50").all();
     *
     */
    public RecordBuilder<E> where(String selection, String... args) {
        modified = true;
        cachedFirst = null;

        this.selection = selection;
        this.bindings = args;
        return this;
    }

    /*!
     * @query_interface|order_by Order by
     * %span.filename
     *   orderBy
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       public RecordBuilder&lt;E&gt; orderBy(String selection);
     *
     *       List&lt;Gallery&gt; galleries =
     *           session.queryGallery().where("name like '%cars%'").orderBy("name asc").all();
     *
     */
    public RecordBuilder<E> orderBy(String order) {
        modified = true;
        cachedFirst = null;

        this.order = order;

        return this;
    }

    /*!
     * @query_interface|distinct Distinct
     * %span.filename
     *   distinct
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       public RecordBuilder&lt;E&gt; distinct();
     *       public RecordBuilder&lt;E&gt; distinct(boolean value);
     *
     */
    public RecordBuilder<E> distinct() {
        return distinct(true);
    }

    public RecordBuilder<E> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /*!
     * @query_interface|limit Limit
     * %span.filename
     *   limit
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       public RecordBuilder&lt;E&gt; limit(int count);
     *       public RecordBuilder&lt;E&gt; limit(int offset, int count);
     */
    public RecordBuilder<E> limit(int count) {
        this.limit = count;
        return this;
    }

    public RecordBuilder<E> limit(int offset, int count) {
        this.offset = offset;
        this.limit = count;
        return this;
    }

    /*!
     * @query_interface|sum Sum
     * %span.filename
     *   sum
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       public RecordBuilder&lt;E&gt; sum(String column);
     *       public RecordBuilder&lt;E&gt; sumDouble(String column);
     *
     *       long cents = session.queryProduct().sum("cents");
     *       double dollar = session.queryProduct().sumDouble("cents / 100.0");
     */
    public long sum(String name) {
        Cursor cursor = db.query(distinct, tableName, new String[] { "sum(" + name + ")" }, selection, bindings, null, null, order, limit());

        cursor.moveToFirst();
        long value = cursor.getLong(0);
        cursor.close();

        return value;
    }

    public double sumDouble(String name) {
        Cursor cursor = db.query(distinct, tableName, new String[] { "sum(" + name + ")" }, selection, bindings, null, null, order, limit());

        cursor.moveToFirst();
        double value = cursor.getDouble(0);
        cursor.close();

        return value;
    }

    private String limit() {
        String lim = null;
        if (offset >= 0 && this.limit >= 0) {
            lim = offset + " , " + Integer.toString(limit);
        } else if (this.limit >= 0) {
            lim = Integer.toString(limit);
        }

        return lim;
    }

    /*!
     * @query_interface|count Count
     * %p
     *   You can count the rows of the current builder with the method count.
     *   You can provide the column name you want to count with the first parameter,
     *   or just leave it blank resulting in a query like:
     *   %code select count(*) from table ... ;
     *
     */
    public long count(String column) {
        Cursor cursor = db.query(distinct, tableName, new String[] { "count("+column+")" }, selection, bindings, null, null, order, limit());
        long count = cursor.getCount();
        cursor.close();
        return count;
    }
    public long count() {
        return count("*");
    }

    /*!
     * @query_interface|cursor Cursor
     * %span.filename
     *   cursor
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       Cursor queryYourself = session.queryPictures().cursor();
     *       while (queryYourself.moveToNext()) {
     *           // This is potentially dangerous and could lead to runtime bugs!
     *           // But if you _know_ what you are doing this is a nice feature to have!
     *           Picture picture = Picture.fromCursor(queryYourself);
     *           String title = queryYourself
     *               .getString(queryYourself.getColumnIndex("title"));
     *       }
     */
    public Cursor cursor() {
        return db.query(distinct, tableName, columns, selection, bindings, null, null, order, limit());
    }

    /**
     * Performs the actual fetching from the database
     * @return fetch all entries and insert it into a list
     */
    public List<E> all() {
        return all(cursor());
    }

    public abstract List<E> all(Cursor c);

    /**
     *
     * @return the first entry if it exists. null otherwise
     */
    public E first() {
        return first(cursor());
    }

    /*!
     *
     * @query_interface|first First, All
     * -#position 1
     * %span.filename
     *   first
     * %pre
     *   %code{ data: { language: 'java' } }
     *     :preserve
     *       Picture first = session.queryPictures().orderBy("name asc").first();
     *       List&lt;Picture&gt; all = session.queryPictures().orderBy("name asc").all();
     *
     */
    public abstract E first(Cursor c);

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
