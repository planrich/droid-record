package at.pasra.record.database

import at.pasra.record.util.Inflector
import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/13/13.
 */
class HasOne extends Relation {

    def table

    HasOne(Table origin) {
        super(origin)
    }

    @Override
    void generateSessionMethods(CodeGenerator c) {}

    @Override
    void generateJavaMethods(CodeGenerator c) {
        c.wrap("public ${target.javaClassName} load${target.javaClassName}(LocalSession session)") {
            c.line("return session.query${Inflector.camelize(Inflector.pluralize(target.javaClassName))}().where(\"${origin.name}_id = ?\", Long.toString(${origin.primary.javaFieldName()})).limit(1).first();")
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {}

    @Override
    void checkIntegrity() {
        def has_foreign_key = false
        def type_match = false
        def type = null
        def foreign_key = foreign_key()
        target.fields.values().each { Field f ->
            if (f.name == foreign_key) {
                has_foreign_key = true
                type = f.type
                if (f.type == origin.primary.type) {
                    type_match = true
                }
            }
        }

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${target.sqlTableName} does not specify a foreign key '${foreign_key}'. " +
                    "This means that a ${origin.sqlTableName} record does _NOT_ have one ${Inflector.singularize(target.sqlTableName)}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${target.sqlTableName}: '${foreign_key}' column type is '${type}' but it should be '${origin.primary.type}'. Please change the type!")
        }
    }
}
