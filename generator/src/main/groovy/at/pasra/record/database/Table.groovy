package at.pasra.record.database

import at.pasra.record.generation.CodeGenerator
import at.pasra.record.util.Inflector
/**
 * Created by rich on 9/8/13.
 */
class Table {

    def static name_regex = /#?[a-zA-Z_][a-zA-Z0-9]*/

    def name
    def fields = [:]

    def sqlTableName;
    def javaClassName;

    Field primary = null;

    def relations = [];

    def javaclass_codegen = []

    def table_order = 1;

    Table() {
    }

    void checkIntegrity() {
        if (name == null) {
            throw new IllegalArgumentException("A table needs a name!")
        }

        def matches = name =~ name_regex
        if (!matches) {
            throw new IllegalArgumentException("A table name must match the regex: '#?[a-zA-Z_][a-zA-Z0-9]*'")
        }

        changeName(name)

        fields.each { _, field ->
            field.checkIntegrity()
        }

        if (fields["_id"] != null) {
            throw new IllegalArgumentException("Please do not use the reserved column name '_id'. " +
                    "By default droid record will add an _id column to uniquely identify " +
                    "every table row.");
        }

        def primary = new Field("_id")
        primary.type = 'long'
        primary.primary = true
        addField(primary)
        this.primary = primary
        primary.tableOrder = 0;
        table_order--

        if (fields.size() == 0) {
            throw new IllegalArgumentException("Every table needs at least one table column. '${name}' has none!")
        }
    }

    void changeName(String name) {
        this.name = Inflector.internalName(name)
        this.javaClassName = Inflector.camelize(name);
        this.sqlTableName = Inflector.sqlTableName(name)
    }

    Field removeField(String field_name) {
        if (fields[field_name] == null) {
            throw new IllegalStateException("The field '${field_name}' is not present in the schema of '${name}' but should be")
        }

        Field field = fields.remove(field_name)

        // correct the index of the other columns
        fields.each { k, Field f ->
            if (f.tableOrder > field.tableOrder) {
                f.tableOrder--;
            }
        }

        this.table_order--;

        return field;
    }

    Field renameField(String old_name, String new_name) {

        if (fields[old_name] == null) {
            throw new IllegalStateException("The field '${old_name}' is not present in the schema of '${name}' but should be!")
        }

        Field field = fields[old_name]
        field.changeName(new_name)

        return field
    }

    Field addField(Field field) {

        if (fields[field.name] != null) {
            throw new IllegalStateException("The field '${field.name}' is aready present in the schema of '${name}'")
        }

        field.checkIntegrity();

        fields[field.name] = field
        if (field.isPrimary() && primary != null) {
            throw new IllegalStateException("It is not possible to have two primary keys! " +
                    "Droid record defines '_id' by default and yet it is not possible " +
                    "to have any other primary keys!")
        }

        if (!field.isPrimary()) {
            field.tableOrder = table_order++;
        }

        return field
    }

    Field[] getOrderedFields(includePrimary = true) {
        def fs = fields.values();
        def list = [];
        fs.each { Field f ->
            if (f.isPrimary() && includePrimary) {
                list << f;
            } else if (!f.isPrimary()) {
                list << f;
            }
        }

        def orderedFields = list.sort() { a, b ->
            return new Integer(a.tableOrder).compareTo(new Integer(b.tableOrder))
        }

        return orderedFields
    }

    String creationSQL(name_suffix = "") {
        def columns = getOrderedFields(true).collect { Field field -> field.columnSQL() }.join ", "
        return "create table ${sqlTableName + name_suffix} (${columns});";
    }

    boolean hasFieldOfType(String type) {
        def has = false
        fields.values().each { f ->
            if (f.javaType() == type) {
                has = true
            }
        }
        return has
    }

    String javaCallGetId(String obj) {
        return "${obj}.get${Inflector.camelize(primary.name)}()"
    }

    void javaCallsNewObjectFromCursor(CodeGenerator c, String objname, String cursorname) {

        def CamName = Inflector.camelize(name);
        c.line("${CamName} record = new ${CamName}();")
        getOrderedFields().each { Field f ->
            c.line("${f.javaCallToDeserialize(objname, cursorname)};")
        }

    }
}
