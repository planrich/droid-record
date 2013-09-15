package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin

import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Field
import com.pasra.android.record.database.Table

import java.nio.ByteBuffer

/**
 * Created by rich on 9/14/13.
 */
class RecordGenerator {

    private Table table;

    RecordGenerator(Table table) {
        this.table = table
    }

    void generateSQLite(String source, String pkg) {

        CodeGenerator c = new CodeGenerator();
        def CamName = Inflector.camelize(table.name)

        def recordname = "${CamName}Record";
        def orderedFields = table.getOrderedFields(false)

        c.line("package ${pkg};")
        c.line();
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import android.content.ContentValues;")
        c.line("import android.database.Cursor;")
        c.line("import com.pasra.android.record.SQLiteConverter;")
        if (table.hasFieldOfType("java.nio.ByteBuffer")) {
            c.line("import java.nio.ByteBuffer;");
        }

        c.line();

        c.wrap("public class ${recordname}") {
            c.line("private static ${recordname} mInstance;")
            c.wrap("public static ${recordname} instance()") {
                c.wrap("if (mInstance == null)") {
                    c.line("mInstance = new ${recordname}();")
                }

                c.line("return mInstance;")
            }

            // INSERT
            c.wrap("public void insert(SQLiteDatabase db, Abstract${CamName} record)") {
                c.line("ContentValues values = new ContentValues(${orderedFields.size()});")
                orderedFields.each { f ->
                    c.line("values.put(\"${f.name}\", ${f.javaCallToSerialize("record")});")
                }
                c.line("long id = db.insert(\"${table.name}\", null, values);")
                c.line("record.setId(id);")
            }

            // LOAD
            c.wrap("public ${CamName} load(SQLiteDatabase db, long id)") {
                c.line("Cursor c = db.rawQuery(\"select * from ${table.name} where id = ?;\", new String[] { Long.toString(id) });")
                c.wrap("if (c.moveToFirst())") {
                    c.line("${CamName} record = new ${CamName}(null);")
                    table.getOrderedFields().each { Field f ->
                        c.line("${f.javaCallToDeserialize("record", "c")};")
                    }
                    c.line("")
                }

                c.line("return null;")
            }


            /*
            c.line("@Override")
            c.wrap("public void deleteIn(Session session)") {

            }

            c.line("@Override")
            c.wrap("public void updateIn(Session session)") {

            }*/

            /*
            c.line("@Override")
            c.wrap("public ${Inflector.camelize(name)} loadFrom(Session session, Long key)") {

            }*/
        }

        File file = AndroidRecordPlugin.file(source, pkg, "${CamName}Record.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();

    }
}
