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

    Table(JsonObject json) {
        this.json = json
    }

    /**
     *
     * @return true if the object has the properties fields (as non empty json map) and name as primitive
     */
    void checkIntegrity() {
        // define ordering!
        def forcedOrder = 0;

        if (json.has("name")
           && json.get("name").isJsonPrimitive()
           && json.has("fields")
           && json.get("fields").isJsonObject()) {
            this.name = Inflector.tabelize(json.get("name").getAsString())
            def jsonFields = json.get("fields").getAsJsonObject()

            jsonFields.entrySet().each { entry ->
                def fieldName = entry.getKey()
                def fieldElement = entry.getValue()
                def field = new Field(fieldName, fieldElement);
                field.tableOrder = forcedOrder++;

                field.checkIntegrity();
                fields[field.name] = field
                if (field.isPrimary()) {
                    primary = field;
                }
            }

            if (primary == null) {
                logger.info 'Table has no primary key, adding id'
                JsonObject id = new JsonObject();
                id.add("type", new JsonPrimitive("long"))
                id.add("primary", new JsonPrimitive(true))
                fields["id"] = new Field("id", id)
                primary = fields["id"]
                primary.checkIntegrity()
                primary.tableOrder = forcedOrder++;
            }

            if (fields.size() == 0) {
                throw new IllegalArgumentException("Every table needs at least one table column. '${name}' has none!")
            }
        } else {
            throw new IllegalArgumentException("Malformed json given!");
        }
    }

    Field[] getOrderedFields(includePrimary = true) {
        def fs = fields.values();
        if (!includePrimary) {
            fs.remove(primary)
        }

        def orderedFields = fields.values().sort() { a, b ->
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

        if (relation.has("has_many")) {
            def has_many = relation.get("has_many")

            def many = []
            if (has_many.isJsonArray()) {
                many = has_many.getAsJsonArray();
            } else if (has_many.isJsonPrimitive()) {
                many = [has_many.getAsString()];
            }

            many.each { plural_name ->
                relations << new HasMany(this, Inflector.singularize(plural_name))
            }
        }

        if (relation.has("has_one")) {
            def has_one = relation.get("has_one")
            if (has_one.isJsonPrimitive()) {
                relations << new HasOne(this, has_one.asString)
            }
        }

        if (relation.has("belongs_to")) {
            def belongs_to = relation.get("belongs_to")
            if (belongs_to.isJsonPrimitive()) {
                relations << new BelongsTo(this, belongs_to.asString)
            }
        }
    }
}
