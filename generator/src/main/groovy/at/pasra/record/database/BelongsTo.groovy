package at.pasra.record.database

import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/17/13.
 */
class BelongsTo extends Relation{

    def to

    protected BelongsTo(Table origin) {
        super(origin)
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
        if (foreign_key != null)
            return foreign_key
        return "${target.name}_id"
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
