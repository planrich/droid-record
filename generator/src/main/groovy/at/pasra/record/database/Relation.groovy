package at.pasra.record.database

import at.pasra.record.generation.CodeGenerator
import com.google.gson.JsonObject

/**
 * Created by rich on 9/13/13.
 */
abstract class Relation {
    protected JsonObject options;
    protected Table origin;
    protected Table target;

    protected Relation(Table origin, Table target, JsonObject options) {
        this.origin = origin
        this.target = target
        this.options = options;
    }

    protected String foreign_key(def name, def fallback_prop_name = "foreign_key") {
        def foreign_key = "${name}_id"

        if (options.has(fallback_prop_name)) {
            foreign_key = options.get(fallback_prop_name).asString
        }
        return foreign_key
    }

    protected String foreign_key() {
        foreign_key(origin.name)
    }

    abstract void generateSessionMethods(CodeGenerator c);
    abstract void generateJavaMethods(CodeGenerator c);
    abstract void generateRecordMethods(CodeGenerator c);

    abstract void checkIntegrity();

    /**
     * Checks if the foreign key exists given the internal name and the fields to check
     * @param name
     * @param fields
     * @return the fk name, if it found the fk, did the type match? and the type
     */
    def check_foreign_key_exists(def name, def fields, def fallback) {

        def has_foreign_key = false
        def type_match = false
        def type

        def fk = foreign_key(name, fallback)
        fields.each { Field f ->
            if (f.name == fk) {
                has_foreign_key = true
                type = f.type
                if (f.type == origin.primary.type) {
                    type_match = true
                }
            }
        }

        return [ fk, has_foreign_key, type_match, type ]
    }
}
