package com.pasra.android.record;

/**
 * Created by rich on 9/8/13.
 */
public abstract class AbstractRecord {

    public abstract String getTableName();

    public abstract int getMigration();
}
