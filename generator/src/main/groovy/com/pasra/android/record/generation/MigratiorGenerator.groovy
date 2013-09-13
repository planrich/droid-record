package com.pasra.android.record.generation

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
        codegen.wrap("if (currentVersion <= targetVersion)") {
            codegen.line("db.execSQL(\"${table.creationSQL()}\");")
        }
    }

    void rmTable(String name, File file, long version) {
        codegen.wrap("if (currentVersion <= targetVersion)") {
            codegen.line("db.execSQL(\"drop table ${name};\");")
        }
    }

    void writeToFile() {

        File target = new File(path);
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(target));

        CodeGenerator c = new CodeGenerator();

        c.line("package ${pkg};")
        c.line()
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line("import com.pasra.android.record.Migrator;")
        c.line()

        c.wrap("public class RecordMigrator implements Migrator") {
            c.line("public static final long MIGRATION_LEVEL = ${migration}L;");
            c.line()

            c.wrap("public long getLatestMigrationLevel()") {
                c.write("return MIGRATION_LEVEL;")
            }

            c.wrap("public void migrate(SQLiteDatabase db, long currentVersion, long targetVersion)") {
                c.write(codegen.toString(), true, false);
            }

        }

        w.write(c.toString())
        w.close();
    }

}
