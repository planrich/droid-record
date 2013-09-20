package com.pasra.android.record.database

import com.pasra.android.record.Inflector
import com.pasra.android.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/17/13.
 */
class BelongsTo extends Relation{
    protected BelongsTo(Table origin, Table target) {
        super(origin, target)
    }

    @Override
    void generateSessionMethods(CodeGenerator c) {
    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaObj = Inflector.camelize(target.name)
        def singular = Inflector.camelize(target.name)
        c.wrap("public ${javaObj} load${singular}(LocalSession session)") {
            c.line("return session.find${singular}(${origin.javaCallGetId("this")});")
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {
    }

    @Override
    void checkIntegrity() {
        def has_foreign_key = false
        def foreign_key = "${target.name}_id"
        origin.fields.values().each { Field f ->
            if (f.name == foreign_key) {
                has_foreign_key = true
            }
        }

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${origin.name} does not specify a foreign key '${foreign_key}'. " +
                    "This means that a ${origin.name} record does _NOT_ belong to ${Inflector.pluralize(target.name)}!")
        }
    }
}
