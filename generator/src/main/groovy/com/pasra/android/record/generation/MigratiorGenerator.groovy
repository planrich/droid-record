package com.pasra.android.record.generation

import com.pasra.android.record.AndroidRecordPlugin
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

    void addTable(Table table, File file, long version) {
        codegen.wrap("if (currentVersion < targetVersion)") {
            codegen.line("db.execSQL(\"${table.creationSQL()}\");")
        }
    }

    void rmTable(String name, File file, long version) {
        codegen.wrap("if (currentVersion < targetVersion)") {
            codegen.line("db.execSQL(\"drop table ${name};\");")
        }
    }

    void writeToFile() {

        def config_table = "android_record_config";
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
                c.line("""db.execSQL("insert or replace into ${config_table} (key,value) values ('${generator_version_key}','${AndroidRecordPlugin.VERSION}')");""")
                c.write(codegen.toString(), true, false);
                c.line("""db.execSQL("insert or replace into ${config_table} (key,value) values (?,?)", new Object[] { "${version_key}", new Long(targetVersion) });""")
            }

        }

        w.write(c.toString())
        w.close();
    }

}
