package at.pasra.record.loading

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.generation.CodeGenerator
import at.pasra.record.generation.JavaObjectGenerator
import at.pasra.record.generation.MarshallGenerator
import at.pasra.record.generation.MigratiorGenerator
import at.pasra.record.generation.RecordBuilderGenerator
import at.pasra.record.generation.RecordGenerator
import at.pasra.record.generation.SessionGenerator
import at.pasra.record.util.Inflector
import at.pasra.record.database.Field
import at.pasra.record.database.Table
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */

/*!
 * @migrations Record migrations
 * %p
 *   To handle changes in the database schema {?class:arname;DR} supports several commands in
 *   a migration change array.
 *   The items are evaluated one after another. That means
 *   that modifications take place in the order you define it.
 * %p
 *   {?class:arname;DR} provides migrations to create, drop, rename tables and add, remove
 *   and rename columns and a custom migration which gives you full control over the database.
 *
 */
class MigrationContext {

    def logger = Logging.getLogger("droid_record")
    GroovyShell shell = new GroovyShell()
    def path;
    def pkg;
    def domainPkg;
    MigratiorGenerator migGen;

    def tables = [:];

    MigrationContext(String path, String pkg, String domainPkg) {
        this.path = path
        this.pkg = pkg
        this.domainPkg = domainPkg
        this.migGen = new MigratiorGenerator(path, pkg);
    }

    Table getTable(String name, String failMsg) {
        def target = tables[name]

        if (target == null) {
            throw InvalidUserDataException(failMsg);
        }

        return target
    }



    /**
     * Add an additional migration oneMigrationStep to the context.
     * @param obj
     * @param file
     * @param version
     */
    void addMigrationStep(File file, long version) {

        logger.info("reading from ${file.path}")
        migGen.migration = version;
        def delegate = new MigrationInvokeableContext(version, file.path)

        def closure = new GroovyShell().evaluate("{->${file.text}}")
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_ONLY

        migGen.oneMigrationStep(version) {
            closure()
        }
    }

    void relations(File file) {
        logger.info("loading relations from ${file.path}")
        def delegate = new RelationsRootContext(this)
        def cl = new GroovyShell().evaluate("{->${file.text}}")
        cl.delegate = delegate
        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl()
    }

    /**
     * Generate the source code
     */
    void generate() {

        logger.info("generating source to path '${path}' into package '${pkg}")

        migGen.writeToFile();

        SessionGenerator session = new SessionGenerator(tables);
        session.generate(path, pkg, domainPkg)

        tables.each { String name, Table table ->
            def objGen = new JavaObjectGenerator(table)
            objGen.generate(path, pkg, domainPkg)

            def marshallGen = new MarshallGenerator(table)
            marshallGen.generate(path, pkg, domainPkg)

            def recordGen = new RecordGenerator(table)
            recordGen.generateSQLite(path, pkg, domainPkg)

            def recordBuilder = new RecordBuilderGenerator(table)
            recordBuilder.generate(path, pkg, domainPkg)
        }

    }

    void generateCurrentSchema(String path, String fileName) {
        def sortedTables = tables.values().sort{ Table a, Table b ->
            return a.name <=> b.name
        }
        def c = new CodeGenerator(2, "dr")
        c.line("/* AUTO GENERATED. DO NOT MODIFY")
        c.line(" * This file has the sole purpose of displaying the current database layout.")
        c.line(" */")

        sortedTables.each { Table table ->
            c.wrap("create_table ${table.name}") {
                table.fields.each { String name, Field field ->
                    c.line("${field.name} { type '${field.type}', default '${field.defaultValue()}'}")
                }
            }
        }

        DroidRecordPlugin.write(path, fileName, c.toString(), true);
    }

    class MigrationInvokeableContext {

        long version = -1;
        String path

        MigrationInvokeableContext(long v, String p) {
            this.version = v;
            this.path = p
        }

        /*!
         * @migrations|create_drop_table Creation and deletion
         *
         * %span.filename
         *   12345678_table.json
         * %pre
         *   %code{ data: { language: 'dsl' } }
         *     :preserve
         *       create_table {
         *         {?ref:name} 'picture'
         *         {?ref:fields} {
         *           {?ref:title} {
         *             type 'string'
         *             init 'empty'
         *           }
         *           likes 'integer'
         *           data 'blob'
         *           datetime 'date'
         *         }
         *       }
         *       drop_table {
         *         table 'picture'
         *       }
         *
         * %p
         *   In the example above a 'pictures' sql table will be created when the migration is run on Android.
         *   The singular {?ref:name} will be used to address this relation.
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
         *     %li double - java.lang.Double
         *
         *   %p
         *     {?ref:title} has a more complex type and it specifies the initial (init) value of this column to be "empty".
         *     Note that this value is then set in the constructor of a record object.
         *   %p
         *     The generated Picture.java file will have getters and setters of each column.
         *
         * After the drop command has been run all data will be lost. Forever!
         */
        def create_table(Closure c) {
            def structCtx = [
                'name': LoadUtil.&string,
                'fields': [
                    '*': [
                      'type': LoadUtil.&type,
                      'init': LoadUtil.&primitive,
                      '__required__': ['type']
                    ],
                    '__*__': { name -> def f = new Field(); f.name = name; f },
                    '__*__key_property__': 'name',
                    '__*__default_value_property__': 'type',
                ],
                '__required__': [ 'name', 'fields' ]
            ]
            Table table = LoadUtil.invoke(c, structCtx, new Table())
            table.checkIntegrity();
            if (tables[table.name] == null) { // name is the internal name
                tables[table.name] = table
            } else {
                throw new IllegalStateException("Duplicate table '${table.name}'.")
            }

            migGen.addTable(table, version);
        }

