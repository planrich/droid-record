package at.pasra.record.generation

import at.pasra.record.AndroidRecordPlugin
import at.pasra.record.database.Relation
import at.pasra.record.database.Table
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
        def javaClassName = table.javaClassName

        def recordname = "${javaClassName}Record";
        def orderedFields = table.getOrderedFields(false)

        c.line("package ${pkg};")
        c.line();
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import android.content.ContentValues;")
        c.line("import android.database.Cursor;")
        c.line("import at.pasra.record.SQLiteConverter;")

        c.line();

        c.wrap("public class ${recordname}") {
            c.line("private static ${recordname} mInstance;")
            c.wrap("public static ${recordname} instance()") {
                c.wrap("if (mInstance == null)") {
                    c.line("mInstance = new ${recordname}();")
                }

                c.line("return mInstance;")
            }

            // SAVE
            c.wrap("public void save(SQLiteDatabase db, Abstract${javaClassName} record)") {
                c.wrap("if (record.${table.primary.javaGetCall()} == null)") {
                    c.line("insert(db, record);")
                }
                c.wrap("else") {
                    c.line("update(db, record);")
                }
            }

            // INSERT
            c.wrap("public void insert(SQLiteDatabase db, Abstract${javaClassName} record)") {
                c.line("ContentValues values = new ContentValues(${orderedFields.size()});")
                orderedFields.each { f ->
                    c.line("values.put(\"${f.name}\", ${f.javaCallToSerialize("record")});")
                }
                c.line("long id = db.insert(\"${table.sqlTableName}\", null, values);")
                c.line("record.setId(id);")
            }

            // LOAD
            c.wrap("public ${javaClassName} load(SQLiteDatabase db, long id)") {
                c.line("Cursor c = db.rawQuery(\"select * from ${table.sqlTableName} where ${table.primary.name} = ?;\", new String[] { Long.toString(id) });")
                c.wrap("if (c.moveToFirst())") {

                    table.javaCallsNewObjectFromCursor(c, "record", "c");
                    c.line("return record;")
                }

                c.line("return null;")
            }

            c.wrap("public void delete(SQLiteDatabase db, long id)") {
                c.line("db.execSQL(\"delete from ${table.sqlTableName} where  ${table.primary.name} = ?;\", new String[] { Long.toString(id) });")
            }

            c.wrap("public void update(SQLiteDatabase db, Abstract${javaClassName} record)") {
                c.line("ContentValues values = new ContentValues(${orderedFields.size()});")
                orderedFields.each { f ->
                    c.line("values.put(\"${f.name}\", ${f.javaCallToSerialize("record")});")
                }

                c.line("long id = record.get${table.primary.javaName}();")
                c.line("db.update(\"${table.sqlTableName}\", values, \"${table.primary.name} = ?\", new String[] { Long.toString(id) });")
            }

            table.relations.each { Relation r ->
                r.generateRecordMethods(c);
            }
        }

        File file = AndroidRecordPlugin.file(source, pkg, "${javaClassName}Record.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();

    }
}
