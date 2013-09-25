package at.pasra.record.database

import at.pasra.record.generation.CodeGenerator

/**
 * Created by rich on 9/13/13.
 */
abstract class Relation {

    protected Table origin;
    protected Table target;

    protected Relation(Table origin, Table target) {
        this.origin = origin
        this.target = target
    }

    abstract void generateSessionMethods(CodeGenerator c);
    abstract void generateJavaMethods(CodeGenerator c);
    abstract void generateRecordMethods(CodeGenerator c);

    abstract void checkIntegrity();
}
