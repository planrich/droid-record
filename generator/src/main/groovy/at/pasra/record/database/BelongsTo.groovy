package at.pasra.record.database

import at.pasra.record.Inflector
import at.pasra.record.generation.CodeGenerator
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
        def type_match = false
        def type = null
        def foreign_key = "${target.name}_id"
        origin.fields.values().each { Field f ->
            if (f.name == foreign_key) {
                has_foreign_key = true
                type = f.type
                if (f.type == origin.primary.type) {
                    type_match = true
                }
            }
        }

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${origin.sqlTableName} does not specify a foreign key '${foreign_key}'. " +
                    "This means that a ${origin.sqlTableName} record does _NOT_ belong to ${Inflector.pluralize(target.sqlTableName)}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${origin.sqlTableName}: '${foreign_key}' column type is '${type}' but it should be '${target.primary.type}'. Please change the type!")
        }
    }
}
