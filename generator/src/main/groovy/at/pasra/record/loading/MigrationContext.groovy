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
