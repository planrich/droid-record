package com.pasra.android.record.generation

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.database.Table
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
        File target = AndroidRecordPlugin.file(path, pkg, "RecordMigrator.java", false);
        this.migGen = new MigratiorGenerator(target.path, pkg);
    }

    /**
     * Add an additional migration step to the context.
     * @param obj
     * @param file
     * @param version
     */
    void addMigrationStep(JsonObject obj, File file, long version) {

        migGen.migration = version;

        JsonObject change = obj.getAsJsonObject("change");
        if (change) {

            JsonObject add_table = change.getAsJsonObject("create_table");
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

            JsonObject rm_table = change.getAsJsonObject("drop_table");
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

    void relations(JsonObject rels) {

        // TODO consider: foreign key must match type of target table or cast it
        rels.entrySet().each { e ->
            JsonElement element = e.value
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject()
                String table_name = e.key
                Table table = tables[table_name]
                table.new_relation(obj)
            }
        }

    }

    /**
     * Generate the source code
     */
    void generate() {

        logger.info("generating source to path '${path}' into package '${pkg}")

        new SessionGenerator(tables).generate(path, pkg)

        tables.each { String name, Table table ->
            new JavaObjectGenerator(table).generate(path, pkg)
            new RecordGenerator(table).generateSQLite(path, pkg)
        }

        logger.info("generating migrator")

        migGen.writeToFile();
    }
}
