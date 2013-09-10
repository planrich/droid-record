package com.pasra.android.record.gen

/**
 * Created by rich on 9/10/13.
 */
class MigratiorGenerator {
    String path;
    String pkg;
    CodeGenerator codegen = new CodeGenerator();

    long migration;
    long oldMigration;

    MigratiorGenerator(String path, String pkg) {
        this.path = path
        this.pkg = pkg;

        codegen.indent(3)
    }

    void step(long version) {
        if (version > migration) {
            migration = version;
        }
    }

    void addTable(Table table, File file, long version) {
        //codegen.line("db.execute...")
    }

    void rmTable(String name, File file, long version) {

    }

    void writeToFile() {

        File target = new File(path);
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(target));

        CodeGenerator c = new CodeGenerator();

        c.line("package ${pkg};")
        c.line()
        c.line("import android.database.sqlite.SQLiteDatabase;")
        c.line()

        c.wrap("public class RecordMigrator") {
            c.line("private long mLastMigration = ${migration};");
            c.line()

            c.wrap("public void migrate(SQLiteDatabase db, long version)") {
                c.wrap("if (mLastMigration <= version)") {
                    c.write(codegen.toString(), true, false);
                }
            }
        }

        w.write(c.toString())
        w.close();
    }

}
