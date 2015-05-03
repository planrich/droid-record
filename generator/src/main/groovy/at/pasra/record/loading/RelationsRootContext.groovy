package at.pasra.record.loading

import at.pasra.record.database.Table
import at.pasra.record.util.Inflector

import javax.el.MethodNotFoundException;

/**
 * Created by rich on 3/14/14.
 */
class RelationsRootContext {

    MigrationContext context;
    RelationsRootContext(MigrationContext ctx) {
        this.context = ctx;
    }

    def methodMissing(String name, args) {
        Table origin = context.getTable(Inflector.internalName(name), "Relation expects table '${name}' to exist. But it does not!")
        if (args.size() == 1 && args[0] instanceof Closure) {
            Closure c = args[0]
            def delegate = new RelationInvokeableContext(context, origin)
            c.delegate = delegate
            c.resolveStrategy = Closure.DELEGATE_ONLY
            c()
        } else {
            throw new MethodNotFoundException("You must provide a closure to a relation!")
        }
    }
}
