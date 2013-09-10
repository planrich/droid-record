package com.pasra.android.record.gen

import com.google.gson.JsonObject
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */
class MigrationContext {

    def logger = Logging.getLogger("android_record")
    def path;
    def pkg;
    def migGen;

    def tables = [:];

    MigrationContext(String path, String pkg) {
        this.path = path
        this.pkg = pkg
        File target = Util.file(path, pkg, "RecordMigrator.java", false);
        this.migGen = new MigratiorGenerator(target.path, pkg);
        this.migGen.oldMigration = 0;

        if (target.exists()) {
            String content = target.text.toString();

            def versionPat = ~/mLastMigration = (\d+);/
            def matcher = content =~ versionPat;
            if (matcher.find()) {
                try {
                    long value = Long.parseLong(matcher.group(1));
                    logger.info("Migration level at ${value}")
                    migGen.oldMigration = value
                } catch (NumberFormatException e) {}
            }
        }
    }

    /**
     * Add an additional migration step to the context.
     * @param obj
     * @param file
     * @param version
     */
    void addMigrationStep(JsonObject obj, File file, long version) {

        migGen.step(version);

        JsonObject change = obj.getAsJsonObject("change");
        if (change) {

            JsonObject add_table = change.getAsJsonObject("add_table");
            if (add_table) {
                Table table = new Table(add_table);
                table.checkIntegrity();
                tables[table.name]
                if (tables[table.name] == null) {
                    tables.put(table.name, table)
                } else {
                    throw new IllegalStateException("Duplicate table '${table.name}'.")
                }

                migGen.addTable(table, file, version);
            }

            JsonObject rm_table = change.getAsJsonObject("rm_rable");
            if (rm_table && rm_table.has("name")) {
                def name = rm_table.get("name").getAsString();
                def table = tables[name];

                if (table == null) {
                    throw new IllegalStateException("Table ${name} is missing but should be present.");
                }

                tables[name] = null;

                migGen.rmTable(name, file, version);
            }
        }
    }

    /**
     * Generate the source code
     */
    void generate() {

        logger.info("generating source to path '${path}' into package '${pkg}")

        tables.each { String name, Table table ->
            table.generateDaoJavaSource(path, pkg);
        }

        logger.info("generating migrator")

        migGen.writeToFile();
    }
}
