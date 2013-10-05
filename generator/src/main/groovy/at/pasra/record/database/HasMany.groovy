package at.pasra.record.database

import at.pasra.record.Inflector
import at.pasra.record.generation.CodeGenerator
import com.google.gson.JsonObject
import org.gradle.api.InvalidUserDataException

/**
 * Created by rich on 9/13/13.
 */
class HasMany extends Relation {

    /*!
     * @relations|has_many_options Options has many
     * %p
     *   You can specify the following options in the has_many value array:
     *   %ul
     *     %li
     *       %strong foreign_key
     *       \- the column name used to specify the foreign key. If this field is not specified
     *       the target table name (singular) is used and '_id' is appended.
     *       (e.g. target table name = 'stock_items', then the foreign key is 'stock_item_id')
     *     %li
     *       %strong many
     *       \- the pluralized table name or a hashed singular table name (e.g 'stock_items' or '#STOCK_ITEM').
     *       The hash should only be used when dealing with legacy databases.
     */
    HasMany(Table origin, Table target, JsonObject options) {
        super(origin, target, options)
    }


    @Override
    void generateSessionMethods(CodeGenerator c) {

    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaClassName = target.javaClassName
        def pluralJavaClassName = Inflector.camelize(Inflector.pluralize(javaClassName))
        c.wrap("public RecordBuilder<${javaClassName}> load${pluralJavaClassName}(LocalSession session)") {
            c.line("return session.query${pluralJavaClassName}().where(\"${this.foreign_key()} = ?\", Long.toString(${origin.primary.javaPrivateFieldName()}) );")
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
