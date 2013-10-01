package at.pasra.record.generation

import at.pasra.record.database.HasOne
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import at.pasra.record.AndroidRecordPlugin
import at.pasra.record.Inflector
import at.pasra.record.database.BelongsTo
import at.pasra.record.database.Field
import at.pasra.record.database.HasMany
import at.pasra.record.database.Relation
import at.pasra.record.database.Table
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */
class MigrationContext {

    def logger = Logging.getLogger("android_record")
    def path;
    def pkg;
    MigratiorGenerator migGen;

    def tables = [:];

    MigrationContext(String path, String pkg) {
        this.path = path
        this.pkg = pkg
        File target = AndroidRecordPlugin.file(path, pkg, "RecordMigrator.java", false);
        this.migGen = new MigratiorGenerator(target.path, pkg);
    }

    /**
     * Add an additional migration oneMigrationStep to the context.
     * @param obj
     * @param file
     * @param version
     */
    void addMigrationStep(JsonObject obj, File file, long version) {

        migGen.migration = version;

        JsonArray changes = obj.getAsJsonArray("change");
        if (changes) {
            migGen.oneMigrationStep(version) {
                for (int i = 0; i < changes.size(); i++) {
                    JsonObject change = changes.get(i).asJsonObject;
                    String cmd_name = change.get("cmd").asString
                    if (cmd_name == "create_table") {
                        Table table = new Table(change);
                        table.checkIntegrity();
                        if (tables[table.name] == null) {
                            tables[table.name] = table
                        } else {
                            throw new IllegalStateException("Duplicate table '${table.name}'.")
                        }

                        migGen.addTable(table, file, version);
                    }

                    if (cmd_name == "drop_table" && change.has("name")) {
                        def name = change.get("name").getAsString();
                        def table = tables[name];

                        if (table == null) {
                            throw new IllegalStateException("Table ${name} is missing but should be present.");
                        }

                        tables[name] = null;

                        migGen.rmTable(name, file, version);
                    }

                    if (cmd_name == "add_column") {
                        if (!change.has("column") || !change.has("table") || !change.has("type")) {
                            throw new IllegalStateException("the add_column migration has malformed properties! " +
                                    "Please specify at least the following: add_column: { table: 'name_plural', column: 'column_name_singular', type: 'string,integer,...' }")
                        }

                        def field_name = change.get("column").asString
                        def table_name = Inflector.singularize(change.get("table").asString)
                        def type = change.get("type")

                        Table table = tables[table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${table_name}! If the Inflector messed up the singular step on the table name, use '#table_name_singluar' in the table property!")
                        }

                        def field = table.addField(new Field(field_name, type))

                        migGen.addField(table, field, file, version)
                    }

                    if (cmd_name == "rename_column") {
                        if (!change.has("column") || !change.has("table") || !change.has("to")) {
                            throw new IllegalStateException("the rename_column migration has malformed properties! " +
                                    "Please specify at least the following: rename_column: " +
                                    "{ table: 'name_singular', column: 'column_name_singular', to: 'new_name_singular' }")
                        }

                        def old_field_name = change.get("column").asString
                        def table_name = Inflector.tabelize(change.get("table").asString)
                        def new_field_name = change.get("to").asString

                        Table table = tables[table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${table_name}!")
                        }

                        table.renameField(old_field_name, new_field_name)
                        migGen.renameField(table, old_field_name, new_field_name, file, version)
                    }

                    if (cmd_name == "remove_column") {
                        if (!change.has("table") || !change.has("column")) {
                            throw new IllegalStateException("the remove_column migration has malformed properties! " +
                                    "Please specify at least the following: remove_column: " +
                                    "{ table: 'name_singular', column: 'column_name_singular' }")
                        }

                        def table_name = Inflector.tabelize(change.get("table").asString)
                        def field_name = change.get("column").asString

                        Table table = tables[table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${table_name}!")
                        }

                        Field field = table.removeField(field_name)
                        migGen.removeField(table, field, file, version)
                    }

                    if (cmd_name == "rename_table") {
                        if (!change.has("table") || !change.has("to")) {
                            throw new IllegalStateException("the rename_table migration has malformed properties! " +
                                    "Please specify at least the following: rename_table: " +
                                    "{ table: 'name_singular', to: 'table_name_singular' }")
                        }

                        def old_table_name = Inflector.tabelize(change.get("table").asString)
                        def new_table_name = Inflector.tabelize(change.get("to").asString)

                        Table table = tables[old_table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${old_table_name}!")
                        }
                        tables.remove(old_table_name)

                        table.changeName(new_table_name)
                        migGen.renameTable(table, Inflector.pluralize(old_table_name), Inflector.pluralize(new_table_name), file, version)
                        tables[new_table_name] = table
                    }

                    if (cmd_name == "migrate_data") {
                        String javaClassName = change.get("class_name").asString
                        migGen.dataMigrator(javaClassName, file, version);
                    }

                    if (cmd_name == "drop_table") {
                        def name = Inflector.singularize(change.get("name").asString)
                        def table = tables[name];

                        if (table == null) {
                            throw new IllegalStateException("Table ${name} is missing but should be present.");
                        }

                        tables[name] = null;

                        migGen.rmTable(name, file, version);
                    }
                }
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

                has_many_relations(relation, origin_name, origin)

                belongs_to_relations(relation, origin_name, origin)

                has_one_relations(relation, origin_name, origin)


            }
        }

    }

