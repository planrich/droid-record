package com.pasra.android.record.database

import com.pasra.android.record.generation.CodeGenerator

/**
 * Created by rich on 9/13/13.
 */
class HasOne extends Relation {
    HasOne(Table origin, String target_table) {
        super(origin, target_table)
    }

    @Override
    void generateJava(CodeGenerator c) {

    }
}
