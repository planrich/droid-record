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
    boolean allowNull = false;
    int tableOrder;

    static final to_java_type = ["integer": "java.lang.Integer",
            "string": "java.lang.String",
            "long": "java.lang.Long",
            "boolean": "java.lang.Boolean",
            "blob": "byte[]",
            "date": "java.util.Date" ]

    static final to_sqlite_type = ["integer": "integer",
            "long": "integer",
            "float": "real",
            "string": "text",
            "blob": "blob",
            "date": "text", ]

    Field(String name, JsonElement json) {
        changeName(name)
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
            if (obj.has("null") && obj.get("null").asBoolean) {
                allowNull = true;
            }
        }

        if (to_java_type[type] == null) {
            throw new IllegalStateException("Type ${type} cannot be used in field ${name}!");
        }
    }

    void changeName(String new_name) {
        this.javaName = Inflector.camelize(new_name)
        this.name = new_name
    }

    String javaType() {
        return to_java_type[type]
    }

    String javaPrivateFieldName() {
        return "m${Inflector.camelize(name)}"
    }

    String javaGetCall() {
        return "get${Inflector.camelize(name)}()"
    }

    String javaCallToSerialize(String objname) {
        String type = javaType();
        def suffix = "";
        def prefix = "";
        if (type == "java.util.Date") {
            prefix = "SQLiteConverter.dateToString("
            suffix = ")";
        }

        return "${prefix}${objname}.get${Inflector.camelize(name)}()${suffix}";
    }

    String javaCallToDeserialize(String objname, String cursorobj) {
        def call = javaCallGetCursor(cursorobj)

        return "${objname}.set${Inflector.camelize(name)}(${call})"
    }

    String javaCallGetCursor(String c) {
        def call = "";
        def type = javaType()
        def i = tableOrder;
        if (type == "byte[]") {
            call = "${c}.getBlob(${i})"
        } else if (type == "java.util.Date") {
            call = "SQLiteConverter.stringToDate(${c}.getString(${i}))"
        } else if (type == "java.lang.String") {
            call = "${c}.getString(${i})";
        } else if (type == "java.lang.Integer") {
            call = "${c}.getInt(${i})";
        } else if (type == "java.lang.Long") {
            call = "${c}.getLong(${i})";
        } else if (type == "java.lang.Boolean") {
            call = "(${c}.getInt(${i}) != 0)";
        }

        return call;
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

    boolean allowsNull() {
        return allowNull;
    }

    String privateFieldName() {
        return "m${Inflector.camelize(name)}"
    }

    String defaultValue() {
        String type = javaType();
        if (type == "byte[]") {
            return "new byte[0]"
        } else if (type == "java.util.Date") {
            return "new java.util.Date(0)"
        } else if (type == "java.lang.String") {
            return "\"\""
        } else if (type == "java.lang.Integer") {
            return "new Integer(0)"
        } else if (type == "java.lang.Long") {
            return "new Long(0L)"
        } else if (type == "java.lang.Boolean") {
            return "new Boolean(false)"
        }

        return "null";
    }

    String javaCallContentValueFromCursor(String cursorobj, String colname) {
        String type = javaType();
        def colIdxCall = "${cursorobj}.getColumnIndex(\"${colname}\")"
        if (type == "byte[]") {
            return "${cursorobj}.getAsByteArray(${colIdxCall})"
        } else if (type == "java.util.Date") {
            return "${cursorobj}.getAsString(${colIdxCall})"
        } else if (type == "java.lang.String") {
            return "${cursorobj}.getAsString(${colIdxCall})"
        } else if (type == "java.lang.Integer") {
            return "${cursorobj}.getAsInteger(${colIdxCall})"
        } else if (type == "java.lang.Long") {
            return "${cursorobj}.getAsLong(${colIdxCall})"
        } else if (type == "java.lang.Boolean") {
            return "${cursorobj}.getAsBoolean(${colIdxCall})"
        }

        throw new InternalError("Can't handle type '${type}'. If a type is not present it should " +
                "already be caugth by Field#checkIntegrity()!")
    }
}
