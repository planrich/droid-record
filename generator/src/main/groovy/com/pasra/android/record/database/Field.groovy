package com.pasra.android.record.database

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pasra.android.record.generation.CodeGenerator
import com.pasra.android.record.Inflector

import java.nio.ByteBuffer
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
    int tableOrder;

    static final to_java_type = ["integer": "java.lang.Integer",
            "string": "java.lang.String",
            "long": "java.lang.Long",
            "boolean": "java.lang.Boolean",
            "blob": "java.nio.ByteBuffer",
            "date": "java.util.Date" ]

    static final to_sqlite_type = ["integer": "integer",
            "long": "integer",
            "float": "real",
            "string": "text",
            "blob": "blob",
            "date": "text", ]

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

        if (to_java_type[type] == null) {
            throw new IllegalStateException("Type ${type} cannot be used in field ${name}!");
        }
    }

    String javaType() {
        return to_java_type[type]
    }

    String javaCallToSerialize(String objname) {
        String type = javaType();
        def suffix = "";
        def prefix = "";
        if (type == "java.nio.ByteBuffer") {
            suffix = ".array()"
        }
        if (type == "java.util.Date") {
            suffix = ")";
            prefix = "SQLiteConverter.dateToString("
        }

        return "${prefix}${objname}.get${Inflector.camelize(name)}()${suffix}";
    }

    String javaCallToDeserialize(String objname, String cursorobj) {
        String type = javaType();
        def i = tableOrder;
        def call = "";
        if (type == "java.nio.ByteBuffer") {
            call = "ByteBuffer.wrap(${cursorobj}.getBlob(${i}))"
        } else if (type == "java.util.Date") {
            call = "SQLiteConverter.stringToDate(${cursorobj}.getString(${i}))"
        } else if (type == "java.lang.String") {
            call = "${cursorobj}.getString(${i})";
        } else if (type == "java.lang.Integer") {
            call = "${cursorobj}.getInt(${i})";
        } else if (type == "java.lang.Long") {
            call = "${cursorobj}.getLong(${i})";
        } else if (type == "java.lang.Boolean") {
            call = "(${cursorobj}.getInt(${i}) != 0)";
        }

        return "${objname}.set${Inflector.camelize(name)}(${call})"
    }

    /**
     * Generate a java field for this database field.
     *
     * @param c
     */
    void generateDaoJavaField(CodeGenerator c) {
        c.line("protected ${javaType()} m${javaName};")
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
        c.line("public ${javaType()} get${javaName}() { return m${javaName}; }")
        c.line("public void set${javaName}(${javaType()} value) { m${javaName} = value; }")
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

    String sqlType() {
        def result = to_sqlite_type[type.toLowerCase()];
        if (result == null) {
            throw new IllegalStateException("unkown datatype: ${type.toLowerCase()} used for column ${name}!");
        }
        return result
    }

    String columnSQL() {
        return "${name} ${sqlType()} ${constraints()}"
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
