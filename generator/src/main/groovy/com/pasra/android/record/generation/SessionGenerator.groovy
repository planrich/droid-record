package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Table

/**
 * Created by rich on 9/14/13.
 */
class SessionGenerator {

    def tables

    SessionGenerator(tables) {
        this.tables = tables
    }

    void generate(String path, String pkg) {

        CodeGenerator c = new CodeGenerator();
        c.doNotModify()

        c.line("package ${pkg};")
        c.line()
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import com.pasra.android.record.RecordBuilder;")
        c.line();
        c.wrap("public class LocalSession") {

            c.line("private SQLiteDatabase mDB;")

            c.wrap("public LocalSession(SQLiteDatabase database)") {
                c.line("this.mDB = database;")
            }

            tables.each { String name, Table table ->
                def javaNameCaml = Inflector.camelize(table.name)
                def javaPluralCamel = Inflector.camelize(Inflector.pluralize(table.name));
                c.wrap("public void insert${javaNameCaml}(Abstract${javaNameCaml} obj)") {
                    c.line("${javaNameCaml}Record record = ${javaNameCaml}Record.instance();")
                    c.line("record.insert(mDB, obj);")
                }

                c.wrap("public ${javaNameCaml} load${javaNameCaml}(java.lang.Long id)") {
                    c.wrap("if (id == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to load a ${name} record with a null key?\");")
                    }

                    c.line("${javaNameCaml}Record record = ${javaNameCaml}Record.instance();")
                    c.line("return record.load(mDB, id);")
                }

                c.wrap("public void delete${javaNameCaml}(java.lang.Long id)") {
                    c.wrap("if (id == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to delete a ${name} record with a null key?\");")
                    }

                    c.line("${javaNameCaml}Record record = ${javaNameCaml}Record.instance();")
                    c.line("record.delete(mDB, id);")
                }

                c.wrap("public void update${javaNameCaml}(Abstract${javaNameCaml} obj)") {
                    c.wrap("if (obj == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"Argument ${name} is null\");")
                    }

                    c.line("${javaNameCaml}Record record = ${javaNameCaml}Record.instance();")
                    c.line("record.update(mDB, obj);")
                }

                // relations
                table.relations.each { relation ->
                    relation.generateSessionMethods(c)
                }

                // generate query builder creators
                c.wrap("public ${javaNameCaml}RecordBuilder query${javaPluralCamel}()") {
                    c.line("return new ${javaNameCaml}RecordBuilder(mDB);")
                }
            }
        }


        File file = AndroidRecordPlugin.file(path, pkg, "LocalSession.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();
    }
}
