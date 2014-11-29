package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.database.Table

/**
 * Created by rich on 9/14/13.
 */
class MarshallGenerator {

    Table table

    MarshallGenerator(Table table) {
        this.table = table
    }

    void generate(String source, String pkg, String domainPkg) {

        CodeGenerator c = new CodeGenerator();
        c.copyrightHeader()
        c.doNotModify()

        def entityClassName = "${table.javaClassName}"
        def javaClassName = "${table.javaClassName}Converter"

        c.line("package ${pkg};")
        c.line()
        c.line("import ${domainPkg}.${entityClassName};")
        c.line()
        c.wrap("public class ${javaClassName}") {

            c.line()

            c.wrap("public static void serialize(${entityClassName} entity)") {
            }

            c.line()

            /*c.wrap("public static ${javaClassName} fromCursor(android.database.Cursor cursor)") {
                table.javaCallsNewObjectFromCursor(c, "record", "cursor");
                c.line("return record;")
            }*/
        }

        DroidRecordPlugin.writeJavaSource(source, pkg, "${javaClassName}.java", c.toString(), true)
    }
}
