package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Table

/**
 * Created by rich on 9/14/13.
 */
class RecordPersistorGenerator {

    private Table table;

    RecordPersistorGenerator(Table table) {
        this.table = table
    }

    void generateSQLite(String source, String pkg) {

        CodeGenerator c = new CodeGenerator();
        def CamName = Inflector.camelize(table.name)

        def recordname = "${CamName}Record";

        c.line("package ${pkg};")
        c.line();
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import java.util.concurrent.Future;")
        c.line();

        c.wrap("public class ${recordname}") {
            c.line("private static ${recordname} mInstance;")
            c.wrap("public static ${recordname} instance()") {
                c.wrap("if (mInstance == null)") {
                    c.line("mInstance = new ${recordname}();")
                }

                c.line("return mInstance;")
            }

            c.wrap("public void insert(SQLiteDatabase db, Abstract${CamName} record)") {
                def orderedFields = table.getOrderedFields(false)
                def marks = ["?"] * orderedFields.size()
                c.line("String sql = \"insert into ${table.name} (${orderedFields.join(",")}) values (${marks.join(",")})\";")
                c.line("String[] args = new String[${orderedFields.size()}];")
                orderedFields.eachWithIndex { f, i ->
                    c.wrap("if (record.get${Inflector.camelize(f.name)}() != null)") {
                        c.line("args[${i}] = record.get${Inflector.camelize(f.name)}().toString();")
                    }
                }
                c.line("db.execSQL(sql, args);")
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
