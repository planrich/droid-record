package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector

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
        c.line();
        c.wrap("public class LocalSession") {

            c.line("private SQLiteDatabase mDB;")

            c.wrap("public LocalSession(SQLiteDatabase database)") {
                c.line("this.mDB = database;")
            }

            tables.each { name, table ->
                def nameCamel = Inflector.camelize(table.name)
                c.wrap("public void insert${nameCamel}(Abstract${nameCamel} obj)") {
                    c.line("${nameCamel}Record record = ${nameCamel}Record.instance();")
                    c.line("record.insert(mDB, obj);")
                }

                c.wrap("public ${nameCamel} load${nameCamel}(java.lang.Long id)") {
                    c.wrap("if (id == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to load a ${name} record with a null key?\");")
                    }

                    c.line("${nameCamel}Record record = ${nameCamel}Record.instance();")
                    c.line("return record.load(mDB, id);")
                }

                c.wrap("public void delete${nameCamel}(java.lang.Long id)") {
                    c.wrap("if (id == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to delete a ${name} record with a null key?\");")
                    }

                    c.line("${nameCamel}Record record = ${nameCamel}Record.instance();")
                    c.line("record.delete(mDB, id);")
                }

                c.wrap("public void update${nameCamel}(Abstract${nameCamel} obj)") {
                    c.wrap("if (obj == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"Argument ${name} is null\");")
                    }

                    c.line("${nameCamel}Record record = ${nameCamel}Record.instance();")
                    c.line("record.update(mDB, obj);")
                }

                // relations
                table.relations.each { relation ->
                    relation.generateSessionMethods(c)
                }
            }
        }


        File file = AndroidRecordPlugin.file(path, pkg, "LocalSession.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();
    }
}
