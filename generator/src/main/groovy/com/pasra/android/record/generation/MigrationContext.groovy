package com.pasra.android.record.generation

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.Inflector
import com.pasra.android.record.database.BelongsTo
import com.pasra.android.record.database.HasMany
import com.pasra.android.record.database.Relation
import com.pasra.android.record.database.Table
import org.gradle.api.InvalidUserDataException
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
                JsonObject relation = element.getAsJsonObject()
                String origin_name = e.key
                Table origin = tables[origin_name]
                if (origin == null) {
                    throw InvalidUserDataException("Relation expects table '${origin_name}' to exist. But it does not!")
                }

                has_man_relations(relation, origin_name, origin)

                belongs_to_relations(relation, origin_name, origin)

                /*
                if (relation.has("has_one")) {
                    def has_one = relation.get("has_one")
                    if (has_one.isJsonPrimitive()) {
                        relations << new HasOne(this, has_one.asString)
                    }
                }*/


            }
        }

    }

    def belongs_to_relations(JsonObject relation, String origin_name, Table origin) {

        if (relation.has("belongs_to")) {
            def belongs_to = relation.get("belongs_to")

            def relations = []
            if (belongs_to.isJsonArray()) {
                relations = belongs_to.getAsJsonArray();
            } else if (belongs_to.isJsonPrimitive()) {
                relations = [belongs_to.getAsString()];
            }

            relations.each { target_name ->
                def target = tables[target_name]
                if (target == null) {
                    throw InvalidUserDataException(
                            "Belongs to relation expects table '${origin_name}' to exist. But it does not!"
                    )
                }
                Relation r = new BelongsTo(origin, target)
                r.checkIntegrity();
                origin.relations << r
            }
        }

    }

    def has_man_relations(JsonObject relation, String origin_name, Table origin) {

        if (relation.has("has_many")) {
            def has_many = relation.get("has_many")

            def many = []
            if (has_many.isJsonArray()) {
                many = has_many.getAsJsonArray();
            } else if (has_many.isJsonPrimitive()) {
                many = [has_many.getAsString()];
            }

            many.each { plural_name ->
                def target_name = Inflector.singularize(plural_name)
                def target = tables[target_name]
                if (target == null) {
                    throw InvalidUserDataException(
                            "Has many relation expects table '${origin_name}' to exist. But it does not! " +
                                    "If it does and the singularize function messed it up, use a " +
                                    "hash in front of the table name. e.g. 'has_many': '#singular_table_name'"
                    )
                }
                Relation r = new HasMany(origin, target)
                r.checkIntegrity();
                origin.relations << r
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
