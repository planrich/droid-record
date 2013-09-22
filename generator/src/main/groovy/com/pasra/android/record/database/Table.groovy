package com.pasra.android.record.database

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.pasra.android.record.AndroidRecordPlugin
import com.pasra.android.record.generation.CodeGenerator
import com.pasra.android.record.Inflector
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */
class Table {

    def logger = Logging.getLogger("android_record")

    String name;
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
        // define ordering!

        if (json.has("name")
           && json.get("name").isJsonPrimitive()
           && json.has("fields")
           && json.get("fields").isJsonObject()) {
            this.name = Inflector.tabelize(json.get("name").getAsString())
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
            throw new IllegalArgumentException("A table from the given json cannot be constructed. Please correct typos and chekc the docs for create_table!");
        }
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

    String creationSQL() {
        def columns = fields.values().collect { Field field -> field.columnSQL() }.join ", "
        return "create table ${name} (${columns});";
    }

    String destructionSQL() {
        return "drop table ${name}";
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
            // TODO think about this! do the columns indices change?
            c.line("${f.javaCallToDeserialize(objname, cursorname)};")
        }

    }
}
