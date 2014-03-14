package at.pasra.record.database

import at.pasra.record.generation.CodeGenerator
import at.pasra.record.util.Inflector
/**
 * Created by rich on 9/8/13.
 *
 * A database field in a sqlite database
 */
class Field {

    def primary = false
    String javaFieldName; // camelized
    String name; // original
    String sqlName;
    String type;
    boolean allowNull = false;
    int tableOrder;

    static final ALLOWED_TYPES = ['integer', 'string', 'long', 'boolean', 'blob', 'date', 'double' ]

    static final TO_JAVA_TYPE_MAP = [
        "integer": "java.lang.Integer",
        "string": "java.lang.String",
        "long": "java.lang.Long",
        "boolean": "java.lang.Boolean",
        "blob": "byte[]",
        "date": "java.util.Date",
        "double": "java.lang.Double"
    ]

    static final TO_SQLITE_TYPE_MAP = [
        "integer": "integer",
        "long": "integer",
        "boolean": "integer",
        "double": "long", // yes -> use Double.longToDoubleBits
        "string": "text",
        "blob": "blob",
        "date": "integer",
    ]

    Field() {
    }

    Field(String name) {
        changeName(name)
    }

    void checkIntegrity() {

        if (name == null) {
            throw new IllegalArgumentException("A field needs a name")
        }

        changeName(name)

        if (type == null) {
            throw new IllegalArgumentException("A field needs a type!")
        }

        if (TO_JAVA_TYPE_MAP[type] == null) {
            throw new IllegalArgumentException("Type ${type} cannot be used in field ${name}. use one of ${ALLOWED_TYPES}!")
        }
    }

    void changeName(String new_name) {
        this.name = Inflector.internalName(new_name)
        this.sqlName = Inflector.tabelize(new_name)
        this.javaFieldName = Inflector.javaClassName(new_name)
    }

    String javaType() {
        return TO_JAVA_TYPE_MAP[type]
    }

    String javaPrivateFieldName() {
        return "m${javaFieldName}"
    }

    String javaGetCall() {
        return "get${javaFieldName}()"
    }

    String javaCallToSerialize(String objname) {
        String type = javaType();
        def suffix = "";
        def prefix = "";
        if (type == "java.util.Date") {
            //prefix = "SQLiteConverter.dateToString("
            //suffix = ")";
            //prefix = ""
            suffix = ".getTime()"
        } else if (type == "java.lang.Double") {
            prefix = "Double.doubleToLongBits(";
            suffix = ")"
        }

        return "${prefix}${objname}.get${javaFieldName}()${suffix}";
    }

    String javaCallToDeserialize(String objname, String cursorobj) {
        def call = javaCallGetCursor(cursorobj)

        return "${objname}.set${javaFieldName}(${call})"
    }

    String javaCallGetCursor(String c) {
        def call = "";
        def type = javaType()
        def i = "${c}.getColumnIndex(\"${sqlName}\")";
        if (type == "byte[]") {
            call = "${c}.getBlob(${i})"
        } else if (type == "java.util.Date") {
            //call = "SQLiteConverter.stringToDate(${c}.getString(${i}))"
            call = "new java.util.Date(${c}.getLong(${i}))"
        } else if (type == "java.lang.String") {
            call = "${c}.getString(${i})";
        } else if (type == "java.lang.Integer") {
            call = "${c}.getInt(${i})";
        } else if (type == "java.lang.Long") {
            call = "${c}.getLong(${i})";
        } else if (type == "java.lang.Boolean") {
            call = "(${c}.getInt(${i}) != 0)";
        } else if (type == "java.lang.Double") {
            call = "Double.longBitsToDouble(${c}.getLong(${i}))";
        }

        return call;
    }

    /**
     * Generate a java field for this database field.
     *
     * @param c
     */
    void generateDaoJavaField(CodeGenerator c) {
        c.line("protected ${javaType()} m${javaFieldName};")
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
        c.line("public ${javaType()} get${javaFieldName}() { return m${javaFieldName}; }")
        c.line("public void set${javaFieldName}(${javaType()} value) { m${javaFieldName} = value; }")
    }

    /**
     * Is this field a primary key?
     * @return
     */
    boolean isPrimary() {
        return primary
    }

    String sqlType() {
        def result = TO_SQLITE_TYPE_MAP[type.toLowerCase()];
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
        return constraints.join(" ")
    }

    boolean allowsNull() {
        return allowNull;
    }

    String privateFieldName() {
        return "m${javaFieldName}"
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
        } else if (type == "java.lang.Double") {
            return "0.0"
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
