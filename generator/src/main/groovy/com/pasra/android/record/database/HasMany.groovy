package com.pasra.android.record.database

import com.pasra.android.record.Inflector
import com.pasra.android.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/13/13.
 */
class HasMany extends Relation {

    HasMany(Table origin, Table target) {
        super(origin, target)
    }

    @Override
    void generateSessionMethods(CodeGenerator c) {

    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaObj = Inflector.camelize(target.name)
        def plural = Inflector.camelize(Inflector.pluralize(target.name))
        c.wrap("public RecordBuilder<${javaObj}> load${plural}(LocalSession session)") {
            c.line("return session.query${plural}().where(\"${origin.name}_id = ?\", Long.toString(${origin.primary.javaPrivateFieldName()}) );")
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {
    }

    @Override
    void checkIntegrity() {
        def has_foreign_key = false
        def foreign_key = "${origin.name}_id"
        target.fields.values().each { Field f ->
            if (f.name == foreign_key) {
                has_foreign_key = true
            }
        }

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${target.name} does not specify a foreign key '${foreign_key}'. " +
                    "This means that a ${target.name} record does _NOT_ have many ${Inflector.pluralize(target.name)}!")
        }
    }
}
