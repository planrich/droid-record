package com.pasra.android.record.gen

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.logging.Logging

/**
 * Created by rich on 9/8/13.
 */
class Table implements Record {

    def logger = Logging.getLogger("android_record")
    String name;
    JsonObject json;
    def fields = [:];
    Field primary = null;

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
            this.name = Util.tabelize(json.get("name").getAsString())
            def jsonFields = json.get("fields").getAsJsonObject()

            jsonFields.entrySet().each { entry ->
                def fieldName = entry.getKey()
                def fieldElement = entry.getValue()
                def field = new Field(fieldName, fieldElement);

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
            }

            if (fields.size() == 0) {
                throw new IllegalArgumentException("Every table needs at least one table column. '${name}' has none!")
            }
        } else {
            throw new IllegalArgumentException("Malformed json given!");
        }
    }

    void generateDaoJavaSource(String source, String pkg) {

        CodeGenerator c = new CodeGenerator();

        c.line("package ${pkg};")
        c.line();
        c.wrap("public class Abstract${Util.camelize(name)}") {
            fields.each { _, Field field ->
                field.generateDaoJavaField(c);
            }

            c.line()

            c.wrap("public Abstract${Util.camelize(name)}(${primary.getType()} id)") {
                c.line("this.mId = id;");
            }

            c.line()

            fields.each { _, Field field ->
                field.generateDaoJavaFieldGetterSetter(c);
            }
        }


        File file = Util.file(source, pkg, "Abstract${Util.camelize(name)}.java", true)
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(c.toString());
        writer.close();
    }


    String creationSQL() {
        return "create table ${name} ();";
    }

    String destructionSQL() {
        return "drop table ${name}";
    }
}
