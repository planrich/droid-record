package com.pasra.android.record;

/**
 * Created by rich on 9/13/13.
 */
public interface Session {

    void insert(Record record);

    void delete(Record record);

    void update(Record record);

}
