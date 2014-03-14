package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin
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
        c.copyrightHeader()
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

            c.line("""private final java.util.Map<Long, ${javaClassName}> primaryKeyCache = new java.util.HashMap<Long, ${javaClassName}>();""")

            c.wrap("public void clearCache()") {
                c.line("primaryKeyCache.clear();")
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

                c.line("primaryKeyCache.put(id, (${javaClassName})record);")
            }

            // LOAD
            c.wrap("public ${javaClassName} load(SQLiteDatabase db, long id)") {

                c.line("${javaClassName} cached = primaryKeyCache.get(id);")
                c.wrap("if (cached != null)") {
                    c.line("return cached;")
                }

                def select = table.getOrderedFields(true).collect({ f -> f.sqlName }).join(", ")
                c.line("Cursor c = db.rawQuery(\"select ${select} from ${table.sqlTableName} where ${table.primary.name} = ?;\", new String[] { Long.toString(id) });")
                c.wrap("if (c.moveToFirst())") {

                    table.javaCallsNewObjectFromCursor(c, "record", "c");
                    c.line("primaryKeyCache.put(id, record);")
                    c.line("return record;")
                }

                c.line("return null;")
            }

            c.wrap("public void delete(SQLiteDatabase db, long id)") {
                c.line("db.execSQL(\"delete from ${table.sqlTableName} where  ${table.primary.name} = ?;\", new String[] { Long.toString(id) });")
                c.line("primaryKeyCache.remove(id);")
            }

            c.wrap("public void update(SQLiteDatabase db, Abstract${javaClassName} record)") {
                c.line("ContentValues values = new ContentValues(${orderedFields.size()});")
                orderedFields.each { f ->
                    c.line("values.put(\"${f.name}\", ${f.javaCallToSerialize("record")});")
                }

                c.line("long id = record.get${table.primary.javaFieldName}();")
                c.line("db.update(\"${table.sqlTableName}\", values, \"${table.primary.name} = ?\", new String[] { Long.toString(id) });")
            }

            table.relations.each { Relation r ->
                r.generateRecordMethods(c);
            }
        }

        DroidRecordPlugin.write(source, pkg, "${javaClassName}Record.java", c.toString(), true);
    }
}
