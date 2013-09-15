package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Field
import com.pasra.android.record.database.Table

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

        c.line("package ${pkg};")
        c.line()
        c.line();
        c.line("// NOTE generated file! do not edit.");
        c.line();
        c.wrap("public class Abstract${Inflector.camelize(table.name)}") {
            table.fields.each { _, Field field ->
                field.generateDaoJavaField(c);
            }

            c.line()

            c.wrap("public Abstract${Inflector.camelize(table.name)}(${table.primary.javaType()} id)") {
                c.line("this.mId = id;");
            }

            c.line()

            table.fields.each { _, Field field ->
                field.generateDaoJavaFieldGetterSetter(c);
            }

            table.relations.each { relation ->
                relation.generateJava(c);
            }

        }


        File file = AndroidRecordPlugin.file(source, pkg, "Abstract${Inflector.camelize(table.name)}.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();

        file = AndroidRecordPlugin.file(source, pkg, "${Inflector.camelize(table.name)}.java", false)
        if (!file.exists()) {
            c = new CodeGenerator();

            c.line("package ${pkg};")
            c.line();
            c.wrap("public class ${Inflector.camelize(table.name)} extends Abstract${Inflector.camelize(table.name)} ") {

                c.wrap("public ${Inflector.camelize(table.name)}(${table.primary.javaType()} id)") {
                    c.line("super(id);");
                }

                c.line()
                c.line("// add your code here")
            }

            writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write(c.toString());
            writer.close();
        }
    }


}
