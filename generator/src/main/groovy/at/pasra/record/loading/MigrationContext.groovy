package at.pasra.record.loading

import at.pasra.record.database.HasAndBelongsTo
import at.pasra.record.database.HasOne
import at.pasra.record.generation.CodeGenerator
import at.pasra.record.generation.JavaObjectGenerator
import at.pasra.record.generation.MigratiorGenerator
import at.pasra.record.generation.RecordBuilderGenerator
import at.pasra.record.generation.RecordGenerator
import at.pasra.record.generation.SessionGenerator
import at.pasra.record.util.Inflector
import at.pasra.record.database.BelongsTo
import at.pasra.record.database.Field
import at.pasra.record.database.Table
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logging

import javax.el.MethodNotFoundException

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
    MigratiorGenerator migGen;

    def tables = [:];

    MigrationContext(String path, String pkg) {
        this.path = path
        this.pkg = pkg
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

        migGen.migration = version;
        def delegate = new MigrationInvokeableContext(version, file.path)

        def closure = shell.evaluate("{->${file.text}}")
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_ONLY

        migGen.oneMigrationStep(version) {
            closure()
        }
    }

    void relations(File file) {

        def delegate = new RelationsInvokeableContext()

        def closure = shell.evaluate("{->${file.text}}")
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()

    }

    /**
     * Generate the source code
     */
    void generate() {

        logger.info("generating source to path '${path}' into package '${pkg}")

        migGen.writeToFile();

        SessionGenerator session = new SessionGenerator(tables);
        session.generate(path, pkg)

        tables.each { String name, Table table ->
            def objGen = new JavaObjectGenerator(table)
            objGen.generate(path, pkg)

            def recordGen = new RecordGenerator(table)
            recordGen.generateSQLite(path, pkg)

            def recordBuilder = new RecordBuilderGenerator(table)
            recordBuilder.generate(path, pkg)
        }

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
         *             default 'empty'
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
         *     {?ref:title} has a more complex type and it specifies the default value of this column to be "empty".
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
                        LoadUtil.&type,
                        [ 'type': LoadUtil.&type,
                          'default': LoadUtil.&string
                        ]
                    ],
                    '__*__': { -> return new Field() },
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
            tables[name] = null;

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
            def field = LoadUtil.invoke(c, structCtx, new Field())

            def table_name = Inflector.internalName(field.table)
            def type = field.type

            Table table = tables[table_name]
            if (table == null) {
                throw new IllegalArgumentException("Couldn't find table ${field.table}! If the Inflector messed up the singular step on the table name, use '#table_name_singluar' in the table property!")
            }

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
            migGen.renameField(table, old_field_name, new_field_name, file, version)
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
            migGen.removeField(table, field, file, version)
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
            migGen.renameTable(table, Inflector.sqlTableName(map.table), Inflector.sqlTableName(map.to), file, version)
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
            migGen.dataMigrator(javaClassName, file, version);
        }
    }

    /*!
     * @relations Relations
     * -#after migrations
     *
     * %p
     *   Relations are all specified in one file. By default this is
     *   %span.migration-ref relations.json
     *   \.
     *   Relationship between tables is just meta information. At migration time this meta
     *   information is used to type check the primary and foreign keys on the relation.
     *
     * %p
     *   In the following section the following migration is created before the relations
     *   are added.
     *
     * %span.filename 123456_pre_migration.json
     * %pre
     *   %code{ data: { language: 'dsl' } }
     *     :preserve
     *       create_table {
     *         name 'picture'
     *         fields {
     *           name 'string'
     *           data 'blob'
     *           gallery_id 'long'
     *         }
     *       }
     *       create_table {
     *         name 'gallery'
     *         fields {
     *           name 'string'
     *           user_id 'long'
     *         }
     *       }
     *       create_table {
     *         name 'user'
     *         fields {
     *           name 'string'
     *           age 'int'
     *         }
     *       }
     *       create_table {
     *         name 'user_picture'
     *         fields {
     *           user_id 'long'
     *           picture_id 'long'
     *         }
     *       }
     */
    class RelationsInvokeableContext {

        def methodMissing(String name, Object args) {
            Table origin = getTable(Inflector.internalName(name), "Relation expects table '${name}' to exist. But it does not!")
            if (args.size == 1 && args[0] instanceof Closure) {
                Closure c = args[0]
                def delegate = new RelationInvokeableContext(origin)
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_ONLY
                c()
            } else {
                throw new MethodNotFoundException("You must provide a closure to a relation!")
            }
        }

    }

    class RelationInvokeableContext {

        Table origin_table

        RelationInvokeableContext(Table o) {
            this.origin_table = o
        }

        /*!
         *@relations|has_one Has One (1..1)
         *
         * %p
         *   Given the following requirement: 'a user has one gallery' add this
         *   rule to your relationships:
         *
         * %span.filename relations.json
         * %pre
         *   %code{ data: { language: 'dsl' } }
         *     :preserve
         *       ...
         *       user {
         *         has_one 'gallery'
         *       }
         */
        def has_one(Object o) {
            if (o instanceof Closure) {
                _has_one(o)
            } else {
                _has_one { ->
                    table o.toString()
                }
            }
        }

        def _has_one(Closure c) {
            def structCtx = [
                'table': LoadUtil.&string,
                'primary_key': LoadUtil.&string,
                'foreign_key': LoadUtil.&string,
                '__required__': ['table']
            ]
            def r = LoadUtil.invoke(c, structCtx, new HasOne(origin_table))

            def target_name = Inflector.internalName(r.table)
            def target = getTable(target_name, "Has one relation expects table '${target_name}' to exist. But it does not!")
            r.target = target
            r.checkIntegrity();
            origin_table.relations << r
        }

        /*!
         * @relations|belongs_to Belongs to
         *
         * %p
         *   Looking at the two sections above it might be useful that given a picture object you can
         *   retrieve it's gallery, or given a gallery you can lookup it's user. Add the following:
         *
         * %span.filename relations.json
         * %pre
         *   %code{ data: { language: 'javascript' } }
         *     :preserve
         *       ...
         *       picture {
         *         belongs_to 'gallery'
         *       }
         *       gallery {
         *         belongs_to 'user'
         *       }
         */
        def belongs_to(Object o) {
            if (o instanceof Closure) {
                _belongs_to(o)
            } else {
                _belongs_to { ->
                    to o.toString()
                }
            }
        }
        def _belongs_to(Closure c) {
            def structCtx = [
                    'to': LoadUtil.&string,
                    'primary_key': LoadUtil.&string,
                    'foreign_key': LoadUtil.&string,
                    '__required__': ['to']
            ]
            def r = LoadUtil.invoke(c, structCtx, new BelongsTo(origin_table))

            def target_name = Inflector.internalName(r.to)
            def target = getTable(target_name, "Belongs to relation expects table '${target_name}' to exist. But it does not!")
            r.target = target
            r.checkIntegrity();
            origin_table.relations << r
        }

        /*!
         * @relations|has_may Has Many (1..n)
         *
         * %p Assuming that any gallery has many pictures (1..n):
         *
         * %span.filename relations.json
         * %pre
         *   %code{ data: { language: 'dsl' } }
         *     :preserve
         *       ...
         *       gallery {
         *         has_many 'pictures'
         *       }
         *
         * %p
         *   Note that the name in
         *   %span.migration-ref has_many
         *   array must be plural. This is more readable as you can simply read 'a gallery has many pictures'.
         *   If {?class:arname;DR} cannot infer the table from the given name in plural you can specifiy the exact
         *   table name by prepending a hash (#) infront of the name (e.g '#picture' instead of 'pictures').
         *
         */
        def has_many(Object o) {
            if (o instanceof Closure) {
                _has_many(o)
            } else {
                _has_many { ->
                    table o.toString()
                }
            }
        }
        def _has_many(Closure c) {

            def structCtx = [
                    'table': LoadUtil.&string,
                    'primary_key': LoadUtil.&string,
                    'foreign_key': LoadUtil.&string,
                    '__required__': ['table']
            ]
            def r = LoadUtil.invoke(c, structCtx, new BelongsTo(origin_table))

            def target_name = Inflector.internalName(Inflector.singularize(r.table))
            def target = getTable(target_name, "Has many relation expects table '${target_name}' to exist. But it does not! " +
                    "If it does and the singularize function messed it up, use a " +
                    "hash in front of the table name. e.g. 'has_many': '#singular_table_name'")
            r.target = target
            r.checkIntegrity();
            origin_table.relations << r
        }

        /*!
         * @relations|has_and_belongs_to Has and belongs to
         *
         * %p
         *   As an example consider the following requirement: "A user has many favourite pictures and a picture can be the favorite of many users".
         *   In a classic relational database this is called a n:m relation.
         *
         * %span.filename relations.json
         * %pre
         *   %code{ data: { language: 'dsl' } }
         *     :preserve
         *       ...
         *       user {
         *         has_and_belongs_to {
         *           many 'galleries'
         *           through 'user_picture'
         *         }
         *       }
         *       gallery {
         *         has_and_belongs_to {
         *           many: 'users'
         *           through: 'user_picture'
         *         }
         *       }
         *
         */
        def has_and_belongs_to(Closure c) {

            def structCtx = [
                'many': LoadUtil.&string,
                'through': LoadUtil.&string,
                '__required__': ['many','through']
            ]
            def map = LoadUtil.invoke(c, structCtx, new HasAndBelongsTo(origin_table))

            def many_table_name = Inflector.internalName(Inflector.singularize(map.many))
            def through_table_name = Inflector.internalName(Inflector.singularize(map.through))

            def many_table = getTable(many_table_name, "Expected table '${many_table_name}' to exist, but it does not!")
            def through_table = getTable(through_table_name, "Expected table '${through_table}' to exist, but it does not!")

            r.target = many_table
            r.through_table = through_table
            r.checkIntegrity()
            origin_table.relations << r

            through_table.javaclass_codegen << { CodeGenerator cg ->
                def javaClassName = Inflector.javaClassName(through_table_name)
                def typeParam1 = Inflector.javaClassName(many_table_name)
                def nameParam1 = many_table_name
                def typeParam2 = origin_table.javaClassName
                def nameParam2 = origin_table.name
                cg.wrap("public static ${javaClassName} of(${typeParam1} ${nameParam1}, ${typeParam2} ${nameParam2})") {
                    cg.line("${javaClassName} obj = new ${javaClassName}();")
                    cg.line("obj.set${typeParam1}Id(${nameParam1}.getId());")
                    cg.line("obj.set${typeParam2}Id(${nameParam2}.getId());")
                    cg.line("return obj;")
                }
            }
        }

    }
}
