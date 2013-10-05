package at.pasra.record.database

import at.pasra.record.Inflector
import at.pasra.record.generation.CodeGenerator
import com.google.gson.JsonObject
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/17/13.
 */
class BelongsTo extends Relation{
    protected BelongsTo(Table origin, Table target, JsonObject options) {
        super(origin, target, options)
    }

    @Override
    void generateSessionMethods(CodeGenerator c) {
    }

    @Override
    void generateJavaMethods(CodeGenerator c) {
        c.wrap("public ${target.javaClassName} load${target.javaClassName}(LocalSession session)") {
            c.line("return session.find${target.javaClassName}(${origin.javaCallGetId("this")});")
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {
    }

    @Override
    protected String foreign_key() {
        def foreign_key = "${target.name}_id"

        if (options.has("foreign_key")) {
            foreign_key = options.get("foreign_key").asString
        }
        return foreign_key
    }

    @Override
    void checkIntegrity() {
        def has_foreign_key = false
        def type_match = false
        def type = null
        def foreign_key = foreign_key();
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
                    "This means that a ${origin.sqlTableName} record does _NOT_ belong to ${target.sqlTableName}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${origin.sqlTableName}: '${foreign_key}' column type is '${type}' but it should be '${target.primary.type}'. Please change the type!")
        }
    }
}
