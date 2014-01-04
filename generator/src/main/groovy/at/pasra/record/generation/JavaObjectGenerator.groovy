package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.Inflector
import at.pasra.record.database.BelongsTo
import at.pasra.record.database.Field
import at.pasra.record.database.Relation
import at.pasra.record.database.Table

/**
 * Created by rich on 9/14/13.
 */
class JavaObjectGenerator {

    Table table;

    JavaObjectGenerator(Table table) {
        this.table = table
    }

    void generate(String source, String pkg) {

        CodeGenerator c = new CodeGenerator();
        c.copyrightHeader()
        c.doNotModify()

        def javaClassName = Inflector.camelize(table.name)

        c.line("package ${pkg};")
        c.line()
        c.line("import at.pasra.record.SQLiteConverter;");
        c.line("import at.pasra.record.RecordBuilder;");
        c.line();
        c.wrap("public class Abstract${Inflector.camelize(table.name)}") {
            table.fields.each { _, Field field ->
                field.generateDaoJavaField(c);
            }

            c.line()

            c.wrap("public Abstract${Inflector.camelize(table.name)}(${table.primary.javaType()} id)") {
                c.line("this.mId = id;");
                table.getOrderedFields(false).each { Field f ->
                    if (!f.allowsNull()) {
                        c.line("this.${f.privateFieldName()} = ${f.defaultValue()};")
                    }
                }
            }

            c.line()

            generateRelations(c)

            c.wrap("public static ${javaClassName} fromCursor(android.database.Cursor cursor)") {
                table.javaCallsNewObjectFromCursor(c, "record", "cursor");
                c.line("return record;")
            }
        }


        DroidRecordPlugin.write(source, pkg, "Abstract${table.javaClassName}.java", c.toString(), true)

        def file = DroidRecordPlugin.file(source, pkg, "${Inflector.camelize(table.name)}.java", false)
        if (!file.exists()) {
            c = new CodeGenerator();
            c.copyrightHeader()

            c.line("package ${pkg};")
            c.line();
            c.wrap("public class ${Inflector.camelize(table.name)} extends Abstract${Inflector.camelize(table.name)} ") {

                c.wrap("public ${Inflector.camelize(table.name)}()") {
                    c.line("super(null);");
                }

                c.line()
                c.line("// add your code here")
            }

            DroidRecordPlugin.write(source, pkg, "${Inflector.camelize(table.name)}.java", c.toString(), false)
        }
    }

    def generateRelations(CodeGenerator c) {
        table.fields.each { _, Field field ->
            field.generateDaoJavaFieldGetterSetter(c);
        }

        // CLEANUP
        def belongs_to = []
        table.relations.each { Relation relation ->
            relation.generateJavaMethods(c);
            if (relation instanceof BelongsTo) {
                belongs_to << relation;
            }
        }
        if (belongs_to.size() > 0) {
            def params = belongs_to.collect({ r -> r.target.name })
            def i = 0
            def javaParams = params.collect({ name -> "${Inflector.camelize(name)} obj${i++}"})
            c.wrap("public static ${Inflector.camelize(table.name)} of(${javaParams.join(", ")})") {
                c.line("${Inflector.camelize(table.name)} obj = new ${Inflector.camelize(table.name)}();")
                params.eachWithIndex { String name, int idx ->
                    c.line("obj.set${Inflector.camelize(name)}Id(obj${idx}.getId());")
                }
                c.line("return obj;")
            }
        }

        table.javaclass_codegen.each { Closure closure ->
            closure.curry(c).run()
        }
    }
}
