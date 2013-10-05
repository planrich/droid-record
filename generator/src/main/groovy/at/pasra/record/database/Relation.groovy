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

    protected String foreign_key() {
        def foreign_key = "${origin.name}_id"

        if (options.has("foreign_key")) {
            foreign_key = options.get("foreign_key").asString
        }
        return foreign_key
    }

    abstract void generateSessionMethods(CodeGenerator c);
    abstract void generateJavaMethods(CodeGenerator c);
    abstract void generateRecordMethods(CodeGenerator c);

    abstract void checkIntegrity();
}
