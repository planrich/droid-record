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

        def javaObj = Inflector.camelize(target.name)
        def record = Inflector.camelize(origin.name) + "Record"
        def originObj = Inflector.camelize(origin.name)
        def plural = Inflector.camelize(Inflector.pluralize(target.name))
        c.wrap("public java.util.List<${javaObj}> load${plural}Blocking(long ${originObj}Id)") {
            c.line("return ${record}.instance().load${plural}Blocking(mDB, ${originObj}Id);")
        }

    }

    @Override
    void generateJavaMethods(CodeGenerator c) {

        def javaObj = Inflector.camelize(target.name)
        def plural = Inflector.camelize(Inflector.pluralize(target.name))
        c.wrap("public java.util.List<${javaObj}> load${plural}(LocalSession session)") {
            c.line("return session.load${plural}Blocking(${origin.javaCallGetId("this")});")
        }
    }

    @Override
    void generateRecordMethods(CodeGenerator c) {

        def javaObj = Inflector.camelize(target.name)
        def originObj = Inflector.camelize(origin.name)
        def plural = Inflector.camelize(Inflector.pluralize(target.name))
        c.wrap("public java.util.List<${javaObj}> load${plural}Blocking(SQLiteDatabase db, long ${originObj}Id)") {
            c.line("java.util.List<${javaObj}> list = new java.util.ArrayList();")
            c.line("Cursor c = db.rawQuery(\"select * from ${target.name} where ${origin.name}_id = ?\", new String[] { Long.toString(${originObj}Id) } );")
            c.wrap("while (c.moveToNext())") {
                target.javaCallsNewObjectFromCursor(c, "record", "c")
                c.line("list.add(record);")
            }
            c.line("return list;");
        }
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
