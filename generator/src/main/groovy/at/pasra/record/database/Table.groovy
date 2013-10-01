package at.pasra.record.database

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import at.pasra.record.generation.CodeGenerator
import at.pasra.record.Inflector
/**
 * Created by rich on 9/8/13.
 */
class Table {

    String name;
    String sqlTableName;
    String javaClassName;
    JsonObject json;

    Field primary = null;
    def fields = [:];

    def relations = [];

    def table_order = 1;

    Table(JsonObject json) {
        this.json = json
    }

    /**
     *
     * @return true if the object has the properties fields (as non empty json map) and name as primitive
     */
    void checkIntegrity() {
        if (json.has("name")
           && json.get("name").isJsonPrimitive()
           && json.has("fields")
           && json.get("fields").isJsonObject()) {
            changeName(json.get("name").getAsString())
            def jsonFields = json.get("fields").getAsJsonObject()

            jsonFields.entrySet().each { entry ->
                def fieldName = entry.getKey()
                def fieldElement = entry.getValue()

                if (fieldName == "id" || fieldName == "_id") {
                    throw new IllegalArgumentException("Please do not use the reserved column name 'id' or '_id'. " +
                            "By default android record will add an _id column to uniquely identify " +
                            "every table row.");
                }

                addField(new Field(fieldName, fieldElement));
            }

            JsonObject id = new JsonObject();
            id.add("type", new JsonPrimitive("long"))
            id.add("primary", new JsonPrimitive(true))
            addField(new Field("_id", id))
            primary.tableOrder = 0;

            if (fields.size() == 0) {
                throw new IllegalArgumentException("Every table needs at least one table column. '${name}' has none!")
            }
        } else {
            throw new IllegalArgumentException("A table from the given json cannot be constructed. Please correct typos and check the docs for create_table!");
        }
    }

    void changeName(String name) {
        def hash_in_front = name.startsWith("#")
        if (hash_in_front) {
            name = name.substring(1)
        }

        this.name = Inflector.tabelize(name)
        this.javaClassName = Inflector.camelize(this.name);
        this.sqlTableName = Inflector.pluralize(this.name)

        if (hash_in_front) {
            this.sqlTableName = this.name;
        }
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
                    "Android record defines '_id' by default and yet it is not possible " +
                    "to have any other primary keys!")
        }

        if (field.isPrimary()) {
            primary = field;
        } else {
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

    /**
     * A relation can be either:
     * has_many
     * has_one
     * belongs_to
     *
     * @param relation
     */
    void new_relation(JsonObject relation) {
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
