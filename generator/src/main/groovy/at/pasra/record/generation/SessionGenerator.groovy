package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.util.Inflector
import at.pasra.record.database.Table
/**
 * Created by rich on 9/14/13.
 */
class SessionGenerator {

    def tables

    SessionGenerator(tables) {
        this.tables = tables
    }

    void generate(String path, String pkg) {

        CodeGenerator c = new CodeGenerator();
        c.copyrightHeader()
        c.doNotModify()

        c.line("package ${pkg};")
        c.line()
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import at.pasra.record.RecordBuilder;")
        c.line();
        c.wrap("public class LocalSession") {

            c.line("private SQLiteDatabase mDB;")
            tables.each { String name, Table table ->
                def fieldName = "${name}_record";
                c.line("private final ${table.javaClassName}Record ${fieldName} = new ${table.javaClassName}Record();")
            }

            c.wrap("public LocalSession(SQLiteDatabase database)") {
                c.line("this.mDB = database;")
            }

            tables.each { String name, Table table ->
                def recordName = "${table.name}_record";
                def javaClassName = table.javaClassName
                def pluralCamel = Inflector.pluralizeCamel(javaClassName);
                c.wrap("public void save${javaClassName}(${javaClassName} obj)") {
                    c.wrap("if (obj == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"Tried to save an instance of ${javaClassName} which was null. Cannot do that!\");")
                    }
                    c.line("${recordName}.save(mDB, obj);")
                }

                c.wrap("public ${javaClassName} find${javaClassName}(java.lang.Long id)") {
                    c.wrap("if (id == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to load a ${name} record with a null key?\");")
                    }

                    c.line("return ${recordName}.load(mDB, id);")
                }

                c.wrap("public void destroy${javaClassName}(${javaClassName} obj)") {
                    c.wrap("if (obj == null)") {
                        c.line("throw new IllegalArgumentException(" +
                                "\"why would you want to delete a ${name} record with a null obj?\");")
                    }

                    c.line("${recordName}.delete(mDB, obj.getId());")
                }

                // relations
                table.relations.each { relation ->
                    relation.generateSessionMethods(c)
                }

                // generate query builder creators
                c.wrap("public ${javaClassName}RecordBuilder query${pluralCamel}()") {
                    c.line("return new ${javaClassName}RecordBuilder(mDB);")
                }
            }

            c.wrap("public void clearCache()") {
                tables.each { String name, Table table ->
                    def fieldName = "${name}_record";
                    c.line("${fieldName}.clearCache();")
                }
            }

            tables.each { String name, Table table ->
                def fieldName = "${name}_record";
                def javaClassName = "${table.javaClassName}Record";
                c.wrap("public ${javaClassName} get${javaClassName}()") {
                    c.line("return ${fieldName};")
                }
            }

            c.wrap("public android.database.Cursor queryRaw(String query, String ... args)") {
                c.line("return mDB.rawQuery(query, args);");
            }
        }

        DroidRecordPlugin.write(path, pkg, "LocalSession.java", c.toString(), true);
    }
}
