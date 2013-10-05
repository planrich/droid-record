package at.pasra.record.generation

import at.pasra.record.AndroidRecordPlugin
import at.pasra.record.database.Table

/*!
 * @query_interface Query interface
 * %p
 *   The query interface allows you to easily select the data records that match the given
 *   rules. The following interface provides access to the records:
 *
 * %span.filename
 *   RecordBuilder.java
 * %pre
 *   %code{ data: { language: 'java' } }
 *     :preserve
 *       public abstract class RecordBuilder&lt;E&gt; {
 *           // Select entries. Calling this function twice will override the effect
 *           // of the first call.
 *           public RecordBuilder&lt;E&gt; where(String selection, String ... args);
 *
 *           // Order the selection.
 *           public RecordBuilder&lt;E&gt; orderBy(String order);
 *
 *           // Directly access the rows and columns.
 *           public Cursor cursor();
 *
 *           // Performs the actual fetching from the database.
 *           // Returns The list of all matching entries
 *           public abstract List&lt;E&gt; all();
 *
 *           // Returns the first entry if it exists. Null otherwise.
 *           public abstract E first();
 *       }
 *
 * @query_interface|examples Examples
 *
 * Here are some examples what the query interface is capable of:
 *
 * %pre
 *   %code{ data: { language: 'java' } }
 *     :preserve
 *       LocalSession session = ...; // get the session e.g. from the application
 *       Picture first = session.queryPictures().orderBy("_id asc").first();
 *       List&lt;Picture&gt; mountainPictures =
 *         session.queryPictures().where("title like ?", "%rocky mountains%").all();
 *
 *       List&lt;Picture&gt; popularMountainPictures = session.queryPictures()
 *           .where("(title like ? and likes &gt; ?) or likes &lt; ?",
 *             "%rocky mountains%",
 *             "100",
 *             "50").orderBy("title asc").all();
 *
 *       Cursor queryYourself = session.queryPictures().cursor();
 *       while (queryYourself.moveToNext()) {
 *           // This is potentially dangerous and could lead to runtime bugs!
 *           // But if you _know_ what you are doing this is a nice feature to have!
 *           Picture picture = Picture.fromCursor(queryYourself);
 *           String title = queryYourself
 *               .getString(queryYourself.getColumnIndex("title"));
 *       }
 */
class RecordBuilderGenerator {

    Table table;

    RecordBuilderGenerator(Table table) {
        this.table = table
    }

    void generate(String path, String pkg) {

        CodeGenerator c = new CodeGenerator();
        c.copyrightHeader();
        c.doNotModify();

        def javaClassName = table.javaClassName

        c.line("package ${pkg};")
        c.line()
        c.line("import at.pasra.record.RecordBuilder;")
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line()
        c.wrap("public class ${javaClassName}RecordBuilder extends RecordBuilder<${javaClassName}>") {
            c.wrap("public ${javaClassName}RecordBuilder(SQLiteDatabase db)") {
                def cols = table.getOrderedFields(true).collect({ f -> "\"${f.name}\"" })
                c.line("super(\"${table.sqlTableName}\", new String[] { ${cols.join(", ")} }, db);");
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
