package at.pasra.record.database

import at.pasra.record.util.Inflector
import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/13/13.
 */
class HasMany extends Relation {

    def table

    HasMany(Table origin) {
        super(origin)
    }


    @Override
    void generateSessionMethods(CodeGenerator c) {

    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaClassName = target.javaClassName
        def pluralJavaClassName = Inflector.pluralizeCamel(javaClassName)
        c.wrap("public RecordBuilder<${javaClassName}> load${pluralJavaClassName}(LocalSession session)") {
            c.line("return session.query${pluralJavaClassName}().where(\"${this.foreign_key()} = ?\", Long.toString(${origin.primary.javaFieldName()}) );")
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

        def fk = foreign_key()
        target.fields.values().each { Field f ->
            if (f.name == fk) {
                has_foreign_key = true
                type = f.type
                if (f.type == origin.primary.type) {
                    type_match = true
                }
            }
        }

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${target.sqlTableName} does not specify a foreign key '${fk}'. " +
                    "This means that a ${origin.sqlTableName} record does _NOT_ have many ${Inflector.pluralize(target.name)}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${target.sqlTableName}: '${foreign_key}' column type is '${type}' but it should be '${origin.primary.type}'. Please change the type!")
        }
    }
}
