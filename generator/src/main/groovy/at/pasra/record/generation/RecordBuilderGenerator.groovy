package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.database.Table

class RecordBuilderGenerator {

    Table table;

    RecordBuilderGenerator(Table table) {
        this.table = table
    }

    void generate(String path, String pkg, String domainPkg) {

        CodeGenerator c = new CodeGenerator();
        c.copyrightHeader();
        c.doNotModify();

        def javaClassName = table.javaClassName

        c.line("package ${pkg};")
        c.line()
        c.line("import at.pasra.record.RecordBuilder;")
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import ${domainPkg}.${javaClassName};")
        c.line()
        c.wrap("public class ${javaClassName}RecordBuilder extends RecordBuilder<${javaClassName}>") {
            c.wrap("public ${javaClassName}RecordBuilder(SQLiteDatabase db)") {
                def cols = table.getOrderedFields(true).collect({ f -> "\"${f.name}\"" })
                c.line("super(\"${table.sqlTableName}\", new String[] { ${cols.join(", ")} }, db);");
            }

            c.line("@Override")
            c.wrap("public java.util.List<${javaClassName}> all(android.database.Cursor c)") {

                c.line("java.util.List<${javaClassName}> list = new java.util.ArrayList<${javaClassName}>();")

                c.wrap("while (c.moveToNext())") {
                    c.line("list.add(${javaClassName}.fromCursor(c));");
                }

                c.line("return list;")
            }

            c.line("@Override")
            c.wrap("public ${javaClassName} first(android.database.Cursor c)") {

                c.wrap("if (c.moveToFirst())") {
                    c.line("${javaClassName} record = ${javaClassName}.fromCursor(c);");
                    c.line("c.close();")
                    c.line("return record;")
                }
                c.line("c.close();")
                c.line("return null;")
            }
        }

        DroidRecordPlugin.writeJavaSource(path, pkg, "${javaClassName}RecordBuilder.java", c.toString(), true);
    }
}
