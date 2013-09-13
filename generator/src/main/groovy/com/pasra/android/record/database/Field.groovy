package com.pasra.android.record.database

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pasra.android.record.generation.CodeGenerator
import com.pasra.android.record.Inflector

/**
 * Created by rich on 9/8/13.
 *
 * A database field in a sqlite database
 */
class Field {

    String javaName; // camelized
    String name; // original
    JsonElement json;
    String type;

    static final TYPES = [ "integer" : Integer,
                           "string" : String,
                           "long" : Long,
                           "boolean" : Boolean
                         ]

    Field(String name, JsonElement json) {
        this.javaName = Inflector.camelize(name);
        this.name = name;
        this.json = json;
    }

    /**
     * Check if the json obj given is valid. Throws an exception otherwise
     */
    void checkIntegrity() {

        // age: 'integer'
        if (json.isJsonPrimitive()) {
            type = json.getAsString()
        // age: { type: 'integer', default: '0' }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (!obj.has("type") && !obj.get("type").isJsonPrimitive()) {
                throw IllegalStateException("Field '${name}' does not have a specified type!");
            }

            type = obj.get("type").getAsString();
        }

        if (TYPES[type] == null) {
            throw new IllegalStateException("Type ${type} cannot be used in field ${name}!");
        }
    }

    /**
     * @throws IllegalStateException if the type is unkown
     * @param type must be a valid sqlite type
     */
    String getType() {
        return TYPES[type].getSimpleName();
    }

    /**
     * Generate a java field for this database field.
     *
     * @param c
     */
    void generateDaoJavaField(CodeGenerator c) {
        c.line("private ${getType()} m${javaName};")
    }

    /**
     * Generate a getter and setter for this field like:
     *
     * age: 'integer'
     *
     * =>
     *
     * void setAge(Integer value) { ... };
     * Integer getAge() { ... };
     *
     * @param c
     */
    void generateDaoJavaFieldGetterSetter(CodeGenerator c) {
        c.line("public ${getType()} get${javaName}() { return m${javaName}; }")
        c.line("public void set${javaName}(${getType()} value) { m${javaName} = value; }")
    }

    /**
     * Is this field a primary key?
     * @return
     */
    boolean isPrimary() {
        return json.isJsonObject() && json.getAsJsonObject().has("primary") && json.getAsJsonObject().get("primary").asBoolean();
    }

    boolean hasConstraint() {
        return json.isJsonObject() && json.getAsJsonObject().has("constraint")
    }

    String typeSQL() {

        def conv = [ "integer": "integer",
                     "long": "integer",
                     "float": "real",
                     "string": "text",
                     "blob": "blob"
                   ]

        def result = conv[type.toLowerCase()];
        if (result == null) {
            throw new IllegalStateException("unkown datatype: ${type.toLowerCase()} used for column ${name}!");
        }
        return result
    }

    String columnSQL() {
        return "${name} ${typeSQL()} ${constraints()}"
    }

    String constraints() {
        def constraints = []
        if (isPrimary()) {
            constraints << "primary key"
        }
        if (hasConstraint()) {
            constraints << json.getAsJsonObject().get("constraint").getAsString()
        }
        return constraints.join(" ")
    }
}
