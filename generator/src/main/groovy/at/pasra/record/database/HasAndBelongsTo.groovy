package at.pasra.record.database

import at.pasra.record.util.Inflector
import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/13/13.
 */
class HasAndBelongsTo extends Relation {

    def many
    def through
    def through_table

    HasAndBelongsTo(Table origin) {
        super(origin)
    }

    @Override
    void generateSessionMethods(CodeGenerator c) {

    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaClassName = target.javaClassName
        def pluralJavaClassName = Inflector.pluralizeCamel(javaClassName)
        c.wrap("public java.util.List<${javaClassName}> load${pluralJavaClassName}(LocalSession session)") {
            c.line("String query = \"select d.* from ${origin.sqlTableName} o, ${through_table.sqlTableName} t, ${target.sqlTableName} d\" +")
            c.line("               \" where\" +")
            c.line("               \" o.${origin.primary.name} = ? and\" +")
            c.line("               \" o.${origin.primary.name} = t.${foreign_key(origin.name, "foreign_key_has")} and\" +")
            c.line("               \" d.${target.primary.name} = t.${foreign_key(target.name, "foreign_key_belongs_to")}\";")
            c.line("android.database.Cursor c = session.queryRaw(query, Long.toString(m${origin.primary.fieldName}));")
            c.line("RecordBuilder<${javaClassName}> rb = session.query${pluralJavaClassName}();")
            c.line("return rb.all(c);");
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {
    }

    @Override
    void checkIntegrity() {
        def ( fk, has_foreign_key , type_match, type ) = check_foreign_key_exists(origin.name, through_table.fields.values())

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${through_table.sqlTableName} does not specify a foreign key '${fk}'. " +
                    "This means that a ${origin.sqlTableName} record does _NOT_ have and belong to many ${Inflector.pluralize(origin.name)}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${through_table.sqlTableName}: '${fk}' column type is '${type}' but it should be '${origin.primary.type}'. Please change the type!")
        }

        ( fk, has_foreign_key , type_match, type ) = check_foreign_key_exists(target.name, through_table.fields.values())

        if (!has_foreign_key) {
            throw new InvalidUserDataException("Table ${through_table.sqlTableName} does not specify a foreign key '${fk}'. " +
                    "This means that a ${target.sqlTableName} record does _NOT_ have and belong to many ${Inflector.pluralize(target.name)}!")
        }

        if (!type_match) {
            throw new InvalidUserDataException("Table ${target.sqlTableName}: '${fk}' column type is '${type}' but it should be '${origin.primary.type}'. Please change the type!")
        }
    }
}
