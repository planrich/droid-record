package at.pasra.record.database

import at.pasra.record.Inflector
import at.pasra.record.generation.CodeGenerator
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

        def javaClassName = target.javaClassName
        def pluralJavaClassName = Inflector.camelize(Inflector.pluralize(javaClassName))
        c.wrap("public RecordBuilder<${javaClassName}> load${pluralJavaClassName}(LocalSession session)") {
            c.line("return session.query${pluralJavaClassName}().where(\"${origin.name}_id = ?\", Long.toString(${origin.primary.javaPrivateFieldName()}) );")
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
            throw new InvalidUserDataException("Table ${target.sqlTableName} does not specify a foreign key '${foreign_key}'. " +
                    "This means that a ${origin.sqlTableName} record does _NOT_ have many ${Inflector.pluralize(target.name)}!")
        }
    }
}