    def has_one_relations(JsonObject relation, String origin_name, Table origin) {

        if (relation.has("has_one")) {
            def belongs_to = relation.get("has_one")

            def relations = []
            if (belongs_to.isJsonArray()) {
                JsonArray array = belongs_to.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    relations << array.get(i).asString;
                }
            } else if (belongs_to.isJsonPrimitive()) {
                relations << belongs_to.asString
            }

            relations.each { target_name ->
                def target = tables[target_name]
                if (target == null) {
                    throw InvalidUserDataException(
                            "Has one relation expects table '${target_name}' to exist. But it does not!"
                    )
                }
                Relation r = new HasOne(origin, target)
                r.checkIntegrity();
                origin.relations << r
            }
        }


    }

    def belongs_to_relations(JsonObject relation, String origin_name, Table origin) {

        if (relation.has("belongs_to")) {
            def belongs_to = relation.get("belongs_to")

            def relations = []
            if (belongs_to.isJsonArray()) {
                JsonArray array = belongs_to.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    relations << array.get(i).asString;
                }
            } else if (belongs_to.isJsonPrimitive()) {
                relations << belongs_to.asString
            }

            relations.each { target_name ->
                def target = tables[target_name]
                if (target == null) {
                    throw InvalidUserDataException(
                            "Belongs to relation expects table '${target_name}' to exist. But it does not!"
                    )
                }
                Relation r = new BelongsTo(origin, target)
                r.checkIntegrity();
                origin.relations << r
            }
        }

    }

    def has_many_relations(JsonObject relation, String origin_name, Table origin) {

        if (relation.has("has_many")) {
            def has_many = relation.get("has_many")

            def many = []
            if (has_many.isJsonArray()) {
                JsonArray array = has_many.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    many << array.get(i).asString;
                }
            } else if (has_many.isJsonPrimitive()) {
                many = [has_many.getAsString()];
            }

            many.each { String plural_name ->
                def target_name = Inflector.singularize(plural_name)
                def target = tables[target_name]
                if (target == null) {
                    throw InvalidUserDataException(
                            "Has many relation expects table '${target_name}' to exist. But it does not! " +
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
            new RecordBuilderGenerator(table).generate(path, pkg)
        }

        logger.info("generating migrator")

        migGen.writeToFile();
    }
}
