package com.pasra.android.record.database

import com.pasra.android.record.generation.CodeGenerator

/**
 * Created by rich on 9/13/13.
 */
abstract class Relation {

    protected Table origin;
    protected String target_table;

    Relation(Table origin, String target_table) {
        this.origin = origin
        this.target_table = target_table
    }

    abstract void generateJava(CodeGenerator c);
}
