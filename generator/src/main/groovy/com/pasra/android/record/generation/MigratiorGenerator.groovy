package com.pasra.android.record.generation

import com.google.gson.JsonElement
import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.Field
import com.pasra.android.record.database.Table

/**
 * Created by rich on 9/10/13.
 */
class MigratiorGenerator {
    String path;
    String pkg;
    CodeGenerator codegen = new CodeGenerator();

    long migration;

    MigratiorGenerator(String path, String pkg) {
        this.path = path
        this.pkg = pkg;

        codegen.indent(2)
    }

    void oneMigrationStep(long version, Closure closure) {
        codegen.wrap("if (currentVersion < targetVersion && currentVersion < ${version}L)") {
            closure.run();
            codegen.line("currentVersion = ${version}L;")
        }
    }

    void addTable(Table table, File file, long version) {
        codegen.line("db.execSQL(\"${table.creationSQL()}\");")
    }

    void rmTable(String name, File file, long version) {
        codegen.line("db.execSQL(\"drop table ${Inflector.pluralize(Inflector.singularize(name))};\");")
    }

    void addField(Table table, Field field, File file, long version) {
        codegen.line("db.execSQL(\"alter table ${table.sqlTableName} add column ${field.columnSQL()};\");")
    }

    void moveContentsTo(Table table, String old_table_name, String new_table_name, mapping = [:]) {
        // wrap in brackets to not pollute the namespace
        codegen.wrap("") {
            codegen.line("Cursor c = " +
                    "db.rawQuery(" +
                    "\"select * from ${old_table_name}\", null);")

            codegen.line("db.execSQL(\"begin\");")
            codegen.wrap("while (c.moveToNext())") {
                def i = 0
                table.getOrderedFields(true).each { Field f ->
                    def name = f.name;
                    if (mapping[f.name] != null) {
                        name = mapping[f.name];
                    }
                    codegen.line("String s${i++} = c.getString(c.getColumnIndex(\"${name}\"));")
                }

                codegen.line("db.rawQuery(\"insert into ${new_table_name} (" +
                        (table.getOrderedFields(true).collect({ f -> f.name }).join(", ")) +
                        ") values (" +
                        (["?"] * i).join(", ") +
                        ");\", new String[] {" +
                        ((0..(i - 1)).collect({ x -> "s${x}" })).join(", ") +
                        "});")
            }
            codegen.line("db.execSQL(\"commit\");")
        }
    }

    void dataMigrator(String name, File file, long version) {
        codegen.line("new ${name}().migrate(db, currentVersion, targetVersion);");
    }

    void removeField(Table table, Field removed, File file, long version) {
        // move contents to new temporary table
        def suffix = "_mig_temp_table"
        codegen.line("db.execSQL(\"${table.creationSQL(suffix)}\");")
        moveContentsTo(table, table.sqlTableName, table.sqlTableName + suffix)
        codegen.line("db.execSQL(\"drop table ${table.sqlTableName}\");")

        // create the table again with the new schema. insert the data back
        codegen.line("db.execSQL(\"${table.creationSQL()}\");")
        moveContentsTo(table, table.sqlTableName + suffix, table.sqlTableName, [:])
        codegen.line("db.execSQL(\"drop table ${table.sqlTableName + suffix}\");")
    }

    void renameTable(Table table, String old_name, String new_name, File file, long version) {
        // table already has new name
        codegen.line("db.execSQL(\"${table.creationSQL()}\");")
        moveContentsTo(table, old_name, new_name)
        codegen.line("db.execSQL(\"drop table ${old_name}\");")
    }

    void renameField(Table table, String old_name, String new_name, File file, long version) {
        // move contents to new temporary table
        def suffix = "_mig_temp_table"
        codegen.line("db.execSQL(\"${table.creationSQL(suffix)}\");")
        moveContentsTo(table, table.sqlTableName, table.sqlTableName + suffix, [new_name: old_name])
        codegen.line("db.execSQL(\"drop table ${table.sqlTableName}\");")

        // create the table again with the new schema. insert the data back
        codegen.line("db.execSQL(\"${table.creationSQL()}\");")
        moveContentsTo(table, table.sqlTableName + suffix, table.sqlTableName, [:])
        codegen.line("db.execSQL(\"drop table ${table.sqlTableName + suffix}\");")
    }

    void writeToFile() {

        def config_table = "android_record_configs";
        def generator_version_key = "generator_version";
        def version_key = "version";

        File target = new File(path);
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(target));

        CodeGenerator c = new CodeGenerator();

        c.line("package ${pkg};")
        c.line()
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import android.database.Cursor;")
        c.line("import com.pasra.android.record.Migrator;")
        c.line()

        c.wrap("public class RecordMigrator implements Migrator") {
            c.line("public static final long MIGRATION_LEVEL = ${migration}L;");
            c.line()
            c.line("private final SQLiteDatabase db;")

            c.wrap("public RecordMigrator(SQLiteDatabase db)") {
                c.line("this.db = db;")
                c.line("this.tryCreateVersioningTable();")
            }

            c.wrap("private void tryCreateVersioningTable()") {
                c.line("db.execSQL(\"create table if not exists ${config_table} (_id integer primary key, key text unique not null, value text);\");")
            }
            c.wrap("public long getCurrentMigrationLevel()") {
                // if it ever happens that more versions are added to this database
                // the latest is taken by ordering descending
                c.line("""Cursor c = db.rawQuery(
                    \"select key, value from ${config_table} where key = ? order by value desc limit 1;\",
                    new String[] { "${version_key}" } );""")

                c.wrap("if (c.moveToNext())") {
                    c.line("long version = Long.parseLong(c.getString(1));")
                    c.line("c.close();")
                    c.line("return version;")
                }
                c.line("return 0;")
            }

            c.wrap("public long getLatestMigrationLevel()") {
                c.line("return MIGRATION_LEVEL;")
            }

            c.line("@Override")
            c.wrap("public void migrate()") {
                c.line("migrate(getCurrentMigrationLevel(), MIGRATION_LEVEL);");
            }

            c.line("@Override")
            c.wrap("public void migrate(long currentVersion, long targetVersion)") {
                c.line("""db.execSQL("insert or replace into ${config_table} (key,value) values ('${
                    generator_version_key
                }','${AndroidRecordPlugin.VERSION}')");""")
                c.write(codegen.toString(), true, false);
                c.line("""db.execSQL("insert or replace into ${
                    config_table
                } (key,value) values (?,?)", new Object[] { "${
                    version_key
                }", new Long(currentVersion) });""")
            }

        }

        w.write(c.toString())
        w.close();
    }

}