        def drop_table(Closure c) {
            def structCtx = [
                'name': LoadUtil.&string,
                '__required__': [ 'name' ],
            ]
            def map = LoadUtil.invoke(c, structCtx)

            def name = Inflector.internalName(map.name)
            getTable(name, "Table ${map.name} is missing but should be present.")
            tables.remove(name)

            migGen.rmTable(Inflector.sqlTableName(map.name), version);
        }

        /*!
         * @migrations|manage_columns Managing columns
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
         *       add_column {
         *         table 'picture'
         *         column 'description'{?ref:1}
         *         type 'string'
         *       }
         *       remove_column {
         *         table 'picture'
         *         column 'datetime'{?ref:2}
         *       }
         *       rename_column {
         *         table 'picture'
         *         column 'title'{?ref:3}
         *         to 'name'
         *       }
         *
         * %p
         *   Column {?ref:1} will be added as a new column to the table.
         *   You can specify the same properties here which you can specify when creating a table.
         *
         * %p
         *   {?ref:2} will be removed. Note that after this migration has been run the data is lost forever!
         *
         * %p
         *   {?ref:3} gets a new name. Note that data conversion cannot be made here!
         */
        def add_column(Closure c) {
            def structCtx = [
                'column': LoadUtil.&string,
                'table': LoadUtil.&string,
                'type': LoadUtil.&type,
                '__required__': ['column','table','type']

            ]
            def map = LoadUtil.invoke(c, structCtx)

            def table_name = Inflector.internalName(map.table)
            def type = map.type

            Table table = tables[table_name]
            if (table == null) {
                throw new IllegalArgumentException("Couldn't find table ${map.table}! If the Inflector messed up the singular step on the table name, use '#table_name_singluar' in the table property!")
            }

            def field = new Field(map.column)
            field.type = map.type
            table.addField(field)

            migGen.addField(table, field, version)
        }

        def rename_column(Closure c) {
            def structCtx = [
                'column': LoadUtil.&string,
                'table': LoadUtil.&string,
                'to': LoadUtil.&string,
                '__required__': ['column','table','to']
            ]
            def map = LoadUtil.invoke(c, structCtx)

            def old_field_name = map.column
            def table_name = Inflector.internalName(map.table)
            def new_field_name = map.to
            Table table = getTable(table_name, "Couldn't find table ${table_name}!")
            table.renameField(old_field_name, new_field_name)
            migGen.renameField(table, old_field_name, new_field_name, version)
        }

        def remove_column(Closure c) {
            def structCtx = [
                'column': LoadUtil.&string,
                'table': LoadUtil.&string,
                '__required__': ['column', 'table']
            ]
            def map = LoadUtil.invoke(c, structCtx)

            def table_name = Inflector.internalName(map.table)
            def field_name = map.column

            Table table = getTable(table_name, "Couldn't find table ${table_name}!")

            Field field = table.removeField(field_name)
            migGen.removeField(table, field, version)
        }

        /*!
         * @migrations|rename_table Changing the name
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
         *       rename_table {
         *         table 'picture'
         *         to 'jpg_picture'
         *       }
         *
         * %p
         *   Before running
         *   %code $ gradle migrate
         *   you should consider renaming the Picture class to JpgPicture in your IDE.
         *   By doing so you won't have to correct the code afterwards manually.
         */
        def rename_table(Closure c) {
            def structCtx = [
                'table': LoadUtil.&string,
                'to': LoadUtil.&string,
                '__required__': ['table', 'to']
            ]
            def map = LoadUtil.invoke(c, structCtx)

            def old_table_name = Inflector.internalName(map.table)
            def new_table_name = Inflector.internalName(map.to)

            Table table = tables[old_table_name]
            if (table == null) {
                throw new IllegalArgumentException("Couldn't find table ${old_table_name}!")
            }
            tables.remove(old_table_name)

            table.changeName(new_table_name)
            migGen.renameTable(table, Inflector.sqlTableName(map.table), Inflector.sqlTableName(map.to), version)
            tables[new_table_name] = table
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
        *           //This custom migrator can be used when the normal migrations cannot handle
         *          //the conversion. Do _NOT_ create, drop or alter tables. Droid Record
         *          //cannot track these changes and this will lead to undefined behaviour.
        *
        *           //It is not advisable to use any generated class here. As this class
        *           //might disappear in the app development process.
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
        *         migrate_data { class_name 'org.your.company.DataMigratorImpl' }
        */
        def migrate_data(Closure c) {
            def structCtx = [
                'class_name': LoadUtil.&string,
                '__required__': ['class_name']
            ]
            def map = LoadUtil.invoke(c, structCtx)
            String javaClassName = map.class_name
            migGen.dataMigrator(javaClassName, version);
        }
    }

}
