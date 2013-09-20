package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Field
import com.pasra.android.record.database.Table

/**
 * Created by rich on 9/19/13.
 */
class RecordBuilderGenerator {

    Table table;

    RecordBuilderGenerator(Table table) {
        this.table = table
    }

    void generate(String path, String pkg) {

        CodeGenerator c = new CodeGenerator();

        def javaClassName = Inflector.camelize(table.name)

        c.line("package ${pkg};")
        c.doNotModify()
        c.line()
        c.line("import com.pasra.android.record.RecordBuilder;")
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line()
        c.wrap("public class ${javaClassName}RecordBuilder extends RecordBuilder<${javaClassName}>") {
            c.wrap("public ${javaClassName}RecordBuilder(SQLiteDatabase db)") {
                def cols = table.getOrderedFields(true).collect({ f -> "\"${f.name}\"" })
                c.line("super(\"${table.name}\", new String[] { ${cols.join(", ")} }, db);");
            }

            c.line("@Override")
            c.wrap("public java.util.List<${javaClassName}> all()") {
                c.line("java.util.List<${javaClassName}> list = new java.util.ArrayList<${javaClassName}>();")

                c.line("android.database.Cursor c = cursor();")
                c.wrap("while (c.moveToNext())") {
                    c.line("list.add(${javaClassName}.fromCursor(c));");
                }

                c.line("return list;")
            }

            c.line("@Override")
            c.wrap("public ${javaClassName} first()") {
                c.line("android.database.Cursor c = cursor();")
                c.wrap("if (c.moveToFirst())") {
                    c.line("${javaClassName} record = ${javaClassName}.fromCursor(c);");
                    c.line("c.close();")
                    c.line("return record;")
                }
                c.line("c.close();")
                c.line("return null;")
            }
        }


        File file = AndroidRecordPlugin.file(path, pkg, "${javaClassName}RecordBuilder.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();
    }
}
