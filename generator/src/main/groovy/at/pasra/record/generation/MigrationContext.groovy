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
import com.google.gson.JsonPrimitive
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */

/*!
 * @migrations Migrations
 * -#after getting_started
 * %p
 *   To handle changes in the database schema AR supports several commands in
 *   a migration change array.
 *   The items in the change array are evaluated on after another. That means
 *   that modifications take place in order you define it.
 * %p
 *   AR provides migrations to create, drop, rename tables and add, remove
 *   and rename columns.
 *
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

    private static String dehash(String name) {
        if (name.startsWith("#")) {
            return name.substring(1)
        }
        return name;
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


                    /*!
                     * @migrations|create_drop_table Creation and deletion of a table
                     *
                     * %span.filename
                     *   12345678_table.json
                     * %pre
                     *   %code{ data: { language: 'dsl' } }
                     *     :preserve
                     *       change: [
                     *         { cmd: 'create_table'
                     *           {?ref:name}: 'picture',
                     *           {?ref:fields}: {
                     *             {?ref:title}: {
                     *               type: 'string',
                     *               default: 'empty'
                     *             },
                     *             likes: 'integer',
                     *             data: 'blob',
                     *             datetime: 'date'
                     *           }
                     *         },
                     *         { cmd: 'drop_table', table: 'picture' }
                     *       ]
                     *
                     * %p
                     *   In the example above a 'pictures' table will be created when the migration is run on android.
                     *   The plural {?ref:name} will be used to address this table.
                     *   If this behaviour is not desired read this
                     *   %a{ href: '#legacy_database' } section.
                     *
                     * %p
                     *   Each key value pair of {?ref:fields}
                     *   is a column of the table with a specified type. In simple cases you can just specify
                     *   one of the following types.
                     *
                     *   %ul
                     *     %li string - java.lang.String
                     *     %li blob - byte[]
                     *     %li integer - java.lang.Integer
                     *     %li long - java.lang.Long
                     *     %li date - java.util.Date
                     *     %li boolean - java.lang.Boolean
                     *
                     *   %p
                     *     {?ref:title} has a more complex type and it specifies the default value of this column to be "empty".
                     *     Note that this value is then set in the constructor of a record object.
                     *   %p
                     *     The generated Picture.java file will have getters and setters of each column.
                     *
                     * After the drop command has been run all data will be lost. Forever!
                     */
                    if (cmd_name == "create_table") {
                        Table table = new Table(change);
                        table.checkIntegrity();
                        if (tables[table.name] == null) { // name is the internal name
                            tables[table.name] = table
                        } else {
                            throw new IllegalStateException("Duplicate table '${table.name}'.")
                        }

                        migGen.addTable(table, file, version);
                    }

                    if (cmd_name == "drop_table" && change.has("table")) {
                        def name = change.get("table").getAsString();
                        def table = tables[Inflector.internalName(name)];

                        if (table == null) {
                            throw new IllegalStateException("Table ${name} is missing but should be present.");
                        }

                        tables[Inflector.internalName(name)] = null;

                        migGen.rmTable(Inflector.sqlTableName(name), file, version);
                    }

                    /*!
                     * @migrations|manage_columns Manage a tables columns
                     * -#after migrations|rename_table
                     *
                     * %p
                     *   No record is limited to the fields you specify while creating the table. Adding,
                     *   removing and renaming of fields is just that easy:
                     *
                     * %span.filename
                     *   12345678_manage_columns.json
                     * %pre
                     *   %code{ data: { language: 'dsl' } }
                     *     :preserve
                     *       change: [
                     *         { cmd: 'add_column', table: 'picture', column: 'description'{?ref:1}, type: 'string' },
                     *         { cmd: 'remove_column', table: 'picture', column: 'datetime'{?ref:2} },
                     *         { cmd: 'rename_column', table: 'picture', column: 'title'{?ref:3}, to: 'name' }
                     *       ]
                     *
                     * %p
                     *   Column {?ref:1} will be added as a new column to the table.
                     *   You can specify the same properties here which you can specify when creating a table.
                     *
                     * %p
                     *   {?ref:2} will be removed. Note that after this migration has been run the data is lost forever!
                     *
                     * %p
                     *   {?ref:3} gets a new name. No data will be lost! Note that data conversion cannot be made here!
                     */
                    if (cmd_name == "add_column") {
                        if (!change.has("column") || !change.has("table") || !change.has("type")) {
                            throw new IllegalStateException("the add_column migration has malformed properties! " +
                                    "Please specify at least the following: add_column: { table: 'name_plural', column: 'column_name_singular', type: 'string,integer,...' }")
                        }

                        def table_name = Inflector.internalName(change.get("table").asString)
                        def type = change.get("type")

                        Table table = tables[table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${table_name}! If the Inflector messed up the singular step on the table name, use '#table_name_singluar' in the table property!")
                        }

                        def field = table.addField(new Field(change.get("column").asString, type))

                        migGen.addField(table, field, file, version)
                    }

                    if (cmd_name == "rename_column") {
                        if (!change.has("column") || !change.has("table") || !change.has("to")) {
                            throw new IllegalStateException("the rename_column migration has malformed properties! " +
                                    "Please specify at least the following: rename_column: " +
                                    "{ table: 'name_singular', column: 'column_name_singular', to: 'new_name_singular' }")
                        }

                        def old_field_name = change.get("column").asString
                        def table_name = Inflector.internalName(change.get("table").asString)
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

                        def table_name = Inflector.internalName(change.get("table").asString)
                        def field_name = change.get("column").asString

                        Table table = tables[table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${table_name}!")
                        }

                        Field field = table.removeField(field_name)
                        migGen.removeField(table, field, file, version)
                    }

                    /*!
                     * @migrations|rename_table Change the name of a table
                     * -#after migrations|create_drop_table
                     *
                     * %p
                     *   Sometimes it can happen that a record receives the wrong name.
                     *   This mistake is easy to correct:
                     *
                     * %span.filename
                     *   12345678_rename_table.json
                     * %pre
                     *   %code{ data: { language: 'dsl' } }
                     *     :preserve
                     *       change: [
                     *         { cmd: 'rename_table', table: 'picture', to: 'jpg_picture' }
                     *       [
                     *
                     * %p
                     *   Before running
                     *   %code $ gradle migrate
                     *   you should consider renaming the Picture class to JpgPicture in your IDE.
                     *   By doing so you won't have to correct the code afterwards manually.
                     */
                    if (cmd_name == "rename_table") {
                        if (!change.has("table") || !change.has("to")) {
                            throw new IllegalStateException("the rename_table migration has malformed properties! " +
                                    "Please specify at least the following: rename_table: " +
                                    "{ cmd: 'rename_table', table: 'name_singular', to: 'table_name_singular' }")
                        }

                        def old_table_name = Inflector.internalName(change.get("table").asString)
                        def new_table_name = change.get("to").asString

                        Table table = tables[old_table_name]
                        if (table == null) {
                            throw new IllegalArgumentException("Couldn't find table ${old_table_name}!")
                        }
                        tables.remove(old_table_name)

                        table.changeName(new_table_name)
                        migGen.renameTable(table, Inflector.sqlTableName(change.get("table").asString), Inflector.sqlTableName(new_table_name), file, version)
                        tables[Inflector.internalName(new_table_name)] = table
                    }

                    /*!
                    * @migrations|custom Custom migrations
                    * -#after migrations|manage_columns
                    *
                    * %p
                    *   When all the above would fail when migrating the database one can implement
                    *   the following interface:
                    * %span.filename DataMigrator.java
                    * %pre
                    *   %code{ data: { language: 'java' } }
                    *     :preserve
                    *       public interface DataMigrator {
                    *           //This custom migrator can be used when the normal migrations cannot handle the conversion.
                    *           //Do _NOT_ create, drop or alter tables. Android Record cannot track these changes and this
                    *           //will lead to undefined behaviour.
                    *
                    *           //It is not advisable to use any generated class here. As this class might disappear in
                    *           //the app development process.
                    *           void migrate(SQLiteDatabase db, long currentVersion, long targetVersion);
                    *       }
                    *
                    * %p
                    *   Then add a migration and specifiy your DataMigrator subclass:
                    *
                    * %span.filename 123456_custom_migration.json
                    * %pre
                    *   %code{ data: { language: 'dsl' } }
                    *     :preserve
                    *       change: [
                    *         { cmd: 'migrate_data', class_name: 'org.your.company.DataMigratorImpl' }
                    *       ]
                    */
                    if (cmd_name == "migrate_data") {
                        String javaClassName = change.get("class_name").asString
                        migGen.dataMigrator(javaClassName, file, version);
                    }
                }
            }
        }
    }

    void relations(JsonObject rels) {

        rels.entrySet().each { e ->
            JsonElement element = e.value
            if (element.isJsonObject()) {
                JsonObject relation = element.getAsJsonObject()
                String origin_name = e.key
                Table origin = tables[Inflector.internalName(origin_name)]
                if (origin == null) {
                    throw InvalidUserDataException("Relation expects table '${Inflector.sqlTableName(origin_name)}' to exist. But it does not!")
                }

                hasManyRelation(relation, origin)

                belongsToRelation(relation, origin)

                hasOneRelation(relation, origin)


            }
        }

    }

    def hasOneRelation(JsonObject relation, Table origin) {

        if (relation.has("has_one")) {
            def has_one = relation.get("has_one")

            def relations = buildUniformRelations(has_one, "one")

            relations.each { JsonObject obj ->
                def target_name = Inflector.internalName(Inflector.singularize(obj.get("one").asString))
                def target = getTable(target_name, "Has one relation expects table '${target_name}' to exist. But it does not!")

                Relation r = new HasOne(origin, target, obj)
                r.checkIntegrity();
                origin.relations << r
            }
        }
    }

    def belongsToRelation(JsonObject relation, Table origin) {

        if (relation.has("belongs_to")) {
            def belongs_to = relation.get("belongs_to")

            def relations = buildUniformRelations(belongs_to, "to")

            relations.each { JsonObject obj ->
                def target_name = Inflector.internalName(Inflector.singularize(obj.get("to").asString))
                def target = getTable(target_name, "Belongs to relation expects table '${target_name}' to exist. But it does not!")
                Relation r = new BelongsTo(origin, target, obj)
                r.checkIntegrity();
                origin.relations << r
            }
        }

    }

    Table getTable(String name, String failMsg) {
        def target = tables[name]

        if (target == null) {
            throw InvalidUserDataException(failMsg);
        }

        return target
    }

    /*!
     * relations|has_many
     *
     */
    def hasManyRelation(JsonObject relation, Table origin) {

        if (relation.has("has_many")) {
            def has_many = relation.get("has_many")

            def relations = buildUniformRelations(has_many, "many")

            relations.each { JsonObject obj ->
                def target_name = Inflector.internalName(Inflector.singularize(obj.get("many").asString))
                def target = getTable(target_name, "Has many relation expects table '${target_name}' to exist. But it does not! " +
                        "If it does and the singularize function messed it up, use a " +
                        "hash in front of the table name. e.g. 'has_many': '#singular_table_name'")

                Relation r = new HasMany(origin, target, obj)
                r.checkIntegrity();
                origin.relations << r
            }
        }

    }

    /*!
     * relations|syntax
     * %p
     *   Every
     */
    def buildUniformRelations(JsonElement relation, String name_prop) {

        def relations = []
        if (relation.isJsonArray()) {
            JsonArray array = relation.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    relations << array.get(i).getAsJsonObject()
                } else {
                    String name = array.get(i).asString;
                    JsonObject obj = new JsonObject();
                    obj.add(name_prop, new JsonPrimitive(name))
                    relations << obj;
                }
            }
        } else if (relation.isJsonPrimitive()) {
            def name = relation.asString
            JsonObject obj = new JsonObject();
            obj.add(name_prop, new JsonPrimitive(name))
            relations << obj;
        }

        return relations
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
